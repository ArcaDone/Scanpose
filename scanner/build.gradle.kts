plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    `maven-publish`
    signing
}

group = "com.github.ArcaDone"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }

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
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activityCompose)
            implementation(libs.mlkit.barcode.scanning)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
        }

        iosMain.dependencies {
        }
    }
}

android {
    namespace = "com.arcadone.scanpose.scanner"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    publications.withType<MavenPublication> {
        val javatarget = "jvm"
        val artifactIdSuffix = artifactId.substringAfter(project.name)
        val targetName = if (artifactIdSuffix == project.name) "" else artifactIdSuffix

        artifactId = "scanpose${if (targetName.isNotEmpty()) "-$targetName" else ""}"
        
        pom {
            name.set("Scanpose Scanner")
            description.set("Barcode scanning module for Scanpose")
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

signing {
    sign(publishing.publications)
}