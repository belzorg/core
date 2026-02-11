import java.lang.System.getenv

val kotest = "6.1.3"

val tld = getenv("TLD") ?: "com"
val org = getenv("ORG") ?: "wliamp"
val tag = getenv("TAG") ?: "0.0.1-SNAPSHOT"
val repo = getenv("REPO") ?: "core"
val actor = getenv("ACTOR") ?: findProperty("actor")?.toString()
val token = getenv("TOKEN") ?: findProperty("repo.pat.core")?.toString()

plugins {
    kotlin("jvm") version "2.3.10" apply false
    id("io.spring.dependency-management") version "1.1.7"
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

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
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
