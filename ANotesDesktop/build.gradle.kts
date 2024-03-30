import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
    id("app.cash.sqldelight") version "2.0.1"
}

group = "com.yurhel.alex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("db")
        }
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.slf4j:slf4j-nop:2.0.10")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.2.1")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
    implementation("org.json:json:20231013")
    // Drive
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20231128-2.0.0")
}

compose.desktop {
    application {
        mainClass = "com.yurhel.alex.anotes.MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ANotesDesktop"
            packageVersion = "1.0.0"
            description = "Desktop version ANotes app"
            copyright = "© 2024 Alex Yurhel. All rights reserved."

            windows {
                iconFile.set(project.file("icon.ico"))
            }
        }
    }
}
