# 🏰 Tower Defense — Android

A feature-rich tower defense game for Android, built entirely with **Kotlin** and the **Android Canvas API**. No external game engines, no sprites — every visual is procedurally drawn on Canvas with over 1,100 lines of hand-crafted drawing code.

> **4,300+ lines** of game logic & rendering · **6 tower types** · **16 enemy types** · **10 unique bosses** · **15 campaign levels** · **37 achievements** · **16 persistent skills** · **3 maps**

---

## Table of Contents

- [Gameplay](#gameplay)
- [Towers](#towers)
- [Enemies & Bosses](#enemies--bosses)
- [Powers & Abilities](#powers--abilities)
- [Game Modes](#game-modes)
- [Campaign](#campaign)
- [Wave Modifiers](#wave-modifiers)
- [Advanced Mechanics](#advanced-mechanics)
- [Meta-Progression](#meta-progression)
- [Architecture](#architecture)
- [Building](#building)
- [Tech Stack](#tech-stack)
- [License](#license)

---

## Gameplay

Defend your base from waves of enemies by placing towers, using special powers, and commanding your player character. Enemies follow winding paths from the top of the screen toward your base, crossing an animated river via bridges.

### Core Loop

- **Player Character** — Tap/drag to move. Auto-attacks nearest enemy in range. Upgradeable damage, speed, HP, and range.
- **Tower Placement** — Place towers along the map. Each has unique stats, a targeting mode (Close / First / Last / Strong), upgrades, and a special ability.
- **Wave System** — Enemies arrive in escalating waves. Every 5th wave spawns a boss with unique abilities. Wave modifiers add variety.
- **Combo System** — Rapid kills chain into combos for bonus gold multipliers.
- **Powers** — 4 active powers with gold costs and cooldowns.
- **Dash** — Teleport toward enemies dealing AoE damage along the path. 8-second cooldown.

---

## Towers

6 tower types, each with unique stats, damage types, targeting modes, upgrades, and an activatable special ability.

| Tower | Cost | Damage | Range | Fire Rate | Type | Ability |
|-------|------|--------|-------|-----------|------|---------|
| 🏹 Arrow | 30g | 8 | 200 | 1.2/s | Physical | **Volley** — burst of arrows |
| 🧨 Magic | 60g | 14 | 220 | 0.8/s | Magic | **Arcane Blast** — AoE burst |
| 💣 Cannon | 100g | 30 | 180 | 0.5/s | Explosive | **Napalm** — area fire |
| ☠️ Poison | 80g | 6 | 210 | 1.0/s | Poison | **Plague** — poison spread |
| ⚡ Tesla | 120g | 20 | 250 | 0.7/s | Electric | **Overcharge** — chain lightning |
| ❄️ Ice | 70g | 0 | 230 | — | Ice | **Deep Freeze** — freezes enemies |

All towers can be upgraded to level 5, increasing damage, range, and fire rate. Tap a tower to select it, then upgrade, sell, change targeting, or activate its ability.

### Tower Synergy

Same-type towers placed near each other boost each other's damage by **+10% per nearby tower** (max 3 stacks = **+30%**). Synergized towers display a colored glow ring.

---

## Enemies & Bosses

### 16 Enemy Types

Goblins, Skeletons, Orcs, Demons, Dragons, and specialized boss minions — Shadows, Slimes, Bats, Spiders, Wisps, and Golem Shards. Each type has unique colors, shapes, speed, and HP scaling.

### Elite Enemies

Every 5th non-boss wave (starting from wave 5) spawns one **Elite Enemy** with:
- 3× HP, 1.5× damage, 2× gold reward
- A golden crown and glow ring for easy identification

### 10 Unique Bosses

Bosses appear every 5th wave with distinct abilities and themed minion squads. No repeats until all 10 have appeared.

| Boss | Ability | HP | Minions | Effect |
|------|---------|-----|---------|--------|
| 👹 Orc King | Charge | 700 | 6 Orcs | Speed burst toward base |
| 💀 Lich Lord | Summon | 600 | 8 Skeletons | Spawns extra minions |
| 😈 Demon Prince | AOE Damage | 900 | 5 Demons | Damages nearby towers & base |
| 🐲 Dragon Queen | Roar | 1100 | 4 Dragons | Damages & buffs nearby minions |
| 👻 Shadow Wraith | Teleport | 550 | 10 Shadows | Jumps ahead on path |
| 🦠 Slime King | Split | 800 | 12 Slimes | Creates clones when low HP |
| 🧛 Vampire Lord | Drain | 750 | 8 Bats | Steals player gold |
| 🕷️ Spider Queen | Summon | 650 | 7 Spiders | Spawns spider waves |
| ❄️ Frost Titan | Quake | 1200 | 6 Wisps | Screen shake + slows towers |
| 🪨 Stone Golem | Shield | 1400 | 5 Shards | Gains damage-absorbing overshield |

---

## Powers & Abilities

### Active Powers

| Power | Cost | Cooldown | Effect |
|-------|------|----------|--------|
| 🔥 Fireball | 40g | 8s | AoE fire damage at player location |
| ❄️ Freeze | 30g | 12s | Slows all enemies on screen |
| 💚 Heal | 25g | 15s | Restores base HP |
| ⚡ Lightning | 50g | 10s | Chain lightning between enemies |

### Player Dash

💨 **Dash** — Teleport up to 200 units toward your move target, dealing AoE damage to all enemies along the dash path. 8-second cooldown. Displayed as a teal bar under the player HP bar.

---

## Game Modes

| Mode | Description |
|------|-------------|
| **Classic** | Standard waves with scaling difficulty. 3 difficulty levels (Easy / Normal / Hard). |
| **Endless** | Infinite waves. Separate high wave record. |
| **Boss Rush** | Boss every 3 waves. How many can you defeat? |
| **Daily Challenge** | Seeded daily run with fixed parameters. |
| **Campaign** | 15 hand-crafted levels with progressive mechanics and restrictions. |

### Maps

3 procedurally-winding maps with randomized path jitter each run:
- 🗺️ **Classic** — Standard 3-path layout
- 🏔️ **Valley** — Mountain terrain variation
- ➕ **Crossroads** — Intersecting path layout

---

## Campaign

15 levels that progressively introduce game mechanics. Each completed level awards diamonds.

| # | Level | Waves | Twist |
|---|-------|-------|-------|
| 1 | 🏹 The Basics | 3 | Arrow towers only, no upgrades |
| 2 | 🧨 Magic Touch | 4 | Arrow + Magic towers |
| 3 | 💣 Heavy Artillery | 5 | Adds Cannon |
| 4 | ⬆️ Upgrades! | 5 | Tower & hero upgrades unlocked |
| 5 | 👑 Boss Battle | 5 | First boss encounter |
| 6 | ☠️ Toxic Strategy | 7 | All tower types available |
| 7 | 🔥 Power Surge | 7 | Fireball & Freeze powers |
| 8 | ⚔️ Full Arsenal | 8 | Everything unlocked |
| 9 | 🐜 The Horde | 10 | 1.5× spawn rate |
| 10 | 💰 Tower Budget | 8 | 0.6× gold income |
| 11 | 💨 Speed Demons | 8 | 1.5× enemy speed |
| 12 | 🛡️ Iron Wall | 10 | 1.5× enemy HP |
| 13 | 💀 Boss Rush | 9 | Boss every 3 waves |
| 14 | 🏹 Arrows Only | 10 | Only Arrow towers allowed |
| 15 | ⭐ Final Stand | 15 | All modifiers cranked up |

---

## Wave Modifiers

Random modifiers can appear on non-boss waves, adding variety and challenge:

| Modifier | Effect |
|----------|--------|
| 💨 Fast | Enemies move 2× faster |
| 🛡️ Armored | Enemies have 50% more HP |
| 💚 Regen | Enemies slowly regenerate HP |
| 🐜 Swarm | Double enemies, half HP each |
| 👻 Invisible | Tower range reduced 30% |
| 💰 Rich | Enemies drop 2× gold |
| 📨 Boss Rally | All enemies move faster |

---

## Advanced Mechanics

### Critical Hits
Towers have a **12% chance** to deal **2× damage**. Critical hits display a red **CRIT!** text and burst of red particles.

### Gold Interest
Between waves, you earn **5% interest** on your current gold. A floating text shows the bonus: "+Xg interest!"

### Day/Night Cycle
The sky toggles between day and night every 8 waves:
- 🌙 **Night** — Dark sky with stars & moon, darkened terrain. Enemies gain **+20% HP**. Towers lose **10% range**. A HUD indicator warns you.
- ☀️ **Day** — Normal bright sky with sun & clouds. Standard stats.

### Tower Synergy
Place same-type towers near each other for a stacking damage bonus:
- Each nearby same-type tower within range grants **+10% damage**
- Maximum 3 stacks (**+30%** total)
- Synergized towers display a colored glow ring matching their type

### Elite Enemies
Every 5th non-boss wave (starting wave 5) spawns one elite enemy:
- **3× HP**, **1.5× damage**, **2× gold reward**
- Visually marked with a **golden crown** and **glow ring**

---

## Meta-Progression

### 💎 Diamonds

Earned from boss kills, rare enemy drops, elite enemies, and campaign completion. Diamonds persist across runs and are spent in the Skill Tree.

### Skill Tree — 16 Skills

**Page 1 — Base Skills:**

| Skill | Effect per Level | Max |
|-------|-----------------|-----|
| 💰 Golden Start | +15 starting gold | 5 |
| 🏰 Fortified Base | +20 base HP | 5 |
| ⚔️ Sharp Blade | +3 attack damage | 5 |
| 👟 Swift Feet | +20 move speed | 5 |
| ❤️ Tough Skin | +15 player HP | 5 |
| 🏹 Tower Mastery | +8% tower damage | 5 |
| 💎 Treasure Hunter | +10% gold from kills | 5 |
| 🌟 Diamond Magnet | +5% diamond drop chance | 3 |
| ⭐ War Veteran | +3 gold per wave | 5 |
| 👁️ Eagle Eye | +15 attack range | 4 |

**Page 2 — Prestige Skills** (unlocked after first prestige):

| Skill | Effect per Level | Max |
|-------|-----------------|-----|
| ❄️ Frost Mastery | +10% ice tower slow | 5 |
| ✨ Quick Cast | -5% ability cooldown | 5 |
| 💸 Haggler | +10% tower sell value | 5 |
| 🛡️ Armor Break | +5% resistance pierce | 5 |
| 🍀 Lucky Waves | Better wave modifier chances | 3 |
| 👑 Midas Touch | +5% gold per prestige | 5 |

### 🏆 Achievements — 37 Total

**Combat & Waves:**
🗡️ First Blood · 🛡️ Survivor (Wave 5) · ⚔️ Veteran (Wave 10) · 👑 Legend (Wave 20) · 🏆 Immortal (Wave 30) · 🌟 Mythic (Wave 50) · ⚔️ Centurion (Wave 100) · 💨 Speed Demon (Wave 10 at 3× speed)

**Kills:**
💀 Slayer (50) · 🔥 Destroyer (200) · 💥 Annihilator (500) · 👻 Genocide (1000)

**Bosses:**
☠️ Boss Slayer (first boss) · 🐉 Boss Hunter (5 bosses) · 🐲 Boss Legend (10 bosses)

**Combos:**
🔗 Combo King (10×) · ⛓️ Combo God (20×)

**Building:**
🏗️ Architect (5 towers) · 🏰 Fortress (10 towers) · 🎯 Arsenal (all types) · ⬆️ Master Builder (Lv5 tower)

**Powers & Upgrades:**
✨ Sorcerer (use a power) · 🌊 Elementalist (all 4 powers) · 🏆 Well Rounded (all upgrades)

**Economy:**
💰 Rich (500g) · 🤑 Millionaire (1000g) · 🏦 Gold Hoarder (2000g) · 💎 Diamond Hoarder (10/run) · 💎 Diamond Mine (50/run)

**Survival:**
🛡️ Untouchable (wave without base damage) · ❤️ Last Stand (win wave at 1 HP base) · 🛠️ Mechanic (repair 3×) · ♾️ Endurance (Endless wave 10)

### 📊 Statistics

Lifetime tracking of: total kills, bosses killed, towers placed, gold earned, diamonds earned, highest combo, highest wave, highest score, total runs, powers used, and more.

---

## Architecture

```
app/src/main/java/com/example/myapp/
├── MainActivity.kt            # Game activity — HUD buttons, tower/power/upgrade/dash controls
├── MainMenuActivity.kt        # Main menu — difficulty, play, settings, skill tree, stats
├── SettingsActivity.kt         # Audio & display settings
├── SkillTreeActivity.kt        # Diamond-based persistent skill upgrades
├── StatsActivity.kt            # Lifetime statistics viewer
├── AchievementsActivity.kt     # Achievement gallery
├── CampaignActivity.kt         # Campaign level select
├── ImmersiveActivity.kt        # Base activity with immersive fullscreen mode
├── SoundManager.kt             # Centralized audio settings singleton
└── game/
    ├── GameEngine.kt           # Core game logic (~1840 lines)
    ├── GameView.kt             # SurfaceView renderer & touch input (~1430 lines)
    ├── EntityRenderer.kt       # Procedural Canvas drawing (~1100 lines)
    ├── Enemy.kt                # Enemy data, EnemyType (16), BossType (10) enums
    ├── Player.kt               # Player movement & attack
    ├── Tower.kt                # Tower entity, TowerType (6), targeting modes
    ├── Projectile.kt           # Projectile entity
    ├── CampaignLevel.kt        # Campaign level definitions (15 levels)
    └── SkillTree.kt            # Persistent skill & diamond system
```

### Key Design Decisions

- **Zero external assets** — All visuals are procedurally drawn with Canvas paths, circles, arcs, and geometric shapes. Each of the 16 enemy types, 10 bosses, 6 towers, and the player have unique hand-drawn looks.
- **SurfaceView game loop** — Dedicated render thread at ~60fps with `synchronized` locks for thread safety.
- **Waypoint path system** — 3 winding paths with randomized jitter per run. Enemies follow assigned paths with smooth waypoint interpolation.
- **Animated river** — Flows across the map with sine-wave animation and bridges at path crossings.
- **SharedPreferences persistence** — High scores, achievements, skill levels, diamonds, campaign progress, statistics, and save games all persist locally.
- **Immersive mode** — All activities extend `ImmersiveActivity` for edge-to-edge fullscreen.

---

## Building

### Requirements

- Android Studio (latest stable)
- JDK 17+
- Android SDK 35 (compileSdk)
- Min SDK 26 (Android 8.0)

### Build & Run

```bash
# Clone
git clone https://github.com/rogal01/tower-defense-android.git
cd tower-defense-android

# Build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or open in Android Studio and hit **Run ▶️**.

### Dependencies

- `androidx.core:core-ktx:1.15.0`
- `androidx.appcompat:appcompat:1.7.0`
- `com.google.android.material:material:1.12.0`
- `androidx.constraintlayout:constraintlayout:2.2.0`
- `androidx.activity:activity-ktx:1.9.3`

No game engine, image loader, or third-party game libraries.

---

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 2.1.0 |
| **Build** | Gradle 8.13, AGP 8.13.2 |
| **UI** | Android Canvas 2D, ViewBinding |
| **Rendering** | Custom SurfaceView @ ~60fps |
| **Persistence** | SharedPreferences |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 |

---

## License

MIT
