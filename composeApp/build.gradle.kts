import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    task("testClasses")

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Glance widgets
            implementation("androidx.glance:glance:1.1.0")
            implementation ("androidx.glance:glance-appwidget:1.1.0")
            implementation ("androidx.glance:glance-material3:1.1.0")

            // Drive / Auth
            implementation("com.google.apis:google-api-services-drive:v3-rev20240509-2.0.0")
            implementation("com.google.android.gms:play-services-auth:21.2.0")
            implementation("com.google.api-client:google-api-client-android:1.23.0")
            implementation("androidx.credentials:credentials:1.3.0-rc01")
            implementation("androidx.credentials:credentials-play-services-auth:1.3.0-rc01")
            implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

            implementation(libs.sqldelight.android)
            implementation(libs.androidx.material.ripple)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.navigation.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // Maybe this is the fix for bug in separate desktop ver ???
            implementation("org.slf4j:slf4j-nop:2.0.10")
            // Drive
            implementation("com.google.api-client:google-api-client:2.5.0")
            implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
            implementation("com.google.apis:google-api-services-drive:v3-rev20231128-2.0.0")

            implementation(libs.sqldelight.jvm)
        }
    }

    sqldelight {
        databases {
            create("Database") {
                packageName.set("db")
            }
        }
    }
}

android {
    namespace = "com.yurhel.alex.anotes"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.yurhel.alex.anotes"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "com.yurhel.alex.anotes.MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.yurhel.alex.anotes"
            packageVersion = "3.0.0"
            description = "Desktop version ANotes app"
            copyright = "© 2024 Alex Yurhel. All rights reserved."

            windows {
                iconFile.set(project.file("icon.ico"))
            }
        }
    }
}
