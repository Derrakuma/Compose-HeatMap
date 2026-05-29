import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  jvmToolchain(17)

  androidLibrary {
    namespace = "com.fleeys.heatmap.core"
    compileSdk = rootProject.extra["compile_sdk"] as Int
    minSdk = 21
    androidResources {
      enable = true
    }
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    outputModuleName.set("heatmap")
    browser {
      binaries.executable()
      commonWebpackConfig {
        outputFileName = "heatmap.js"
        devServer = KotlinWebpackConfig.DevServer(
          static = mutableListOf("build/processedResources/wasmJs/main")
        )
      }
    }
  }

  jvm("desktop")

  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  )

  sourceSets {
    val desktopMain by getting
    val desktopTest by getting

    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
    }
    commonMain.dependencies {
      implementation(libs.compose.foundation)
      implementation(libs.compose.ui)
      implementation(libs.kotlinx.datetime)
    }
    desktopMain.dependencies {
      implementation(compose.desktop.currentOs)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    desktopTest.dependencies {
      implementation(libs.compose.ui.test.junit4)
    }
  }

}

dependencies {
  "androidRuntimeClasspath"(libs.compose.ui.tooling)
}

tasks.register<Copy>("copyJsResources") {
  from("src/wasmJsMain/resources")
  into("build/processedResources/wasmJs/main")
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.getByName("wasmJsProcessResources").dependsOn("copyJsResources")

mavenPublishing {
  signAllPublications()
  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

  coordinates("com.fleeys", "heatmap", "1.0.5")

  pom {
    name.set("heatmap")
    description.set("Effortlessly create GitHub-style heatmaps in Jetpack Compose?perfect for visualizing a variety of time-based data patterns.")
    inceptionYear.set("2024")
    url.set("https://github.com/iFleey/Compose-HeatMap")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("fleey")
        name.set("fleey")
        url.set("https://github.com/iFleey")
      }
    }
    scm {
      url.set("https://github.com/iFleey/Compose-HeatMap")
      connection.set("scm:git:git://github.com/iFleey/Compose-HeatMap.git")
      developerConnection.set("scm:git:ssh://git@github.com/iFleey/Compose-HeatMap.git")
    }
  }
}
