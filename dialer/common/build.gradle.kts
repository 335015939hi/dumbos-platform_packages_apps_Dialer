plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

android {
    namespace = "com.android.dialer.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    sourceSets.getByName("main") {
        java.exclude(
            "**/inject/demo/**",
            "**/googledialer/**"
        )

        aidl { srcDir("./src/main/java") }
    }

    buildTypes {
        release {
            consumerProguardFiles(
                "proguard.flags",
                "proguard_base.flags",
                "proguard_release.flags"
            )
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.material)

    implementation(libs.guava)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    implementation(libs.protobuf.java)

    implementation(libs.jsr305)

    implementation(libs.geocoder)

    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(project(":dialer:resources"))
    implementation(project(":incallui:resources"))
    implementation(project(":quantum"))
    implementation(project(":product"))
    implementation(project(":protos"))
}
