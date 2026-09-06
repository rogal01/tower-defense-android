# Codebase & Architecture Findings

## 1. Build & Runtime Environment
- Gradle 9.3.1 with Android Gradle Plugin 9.1.0 and Kotlin 2.2.10.
- Android SDK located at C:/Users/oliwi/AppData/Local/Android/Sdk (configured in local.properties).
- Compiles with Java 17 and Android SDK 35.

## 2. In-Game UI Issues (activity_main.xml & MainActivity.kt)
- **Extreme Bottom Row Clutter**: Two giant HorizontalScrollViews with 16 build buttons and 8 upgrade/tower buttons.
- **Missing Contextual Inspector**: When a tower is tapped, the upgrade/target/sell/ability buttons stay hidden in a horizontal scroll list rather than presenting a dedicated tower inspector panel.
- **Placement Trap**: Once a build button is tapped, there is no way to cancel placement without finding an invalid spot or placing unwantedly.
- **Button Styling**: Buttons use raw hex backgroundTint without rounded corners, ripples, or tactile states.
- **Top HUD Squish**: 8 separate views jammed into one horizontal bar causing cutoffs on smaller phone screens. Speed is split into two buttons (2x, 3x).

## 3. Visual & Styling Deficiencies
- Only 1 drawable in es/drawable/ (ic_launcher_foreground.xml). No custom shape drawables, card backgrounds, or button selector states.
- Emojis are hardcoded and text is 10-11sp, hard to read during intense waves.
- Main Menu is a plain vertical stack of standard buttons with minimal visual hierarchy.

## 4. Gameplay Ergonomics (GameView.kt)
- Moving the player vs selecting a tower can conflict because tapping near a tower can accidentally move the hero or select the tower.
- Tower placement preview lacks clear green/red validity feedback and an obvious cancel button.
- Tower selection needs clear animated pulse/range highlight on Canvas.
