plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    application
}

group = "io.github.asolntsev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.codeborne:selenide:7.13.0")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.assertj:assertj-core:3.27.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}
