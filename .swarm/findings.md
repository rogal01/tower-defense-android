# Findings & Discoveries

## Skill Tree & Diamond Economy
- Currently, SkillTree.kt has 10 basic skills and 6 prestige skills, plus 6 relics in the Relic Vault.
- Diamonds were primarily spent on basic stat increments (+15 starting gold, +3 damage), which felt underwhelming relative to the rare feeling of diamonds.
- Players need high-impact meta-progression:
  1. **Elemental Alchemy Branch**: Boosts fusions (damage, AoE radius, conduit resonance, status duration).
  2. **Citadel & Combat Branch**: Base energy barrier shield, trap overhaul (+uses, +damage), hero critical strikes.
  3. **Relic Vault Expansion (6 -> 12 Relics)**: Truly legendary effects like Prismatic Catalyst, Chrono Singularity, Titan Warhorn, Astral Harvester, Grimoire of Conduit, Philosopher's Stone.
  4. **Pre-Run Diamond Blessings**: Spend diamonds before a run for run-defining bonuses (Midas King, Catalyst Blessing, High Roller 3x Diamond Bounty).
  5. **In-game Diamond Excitement**: Flawless boss clears (+2 💎), fusion milestones (+5 💎), and gold-to-diamond conversion.

## Campaign & Boss Mechanics
- 80 levels across 7 Acts in CampaignLevel.kt.
- Act Climax levels (10, 20, 30, 40, 50, 65, 80) can have designated signature bosses, custom telegraph moves, and regional elemental modifiers.
- CampaignActivity.kt will have an Act tab strip and climax boss badges.

## Audio & Visual Juice
- SoundManager.kt can generate procedural chiptune battle & menu music via AudioTrack, looping seamlessly without external assets.
- GameView.kt will maintain zero GC allocations using a preallocated ring buffer of ShockwaveRing instances for reaction impact ripples.