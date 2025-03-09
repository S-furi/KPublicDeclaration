plugins {
    kotlin("jvm") version "2.1.10"
    application
}

application {
    mainClass.set("io.github.sfuri.PublicDeclarationFinderKt")
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
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
