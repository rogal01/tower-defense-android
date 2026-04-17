# iOS Engineering Notes For Enabling Support

This guide documents the current path for enabling the iOS side of the project on macOS.

It is intentionally framed as an engineering note, not as a fully verified turnkey setup. The iOS app shell exists, but the shared Kotlin module still contains JVM-specific APIs that need to be migrated before end-to-end parity is reliable.

## Prerequisites

- macOS with Xcode 15+ installed
- JDK 17
- Kotlin Multiplatform plugin in Android Studio if you want to inspect the shared module from the IDE

## Current Readiness

- SwiftUI and SpriteKit starter files are present under `ios/`
- iOS targets in `shared/build.gradle.kts` are disabled by default
- `commonMain` still contains `java.*`, `System.currentTimeMillis()`, and `Math.*` usage

Before treating iOS as supported, review [../docs/KMP_PORTABILITY_AUDIT.md](../docs/KMP_PORTABILITY_AUDIT.md).

## Step 1: Enable iOS targets in the shared module

Open `shared/build.gradle.kts` and uncomment the iOS target block:

```kotlin
listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
).forEach {
    it.binaries.framework {
        baseName = "shared"
        isStatic = true
    }
}
```

Also enable `iosMain` and point it at the consolidated iOS source folder:

```kotlin
sourceSets {
    commonMain.dependencies { }
    androidMain.dependencies { }
    val iosMain by getting {
        kotlin.srcDir("../ios/shared/src/iosMain/kotlin")
        dependencies { }
    }
}
```

## Step 2: Fix JVM-specific code in `commonMain`

Before iOS compilation works, replace the JVM-specific APIs that still exist in the shared module:

| Current API | Multiplatform-safe replacement |
| --- | --- |
| `java.util.Random()` | `kotlin.random.Random` |
| `java.util.Random(seed)` | `kotlin.random.Random(seed)` |
| `Math.sqrt(x)` | `kotlin.math.sqrt(x)` |
| `Math.random()` | a shared `Random` source |
| `Math.cos(x)` / `Math.sin(x)` | `kotlin.math.cos(x)` / `kotlin.math.sin(x)` |
| `Math.abs(x)` | `kotlin.math.abs(x)` |
| `Math.PI` | `kotlin.math.PI` |
| `System.currentTimeMillis()` | `kotlinx-datetime` or `expect/actual` helpers |
| `java.util.Calendar` | `kotlinx-datetime` or `expect/actual` helpers |

## Step 3: Build the shared framework

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

This generates:

```text
shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework
```

## Step 4: Create the Xcode project

1. Open Xcode
2. Create a new iOS App project
3. Use `TowerDefense` as the product name
4. Save it inside the repo's `ios/` directory

## Step 5: Link the shared framework

1. Add the generated framework directory to `Framework Search Paths`
2. Add `-framework shared` to `Other Linker Flags`
3. Add a run-script phase that rebuilds the Kotlin framework before compilation

Example run script:

```bash
cd "$SRCROOT/.."
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

## Step 6: Copy the Swift files

The `ios/app/` directory already contains starter Swift files:

- `TowerDefenseApp.swift`
- `GameContainerView.swift`
- `GameViewModel.swift`
- `GameSpriteView.swift`

Copy these into the Xcode project or replace the generated files.

## Step 7: Implement rendering parity

The Android version uses Canvas 2D. The iOS path is currently planned around SpriteKit.

Recommended next step:

- port the rendering responsibilities from Android `GameView.kt` / `EntityRenderer.kt`
- keep the gameplay rules in the shared Kotlin module
- let SpriteKit own only rendering and iOS-specific input presentation

## Notes

- The shared framework must be rebuilt whenever Kotlin code changes
- For release builds, use `linkReleaseFrameworkIosArm64`
- Test on the iOS simulator first, then on a physical device
- Until the portability audit is complete, describe iOS as scaffolded or in progress rather than fully supported
