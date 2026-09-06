# Game Expansion Roadmap

## Goal
Transform the tower defense game into a rich, replayable RPG tower defense experience with branching tower specializations, hero artifact loot drops, interactive field hazards, and an illustrated bestiary codex.

## Tasks
- [x] Task 1: Add Branching Tower Specializations at Level 5 in `Tower.kt` (e.g., Arrow -> Sniper Perch vs Ranger Volley) -> Verify: Reaching Lv 5 presents branch choice and updates tower projectile behavior. (COMPLETED)
- [ ] Task 2: Implement Hero Artifact Loot System in `GameEngine.kt` (relic drops from bosses: Boots of Swiftness, Midas Crown, Vampiric Fang) -> Verify: Defeating bosses drops equipped passive relics.
- [ ] Task 3: Add Elemental Combos in `GameEngine.kt` (Ice + Tesla = Superconductor AoE stun; Flame + Tar = Ignited Lava hazard) -> Verify: Afflicting enemies with dual elements triggers combo visual and damage.
- [ ] Task 4: Add Interactive Map Hazards in `GameView.kt` & `GameEngine.kt` (tappable explosive powder kegs and collapsible stone bridges) -> Verify: Hero or projectiles detonating powder kegs deals AoE blast to nearby creeps.
- [x] Task 5: Add Animated Bestiary & Lore Codex screen (`activity_bestiary.xml` & `BestiaryActivity.kt`) -> Verify: Bestiary lists unlocked enemies with pixel art sprites, stats, and elemental weaknesses. (COMPLETED)
- [ ] Task 6: Add Blood Moon & Thunderstorm Dynamic Weather Events in `GameEngine.kt` -> Verify: Random weather waves display screen tint, lightning strikes, and bonus diamond rewards.
- [x] Task 7: Full verification & APK build (`./gradlew testDebugUnitTest` and `./gradlew assembleDebug`) -> Verify: All tests pass and debug APK compiles cleanly. (COMPLETED)

## Done When
- [x] Towers have meaningful Level 5 archetype choices altering gameplay.
- [ ] Defeating bosses provides collectible run-modifying artifacts.
- [ ] Players can trigger reactive environmental combos on the battlefield.
- [x] The Main Menu offers an interactive Bestiary compendium.
- [x] `./gradlew assembleDebug` completes with 0 errors.

## Notes
- Keep all sprites procedurally baked in `SpriteManager` to maintain <10MB APK footprint.
- Keep artifact perks lightweight to prevent breaking the balanced upgrade curve.
- Added 5 new monster archetypes: Dark Necromancer, Phantom Ghost, Magma Crab, Screeching Harpy, and Ancient Treant with unique combat mechanics, dynamic elemental interactions, and pixel art sprites.
