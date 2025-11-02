plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    jacoco
}

dependencies {
    implementation(libs.fraud.risk.payment)
}
