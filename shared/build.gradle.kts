import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

val appVersion = providers.gradleProperty("app.version").get()

abstract class GenerateAppBuildInfo : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile.resolve("com/xechoz/sharefile/platform")
        dir.mkdirs()
        dir.resolve("AppBuildInfo.kt").writeText(
            """
            package com.xechoz.sharefile.platform

            object AppBuildInfo {
                const val VERSION = "${version.get()}"
            }
            """.trimIndent() + "\n",
        )
    }
}

val generateAppBuildInfo by tasks.registering(GenerateAppBuildInfo::class) {
    version.set(appVersion)
    outputDir.set(layout.buildDirectory.dir("generated/appBuildInfo"))
}

kotlin {
    android {
        namespace = "com.xechoz.sharefile.shared"
        compileSdk = 36
        minSdk = 26
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTest {}
    }
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        getByName("commonMain") {
            kotlin.srcDir(generateAppBuildInfo.map { it.outputDir })
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.components.resources)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        create("jvmCommonMain") {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.nanohttpd)
                implementation(libs.zxing.core)
            }
        }
        create("jvmCommonTest") {
            dependsOn(getByName("commonTest"))
            dependencies {
                implementation(kotlin("test"))
            }
        }
        getByName("androidMain") {
            dependsOn(getByName("commonMain"))
            dependsOn(getByName("jvmCommonMain"))
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.camera.core)
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                implementation(libs.kotlinx.coroutines.android)
            }
        }
        getByName("androidHostTest") {
            dependsOn(getByName("jvmCommonTest"))
        }
        getByName("desktopMain") {
            dependsOn(getByName("commonMain"))
            dependsOn(getByName("jvmCommonMain"))
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        getByName("desktopTest") {
            dependsOn(getByName("jvmCommonTest"))
        }
    }
}

compose.resources {
    packageOfResClass = "com.xechoz.sharefile.resources"
}
