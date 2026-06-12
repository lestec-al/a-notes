import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    android {
        namespace = "com.yurhel.alex.anotes.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
        // For testing on android
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "META-INF/INDEX.LIST"
                excludes += "META-INF/DEPENDENCIES"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation.compose)
            implementation(libs.material.icons.extended)

            implementation(libs.sqldelight.coroutines)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ui.test)
            implementation(libs.sqldelight.jvm)
        }
        androidMain.dependencies {
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            implementation(libs.androidx.glance)
            implementation (libs.androidx.glance.appwidget)
            implementation (libs.androidx.glance.material3)

            implementation(libs.google.api.services.drive)
            implementation(libs.google.api.client.android)
            implementation(libs.googleid)
            implementation(libs.play.services.auth)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)

            implementation(libs.sqldelight.android)
        }
        jvmMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            implementation(libs.slf4j.nop) // The fix for the build bug in desktop ver ???

            implementation(libs.google.api.client)
            implementation(libs.google.oauth.client.jetty)
            implementation(libs.google.api.services.drive)

            implementation(libs.sqldelight.jvm)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
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

compose {
    resources {
        packageOfResClass = "com.yurhel.alex.anotes.shared"
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}