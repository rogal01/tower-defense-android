# Tower Defense - Android & iOS

**The TD Ecosystem:** [📱 Kotlin/Android (Flagship)](https://github.com/rogal01/tower-defense-android) | [🎮 Godot (Fast Iteration)](https://github.com/rogal01/tower-defense-godot) | [⚙️ Unity (Code-First)](https://github.com/rogal01/tower-defense-unity-port)

A feature-rich tower defense game built with **Kotlin Multiplatform**, with shared gameplay logic and native rendering on **Android Canvas** and **iOS SpriteKit**.

This is the flagship repository in my tower-defense ecosystem. It combines game systems programming, cross-platform architecture, native rendering, progression design, and long-term content support in one codebase.

> Portfolio note: add a short gameplay GIF or screenshot strip near the top before making the repo public.

## At A Glance

- 6,300+ lines of shared game logic
- 10 tower types, 3 trap types, 18 enemy types, 10 bosses
- 8 maps and 40 campaign levels
- 6 game modes
- 50 achievements and 16 persistent skills
- English and Polish localization
- Native Android and iOS frontends without a third-party game engine

## Why This Matters To Clients

- It shows that I can share core business logic across platforms while keeping native-feeling frontends.
- It proves I can work without depending on a full third-party engine when the product calls for tighter platform control.
- It demonstrates how I separate simulation rules from rendering and platform integration.
- It translates well to cross-platform product work where the same logic must survive very different presentation layers.

## Why This Repo Stands Out

- **Cross-platform architecture**: gameplay systems live in a shared Kotlin Multiplatform module while each platform keeps native rendering and integrations.
- **No external game engine**: rendering is handled with Android Canvas and iOS SpriteKit rather than Unity or Godot.
- **Procedural presentation**: visuals are hand-drawn in code instead of relying only on asset packs.
- **Large gameplay surface**: towers, traps, bosses, powers, campaign content, meta progression, and achievements all interact in one system.

## Gameplay Systems

### Core Loop
- Place towers and traps to stop waves of enemies before they reach the base.
- Move and upgrade a player-controlled hero who auto-attacks nearby enemies.
- Spend gold during a run and diamonds between runs.
- Combine towers, powers, upgrades, and map positioning to survive increasingly complex waves.

### Included Systems
- 10 unique towers with upgrades, targeting modes, and special abilities
- 3 trap types for path control and passive damage
- Elite enemies and boss encounters with unique mechanics
- Multiple game modes including campaign, endless, boss rush, and daily challenge
- Combo system, bounty objectives, wave modifiers, and day/night gameplay effects
- Persistent progression via achievements, skill tree upgrades, diamonds, stats, and campaign stars

## Technical Architecture

### Shared Layer
The `shared/` module contains the gameplay core:

- game rules and simulation
- enemies, towers, player, projectiles, and maps
- progression systems such as skills, campaign data, and achievements
- platform-agnostic interfaces for preferences and audio

### Android Layer
The Android app uses:

- `SurfaceView` for the main render loop
- Android Canvas for world and HUD rendering
- SharedPreferences for persistence
- native activities for menus, settings, progression, and stats

### iOS Layer
The iOS app uses:

- SwiftUI for app and HUD composition
- SpriteKit for game rendering
- UserDefaults for persistence
- the same shared Kotlin gameplay logic compiled into an iOS framework

## Project Layout

```text
shared/   # Kotlin Multiplatform gameplay code
app/      # Android app, menus, rendering, audio, UI
ios/      # iOS app, SwiftUI container, SpriteKit integration
```

## Build

### Android

```bash
git clone https://github.com/rogal01/tower-defense-android.git
cd tower-defense-android
./gradlew assembleDebug
./gradlew installDebug
```

### iOS

1. Build the shared framework with Gradle.
2. Open the iOS project in Xcode.
3. Link the generated framework.
4. Run on simulator or device.

See `ios/IOS-SETUP-GUIDE.md` for the full iOS setup flow.

## Technical Highlights

- cross-platform software architecture
- native rendering without a third-party engine
- shared gameplay simulation across mobile platforms
- long-form game systems design and progression balancing
- maintainable separation between logic, rendering, and platform APIs

## Part Of A Multi-Engine Ecosystem

This repository is the **flagship** version of a broader tower-defense portfolio that also includes:

- a **Godot** version focused on fast iteration, UI flow, and engine-driven experimentation
- a **Unity** version focused on code-first runtime structure and maintainable gameplay bootstrap

Together, the three repositories show that I can adapt the same product space across different engines and technical constraints instead of staying locked into a single stack.

## License

MIT. See `LICENSE` for details.
