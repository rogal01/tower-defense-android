# Tower Defense Game

A feature-rich tower defense game for Android, built entirely with Kotlin and the Android Canvas API — no external game engines or sprite assets required. Every visual is procedurally drawn on Canvas.

## Screenshots

*The entire game is rendered procedurally — no sprites or image assets.*

## Gameplay

Defend your base from waves of enemies by placing towers, using special powers, and commanding your player character. Enemies follow winding S-curve paths from the top of the screen toward your base at the bottom, crossing an animated river via bridges.

### Core Mechanics

- **Player Character** — Tap anywhere to move. Automatically attacks the nearest enemy within range. Upgradeable damage, speed, HP, and attack range.
- **Tower Placement** — Place 5 tower types along the map. Each has unique stats, targeting priority (Close/First/Last/Strong), and upgradeable damage, range, and fire rate.
- **Wave System** — Enemies arrive in escalating waves. Every 5th wave spawns a boss with unique abilities.
- **Combo System** — Rapid kills build combos for bonus gold and a gold multiplier.
- **Powers** — 4 active powers (Fireball, Freeze, Heal, Lightning) with gold costs and cooldowns.

### Tower Types

| Tower | Cost | Damage | Range | Fire Rate | Role |
|-------|------|--------|-------|-----------|------|
| 🏹 Arrow | 30g | 8 | 200 | 1.2/s | Fast, cheap, balanced |
| 🧨 Magic | 60g | 14 | 220 | 0.8/s | High damage, long range |
| 💣 Cannon | 100g | 30 | 180 | 0.5/s | Heavy hitter, slow |
| ☠️ Poison | 80g | 6 | 210 | 1.0/s | Sustained DPS |
| ⚡ Tesla | 120g | 20 | 250 | 0.7/s | Best range, strong damage |

All towers can be upgraded (tap to select, then upgrade) for increased damage, range, and fire rate.

### Enemy Types

16 enemy variants including Goblins, Skeletons, Orcs, Demons, Dragons, and various boss minion types (Shadows, Slimes, Bats, Spiders, Wisps, Golem Shards).

### Boss System

10 unique bosses with distinct abilities, appearing every 5th wave (or every 3rd in Boss Rush):

| Boss | Ability | Effect |
|------|---------|--------|
| Orc King | Charge | Speed burst toward base |
| Lich Lord | Summon | Spawns extra minions |
| Demon Prince | AOE Damage | Damages nearby towers & base |
| Dragon Queen | Roar | Damages nearby player |
| Shadow Wraith | Teleport | Jumps ahead on path |
| Slime King | Split | Creates clones when low HP |
| Vampire Lord | Drain | Steals player gold |
| Spider Queen | Summon | Spawns spider minions |
| Frost Titan | Quake | Screen shake + slows towers |
| Stone Golem | Shield | Gains overshield HP |

### Difficulty Modes

- **Easy** — Reduced enemy stats, more starting gold
- **Normal** — Balanced experience
- **Hard** — Locked until wave 10 on Normal; tougher enemies, less gold
- **Endless** — Infinite waves with persistent high wave tracking

### Campaign Mode

15 hand-crafted levels that progressively introduce game mechanics:

| # | Level | Waves | Twist |
|---|-------|-------|-------|
| 1 | The Basics | 3 | Arrow towers only, no upgrades |
| 2 | Magic Touch | 4 | Arrow + Magic towers |
| 3 | Heavy Artillery | 5 | Adds Cannon |
| 4 | Upgrades! | 5 | Tower & hero upgrades unlocked |
| 5 | Boss Battle | 5 | First boss encounter |
| 6 | Toxic Strategy | 7 | All 5 tower types |
| 7 | Power Surge | 7 | Fireball & Freeze powers |
| 8 | Full Arsenal | 8 | Everything unlocked |
| 9 | The Horde | 10 | 1.5x spawn rate |
| 10 | Tower Budget | 8 | 0.6x gold income |
| 11 | Speed Demons | 8 | 1.5x enemy speed |
| 12 | Iron Wall | 10 | 1.5x enemy HP |
| 13 | Boss Rush | 9 | Boss every 3 waves |
| 14 | Arrows Only | 10 | Only Arrow towers allowed |
| 15 | Final Stand | 15 | All modifiers cranked up |

Levels unlock sequentially. Each completed level awards diamonds.

### Meta-Progression

- **Diamonds** — Earned from boss kills, rare enemy drops, and campaign completion. Persist across runs.
- **Skill Tree** — 10 upgradeable skills purchased with diamonds:

| Skill | Effect | Max Level |
|-------|--------|-----------|
| Golden Start | +15 starting gold/level | 5 |
| Fortified Base | +20 base HP/level | 5 |
| Sharp Blade | +3 attack damage | 5 |
| Swift Feet | +20 move speed | 5 |
| Tough Skin | +15 player HP | 5 |
| Tower Mastery | +8% tower damage/level | 5 |
| Treasure Hunter | +10% gold from kills/level | 5 |
| Diamond Magnet | +5% diamond drop chance/level | 3 |
| War Veteran | +3 gold per wave bonus | 5 |
| Eagle Eye | +15 attack range | 4 |

- **Achievements** — 25 achievements tracking combat milestones, progression, economy, and exploration:
  - 🗡️ First Blood, 🛡️ Survivor, ⚔️ Veteran, 👑 Legend, 🏆 Immortal, 🌟 Mythic
  - 💀 Slayer, 🔥 Destroyer, 💥 Annihilator
  - 🔗 Combo King, ⛓️ Combo God
  - ☠️ Boss Slayer, 🐉 Boss Hunter
  - 🏗️ Architect, 🏰 Fortress, 🎯 Arsenal
  - ✨ Sorcerer, ⬆️ Master Builder
  - 💰 Rich, 🤑 Millionaire, 📊 Score Chaser
  - 💎 Diamond Hoarder, 🔧 Mechanic, 🎖️ Well Rounded, ♾️ Endurance
- **Statistics** — Lifetime combat stats, progress tracking, and records

## Architecture

```
app/src/main/java/com/example/myapp/
├── MainActivity.kt          # Game activity (HUD buttons, tower/power/upgrade controls)
├── MainMenuActivity.kt      # Main menu (difficulty select, play, settings, skill tree, stats)
├── SettingsActivity.kt       # Audio & display settings
├── SkillTreeActivity.kt      # Diamond-based persistent upgrades
├── StatsActivity.kt          # Lifetime statistics viewer
├── AchievementsActivity.kt   # Achievements gallery
├── CampaignActivity.kt       # Campaign level select screen
├── SoundManager.kt           # Audio settings manager
└── game/
    ├── GameEngine.kt         # Core game logic (~1150 lines: waves, combat, paths, abilities, scoring, campaign)
    ├── GameView.kt           # SurfaceView renderer (~1100 lines: terrain, entities, HUD, touch input)
    ├── EntityRenderer.kt     # Procedural Canvas drawing for all entities (~1100 lines)
    ├── Enemy.kt              # Enemy data class, EnemyType, BossAbility, BossType enums
    ├── Player.kt             # Player entity (movement, attack, targeting)
    ├── Tower.kt              # Tower entity, TowerType enum with per-type stats, targeting modes
    ├── Projectile.kt         # Projectile entity
    ├── CampaignLevel.kt      # Campaign level definitions (15 levels)
    └── SkillTree.kt          # Persistent skill/diamond system
```

### Key Design Decisions

- **No external assets** — All visuals are procedurally drawn with Canvas paths, circles, arcs, and shapes. Each enemy, boss, tower, and the player have unique hand-drawn looks (~1100 lines of drawing code).
- **SurfaceView game loop** — Runs at ~60fps on a dedicated thread with `synchronized` locks for thread safety.
- **Waypoint path system** — 3 winding S-curve paths from screen top to base. Enemies follow assigned paths with waypoint interpolation.
- **Animated river** — Flows horizontally across the map with bridges where paths cross.
- **SharedPreferences persistence** — High scores, achievements, skill levels, diamonds, campaign progress, and lifetime stats all persist locally.

## Building

### Requirements

- Android Studio (latest stable)
- JDK 17+
- Android SDK 35 (compileSdk)
- Min SDK 26 (Android 8.0)

### Build & Run

```bash
# Clone the project
git clone https://github.com/rogal01/tower-defense-android.git
cd tower-defense-android

# Build from command line:
./gradlew assembleDebug

# Install on connected device:
./gradlew installDebug
```

Or open in Android Studio and hit Run.

### Dependencies

- `androidx.core:core-ktx:1.15.0`
- `androidx.appcompat:appcompat:1.7.0`
- `com.google.android.material:material:1.12.0`
- `androidx.constraintlayout:constraintlayout:2.2.0`
- `androidx.activity:activity-ktx:1.9.3`

No game engine, image loader, or third-party game libraries required.

## Configuration

Settings are available in-game:

- **Master / Music / SFX Volume** — Seek bars (for future audio implementation)
- **Screen Shake** — Toggle on/off
- **Show FPS** — Toggle debug FPS counter
- **Reset Progress** — Clears all saved data

## Tech Stack

- **Language:** Kotlin 2.1.0
- **Build:** Gradle 8.13, AGP 8.13.2
- **UI:** Android Canvas 2D, ViewBinding
- **Rendering:** Custom SurfaceView at ~60fps
- **Persistence:** SharedPreferences

## License

MIT
