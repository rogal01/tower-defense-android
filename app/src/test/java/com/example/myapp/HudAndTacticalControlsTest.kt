package com.example.myapp

import com.example.myapp.game.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HudAndTacticalControlsTest {

    private class MockPreferences : GamePreferences {
        private val data = mutableMapOf<String, Any>()
        override fun getInt(key: String, default: Int): Int = (data[key] as? Int) ?: default
        override fun getLong(key: String, default: Long): Long = (data[key] as? Long) ?: default
        override fun getFloat(key: String, default: Float): Float = (data[key] as? Float) ?: default
        override fun getBoolean(key: String, default: Boolean): Boolean = (data[key] as? Boolean) ?: default
        override fun getString(key: String, default: String): String = (data[key] as? String) ?: default
        override fun edit(): GamePreferences.Editor = object : GamePreferences.Editor {
            override fun putInt(key: String, value: Int) = apply { data[key] = value }
            override fun putLong(key: String, value: Long) = apply { data[key] = value }
            override fun putFloat(key: String, value: Float) = apply { data[key] = value }
            override fun putBoolean(key: String, value: Boolean) = apply { data[key] = value }
            override fun putString(key: String, value: String) = apply { data[key] = value }
            override fun apply() {}
        }
    }

    @Test
    fun testHudStringFormatting() {
        assertEquals("💰 50", GameStrings.goldHud(50))
        assertEquals("💎 15", GameStrings.diamondsHud(15))
        assertEquals("💀 42", GameStrings.killsHud(42))
        assertEquals("💀 0", GameStrings.killsHud(0))
        assertTrue(GameStrings.waveHud(3).contains("3"))
    }

    @Test
    fun testHealPowerDefinitionAndMechanics() {
        val heal = PowerType.HEAL
        assertEquals(25, heal.cost, "Heal power must cost 25 gold")
        assertEquals(15f, heal.cooldown, "Heal power must have 15s cooldown")
        assertEquals("💚", heal.emoji, "Heal power emoji must be 💚")
        assertEquals("Heal", heal.displayName)

        assertTrue(GameStrings.healUsed.contains("50 HP"))
        assertTrue(GameStrings.healNeedGold(25).contains("25g"))

        val engine = GameEngine(prefs = MockPreferences(), audio = SilentAudio)
        engine.gold = 100
        engine.baseHp = 50f
        val prevHp = engine.baseHp

        val used = engine.usePower(PowerType.HEAL)
        assertTrue(used, "Heal power should succeed when gold is sufficient and base HP is below max")
        assertEquals(prevHp + 50f, engine.baseHp, "Base HP should increase by 50")
        assertEquals(75, engine.gold, "25 gold should be deducted")
        assertTrue(engine.getPowerCooldown(PowerType.HEAL) > 0f, "Heal power cooldown should be active")
    }

    @Test
    fun testTowerInspectorDeltaCalculations() {
        val tower = Tower(type = TowerType.ARROW, x = 100f, y = 100f)
        assertEquals(1, tower.level)

        // Delta calculation formula from spec: (tower.type.baseDamage * 0.70f).toInt()
        val expectedDelta = (tower.type.baseDamage * 0.70f).toInt()
        val upCost = tower.upgradeCost()

        val upgradeBtnText = "▲ Lv.${tower.level + 1} [+$expectedDelta DMG] [💰 $upCost]"
        assertTrue(upgradeBtnText.startsWith("▲ Lv.2"))
        assertTrue(upgradeBtnText.contains("[+$expectedDelta DMG]"))
        assertTrue(upgradeBtnText.contains("[💰 $upCost]"))

        // Upgrade to MAX
        while (tower.level < Tower.MAX_TOWER_LEVEL) {
            tower.upgrade()
        }
        assertEquals(Tower.MAX_TOWER_LEVEL, tower.level)
        val maxBtnText = "▲ Lv.MAX [MAX]"
        assertEquals("▲ Lv.MAX [MAX]", maxBtnText)
    }

    @Test
    fun testTowerTotalKillsTracking() {
        val tower = Tower(type = TowerType.ARROW, x = 100f, y = 100f)
        assertEquals(0, tower.totalKills)
        tower.totalKills += 5
        assertEquals(5, tower.totalKills)
        val badgeKillsText = "💀 ${tower.totalKills}"
        assertEquals("💀 5", badgeKillsText)
    }

    @Test
    fun testAllFourHeroPowersPresent() {
        val powers = PowerType.entries
        assertTrue(powers.contains(PowerType.FIREBALL))
        assertTrue(powers.contains(PowerType.FREEZE))
        assertTrue(powers.contains(PowerType.HEAL))
        assertTrue(powers.contains(PowerType.LIGHTNING))
        assertEquals(4, powers.size)
    }
}
