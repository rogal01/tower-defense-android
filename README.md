# 🏰 Tower Defense — Android & iOS

A feature-rich tower defense game built with **Kotlin Multiplatform (KMP)**, with native rendering on **Android Canvas** and **iOS SpriteKit**. No external game engines, no sprites — every visual is procedurally drawn with over 1,200 lines of hand-crafted drawing code.

> **6,300+ lines** of shared game logic · **10 tower types** · **3 trap types** · **18 enemy types** · **10 unique bosses** · **8 maps** · **40 campaign levels** · **50 achievements** · **16 persistent skills** · **6 game modes** · **EN/PL localization** · **iOS & Android**

---

## Table of Contents

- [Gameplay](#gameplay)
- [Towers](#towers)
- [Traps](#traps)
- [Enemies & Bosses](#enemies--bosses)
- [Powers & Abilities](#powers--abilities)
- [Game Modes](#game-modes)
- [Maps](#maps)
- [Campaign](#campaign)
- [Wave Modifiers](#wave-modifiers)
- [Advanced Mechanics](#advanced-mechanics)
- [Meta-Progression](#meta-progression)
- [Localization](#localization)
- [Architecture](#architecture)
- [Building](#building)
- [iOS Setup](#ios-setup)
- [Tech Stack](#tech-stack)
- [License](#license)

---

## Gameplay

Defend your base from waves of enemies by placing towers and traps, using special powers, and commanding your player character. Enemies follow winding paths across 8 unique maps toward your base.

### Core Loop

- **Player Character** — Tap/drag to move. Auto-attacks nearest enemy in range. Upgradeable ATK, SPD, HP, and base HP.
- **Tower Placement** — Place 10 tower types on open terrain. Each has unique stats, targeting modes (Close / First / Last / Strong), upgrades up to Lv5, and a special ability.
- **Trap Placement** — Place traps directly on enemy paths for passive area denial.
- **Wave System** — Enemies arrive in escalating waves. Every 5th wave spawns a boss with unique abilities. Wave modifiers add variety.
- **Combo System** — Rapid kills chain into combos (up to 50×+) for bonus gold multipliers.
- **Bounty Board** — 3 randomly-generated objectives per run with gold and diamond rewards.
- **Powers** — 4 active powers with gold costs and cooldowns.
- **Dash** — Teleport toward enemies dealing AoE damage along the path. 8-second cooldown.

---

## Towers

10 tower types, each with unique stats, damage types, targeting modes, 5 upgrade levels, and an activatable special ability.

| Tower | Cost | Damage | Range | Fire Rate | Type | Ability |
|-------|------|--------|-------|-----------|------|---------|
| 🏹 Arrow | 30g | 8 | 200 | 1.2/s | Physical | **Volley** — burst of arrows |
| 🧨 Magic | 60g | 14 | 220 | 0.8/s | Magic | **Arcane Blast** — AoE burst |
| 💣 Cannon | 100g | 30 | 180 | 0.5/s | Explosive | **Napalm** — area fire |
| ☠️ Poison | 80g | 6 | 210 | 1.0/s | Poison | **Plague** — poison spread |
| ⚡ Tesla | 120g | 20 | 250 | 0.7/s | Electric | **Overcharge** — chain lightning |
| ❄️ Ice | 70g | 0 | 230 | — | Ice | **Deep Freeze** — freezes enemies |
| 🔥 Flame | 90g | 12 | 190 | 0.9/s | Fire | **Inferno** — mass burn DoT |
| 💀 Necro | 110g | 18 | 200 | 0.6/s | Dark | **Soul Harvest** — execute low-HP enemies |
| 🎯 Ballista | 140g | 50 | 300 | 0.3/s | Physical | **Siege Shot** — 5× damage to strongest |
| 🌀 Vortex | 100g | 4 | 240 | 1.5/s | Magic | **Singularity** — pulls enemies in + AoE |

### Damage Types

8 damage types interact with enemy resistances:

| Type | Towers | Notes |
|------|--------|-------|
| Physical | Arrow, Ballista | Baseline — most enemies neutral |
| Magic | Magic, Vortex | Effective against armored |
| Explosive | Cannon | AoE splash damage |
| Poison | Poison | Damage-over-time |
| Electric | Tesla | Chain lightning |
| Ice | Ice | Slows enemies |
| Fire | Flame | Burns enemies over time. Demons resist, Shadows vulnerable |
| Dark | Necro | Execute bonus. Shadows immune, Wisps vulnerable |

### Tower Synergy

Same-type towers placed near each other boost damage by **+10% per nearby tower** (max 3 stacks = **+30%**). Synergized towers display a colored glow ring.

---

## Traps

3 trap types placed directly on enemy paths for passive area denial:

| Trap | Cost | Effect |
|------|------|--------|
| 🗡️ Spikes | 30g | Deals damage to enemies walking through |
| 🟤 Tar Pit | 40g | Slows enemies passing through |
| 💣 Landmine | 60g | Explodes on contact dealing AoE damage |

Traps are consumed on use and can be stacked on the same path tile.

---

## Enemies & Bosses

### 18 Enemy Types

Goblins, Skeletons, Orcs, Demons, Dragons, Fast Skeletons, Armored Golems, and specialized boss minions — Shadows, Slimes, Bats, Spiders, Wisps, and Golem Shards. Each type has unique speed, HP, resistances, and visual appearance.

### Elite Enemies

Every 5th non-boss wave (starting from wave 5) spawns one **Elite Enemy** with:
- 3× HP, 1.5× damage, 2× gold reward
- A golden crown and glow ring

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

💨 **Dash** — Teleport up to 200 units toward your move target, dealing AoE damage to all enemies along the dash path. 8-second cooldown.

---

## Game Modes

| Mode | Description |
|------|-------------|
| **Campaign** | 40 hand-crafted levels with progressive mechanics and restrictions. Earn stars based on score. |
| **Endless** | Infinite waves with scaling difficulty. Choose map and difficulty (Easy / Normal / Hard). Separate high-wave record. |
| **Boss Rush** | Boss every 3 waves. How many can you defeat? |
| **Daily Challenge** | Seeded daily run with fixed parameters. Same seed for everyone. |
| **Randomizer** | Everything scrambled — tower costs, power cooldowns, enemy stats, starting gold. |
| **Continue** | Resume a saved game in progress. |

---

## Maps

8 maps with unique path layouts, terrain, and ambient visual effects:

| Map | Paths | Terrain | Special |
|-----|-------|---------|---------|
| 🗺️ Classic | 3 winding | Grass, river, bridges | Standard layout |
| 🏔️ Valley | 2 converging | Mountain terrain | Narrow chokepoints |
| ➕ Crossroads | 4 intersecting | Plains | Crossing paths |
| 🏜️ Desert | 2 winding | Sand dunes | Open terrain |
| ❄️ Snow | 3 branching | Snow, frozen lake | Ice-themed enemies |
| 🌋 Lava | 2 straight | Volcanic rock | Eruptions damage towers periodically |
| 🌿 Enchanted | 3 curving | Magical forest | Sparkle effects |
| 🌋 Volcano | 2 narrow | Active volcano | Periodic fire eruptions |

---

## Campaign

40 levels that progressively introduce game mechanics. Each completed level awards diamonds. Earn ⭐⭐⭐ stars based on your score.

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
| 16 | 🧱 Barricade Basics | 6 | Arrow + Blockade placement |
| 17 | 🌩️ Tesla Lab | 7 | Tesla + Ice only |
| 18 | 🧪 Poison Garden | 8 | Poison + Magic, regen enemies |
| 19 | 💣 Demolition | 8 | Cannon + Cannon, 1.3× HP |
| 20 | 🏰 Fortress | 10 | All 6 towers, limited gold |
| 21 | ⚡ Blitz | 8 | 1.8× speed, 1.2× gold |
| 22 | 🛡️ Armored Assault | 10 | 1.5× HP, armor regen |
| 23 | 🐉 Dragon's Den | 8 | Dragon waves, boss every 4 |
| 24 | 💰 Penny Pincher | 10 | 0.4× gold, 0.8× HP |
| 25 | ☀️ Daylight Siege | 12 | Day-only, 1.3× HP |
| 26 | 🌙 Nightfall | 10 | Always night |
| 27 | 🧛 Undead Rising | 10 | Skeletons + Bats, regen |
| 28 | 🕸️ Spider Nest | 12 | Spiders only, 2× spawn |
| 29 | 🏋️ Titan Trial | 10 | All enemies 2× HP |
| 30 | 🎯 Marksman | 15 | Arrow + Tesla, 1.5× speed |
| 31 | 🔥 Playing with Fire | 8 | Arrow + Flame towers |
| 32 | 💀 Dark Arts | 8 | Arrow + Magic + Necro |
| 33 | 🎯 Siege Warfare | 10 | Ballista + Arrow |
| 34 | 🌀 Event Horizon | 10 | Vortex + Cannon + Ice |
| 35 | 🧊 Fire & Ice | 10 | Flame + Ice only |
| 36 | 💀 Necro Rush | 10 | Necro + Poison + Arrow, 2.5× spawn |
| 37 | 🌀 Gravity Well | 12 | Vortex + Tesla |
| 38 | 🎯 Sniper Alley | 12 | Ballista only |
| 39 | ⚔️ Full Armory | 15 | All 10 tower types |
| 40 | 💀 Absolute Zero | 25 | Hardest level — 1.8× HP, 0.7× gold |

---

## Wave Modifiers

Random modifiers can appear on non-boss waves:

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
Towers have a **12% chance** to deal **2× damage**. Critical hits display a red **CRIT!** text.

### Gold Interest
Between waves, you earn **5% interest** on your current gold.

### Day/Night Cycle
The sky toggles between day and night every 8 waves:
- 🌙 **Night** — Dark sky with stars & moon. Enemies gain **+20% HP**. Towers lose **10% range**.
- ☀️ **Day** — Normal bright sky. Standard stats.

### Kill Streak Border Glow
Rapid kills create a glowing border effect around the screen that intensifies with your combo.

### Path Highlighting
Enemy paths glow when you're placing towers or traps, making it easy to see valid placement locations.

### Ambient Map Animations
Each map has unique ambient visual effects — flowing water, falling snow, drifting sand, lava bubbles, magical sparkles, and volcanic eruptions.

### Bounty Board
Each run generates 3 random bounties (e.g. "Kill 20 Goblins", "Reach Wave 10"). Completing bounties mid-run awards gold and diamonds.

### Run History
A full log of past games with mode, wave reached, score, and date for tracking progress over time.

---

## Meta-Progression

### 💎 Diamonds

Earned from boss kills, elite enemies, bounties, and campaign completion. Diamonds persist across runs and are spent in the Skill Tree.

### Skill Tree — 16 Skills

**Page 1 — Base Skills (10):**

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

**Page 2 — Prestige Skills (6, unlocked after first prestige):**

| Skill | Effect per Level | Max |
|-------|-----------------|-----|
| ❄️ Frost Mastery | +10% ice tower slow | 5 |
| ✨ Quick Cast | -5% ability cooldown | 5 |
| 💸 Haggler | +10% tower sell value | 5 |
| 🛡️ Armor Break | +5% resistance pierce | 5 |
| 🍀 Lucky Waves | Better wave modifier chances | 3 |
| 👑 Midas Touch | +5% gold per prestige | 5 |

### 🏆 Achievements — 50 Total

**Combat & Waves (8):**
🗡️ First Blood · 🌊 Survivor (Wave 5) · ⭐ Veteran (Wave 10) · 🏆 Legend (Wave 20) · 💀 Immortal (Wave 30) · 🔥 Mythic (Wave 50) · 💯 Centurion (Wave 100) · ⚡ Speed Demon (Wave 10 at 3×)

**Kills (4):**
⚔️ Slayer (50) · 💣 Destroyer (200) · ☠️ Annihilator (500) · 💀 Genocide (1000)

**Bosses (4):**
👹 Boss Slayer (first) · 🐉 Boss Hunter (5) · 👑 Boss Legend (10) · 🗡️ Gauntlet (5 in Boss Rush)

**Combos (4):**
🔗 Combo King (10×) · ⛓️ Combo God (20×) · 🔥 Unstoppable (30×) · ⚡ Godlike (50×)

**Building (3):**
🏗️ Architect (5 towers) · 🏰 Fortress (10 towers) · 🎯 Arsenal (all types)

**Powers & Upgrades (3):**
🧙 Sorcerer (use a power) · 🌈 Elementalist (all 4 powers) · 🌟 Well Rounded (all upgrades) · ⬆️ Master Builder (Lv5 tower)

**Economy (5):**
💰 Rich (500g) · 💎 Millionaire (1000g) · 🏦 Gold Hoarder (2000g) · 💎 Diamond Hoarder (10/run) · ⛏️ Diamond Mine (50/run)

**Survival (4):**
🛡️ Untouchable (no base damage) · ❤️‍🔥 Last Stand (1 HP base) · 🔧 Mechanic (repair 3×) · ♾️ Endurance (Endless wave 10)

**Traps (3):**
🪤 Trapper (first trap) · 💣 Minefield (10 traps) · 💥 Triple Threat (3 kills with one mine)

**Bounties (2):**
🎯 Bounty Hunter (first bounty) · 👑 Bounty King (all 3 bounties)

**Maps & Modes (3):**
🌋 Volcanic Victory (Wave 15 on Volcano) · 🐺 Lone Wolf (Wave 5 with no towers) · 🌍 Cartographer (all 8 maps)

**Campaign (5):**
📜 Campaign stars and completion achievements

### 📊 Statistics

Lifetime tracking of: total kills, bosses killed, towers placed, gold earned, diamonds earned, highest combo, highest wave, highest score, total runs, favorite tower, and more.

---

## Localization

Full **English** and **Polish** language support. All UI text, achievements, tutorials, toasts, and menus are localized. Language can be switched in Settings.

---

## Architecture

### Kotlin Multiplatform (KMP)

The game logic lives in a **shared KMP module** compiled for both Android and iOS. Platform-specific code (rendering, preferences, audio) is implemented natively on each platform.

```
shared/src/
├── commonMain/kotlin/com/example/myapp/game/
│   ├── GameEngine.kt           # Core game logic (~3,070 lines)
│   ├── Enemy.kt                # Enemy data, EnemyType (18), BossType (10)
│   ├── Tower.kt                # Tower entity, TowerType (10), targeting
│   ├── Player.kt               # Player movement & attack
│   ├── Projectile.kt           # Projectile entity
│   ├── SkillTree.kt            # Persistent skill & diamond system
│   ├── CampaignLevel.kt        # Campaign level definitions (40 levels)
│   ├── GamePreferences.kt      # Platform-agnostic preferences interface
│   ├── GameAudio.kt            # Platform-agnostic audio interface
│   ├── GamePoint.kt            # 2D point utility
│   └── GameEngineHolder.kt     # Singleton engine holder
├── androidMain/
│   └── AndroidGamePreferences.kt  # SharedPreferences implementation
└── iosMain/
    └── IosGamePreferences.kt      # UserDefaults implementation

app/src/main/java/com/example/myapp/
├── MainActivity.kt            # Game activity — HUD, tower/power/trap/upgrade controls
├── MainMenuActivity.kt        # Main menu — modes, settings, skill tree, stats
├── SettingsActivity.kt        # Audio, display, language settings
├── SkillTreeActivity.kt       # Diamond-based persistent skill upgrades
├── StatsActivity.kt           # Lifetime statistics viewer
├── AchievementsActivity.kt    # Achievement gallery (50 achievements)
├── CampaignActivity.kt        # Campaign level select with stars
├── RunHistoryActivity.kt      # Past game run history log
├── HelpActivity.kt            # In-game help & mechanics reference
├── TutorialDialog.kt          # First-run tutorial dialog
├── GameStrings.kt             # EN/PL localization strings
├── LocaleHelper.kt            # Language selection helper
├── AndroidGameAudio.kt        # Android audio implementation
├── SoundManager.kt            # Audio settings singleton
├── ImmersiveActivity.kt       # Fullscreen base activity
└── game/
    ├── GameEngine.kt           # Android-specific engine shim
    ├── GameView.kt             # SurfaceView renderer (~2,100 lines)
    └── EntityRenderer.kt       # Procedural Canvas drawing (~1,200 lines)

ios/app/
├── TowerDefenseApp.swift       # iOS app entry point
├── GameContainerView.swift     # SwiftUI HUD — all towers, traps, powers, upgrades
├── GameViewModel.swift         # ViewModel bridging shared engine to SwiftUI
└── GameSpriteView.swift        # SpriteKit rendering — paths, towers, enemies, base
```

### Key Design Decisions

- **Kotlin Multiplatform** — Shared game logic compiled for both Android and iOS from a single Kotlin codebase.
- **Zero external assets** — All visuals are procedurally drawn. Android uses Canvas paths, circles, arcs, and shapes. iOS uses SpriteKit with emoji rendering.
- **SurfaceView game loop (Android)** — Dedicated render thread at ~60fps with `synchronized` locks for thread safety.
- **SpriteKit game loop (iOS)** — Native SKScene with layered rendering (terrain, paths, traps, towers, enemies, effects).
- **Waypoint path system** — Up to 4 winding paths per map with randomized jitter. Enemies follow assigned paths with smooth waypoint interpolation.
- **SharedPreferences / UserDefaults** — High scores, achievements, skill levels, diamonds, campaign progress, bounties, run history, and settings all persist locally.
- **Full localization** — All strings routed through `GameStrings.kt` with EN/PL support.

---

## Building

### Android

#### Requirements

- Android Studio (latest stable)
- JDK 17+
- Android SDK 35 (compileSdk)
- Min SDK 26 (Android 8.0)

#### Build & Run

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

## iOS Setup

The iOS app lives in `ios/` and uses the shared KMP module for game logic.

#### Requirements

- Xcode 15+
- macOS Sonoma or later
- CocoaPods or SPM for framework linking

#### Setup

1. Build the shared framework: `./gradlew :shared:linkDebugFrameworkIosArm64`
2. Open `ios/TowerDefense.xcodeproj` in Xcode
3. Link the shared framework from `shared/build/bin/iosArm64/debugFramework/`
4. Build & run on simulator or device

See [ios/IOS-SETUP-GUIDE.md](ios/IOS-SETUP-GUIDE.md) for detailed setup instructions.

---

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin 2.1.0, Swift 5.9 |
| **Architecture** | Kotlin Multiplatform (KMP) |
| **Build** | Gradle 8.13, AGP 8.13.2, Xcode 15 |
| **Android UI** | Android Canvas 2D, SurfaceView @ ~60fps |
| **iOS UI** | SwiftUI + SpriteKit |
| **Persistence** | SharedPreferences (Android), UserDefaults (iOS) |
| **Localization** | English, Polish |
| **Min SDK** | Android 26 (8.0), iOS 16 |
| **Target SDK** | Android 35 |

---

## License

MIT — see [LICENSE](LICENSE) for details.
