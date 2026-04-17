plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    // iOS targets remain intentionally disabled on the default branch.
    // The iOS shell exists under ../ios/, but commonMain still contains JVM-only APIs
    // that must be migrated before end-to-end iOS builds are reliable.
    // listOf(
    //     iosX64(),
    //     iosArm64(),
    //     iosSimulatorArm64()
    // ).forEach {
    //     it.binaries.framework {
    //         baseName = "shared"
    //         isStatic = true
    //     }
    // }

    sourceSets {
        commonMain.dependencies { }
        androidMain.dependencies { }
        // Enable iosMain only after the commonMain portability audit is resolved.
        // val iosMain by getting {
        //     kotlin.srcDir("../ios/shared/src/iosMain/kotlin")
        //     dependencies { }
        // }
    }
}

android {
    namespace = "com.example.myapp.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
