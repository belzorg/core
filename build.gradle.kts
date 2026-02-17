import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.lang.System.getenv
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

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
