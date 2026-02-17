import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    `maven-publish`
}

group = "com.github.ArcaDone"
version = "1.0.0"

publishing {
    publications.withType<MavenPublication> {
        val artifactIdSuffix = artifactId.substringAfter(project.name)
        val targetName = if (artifactIdSuffix == project.name) "" else artifactIdSuffix

        artifactId = "scanposeui${if (targetName.isNotEmpty()) "-$targetName" else ""}"

        pom {
            name.set("Scanpose Shared UI")
            description.set("Shared UI components for Scanpose")
            url.set("https://github.com/Arcadone/Scanpose")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("arcadone")
                    name.set("Danilo Arcadipane")
                    email.set("danilo.arcadipane@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/Arcadone/Scanpose.git")
                developerConnection.set("scm:git:ssh://github.com/Arcadone/Scanpose.git")
                url.set("https://github.com/Arcadone/Scanpose")
            }
        }
    }
}

kotlin {
    androidTarget() //We need the deprecated target to have working previews

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            api(compose.material)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.compose.nav3)
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)
            implementation(libs.multiplatformSettings)
            implementation(libs.kotlinx.datetime)
            implementation(project(":scanner"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}
android {
    namespace = "com.arcadone.scanpose"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
