import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Generated Gradle files can be redirected outside a synchronized workspace.
// This avoids duplicate intermediate JARs created by macOS file providers.
val localGradleProperties = Properties().apply {
    val localConfig = rootProject.file(".gradle-local.properties")
    if (localConfig.isFile) {
        localConfig.inputStream().use(::load)
    }
}

localGradleProperties.getProperty("build.root")
    ?.takeIf(String::isNotBlank)
    ?.let { configuredBuildRoot ->
        val externalBuildRoot = file(configuredBuildRoot)
        layout.buildDirectory.set(externalBuildRoot.resolve("root"))
        subprojects {
            layout.buildDirectory.set(externalBuildRoot.resolve(name))
        }
    }
