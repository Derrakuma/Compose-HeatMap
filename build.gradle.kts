// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  extra.apply {
    set("compile_sdk", 37)
    set("target_sdk", 36)
  }
}

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.androidMultiplatformLibrary) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.jetbrainsKotlinAndroid) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.mavenPublish) apply false
}
