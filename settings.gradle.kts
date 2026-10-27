pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "tondiraba"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "2.4.21")
      library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    }
  }
}
