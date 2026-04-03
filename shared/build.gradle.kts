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

    // iOS targets — uncomment when building on Mac with Xcode
    // Requires replacing java.* imports in commonMain with kotlin.* equivalents first.
    // iOS-specific files are consolidated under ../ios/.
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
