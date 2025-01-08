import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.utils.StdLogger
import com.google.protobuf.gradle.proto
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.protobuf")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val excludedSources = arrayOf(
    "dialer/binary/google",
    "dialer/rootcomponentgenerator",
    "dialer/inject/demo",
    "incallui/maps/impl",
    "incallui/calllocation/impl",
    "dialer/constants/googledialer"
)

val excludedResources = arrayOf(
    "voicemail/error/res",
    "precall/impl/res",
    "searchfragment/remote/res",
    "dialer/theme/res",
    "incallui/maps/impl/res"
)

val excludedManifests = arrayOf(
    "dialer/backup/AndroidManifest.xml",
    "calllocation/impl/AndroidManifest.xml",
    "incallui/maps/impl/AndroidManifest.xml",
)

val generateManifestTask = tasks.register("GenerateManifest") {
    val logger = StdLogger(StdLogger.Level.ERROR)
    val invoker = ManifestMerger2.newMerger(
        File("${rootDir.absolutePath}${File.separatorChar}AndroidManifest.xml"),
        logger,
        ManifestMerger2.MergeType.APPLICATION
    )
    File("${rootDir.absolutePath}${File.separatorChar}java").walkBottomUp()
        .forEach { file ->
            if (file.isFile && file.name == "AndroidManifest.xml" &&
                !excludedManifests.any { file.path.contains(it) }
            ) {
                invoker.addLibraryManifests(file)
            }
        }
    invoker.withFeatures(ManifestMerger2.Invoker.Feature.NO_PLACEHOLDER_REPLACEMENT)

    val report = invoker.merge()
    if (report.result.isSuccess) {
        val newManifest = File("MergedManifest.xml")
        newManifest.writeText(
            report.getMergedXmlDocument(MergingReport.MergedManifestKind.MERGED)
                .prettyPrint().replace("package=\"com.android.dialer\"", ""),
            Charsets.UTF_8
        )
    } else {
        project.logger.error("Failed to merge manifest files!")
    }
    report.loggingRecords.forEach {
        when (it.severity) {
            MergingReport.Record.Severity.WARNING -> project.logger.warn(it.message)
            MergingReport.Record.Severity.ERROR -> project.logger.error(it.message)
            MergingReport.Record.Severity.INFO -> project.logger.info(it.message)
            else -> {}
        }
    }
}

tasks.named("preBuild") {
    dependsOn(generateManifestTask)
}

android {
    namespace = "com.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.dialer"
        minSdk = 35
        targetSdk = 35
        versionCode = 2900000 + 1
        versionName = "23.0.1"
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }

    sourceSets.getByName("main") {
        val getDirs = { dir: File ->
            dir.walkBottomUp()
                .mapNotNull { file ->
                    if (file.isDirectory && file.absolutePath.contains(".*${File.separatorChar}res$".toRegex())
                        && !excludedResources.any { file.path.contains(it) }
                    ) {
                        file.path
                    } else {
                        null
                    }
                }.toList().toTypedArray()
        }
        res.srcDirs(
            getDirs(File("${rootDir.absolutePath}${File.separatorChar}java")) +
                    getDirs(File("${rootDir.absolutePath}${File.separatorChar}assets"))
        )

        manifest.srcFile("../MergedManifest.xml")

        java.srcDirs("../java")
        excludedSources.forEach {
            java.exclude("**/$it/*.java")
        }

        proto { srcDir("../java") }
        aidl { srcDir("../java") }
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
                "../proguard.flags",
                "../java/com/android/dialer/proguard/proguard_base.flags",
                "../java/com/android/dialer/proguard/proguard.flags",
                "../java/com/android/dialer/proguard/proguard_release.flags"
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.29.2"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.collection:collection:1.4.5")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation("com.googlecode.libphonenumber:geocoder:2.246")

    implementation("com.google.guava:guava:33.4.0-android")

    val daggerVersion = "2.54"
    implementation("com.google.dagger:dagger:$daggerVersion")
    annotationProcessor("com.google.dagger:dagger-compiler:$daggerVersion")

    implementation("com.google.protobuf:protobuf-java:4.29.2")

    val autoValueVersion = "1.11.0"
    compileOnly("com.google.auto.value:auto-value-annotations:$autoValueVersion")
    annotationProcessor("com.google.auto.value:auto-value:$autoValueVersion")

    val autoServiceVersion = "1.1.1"
    compileOnly("com.google.auto.service:auto-service-annotations:$autoServiceVersion")
    annotationProcessor("com.google.auto.service:auto-service:$autoServiceVersion")

    compileOnly("com.google.auto:auto-common:1.2.2")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.squareup:javapoet:1.13.0")

    val glideVersion = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    annotationProcessor("com.github.bumptech.glide:compiler:$glideVersion")

    implementation("com.android.volley:volley:1.2.1")

    implementation("me.leolin:ShortcutBadger:1.1.22@aar")

    val mime4jVersion = "0.8.11"
    implementation("org.apache.james:apache-mime4j-core:$mime4jVersion")
    implementation("org.apache.james:apache-mime4j-dom:$mime4jVersion")

    val grpcVersion = "1.11.0"
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-okhttp:$grpcVersion")

    implementation(project(":libbackup"))
    implementation(project(":lib:platform_frameworks_ex:common"))

    implementation("commons-io:commons-io:2.18.0")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
}
