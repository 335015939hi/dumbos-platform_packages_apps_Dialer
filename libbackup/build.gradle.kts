plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.google.android.libraries.backup"
    compileSdk = 35

    defaultConfig {
        minSdk = 35
    }

    sourceSets.getByName("main") {
        java.srcDirs("libbackup/src")
        java.exclude("**/shadow/**")
    }
}

dependencies {
    compileOnly("com.android.support:support-annotations:28.0.0")
}
