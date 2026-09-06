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
}
