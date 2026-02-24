import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.lang.System.getenv
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

val kotest = "6.1.3"

fun envOrProp(env: String, prop: String = env, def: String = ""): String =
    getenv(env) ?: (findProperty(prop) as String?) ?: def

val tld = envOrProp("TLD", "core.tld", "core")
val org = envOrProp("ORG", "core.org", "team")
val tag = envOrProp("TAG", "core.tag", "0.0.1-SNAPSHOT")
val repo = envOrProp("REPO", "core.repo", "repository")
val actor = envOrProp("ACTOR", "core.actor", "username")
val token = envOrProp("TOKEN", "core.token", "password")

plugins {
    kotlin("jvm") version "2.3.10" apply false
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
    `maven-publish`
}

allprojects {
    group = "$tld.$org"
    version = tag

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property"
            )
        }
    }

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("testImplementation", "io.kotest:kotest-runner-junit5:$kotest")
        add("testImplementation", "io.kotest:kotest-assertions-core:$kotest")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.withType<JacocoReport>())
    }

    val forbiddenCoreImports = listOf(
        "org.springframework",
        "jakarta.persistence",
        "javax.persistence",
        "org.hibernate",
        "org.jooq",
        "org.mybatis",
        "org.apache.kafka",
        "io.grpc",
        "retrofit2",
        "okhttp3",
        "org.springframework.web",
        "org.springframework.data",
        "software.amazon.awssdk",
        "com.mongodb",
        "redis.clients"
    )

    tasks.register("verifyHexagonalCoreBoundary") {
        group = "verification"
        description = "Fails when non-core (infrastructure/framework) logic leaks into the core modules."

        doLast {
            val sourceFiles = fileTree(projectDir) {
                include("src/main/kotlin/**/*.kt")
                include("src/main/java/**/*.java")
                exclude("**/package-info.kt")
            }.files

            val violations = sourceFiles.flatMap { source ->
                findForbiddenImports(source, projectDir, forbiddenCoreImports)
            }

            if (violations.isNotEmpty()) {
                val details = violations.joinToString("\n") { " - $it" }
                throw GradleException(
                    "Found non-core logic in module '${project.name}'. Keep this repository pure core (hexagonal domain + ports).\n$details"
                )
            }
        }
    }

    tasks.named("check") {
        dependsOn("verifyHexagonalCoreBoundary")
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(true)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("core") {
                from(components["java"])
                groupId = project.group.toString()
                artifactId = project.path
                version = project.version.toString()
            }
        }

        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/$org/$repo")
                credentials {
                    username = actor
                    password = token
                }
            }
        }
    }
}

fun findForbiddenImports(source: File, baseDir: File, forbiddenPrefixes: List<String>): List<String> {
    val results = mutableListOf<String>()

    source.readLines().forEachIndexed { index, rawLine ->
        val line = rawLine.trim()
        if (!line.startsWith("import ")) return@forEachIndexed

        val importedSymbol = line
            .removePrefix("import ")
            .removeSuffix(";")
            .trim()

        forbiddenPrefixes
            .firstOrNull { importedSymbol.startsWith(it) }
            ?.let { forbidden ->
                results.add("${source.relativeTo(baseDir)}:${index + 1} -> '$importedSymbol' matches forbidden prefix '$forbidden'")
            }
    }

    return results
}

tasks.register("listModules") {
    doLast {
        println(
            rootProject
                .subprojects
                .map { it.name }
                .sorted()
                .joinToString("\",\"", "[\"", "\"]")
        )
    }
}
