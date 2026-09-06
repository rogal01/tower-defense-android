package com.example.myapp.game

import kotlin.test.*

class SkillTreeSystemTest {

    private lateinit var prefs: FakeGamePreferences
    private lateinit var skillTree: SkillTree

    @BeforeTest
    fun setUp() {
        prefs = FakeGamePreferences()
        skillTree = SkillTree(prefs)
    }

    @Test
    fun testInitialCurrencyAndSkillLevels() {
        assertEquals(0, skillTree.diamonds)
        assertEquals(0, skillTree.prestigeLevel)
        for (skill in skillTree.skills) {
            assertEquals(0, skillTree.getLevel(skill.id))
        }
    }

    @Test
    fun testAddDiamondsAndUpgradeSkill() {
        skillTree.addDiamonds(50)
        assertEquals(50, skillTree.diamonds)

        // Upgrade Golden Start (baseCost: 3)
        val canUp = skillTree.canUpgrade("start_gold")
        assertTrue(canUp)

        val success = skillTree.upgrade("start_gold")
        assertTrue(success)
        assertEquals(1, skillTree.getLevel("start_gold"))
        assertEquals(47, skillTree.diamonds)
        assertEquals(15, skillTree.bonusStartGold())

        // Upgrade again (cost: 3 + 1 * 2 = 5)
        skillTree.upgrade("start_gold")
        assertEquals(2, skillTree.getLevel("start_gold"))
        assertEquals(42, skillTree.diamonds)
        assertEquals(30, skillTree.bonusStartGold())
    }

    @Test
    fun testSkillMaxLevelCap() {
        skillTree.addDiamonds(1000)
        val skill = skillTree.skills.first { it.id == "start_gold" }

        for (i in 0 until skill.maxLevel) {
            assertTrue(skillTree.upgrade(skill.id))
        }

        assertEquals(skill.maxLevel, skillTree.getLevel(skill.id))
        assertFalse(skillTree.canUpgrade(skill.id), "Cannot upgrade skill past max level")
        assertFalse(skillTree.upgrade(skill.id))
    }

    @Test
    fun testGameplayBonusCalculations() {
        skillTree.addDiamonds(100)
        skillTree.upgrade("player_damage")
        assertEquals(3f, skillTree.bonusPlayerDamage())

        skillTree.upgrade("tower_damage")
        assertEquals(1.08f, skillTree.towerDamageMultiplier(), 0.001f)
    }

    @Test
    fun testRelicUnlockSystemAndPersistence() {
        skillTree.addDiamonds(100)
        assertFalse(skillTree.isRelicUnlocked(RelicId.ZEPHYR_GREAVES))
        assertTrue(skillTree.canUnlockRelic(RelicId.ZEPHYR_GREAVES))

        // Unlock Zephyr Greaves (25 diamonds)
        assertTrue(skillTree.unlockRelic(RelicId.ZEPHYR_GREAVES))
        assertTrue(skillTree.isRelicUnlocked(RelicId.ZEPHYR_GREAVES))
        assertEquals(75, skillTree.diamonds)
        assertEquals(1, skillTree.unlockedRelicsCount())
        assertTrue(prefs.getBoolean("ach_relic_first", false))

        // Cannot unlock again
        assertFalse(skillTree.canUnlockRelic(RelicId.ZEPHYR_GREAVES))
        assertFalse(skillTree.unlockRelic(RelicId.ZEPHYR_GREAVES))

        // Unlock 2 more relics to test relic_3 achievement
        skillTree.unlockRelic(RelicId.ARTEMIS_QUIVER) // 30 diamonds -> 45
        assertFalse(prefs.getBoolean("ach_relic_3", false))
        skillTree.unlockRelic(RelicId.MIDAS_CRUCIBLE) // 20 diamonds -> 25
        assertEquals(3, skillTree.unlockedRelicsCount())
        assertTrue(prefs.getBoolean("ach_relic_3", false))

        // Check Midas starting gold bonus
        assertEquals(100, skillTree.bonusStartGold())

        // Create new SkillTree instance with same prefs to verify persistence
        val restoredTree = SkillTree(prefs)
        assertEquals(3, restoredTree.unlockedRelicsCount())
        assertTrue(restoredTree.isRelicUnlocked(RelicId.ZEPHYR_GREAVES))
        assertTrue(restoredTree.isRelicUnlocked(RelicId.ARTEMIS_QUIVER))
        assertTrue(restoredTree.isRelicUnlocked(RelicId.MIDAS_CRUCIBLE))
    }

    @Test
    fun testAlchemyAndCombatSkillsUpgradeAndEffects() {
        skillTree.addDiamonds(500)

        // Upgrade Alchemy Skills
        assertTrue(skillTree.upgrade("fusion_potency"))
        assertEquals(1, skillTree.getLevel("fusion_potency"))
        assertEquals(1.12f, skillTree.fusionDamageMultiplier(), 0.001f)

        assertTrue(skillTree.upgrade("catalyst_radius"))
        assertEquals(1, skillTree.getLevel("catalyst_radius"))
        assertEquals(1.10f, skillTree.fusionRadiusMultiplier(), 0.001f)

        assertTrue(skillTree.upgrade("conduit_resonance"))
        assertEquals(1, skillTree.getLevel("conduit_resonance"))
        assertEquals(0.06f, skillTree.conduitExtraEcho(), 0.001f)

        assertTrue(skillTree.upgrade("status_duration"))
        assertEquals(1, skillTree.getLevel("status_duration"))
        assertEquals(0.8f, skillTree.statusDurationBonus(), 0.001f)

        // Upgrade Fortification / Combat Skills
        assertTrue(skillTree.upgrade("citadel_barrier"))
        assertEquals(1, skillTree.getLevel("citadel_barrier"))
        assertEquals(30f, skillTree.citadelBarrierHp())

        assertTrue(skillTree.upgrade("trap_overhaul"))
        assertEquals(1, skillTree.getLevel("trap_overhaul"))
        assertEquals(1.20f, skillTree.trapDamageMultiplier(), 0.001f)
        assertEquals(1, skillTree.trapBonusUses())

        assertTrue(skillTree.upgrade("hero_critical"))
        assertEquals(1, skillTree.getLevel("hero_critical"))
        assertEquals(0.05f, skillTree.heroCritChance(), 0.001f)
    }

    @Test
    fun testPreRunDiamondBlessings() {
        assertEquals(DiamondBlessing.NONE, skillTree.getActiveBlessing())
        assertFalse(skillTree.canAffordBlessing(DiamondBlessing.MIDAS))

        skillTree.addDiamonds(25)
        assertTrue(skillTree.canAffordBlessing(DiamondBlessing.MIDAS))
        assertTrue(skillTree.purchaseBlessing(DiamondBlessing.MIDAS))
        assertEquals(DiamondBlessing.MIDAS, skillTree.getActiveBlessing())
        assertEquals(17, skillTree.diamonds) // 25 - 8

        // Clear active blessing
        skillTree.clearActiveBlessing()
        assertEquals(DiamondBlessing.NONE, skillTree.getActiveBlessing())

        // Test Catalyst blessing
        assertTrue(skillTree.purchaseBlessing(DiamondBlessing.CATALYST))
        assertEquals(DiamondBlessing.CATALYST, skillTree.getActiveBlessing())
        assertEquals(7, skillTree.diamonds) // 17 - 10

        // Cannot afford High Roller (cost 15, have 7)
        assertFalse(skillTree.canAffordBlessing(DiamondBlessing.HIGH_ROLLER))
        assertFalse(skillTree.purchaseBlessing(DiamondBlessing.HIGH_ROLLER))
        assertEquals(DiamondBlessing.CATALYST, skillTree.getActiveBlessing())
    }

    @Test
    fun testAllTwelveRelicsUnlockableAndPersistent() {
        skillTree.addDiamonds(1000)
        assertEquals(12, RelicId.entries.size)

        for (relic in RelicId.entries) {
            assertTrue(skillTree.canUnlockRelic(relic), "Should be able to unlock ${relic.title}")
            assertTrue(skillTree.unlockRelic(relic))
            assertTrue(skillTree.isRelicUnlocked(relic))
        }

        assertEquals(12, skillTree.unlockedRelicsCount())

        // Verify relic passive contributions to skill effects
        assertTrue(skillTree.fusionDamageMultiplier() >= 1.40f) // Prismatic Catalyst adds 0.40f
        assertTrue(skillTree.fusionRadiusMultiplier() >= 1.25f) // Prismatic Catalyst adds 0.25f
        assertTrue(skillTree.conduitExtraEcho() >= 0.25f)       // Grimoire of Conduit adds 0.25f
        assertTrue(skillTree.citadelBarrierHp() >= 100f)        // Aegis of Dawn adds 100f
        assertTrue(skillTree.trapBonusUses() >= 2)             // Demolition Satchel adds 2

        // Verify persistence in a new instance
        val restored = SkillTree(prefs)
        assertEquals(12, restored.unlockedRelicsCount())
        for (relic in RelicId.entries) {
            assertTrue(restored.isRelicUnlocked(relic))
        }
    }

    @Test
    fun testPrestigeDamageMultiplier() {
        assertEquals(1.0f, skillTree.prestigeDamageMultiplier(), 0.001f)
    }
}

