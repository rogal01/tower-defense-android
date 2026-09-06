---
name: Tower Defense Android
description: Tactical dark-fantasy roguelite tower defense game
colors:
  primary: #FFD700
  primary-dark: #E65100
  secondary: #00E5FF
  neutral-bg: #1A1A2E
  neutral-surface: #0F2035
  neutral-header: #16213E
  accent-emerald: #00C853
  accent-crimson: #D50000
  accent-purple: #BB86FC
  text-primary: #FFFFFF
  text-secondary: #B0BEC5
  text-muted: #607D8B
typography:
  display:
    fontFamily: sans-serif
    fontSize: 32sp
    fontWeight: 700
    lineHeight: 1.2
  headline:
    fontFamily: sans-serif
    fontSize: 22sp
    fontWeight: 700
    lineHeight: 1.25
  title:
    fontFamily: sans-serif
    fontSize: 16sp
    fontWeight: 700
    lineHeight: 1.3
  body:
    fontFamily: sans-serif
    fontSize: 14sp
    fontWeight: 400
    lineHeight: 1.4
  label:
    fontFamily: sans-serif
    fontSize: 12sp
    fontWeight: 700
    letterSpacing: 0.05em
rounded:
  xs: 4dp
  sm: 8dp
  md: 12dp
  lg: 16dp
  pill: 999dp
spacing:
  xs: 4dp
  sm: 8dp
  md: 12dp
  lg: 16dp
  xl: 24dp
components:
  button-primary:
    backgroundColor: {colors.primary}
    textColor: #101820
    rounded: {rounded.sm}
    padding: 12dp 16dp
  button-emerald:
    backgroundColor: {colors.accent-emerald}
    textColor: {colors.text-primary}
    rounded: {rounded.sm}
    padding: 12dp 16dp
  button-danger:
    backgroundColor: {colors.accent-crimson}
    textColor: {colors.text-primary}
    rounded: {rounded.sm}
    padding: 8dp 16dp
  card-glass:
    backgroundColor: {colors.neutral-surface}
    rounded: {rounded.md}
    padding: 14dp
  chip-hud:
    backgroundColor: #D90F1A26
    rounded: {rounded.sm}
    padding: 6dp 10dp
---

# Design System: Tower Defense Android

## Overview

**Creative North Star: The Neon Citadel**

Tower Defense Android combines the precision of high-stakes tactical grid strategy with the electrifying energy of dark fantasy cyberpunk-sorcery. The user experience is atmospheric yet hyper-legible: shadowy, obsidian battlefield depths set the stage for luminescent cyan spell trails, molten gold currency rewards, and searing crimson threat vectors.

Surfaces exist in two primary modes: the *Combat Canvas* (uncompromising real-time clarity, high-contrast path edge definition, crisp range geometries, and zero-latency feedback) and the *Tactical Overlays & Metagame* (translucent glassmorphic slate cards, glowing status chips, and tactile high-affordance control buttons).

**Key Characteristics:**
- **Obsidian Depths & Radiant Highlights:** Deep midnight slate backgrounds (#1A1A2E / #0F2035) pierced by electric cyan (#00E5FF) and sovereign gold (#FFD700).
- **Tactile Ergonomics:** Every interactive control meets or exceeds Android's 48dp touch target threshold with immediate visual ripple feedback.
- **Informative Elemental Language:** Immediate color association for statuses (Ice = Cyan, Fire = Flame Orange, Poison = Toxic Lime, Lightning = Radiant Electric Purple).
- **Glanceable Status Density:** Translucent HUD chips that display vital metrics without obstructing active gameplay.

## Colors

The palette balances dark fantasy immersion with high-contrast legibility under any ambient lighting condition.

### Primary
- **Sovereign Gold** (#FFD700): Reserved for currency, milestone titles, victory stars, and paramount campaign achievements. Communicates triumph and value.
- **Flame Amber** (#FF9800): Used for active tab selections, primary call-to-action cards, and fire element synergies.

### Secondary
- **Electric Cyan** (#00E5FF): Used for diamond metaprogression currency, ice synergies, range indicators, and unclaimed bounty borders.

### Tertiary
- **Prestige Purple** (#BB86FC): Used for permanent synergistic masteries, boss-rush badges, and void merchant banners.

### Neutral
- **Night Slate Base** (#1A1A2E): Root window background, deep combat canvas void.
- **Midnight Navy Glass** (#0F2035 / #EE141F30): Translucent surface cards, dialog backdrops, and modal overlays.
- **Steel Header** (#16213E): Top status app bars and persistent containers.
- **Text Primary** (#FFFFFF): Core titles, numerical metrics, and crisp button labels.
- **Text Secondary** (#B0BEC5): Descriptive tooltips, body copy, and secondary specifications.
- **Text Muted** (#607D8B): Locked states, disabled actions, and subtle dividers.

### Named Rules
**The Rarity of Gold Rule.** Sovereign Gold (#FFD700) is reserved for currency, top achievements, and primary highlights. It never coats general background surfaces.
**The Elemental Fidelity Rule.** An elemental hue is strictly bound to its mechanic (Ice is always Cyan #00E5FF, Fire is always Amber #FF9800, Poison is always Emerald/Lime #00C853, Lightning is always Purple/Electric #BB86FC).

## Typography

**Display Font:** System Sans-Serif Bold (Roboto / Android System Default)
**Body Font:** System Sans-Serif Regular / Medium
**Label/Mono Font:** System Sans-Serif Bold with Tabular Numbers

**Character:** Bold, heroic, punchy, and instantly readable during high-speed combat.

### Hierarchy
- **Display** (Bold, 32sp, line-height 1.2): Main menu hero titles and victory banners.
- **Headline** (Bold, 22sp, line-height 1.25): Modal titles (Merchant Shop, Milestone Blessing, Battle Results).
- **Title** (Bold, 16sp, line-height 1.3): Card headers, achievement names, tower names.
- **Body** (Regular, 14sp, line-height 1.4): Explanatory mechanics, pact descriptions, lore.
- **Label** (Bold, 12sp, letter-spacing 0.05em): Tier badges, button actions, HUD chip values.

### Named Rules
**The Strict SP Rule.** All typography units in XML and programmatic views must use sp units to honor Android accessibility font scaling.

## Layout

- **Combat Canvas:** Expands edge-to-edge (match_parent), maintaining responsive aspect scaling whether running on 18:9, 19.5:9, 20:9 phones, or 16:10 tablets.
- **Status HUD:** Pinned to top edge with translucent background (#D90E1824) and 6dp elevation, packing essential metrics into balanced 34dp–48dp chips.
- **Controls & Specializations:** Docked comfortably in bottom third, easily accessible by thumb during handheld play.
- **Dialog Scaffolding:** Centered ScrollView containers with 16dp–20dp internal padding, ensuring all options remain fully scrollable and unclamped on small displays.

## Elevation & Depth

Depth is established through a hybrid of translucent dark slate tonal layering and soft ambient drop shadows:
- **Level 0 (Battleground):** Solid #1A1A2E canvas with rendered biome paths.
- **Level 1 (In-Game HUD):** #D90E1824 translucent strip with 6dp elevation.
- **Level 2 (Inspectors & Action Docks):** #EE141F30 glassmorphic cards with 2E4A62 subtle borders and 4dp elevation.
- **Level 3 (Modal Dialogs):** Centered floating glass dialogs with deep scrim backdrop (#AA000000) and glowing cyan or gold accent borders.

### Named Rules
**The Scrim Anchor Rule.** Every interrupting modal must render over a dark scrim to instantly focus player attention on critical decisions.

## Shapes

- **HUD Chips & Action Buttons:** 8dp corner radius (g_hud_chip, g_btn_green, g_btn_primary).
- **Cards & Dialog Shells:** 12dp–16dp corner radius (g_card_glass, g_card_build).
- **Power Buttons:** Perfect circles (g_circle_power).
- **Language / Tier Badges:** Pill shapes with 999dp corner radius (g_pill_lang).

## Components

### Buttons
- **Shape:** 8dp corner radius.
- **Touch Target:** Minimum 48dp height and 48dp width.
- **Primary Action (Emerald):** #00C853 with dark green pressed state (#1B5E20). Used for Confirm, Purchase, Next Wave, Select Blessing.
- **Secondary Action (Gold/Amber):** #FF9800 to #FFD700 gradient with deep amber pressed state. Used for Hero abilities and shop purchases.
- **Danger Action (Crimson):** #D50000 with dark red pressed state (#B71C1C). Used for Abandon Run, Pause, and Cancel Placement.
- **Action Neutral:** Slate glass with #3B4E68 border for secondary dismissals and toggles.

### Cards & Modals
- **Merchant Item Card:** Deep slate #D90F1A26 with category tier badge (Synergy, Pact, Booster) at top, descriptive iconography, detailed effect, and high-contrast purchase button.
- **Achievement Row:** 10dp radius slate container with glowing cyan border when claimable, gold title when unlocked, and 48dp claim button.
- **HUD Chip:** Translucent dark container (#D90F1A26) with 1px stroke (#33455A64) holding an emoji and high-contrast numerical readout.

## Do's and Don'ts

### Do:
- **Do** maintain a minimum 48x48 dp touch target for all interactive elements.
- **Do** provide instant visual feedback (ripples, color shifts, haptics) on button touches.
- **Do** format numbers with clear symbols (💰 Gold, 💎 Diamonds, ⚔️ Waves).
- **Do** use semantic colors for game mechanics consistently across all activities.

### Don't:
- **Don't** use tiny sub-40dp buttons that frustrate players with fat-finger misses.
- **Don't** hide text or stats behind system navigation or status bars.
- **Don't** invent random neon colors that dilute the core dark-fantasy palette.
- **Don't** allocate objects inside the GameView.onDraw() loop to prevent garbage collection stutters.
