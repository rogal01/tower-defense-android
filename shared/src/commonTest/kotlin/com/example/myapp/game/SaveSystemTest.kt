package com.example.myapp.game

import kotlin.test.*

class SaveSystemTest {

    private lateinit var prefs: FakeGamePreferences

    @BeforeTest
    fun setUp() {
        prefs = FakeGamePreferences()
    }

    @Test
    fun testSaveAndRestoreTowersWithSpecializations() {
        val engine1 = GameEngine(prefs = prefs, audio = SilentAudio)
        engine1.wave = 12
        engine1.gold = 1500
        engine1.score = 4200

        // Create customized towers
        val sniperTower = Tower(
            x = 250f, y = 350f, level = 5,
            range = 260f, damage = 45f, fireRate = 0.9f,
            type = TowerType.ARROW,
            specialization = TowerSpecialization.SNIPER,
            targetingMode = TargetingMode.STRONG
        )
        val clusterMortar = Tower(
            x = 400f, y = 600f, level = 6,
            range = 300f, damage = 80f, fireRate = 0.4f,
            type = TowerType.CANNON,
            specialization = TowerSpecialization.CLUSTER_MORTAR,
            targetingMode = TargetingMode.CLOSE
        )
        engine1.towers.add(sniperTower)
        engine1.towers.add(clusterMortar)

        // Save game state
        engine1.saveGame()
        assertTrue(prefs.getBoolean("has_save", false), "Save flag should be true after saving")

        // Create a new engine instance to simulate game restart
        val engine2 = GameEngine(prefs = prefs, audio = SilentAudio)
        val loaded = engine2.loadGame()

        assertTrue(loaded, "Loading saved game should succeed")
        assertEquals(12, engine2.wave)
        assertEquals(1500, engine2.gold)
        assertEquals(4200, engine2.score)
        assertEquals(2, engine2.towers.size)

        val restoredSniper = engine2.towers.first { it.type == TowerType.ARROW }
        assertEquals(5, restoredSniper.level)
        assertEquals(TowerSpecialization.SNIPER, restoredSniper.specialization)
        assertEquals(TargetingMode.STRONG, restoredSniper.targetingMode)

        val restoredMortar = engine2.towers.first { it.type == TowerType.CANNON }
        assertEquals(6, restoredMortar.level)
        assertEquals(TowerSpecialization.CLUSTER_MORTAR, restoredMortar.specialization)
        assertEquals(TargetingMode.CLOSE, restoredMortar.targetingMode)
    }

    @Test
    fun testSaveAndRestoreTrapsAndBlockades() {
        val engine1 = GameEngine(prefs = prefs, audio = SilentAudio)
        engine1.wave = 3

        engine1.traps.add(Trap(x = 100f, y = 200f, type = TrapType.SPIKE, uses = 3, maxUses = 5))
        engine1.blockades.add(Blockade(x = 300f, y = 400f, hp = 150f, maxHp = 200f))

        engine1.saveGame()

        val engine2 = GameEngine(prefs = prefs, audio = SilentAudio)
        assertTrue(engine2.loadGame())

        assertEquals(1, engine2.traps.size)
        val restoredTrap = engine2.traps.first()
        assertEquals(TrapType.SPIKE, restoredTrap.type)
        assertEquals(3, restoredTrap.uses)
        assertEquals(5, restoredTrap.maxUses)

        assertEquals(1, engine2.blockades.size)
        val restoredBlockade = engine2.blockades.first()
        assertEquals(150f, restoredBlockade.hp)
        assertEquals(200f, restoredBlockade.maxHp)
    }

    @Test
    fun testClearSaveOnGameOver() {
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)
        engine.wave = 5
        engine.saveGame()
        assertTrue(prefs.getBoolean("has_save", false))

        // Trigger clearSave
        engine.clearSave()
        assertFalse(prefs.getBoolean("has_save", false), "Save flag should be cleared")
    }
}
