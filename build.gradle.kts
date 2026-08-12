import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
  kotlin("jvm") version "2.3.20"
  id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "io.github.codeprism"
version = "262.0.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  testImplementation(kotlin("test"))

  intellijPlatform {
    intellijIdeaUltimate("2026.2")
    bundledPlugin("org.jetbrains.plugins.yaml")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
  }
}

kotlin {
  jvmToolchain(25)
}

intellijPlatform {
  pluginVerification {
    ides {
      create(IntelliJPlatformType.IntellijIdeaUltimate, "2026.2")
    }
  }
}

tasks {
  // This plugin has no searchable-options metadata. Skipping its IDE UI bootstrap
  // also keeps headless CI/package builds independent of desktop activation state.
  buildSearchableOptions {
    enabled = false
  }

  withType<JavaCompile> {
    sourceCompatibility = "25"
    targetCompatibility = "25"
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
  }

  withType<org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask> {
    // Keep Plugin Verifier's extracted-plugin cache inside the project build directory.
    jvmArgs("-Dplugin.verifier.home.dir=${layout.buildDirectory.dir("pluginVerifierHome").get().asFile.absolutePath}")
    // The verifier can still perform binary compatibility checks without fetching API-change metadata.
    offline.set(true)
  }

  patchPluginXml {
    sinceBuild.set("262")
    untilBuild.set("262.*")
  }

  runIde {
    // YAML is bundled in the target IDE. This task launches a sandboxed IDEA instance.
  }

}
