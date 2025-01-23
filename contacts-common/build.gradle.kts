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
    namespace = "com.android.contacts.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.support.v4)
    implementation(libs.appcompat.v7)
    implementation(libs.recyclerview.v7)
    implementation(libs.design)

    implementation(libs.protobuf.java)

    implementation(libs.guava)

    implementation(libs.geocoder)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    implementation(libs.zxing.core)

    implementation(project(":dialer:common"))
    implementation(project(":protos"))
    implementation(project(":dialer:resources"))

    implementation(project(":lib:platform_frameworks_ex:common"))
}
