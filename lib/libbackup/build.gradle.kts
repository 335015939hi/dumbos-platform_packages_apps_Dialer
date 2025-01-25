plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

android {
    namespace = "com.google.android.libraries.backup"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets.getByName("main") {
        java.srcDirs("libbackup/src")
        java.exclude("**/shadow/**")
    }
}

dependencies {
    compileOnly(libs.support.annotations)
}
