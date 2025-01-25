import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

android {
    namespace = "com.android.dialer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.android.dialer"
        minSdk = libs.versions.minSdk.get().toInt()
        //noinspection ExpiredTargetSdkVersion
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 2900000 + 1
        versionName = "23.0.1"
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")

        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets.getByName("main") {
        aidl { srcDir("./src/main/java") }

        java.exclude(
            "**/rootcomponentgenerator/**",
            "**/binary/google/**"
        )
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val useKeystoreProperties = keystorePropertiesFile.canRead()
    val keystoreProperties = Properties()
    if (useKeystoreProperties) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    if (useKeystoreProperties) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard.flags"
            )
            signingConfig = if (useKeystoreProperties) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            applicationIdSuffix = ".preview"
            resValue("string", "applicationLabel", "Preview Phone")
        }

        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "applicationLabel", "Phone d")
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.support.v4)
    implementation(libs.appcompat.v7)
    implementation(libs.recyclerview.v7)
    implementation(libs.support.v13)
    implementation(libs.design)

    implementation(libs.guava)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    implementation(libs.protobuf.java)

    implementation(libs.geocoder)

    implementation(libs.shortcutbadger)

    compileOnly(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value)

    // Using an old version because they migrated to androidx in 4.10.0 which breaks building with
    // the android support libraries
    // TODO: Migrate to Androidx and update Glide
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.javapoet)

    implementation(project(":lib:libbackup"))
    implementation(project(":lib:platform_frameworks_ex:common"))

    implementation(project(":dialer:common"))
    implementation(project(":dialer:resources"))
    implementation(project(":incallui:incallui"))
    implementation(project(":quantum"))
    implementation(project(":product"))
    implementation(project(":protos"))
    implementation(project(":voicemail"))
    implementation(project(":contacts-common"))
    implementation(project(":bubble"))
}
