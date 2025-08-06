// build.gradle.kts (Project Level)
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
// Remove the allprojects block that contained the repository declarations.
// It's no longer needed because repositories are defined centrally in settings.gradle.kts.