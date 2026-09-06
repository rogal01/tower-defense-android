# Task Plan: Major Expansion (Skill Tree Mastery & Diamond Economy, Campaign World Map & Bosses, Audio/Music & Visual Juice)

## Goal
Deliver a comprehensive gameplay, progression, and presentation expansion:
1. Deepen the Skill Tree & Diamond Economy: Add an Elemental Alchemy mastery tree (4 skills), Citadel Fortification tree (3 skills), expand Relic Vault to 12 Legendary Relics, add Pre-Run Diamond Blessings/Runes, and amplify in-game Diamond rewards (Flawless Boss bonuses, Fusion bounties, and Midas conversions).
2. Campaign Chapter Boss Encounters & World Map (Pillar 4): Create multi-phase Climax Boss encounters for the 7 Acts with custom telegraphs, regional elemental perks, and an interactive Act Navigation Hub in CampaignActivity.
3. Music, Audio & Visual Juice (Pillar 5): Implement procedural dual-track BGM (Battle & Menu), synthesized Fusion SFX, and zero-allocation expanding shockwave ring VFX in GameView.

## Current Phase
Phase 2: Skill Tree Mastery & Diamond Economy (In Progress)

## Phases

### Phase 1: Requirements & Discovery (Complete)
- [x] Analyze Relic, Merchant, and SkillTree systems
- [x] Analyze Diamond economy, drops, and spending sinks
- [x] Analyze Campaign levels, Acts, and Boss telegraphs
- [x] Analyze Audio/SoundManager and GameView VFX capabilities
- [x] Update task_plan.md, findings.md, progress.md, and implementation_plan.md
- **Status:** complete

### Phase 2: Skill Tree Mastery & Diamond Economy (Complete)
- [x] Add 7 new diamond skills to SkillTree.kt (Elemental Alchemy: fusion_potency, catalyst_radius, conduit_resonance, status_duration; Fortifications: citadel_barrier, trap_overhaul, hero_critical)
- [x] Expand Relic Vault from 6 to 12 Legendary Relics in Relic.kt (Prismatic Catalyst, Grimoire of Conduit, Astral Harvester, Titan Warhorn, Chrono Singularity, Philosopher Stone)
- [x] Implement Pre-Run Diamond Blessings (Blessing of Midas, Blessing of Catalyst, High Roller Pact)
- [x] Add Flawless Boss wave diamond bonus & Fusion bounties in GameEngine.kt
- [x] Upgrade SkillTreeActivity.kt with 3-tab layout (Passives, Elemental Alchemy, Relic Vault)
- [x] Add English and Polish localizations in GameStrings.kt
- **Status:** complete

### Phase 3: Campaign Chapter Boss Encounters & Visual World Map (Complete)
- [x] Assign Act Climax milestone bosses (Levels 10, 20, 30, 40, 50, 65, 80) with unique telegraph abilities and regional elemental perks
- [x] Upgrade CampaignActivity.kt with an Act/Chapter selector bar, visual act banners, climax boss badges, and regional bonus indicators
- [x] Add localized act lore and boss encounter titles in GameStrings.kt
- **Status:** complete

### Phase 4: Music, Audio & Visual Juice (Complete)
- [x] Implement procedural background music (BGM) in SoundManager.kt (Battle & Menu loops) with volume setting adherence
- [x] Add synthesized SFX types for Fusions (FUSION_ELEMENTAL, FUSION_ARCANE, FUSION_DARK, FUSION_SIEGE, DIAMOND_CHEST)
- [x] Add zero-allocation Shockwave Ripple system in GameView.kt with radial expanding rings on major explosions/reactions
- **Status:** complete

### Phase 5: Verification & Walkthrough (Complete)
- [x] Write unit tests in GameEngineSystemTest.kt & SkillTreeSystemTest.kt
- [x] Run ./gradlew testDebugUnitTest across both :shared and :app modules (BUILD SUCCESSFUL)
- [x] Update walkthrough.md, commit, and push to origin/master
- **Status:** complete