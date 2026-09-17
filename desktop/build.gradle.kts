import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

val appVersion = providers.gradleProperty("app.version").get()

abstract class GenerateSlimLauncher : DefaultTask() {
    @get:Input
    abstract val version: Property<String>

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            templateFile.get().asFile.readText().replace("@APP_VERSION@", version.get()),
        )
        output.setExecutable(true)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.xechoz.sharefile.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ShareFile"
            packageVersion = appVersion
            linux {
                iconFile.set(project.file("packaging/icon/sharefile.png"))
            }
            windows {
                iconFile.set(project.file("packaging/icon/sharefile.ico"))
            }
        }
    }
}

val generateSlimLauncher by tasks.registering(GenerateSlimLauncher::class) {
    version.set(appVersion)
    templateFile.set(layout.projectDirectory.file("packaging/sharefile.in"))
    outputFile.set(layout.buildDirectory.file("generated/slim/sharefile"))
}

val slimDist by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Stages the app for system-JRE Linux packages (deb/rpm/arch)"
    dependsOn(tasks.named("createDistributable"), generateSlimLauncher)
    into(layout.buildDirectory.dir("slim"))
    from(layout.buildDirectory.dir("compose/binaries/main/app/ShareFile/lib/app")) {
        into("lib")
    }
    from(generateSlimLauncher) {
        into("bin")
        filePermissions {
            unix("755")
        }
    }
}
