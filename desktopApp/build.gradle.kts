import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    implementation(libs.androidx.lifecycle.viewmodelCompose)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.slf4j.nop) // The fix for the build bug in desktop ver
}

compose.desktop {
    application {
        mainClass = "com.yurhel.alex.anotes.MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ANotes"
            packageVersion = "${libs.versions.shared.versionName.get()}.0"
            description = "ANotes: desktop version"
            copyright = "© 2026 Aliaksei Yurhel. All rights reserved."

            windows {
                iconFile.set(project.file("icon.ico"))
            }
        }
    }
}