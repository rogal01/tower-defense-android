# iOS App Setup Guide — Tower Defense KMP

This guide walks you through setting up the iOS build on your Mac.

## Prerequisites
- macOS with Xcode 15+ installed
- JDK 17 (install via `brew install openjdk@17`)
- Kotlin Multiplatform plugin in Android Studio (optional)

## Step 1: Enable iOS targets in shared module

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

Also point `iosMain` at the consolidated iOS source folder:

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

## Step 2: Fix Java-specific code in commonMain

Before iOS compilation works, replace Java-specific APIs in the shared module:

| Java API | Kotlin/Common replacement |
|---|---|
| `java.util.Random()` | `kotlin.random.Random` |
| `java.util.Random(seed)` | `kotlin.random.Random(seed)` |
| `rng.nextFloat()` | `rng.nextFloat()` (same API) |
| `Math.sqrt(x)` | `kotlin.math.sqrt(x)` |
| `Math.random()` | `kotlin.random.Random.nextDouble()` |
| `Math.cos(x)` / `Math.sin(x)` | `kotlin.math.cos(x)` / `kotlin.math.sin(x)` |
| `Math.abs(x)` | `kotlin.math.abs(x)` |
| `Math.PI` | `kotlin.math.PI` |
| `System.currentTimeMillis()` | Create expect/actual or use `Clock.System` from kotlinx-datetime |
| `java.util.Calendar` | Create expect/actual or use kotlinx-datetime |

## Step 3: Build the shared framework

```bash
cd /path/to/project
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

This generates: `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework`

## Step 4: Create Xcode project

1. Open Xcode → File → New → Project → iOS → App
2. Product Name: `TowerDefense`
3. Interface: SwiftUI
4. Language: Swift
5. Save to the `ios/` directory in this project

## Step 5: Link the shared framework

1. In Xcode, select your project → Build Settings
2. Search "Framework Search Paths" → Add:
   ```
   $(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework
   ```
3. Add to "Other Linker Flags": `-framework shared`
4. Add a Run Script build phase (before "Compile Sources"):
   ```bash
   cd "$SRCROOT/.."
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   ```

## Step 6: Copy Swift files

The `ios/app/` directory already contains starter Swift files:
- `TowerDefenseApp.swift` — App entry point
- `GameContainerView.swift` — Main SwiftUI view with HUD
- `GameViewModel.swift` — Bridges shared GameEngine to Swift
- `GameSpriteView.swift` — SpriteKit wrapper (rendering TODO)

Copy these into your Xcode project or replace the generated files.

## Step 7: Implement rendering

The Android version uses Canvas 2D. For iOS, options are:
- **SpriteKit** (recommended) — Apple's 2D game framework
- **Metal** — Lower-level, more control
- **Core Graphics** — Similar to Android Canvas

Port `EntityRenderer.kt` and `GameView.kt` rendering logic to a SpriteKit `SKScene`.

## Project Structure

```
project/
├── app/                    # Android app module
├── shared/                 # KMP shared module
│   ├── src/commonMain/     # Pure Kotlin game logic
│   ├── src/androidMain/    # Android implementations
├── ios/                    # All iOS-specific files in one place
│   ├── app/                # Swift/Xcode app files
│   │   ├── TowerDefenseApp.swift
│   │   ├── GameContainerView.swift
│   │   ├── GameViewModel.swift
│   │   └── GameSpriteView.swift
│   ├── shared/src/iosMain/ # iOS-only Kotlin sources
│   └── IOS-SETUP-GUIDE.md
└── build.gradle.kts
```

## Notes
- The shared framework must be rebuilt whenever Kotlin code changes
- For release builds, use `linkReleaseFrameworkIosArm64` instead
- Test on iOS Simulator first, then physical device
