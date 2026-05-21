import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.compose.compiler)
}

val distributionsDir = layout.buildDirectory.dir("distributions")
val sampleWasmFile = layout.buildDirectory.file("compileSync/wasmJs/main/developmentExecutable/kotlin/heatmap-sample.wasm")

kotlin {
  jvmToolchain(17)

  androidTarget {

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    outputModuleName.set("heatmap-sample")
    browser {
      binaries.executable()
      commonWebpackConfig {
        outputFileName = "heatmap-sample.js"
        outputPath = distributionsDir.get().asFile
        devServer = KotlinWebpackConfig.DevServer(
          static = mutableListOf(distributionsDir.get().asFile.absolutePath)
        )
      }
    }
    binaries.executable()
  }

  jvm("desktop")

  sourceSets {
    val commonMain by getting
    val androidMain by getting
    val desktopMain by getting

    commonMain.dependencies {
      implementation(project(":library"))
      implementation(libs.compose.material)
      implementation(libs.compose.material3)
      implementation(libs.kotlinx.datetime)
    }

    androidMain.dependencies {
      implementation(libs.androidx.core.ktx)
      implementation(libs.androidx.appcompat)
      implementation(libs.material)
      implementation(libs.androidx.lifecycle.runtime.ktx)
      implementation(libs.androidx.activity.compose)
      implementation(libs.compose.ui.tooling.preview)
    }

    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
    }

  }
}

android {
  namespace = "com.fleeys.heatmap.sample"

  compileSdk = rootProject.extra["compile_sdk"] as Int

  defaultConfig {
    applicationId = "com.fleeys.heatmap.sample"
    minSdk = 21
    targetSdk = rootProject.extra["target_sdk"] as Int
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

compose.desktop {
  application {
    mainClass = "com.fleeys.heatmap.sample.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.fleeys.heatmap.sample"
      packageVersion = "1.0.0"
    }
  }
}

tasks.register<Copy>("copyHelperJs") {
  from(project(":library").file("src/wasmJsMain/resources/helper.js"))
  into(distributionsDir)
}

tasks.register<Copy>("copyHtml") {
  from(file("src/wasmJsMain/resources/index.html"))
  into(distributionsDir)
}

tasks.register<Copy>("copyWasmFile") {
  dependsOn("compileDevelopmentExecutableKotlinWasmJs")
  from(sampleWasmFile)
  into(distributionsDir)
  doLast {
    val wasmFile = sampleWasmFile.get().asFile
    println("Attempted to copy heatmap-sample.wasm from ${wasmFile.parentFile}")
    if (!wasmFile.exists()) {
      println("File not found! Check if the path is correct.")
    } else {
      println("Copied heatmap-sample.wasm to distributions directory.")
    }
  }
}

tasks.matching { it.name == "wasmJsBrowserRun" }.configureEach {
  dependsOn("copyHelperJs", "copyHtml", "copyWasmFile")
}
