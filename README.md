# Tower Defense - Kotlin Multiplatform Flagship

**The TD Ecosystem:** [📱 Kotlin/Android (Flagship)](https://github.com/rogal01/tower-defense-android) | [🎮 Godot (Fast Iteration)](https://github.com/rogal01/tower-defense-godot) | [⚙️ Unity (Code-First)](https://github.com/rogal01/tower-defense-unity-port)

A feature-rich tower defense game built around a shared Kotlin gameplay core, with a fully supported Android runtime and an in-progress iOS integration path.

This repository is the flagship project in my tower-defense portfolio. It focuses on game systems programming, native mobile rendering, long-term progression design, and the tradeoffs involved in moving shared logic across platform boundaries without depending on Unity or Godot.

> Portfolio note: add a short gameplay GIF or a screenshot strip near the top before making the repo public.

## At A Glance

- 6,000+ lines of shared gameplay logic
- 10 tower types, 3 trap types, 18 enemy types, 10 bosses
- 8 maps and 40 campaign levels
- 6 game modes
- 50 achievements and 16 persistent skills
- English and Polish localization
- Native Android frontend with an iOS app shell already scaffolded

## Why This Matters To Clients

- It shows that I can separate core logic from rendering and platform-specific UI.
- It demonstrates that I can adapt architecture across multiple stacks instead of locking everything into one engine.
- It proves I can ship maintainable systems in a content-heavy product, not just isolated gameplay prototypes.
- It maps well to client work where the same business logic needs to survive different runtimes, interfaces, or technical constraints.

## Platform Readiness

| Platform | Status | Notes |
| --- | --- | --- |
| Android | Primary, verified runtime | Main supported platform today. Builds and runs from the Android module. |
| iOS | Scaffolded, partial, in progress | SwiftUI/SpriteKit shell exists, but the shared module still contains JVM-only APIs and the iOS Gradle targets are intentionally disabled on the default branch. |

If you want the exact blockers for iOS parity, see [docs/KMP_PORTABILITY_AUDIT.md](docs/KMP_PORTABILITY_AUDIT.md) and [ios/IOS-SETUP-GUIDE.md](ios/IOS-SETUP-GUIDE.md).

## Quick Ecosystem Comparison

| Repo | Primary Strength | Best Use Case |
| --- | --- | --- |
| Kotlin/Android | Shared mobile architecture | Native-feeling mobile products with a large reusable logic layer |
| Godot | Fast iteration and UX polish | Gameplay validation, onboarding, UI flow, fast prototyping |
| Unity | Code-first runtime structure | Engine-based products that still need maintainable architecture |

## Core Gameplay Systems

- 10 unique towers with upgrades, targeting modes, and special abilities
- 3 trap types for lane control and passive damage
- Elite enemies and boss encounters with unique mechanics
- Multiple modes including campaign, endless, boss rush, daily challenge, randomizer, and loadout
- Persistent progression via achievements, skill tree upgrades, diamonds, stats, and campaign stars
- Player-controlled hero with movement, dash, upgrades, and repair utility

## Technical Architecture

### Shared layer

The `shared/` module contains the gameplay core:

- simulation rules and combat logic
- towers, enemies, player, projectiles, traps, and bosses
- campaign data, achievements, skill tree, and run history logic
- platform-facing interfaces for preferences and audio

### Android layer

The `app/` module is the production runtime today:

- `MainActivity` now acts as a thin coordinator
- `MainGameSessionConfigurator` applies intent-driven run setup such as campaign, loadout, map, and continue flow
- `MainGameUiController` owns HUD wiring, button behavior, dialogs, and lifecycle-oriented run controls
- `game/GameView.kt` contains the Android render loop, touch handling, and Canvas drawing

### iOS layer

The `ios/` folder contains the current iOS shell:

- SwiftUI app container and view-model bridge
- planned SpriteKit rendering path
- integration notes for enabling the shared framework once `commonMain` becomes fully portable

## Project Layout

```text
app/     Android UI, rendering, activities, audio, platform integration
shared/  Kotlin Multiplatform gameplay logic and progression systems
ios/     SwiftUI/SpriteKit shell and iOS integration notes
docs/    Portfolio docs, audit notes, and case-study support material
```

## Build

### Android

Verified local prerequisites for this repo:

- Android Studio JBR or another Java 21 JDK for the Gradle daemon
- Android SDK installed and exposed through `ANDROID_HOME` or `local.properties`

```bash
git clone https://github.com/rogal01/tower-defense-android.git
cd tower-defense-android
./gradlew assembleDebug
./gradlew installDebug
```

### Release signing

Release signing is optional for public clones. If you want a locally signed release build, add:

- `keystore.jks` in the repo root
- `KEYSTORE_PASSWORD` and `KEY_PASSWORD` in `local.properties`

If those are missing, the project still remains usable for debug builds and code review.

### iOS

iOS is not presented as a turnkey build yet. The current state is:

1. shared iOS targets are intentionally disabled by default
2. `commonMain` still needs portability cleanup
3. the SwiftUI/SpriteKit shell is present for future parity work

See [ios/IOS-SETUP-GUIDE.md](ios/IOS-SETUP-GUIDE.md) for the current engineering notes.

## Technical Highlights

- native Android rendering without a third-party engine
- large shared gameplay simulation in Kotlin Multiplatform
- clear separation between game rules, rendering, and platform integration
- content-heavy progression systems with campaign, meta upgrades, and achievements
- architecture work that is honest about what is already shipping versus what is still under migration

## Engineering Challenges Solved

- kept a large gameplay surface maintainable while supporting multiple maps, modes, and progression systems
- structured the Android gameplay screen around smaller collaborators instead of a single oversized activity
- isolated the real KMP portability blockers instead of overstating cross-platform readiness
- preserved a native mobile feel without relying on a heavyweight engine

## Best-Fit Client Work

This repo is a strong fit for clients who need:

- native Android work with heavy custom logic
- shared logic across multiple frontends or runtimes
- maintainable game or simulation systems with long-term feature growth
- architecture support for products that cannot rely on one engine or framework forever

## What I Would Improve Next

- finish the remaining `commonMain` portability migration so the iOS framework can be re-enabled with confidence
- extract more render-only responsibilities out of `GameView.kt`
- add a proper media pack with gameplay GIFs and platform-specific screenshots
- rename legacy internal package names like `com.example.myapp` in a dedicated cleanup pass

## License

MIT. See [LICENSE](LICENSE) for details.
