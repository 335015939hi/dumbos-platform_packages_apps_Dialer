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
    namespace = "com.android.voicemail"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    lint {
        abortOnError = false
    }

    buildTypes {
        release {
            consumerProguardFiles("proguard.flags")
        }
    }
}

dependencies {
    implementation(libs.support.v4)

    implementation(libs.apache.mime4j.core)
    implementation(libs.apache.mime4j.dom)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    implementation(libs.volley)

    implementation(libs.grpc.stub)
    compileOnly(libs.grpc.protobuf)
    implementation(libs.grpc.okhttp)

    implementation(libs.javax.annotation.api)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    implementation(project(":dialer:common"))
    implementation(project(":protos"))
}
