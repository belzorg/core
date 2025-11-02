val ktor = "3.3.1"
val resilience4j = "2.3.0"

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    jacoco
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
        maven("https://packages.confluent.io/maven/")
        maven {
            url = uri("https://maven.pkg.github.com/hamsaqua/starter")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: findProperty("app.actor")?.toString()
                password = System.getenv("GITHUB_TOKEN") ?: findProperty("app.token")?.toString()
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.ktor.plugin")
    apply(plugin = "jacoco")

    dependencies {
        implementation("io.ktor:ktor-server-core-jvm:${ktor}")
        implementation("io.ktor:ktor-server-cio-jvm:${ktor}")
        implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktor}")
        implementation("io.ktor:ktor-server-metrics-micrometer-jvm:${ktor}")
        implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${ktor}")
        implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktor}")
        implementation("io.micrometer:micrometer-registry-prometheus:1.15.5")
        implementation("io.github.resilience4j:resilience4j-kotlin:${resilience4j}")
        implementation("io.github.resilience4j:resilience4j-retry:${resilience4j}")
        implementation("io.github.resilience4j:resilience4j-circuitbreaker:${resilience4j}")
        implementation("io.github.resilience4j:resilience4j-reactor:${resilience4j}")
        implementation("io.r2dbc:r2dbc-pool:1.0.2.RELEASE")
        implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
        implementation("io.lettuce:lettuce-core:7.0.0.RELEASE")
        implementation("io.projectreactor.kafka:reactor-kafka:1.3.24")
        implementation("ch.qos.logback:logback-classic:1.5.13")
        testImplementation("io.ktor:ktor-server-test-host:${ktor}")
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
        testImplementation("io.kotest:kotest-assertions-core:5.9.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
