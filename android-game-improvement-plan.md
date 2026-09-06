# Android Game Experience Overhaul

## Goal
Transform the Android Tower Defense game into a modern, tactile, and straightforward mobile experience by overhauling button designs, replacing cluttered menus with a contextual inspector, improving touch ergonomics, and polishing the visual presentation.

## Tasks
- [x] Task 1: Create reusable rounded button and card drawables in `app/src/main/res/drawable/` (`bg_btn_primary.xml`, `bg_btn_action.xml`, `bg_btn_danger.xml`, `bg_btn_green.xml`, `bg_hud_chip.xml`, etc.) -> Verified: Drawables compile without resource errors.
- [x] Task 2: Redesign top in-game HUD in `activity_main.xml` (compact badges for Gold/Wave/Diamonds and unified Speed/Auto/Pause cluster) -> Verified: Top HUD fits seamlessly across all screen widths.
- [x] Task 3: Replace the 16-button double horizontal scroll with a Categorized Build Dock ([🏹 Towers] [🛡️ Defenses] [⚡ Hero]) in `activity_main.xml` -> Verified: Switching tabs toggles visible building items cleanly.
- [x] Task 4: Add Contextual Tower Inspector Card to `activity_main.xml` and wire it in `MainActivity.kt` (shows tower details, Upgrade, Target Mode, Ability, Sell, and Deselect) -> Verified: Tapping a tower opens the inspector card; tapping ground or Deselect closes it.
- [x] Task 5: Add Active Placement Bar with a Cancel Placement button in `activity_main.xml` & `MainActivity.kt` -> Verified: Player can cancel tower/trap placement at any time without accidental builds.
- [x] Task 6: Modernize right-side Power buttons (Fireball, Freeze, Lightning, Dash) with circular badges, cooldown text, and haptic feedback -> Verified: Power buttons animate and display readable cooldown counters.
- [x] Task 7: Improve touch interactions and visual indicators in `GameView.kt` (valid/invalid placement rings, pulsing tower selection ring, hero drag disambiguation) -> Verified: Canvas renders green/red placement circle and selected tower ring.
- [x] Task 8: Restyle Main Menu in `activity_menu.xml` with card-based hierarchy (Campaign, Endless, Continue, quick utility grid) -> Verified: Main Menu displays structured game modes and language toggle.
- [x] Task 9: Redesign Audio and Sound Effects in `SoundManager.kt` and `MusicManager.kt` -> Verified: 44.1 kHz procedural sound effects with ADSR envelopes, distinct Menu / Battle melodic tracks.
- [x] Task 10: Fix save file system in `GameEngine.kt` and `MainActivity.kt` for long runs -> Verified: Towers, traps, blockades, endless buffs, and per-wave auto-saving are serialized and restored reliably.
- [x] Task 11: Balance upgrade system in `Tower.kt` and `GameEngine.kt` -> Verified: Max level caps (Tower lvl 10, Player attributes lvl 10-20), linear damage growth, quadratic gold costs.
- [x] Task 12: Full verification & APK build (`./gradlew testDebugUnitTest` and `./gradlew assembleDebug`) -> Verified: Tests pass and debug APK `app-debug.apk` builds cleanly (6.70 MB).

## Done When
- [x] In-game bottom bar has zero confusing 16-button horizontal scrollbar clutter and provides a dedicated contextual inspector for selected towers.
- [x] Placements can be cancelled cleanly with visible UI feedback.
- [x] All buttons have modern rounded shapes, high-contrast typography, and tactile ripple feedback.
- [x] Main menu is visually engaging and straightforward to navigate.
- [x] Audio engine plays high-fidelity procedural SFX and adaptive background music.
- [x] Save files preserve entire field state (towers, traps, blockades, endless buffs) across long runs.
- [x] Upgrade system is strictly balanced with maximum level caps and quadratic gold curves.
- [x] `./gradlew assembleDebug` completes with 0 errors.
