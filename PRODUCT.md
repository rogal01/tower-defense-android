# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Mobile gamers and tactical defense fans who enjoy:
- Strategic, real-time tower placement and tactical positioning.
- Roguelike draft decision-making with high-stakes synergies, elemental combinations, and short-term boosters.
- Metaprogression via persistent Diamond vaults, skill trees, and rewarding achievement challenges.
- Fast, low-friction mobile sessions (2-minute quick runs to 25-minute endless defense).

## Product Purpose

Deliver an intensely satisfying, high-framerate dark-fantasy tower defense experience on Android that breaks away from static formulaic TD games through live elemental synergies (Thermal Shock, Neurotoxin Chain, Soul Conduit), dynamic 3-wave cadence draft shops (Wandering Merchant), milestone blessings, and an active hero defending the realm.

## Positioning

Unlike typical static mobile tower defense clones that rely on pay-to-win walls or rigid upgrade ladders, Tower Defense Android combines arcade-action hero micro-positioning with deep roguelike drafting:
1. **Dynamic Draft Cadence**: A wandering void merchant appears every 3 waves offering permanent elemental synergies, high-stakes pacts (massive power at severe cost), and short-term wave boosters.
2. **Elemental Reaction System**: Combinations between Fire, Ice, Poison, and Lightning create emergent battlefield reactions (e.g. shatter burst, toxic vapors, chain lightning).
3. **Pure Tactical Gameplay**: Fully offline, zero microtransaction barriers, fair skill progression, and responsive hardware-accelerated Canvas rendering.

## Operating Context

- Mobile handheld devices (phones and tablets) in portrait and landscape orientations.
- Quick on-the-go sessions with instant pause and auto-resume capabilities.
- Direct finger-touch ergonomics with clear visual range indicators and responsive haptic feedback on spells, impacts, and wave banners.

## Capabilities and Constraints

- **Towers**: 10 distinct towers (Arrow, Cannon, Magic, Ice, Lightning, Poison, Sniper, Flame, Barrage, Tesla) with multi-tier upgrades and specialization forks.
- **Enemies & Bosses**: Diverse enemy archetypes with distinct elemental resistances, shields, speeds, and fearsome wave bosses with telegraphed special abilities.
- **Roguelike Mechanics**:
  - Wandering Merchant shop every 3 waves (draft synergies, pacts, temporary boosters).
  - Wave 10/20/30 milestone blessings.
  - Dynamic weather biomes altering projectile speeds, elemental durations, and movement.
- **Metaprogression**: Skill tree upgrades funded by Diamonds earned through battle feats and 32 achievements.
- **Architecture**: Kotlin Multiplatform shared game engine with Android native ViewBinding and custom SurfaceView Canvas rendering.
- **Constraints**: Minimum Android 7.0+ (API 24+), 60/120 FPS target, no network dependency for core gameplay.

## Brand Commitments

- **Aesthetic**: Dark fantasy tactical cyber-magic (The Neon Citadel).
- **Palette Identity**: Night slate (#1A1A2E), Deep Navy (#0F2035), Neon Cyan (#00E5FF), Radiant Gold (#FFD700), Emerald (#00C853), Crimson Danger (#D50000).
- **Tone**: Heroic, punchy, clear, and rewarding.

## Evidence on Hand

- Production Kotlin codebase with passing test suite (shared & pp unit tests).
- Custom 2D hardware-accelerated Canvas renderer in GameView.kt.
- Comprehensive bilingual localization (English & Polish) in GameStrings.kt and strings.xml.
- Automated GitHub Actions APK deployment pipeline targeting -latest release.

## Product Principles

1. **Immediate Tactical Clarity**: The player must be able to read incoming creep paths, tower ranges, and elemental statuses in a fraction of a second.
2. **High-Stakes Deliberation**: Upgrades and shop drafts are never brainless stat bumps; pacts demand trade-offs, and synergies reward deliberate cross-tower planning.
3. **Tactile & Responsive Feedback**: Every tap, spell cast, and tower upgrade triggers clear audio, visual, and haptic feedback.
4. **Frugal & Clean Performance**: Zero background battery drain, rapid cold start, zero garbage-collection stutter in the render loop.

## Accessibility & Inclusion

- Complete localization in English and Polish with culturally aligned phrasing.
- Strict minimum 48dp touch targets across all interactive buttons, dialogs, and controls.
- High-contrast visual distinctions for terrain, projectile trails, and status rings.
- Dynamic TalkBack labels on all icon-only buttons (contentDescription).
