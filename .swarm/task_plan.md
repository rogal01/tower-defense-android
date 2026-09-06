# Task Plan - Android Game Experience Overhaul

## Phase 1: Design System & Drawable Assets
- [ ] Create rounded tactile button drawables with ripple feedback (g_btn_primary.xml, g_btn_secondary.xml, g_btn_action.xml, g_btn_danger.xml, g_btn_card.xml, g_hud_chip.xml).
- [ ] Define consistent color tokens and typography styles in colors.xml and 	hemes.xml.

## Phase 2: In-Game HUD & Contextual Bottom Controls
- [ ] Overhaul ctivity_main.xml:
  - Streamline Top HUD with compact badges (Gold, Wave, Kills) and unified Speed/Auto/Pause cluster.
  - Replace 16-button scroll with a clean Categorized Dock ([?? Towers] [??? Traps] [? Hero]).
  - Add dedicated **Selected Tower Inspector Card** ([Upgrade] [Target] [Ability] [Sell] [? Close]).
  - Add **Active Placement Bar** with clear item details and [? Cancel Placement] button.
  - Modernize floating power buttons (Fireball, Freeze, Lightning, Dash) with circular badges and clean cooldown text.
- [ ] Update MainActivity.kt:
  - Wire up category switching and contextual inspector transitions.
  - Implement 1-tap speed cycling (1x -> 2x -> 3x -> 1x).
  - Add placement cancellation logic and dynamic affordability state updates.

## Phase 3: Touch & Gameplay Ergonomics (GameView.kt)
- [ ] Add explicit placement cancellation and tap handling.
- [ ] Enhance tower placement preview with green/red placement validity ring.
- [ ] Enhance selected tower canvas highlight (pulsing selection ring + clean range circle).
- [ ] Add tactile haptic feedback on button clicks and tower actions.
- [ ] Disambiguate hero movement vs tower selection to eliminate misclicks.

## Phase 4: Main Menu & Navigation Polish
- [ ] Modernize ctivity_menu.xml with hero card layout, prominent play modes, and organized utility cards.
- [ ] Polish MainMenuActivity.kt language toggle and stats header chips.

## Phase 5: Verification & Testing
- [ ] Run ./gradlew compileDebugKotlin and verify clean build with zero warnings.
- [ ] Run unit tests ./gradlew testDebugUnitTest.
- [ ] Build debug APK ./gradlew assembleDebug and verify APK output.
