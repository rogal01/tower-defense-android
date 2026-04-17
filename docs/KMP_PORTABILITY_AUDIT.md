# KMP Portability Audit

This document tracks the portability work that still needs to be completed before the shared gameplay module can be treated as fully Android+iOS ready.

## Current Status

- Android is the primary, verified runtime.
- The iOS shell and SpriteKit/SwiftUI scaffolding are present under `ios/`.
- iOS targets in `shared/build.gradle.kts` are intentionally disabled on the default branch.
- The main blocker is still inside `commonMain`: there are JVM-specific APIs that prevent a clean multiplatform build.

## Verified JVM-Specific Usage In `commonMain`

### Remaining migration categories

- `java.util.Random`
  - Used for randomized path generation and seeded run helpers in `shared/src/commonMain/kotlin/com/example/myapp/game/GameEngine.kt`
- `java.util.Calendar`
  - Used for daily challenge seeding in `shared/src/commonMain/kotlin/com/example/myapp/game/GameEngine.kt`
- `System.currentTimeMillis()`
  - Used for run-history timestamps and randomizer seeding in `shared/src/commonMain/kotlin/com/example/myapp/game/GameEngine.kt`
- `Math.*`
  - Still used heavily in `GameEngine.kt` for randomness, trigonometry, and distance calculations

### Completed in this pass

- Replaced low-risk geometry helpers in:
  - `Enemy.kt`
  - `Tower.kt`
  - `Player.kt`
  - `Projectile.kt`
- These now use `kotlin.math.sqrt`, which keeps those shared helpers multiplatform-safe.

## Recommended Migration Sequence

1. Replace `java.util.Random` with `kotlin.random.Random`
2. Replace `Math.random()` with a shared or injected `Random` source
3. Replace `Math.*` trigonometry/constants with `kotlin.math.*`
4. Move time/date access behind a multiplatform abstraction
   - `kotlinx-datetime` or a small `expect/actual` wrapper are both good options
5. Re-enable iOS Gradle targets only after the shared module is clean and verified

## Portfolio-Safe Positioning

Use this wording until the audit is resolved:

- Android: supported and verified
- iOS: scaffolded, partially integrated, and under active migration
- Shared gameplay core: substantial and real, but not yet fully portable end-to-end
