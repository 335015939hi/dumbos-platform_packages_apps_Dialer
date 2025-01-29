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
    namespace = "com.android.incallui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets.getByName("main") {
        java.exclude(
            "**/calllocation/impl/**",
            "**/maps/impl/**"
        )
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.appcompat)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    implementation(libs.guava)

    implementation(libs.protobuf.java)

    // Using an old version because they migrated to androidx in 4.10.0 which breaks building with
    // the android support libraries
    // TODO: Migrate to Androidx and update Glide
    implementation(libs.glide)

    implementation(project(":contacts-common"))
    implementation(project(":protos"))
    implementation(project(":dialer:common"))
    implementation(project(":dialer:resources"))
    implementation(project(":bubble"))

    implementation(project(":lib:platform_frameworks_ex:common"))
}
