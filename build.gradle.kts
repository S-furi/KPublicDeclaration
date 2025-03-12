plugins {
    kotlin("jvm") version "2.1.10"
    application
}

application {
    mainClass.set("io.github.sfuri.RunnerKt")
}

group = "io.github.sfuri"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.kotest)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.bundles.logger)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
