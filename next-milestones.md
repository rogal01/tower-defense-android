# Next Milestones: Tower Defense Android

## Goal
Elevate gameplay depth, boss encounters, and roguelite replayability following the foundation established in `PRODUCT.md` and `DESIGN.md`.

## Proposed Tracks & Actionable Tasks

### Track 1: Boss Telegraphs & Special Phase Attacks (Tactical Intensity)
- [x] Task 1: Add telegraph zone rendering in `GameView.kt` (red translucent charge circles/lines before boss abilities trigger) → Visual red warning indicator appears 1.5s before boss ability fires with pulsing border and radial sweep.
- [x] Task 2: Implement 3 distinct boss phase mechanics in `GameEngine.kt` (*Titan Stomp* stunning adjacent towers for 3s, *Shadow Minion Summon*, *Shield Barrier*) → Boss health thresholds (75%, 50%, 25%) trigger phase abilities with telegraphs in unit tests.
- [x] Task 3: Add boss health bar HUD overlay with phase notches and ability cooldown timer → Boss HP bar displays ability warning icon and countdown when charging.
- [x] Task 3b: Dynamic Campaign 3-Star Objectives & Heroic Challenge Mode (+35% enemy HP, +20% speed, rapid telegraphs, +5 diamond bounty, 💀 golden badge).

### Track 2: Hero Active Relics & Equipment (Metagame & Build Variety)
- [ ] Task 4: Create Relic system in `shared` engine with 6 equipable relics (e.g. *Ring of Sparks*, *Frost Amulet*, *Vampiric Blade*, *Crown of Wealth*) → Verify: Relic passives modify hero stats and apply in unit tests.
- [ ] Task 5: Add Relic loadout slot in `activity_menu.xml` and in-run merchant shop draft pool → Verify: Player can equip 1 relic in menu and draft temporary relics from Merchant.

### Track 3: Roguelite Run Mutators (Replayability & Modifiers)
- [ ] Task 6: Implement Mutator system for Endless & Daily Challenge (e.g. *Speed Frenzy* +50% speed / +50% gold, *Elemental Surge* enemies immune to 1 element, *Tight Economy*) → Verify: Mutator modifiers adjust creep stats and rewards accordingly.
- [ ] Task 7: Add Mutator selection chip selector to `dialog_map_selection.xml` → Verify: Selected mutators display with badges on start screen.

## Done When
- [x] Chosen track implemented with unit tests in `GameEngineSystemTest.kt` and `EnemySystemTest.kt` passing.
- [x] All new UI components comply with 48dp touch targets and "Neon Citadel" palette in `DESIGN.md`.
- [ ] Release APK built and verified via GitHub Actions.
