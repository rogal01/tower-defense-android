package com.example.myapp.game

import kotlin.test.*

class TowerSystemTest {

    @Test
    fun testTowerCreationAndDefaults() {
        val tower = Tower(
            x = 100f,
            y = 150f,
            level = 1,
            range = 200f,
            damage = 10f,
            fireRate = 1.0f,
            type = TowerType.ARROW
        )

        assertEquals(100f, tower.x)
        assertEquals(150f, tower.y)
        assertEquals(1, tower.level)
        assertEquals(200f, tower.range)
        assertEquals(10f, tower.damage)
        assertEquals(TowerType.ARROW, tower.type)
        assertEquals(TargetingMode.CLOSE, tower.targetingMode)
        assertEquals(TowerSpecialization.NONE, tower.specialization)
        assertTrue(tower.canUpgrade())
        assertFalse(tower.canSpecialize(), "Tower at Level 1 should not be able to specialize")
    }

    @Test
    fun testDistanceCalculation() {
        val tower = Tower(x = 0f, y = 0f)
        val dist = tower.distanceTo(30f, 40f)
        assertEquals(50f, dist, 0.01f)
    }

    @Test
    fun testTowerUpgradeProgressionAndCaps() {
        val tower = Tower(x = 50f, y = 50f, level = 1, type = TowerType.ARROW)

        // Level up incrementally
        for (lvl in 1..9) {
            assertTrue(tower.canUpgrade(), "Tower should be upgradable at level $lvl")
            val cost = tower.upgradeCost()
            assertTrue(cost > 0, "Upgrade cost must be positive at level $lvl")
            tower.level++
        }

        assertEquals(10, tower.level)
        assertEquals(Tower.MAX_TOWER_LEVEL, tower.level)
        assertFalse(tower.canUpgrade(), "Tower must be capped at MAX_TOWER_LEVEL (10)")
        assertEquals(Int.MAX_VALUE, tower.upgradeCost(), "Upgrade cost at max level should be Int.MAX_VALUE")
    }

    @Test
    fun testTargetingModeCycle() {
        var mode = TargetingMode.CLOSE
        mode = mode.next()
        assertEquals(TargetingMode.FIRST, mode)
        mode = mode.next()
        assertEquals(TargetingMode.LAST, mode)
        mode = mode.next()
        assertEquals(TargetingMode.STRONG, mode)
        mode = mode.next()
        assertEquals(TargetingMode.CLOSE, mode)
    }

    @Test
    fun testSpecializationUnlockAtLevel5() {
        val arrowTower = Tower(x = 0f, y = 0f, level = 4, type = TowerType.ARROW)
        assertFalse(arrowTower.canSpecialize(), "Should not specialize at Level 4")

        arrowTower.level = 5
        assertTrue(arrowTower.canSpecialize(), "Must unlock specialization at Level 5")

        val arrowBranches = arrowTower.availableSpecializations()
        assertEquals(2, arrowBranches.size)
        assertTrue(arrowBranches.contains(TowerSpecialization.SNIPER))
        assertTrue(arrowBranches.contains(TowerSpecialization.RANGER))

        // Apply Sniper
        arrowTower.applySpecialization(TowerSpecialization.SNIPER)
        assertEquals(TowerSpecialization.SNIPER, arrowTower.specialization)
        assertFalse(arrowTower.canSpecialize(), "Cannot specialize again once specialized")
    }

    @Test
    fun testBombAndMagicTowerSpecializationBranches() {
        val bombTower = Tower(x = 0f, y = 0f, level = 5, type = TowerType.CANNON)
        val bombBranches = bombTower.availableSpecializations()
        assertTrue(bombBranches.contains(TowerSpecialization.CLUSTER_MORTAR))
        assertTrue(bombBranches.contains(TowerSpecialization.RAILGUN))

        val magicTower = Tower(x = 0f, y = 0f, level = 5, type = TowerType.MAGIC)
        val magicBranches = magicTower.availableSpecializations()
        assertTrue(magicBranches.contains(TowerSpecialization.ARCANE_BEAM))
        assertTrue(magicBranches.contains(TowerSpecialization.RIFT_WARP))
    }

    @Test
    fun testFireCooldownAndRecoil() {
        val tower = Tower(x = 0f, y = 0f, fireRate = 2.0f) // 0.5s cooldown
        assertTrue(tower.canFire())

        tower.fire()
        assertFalse(tower.canFire(), "Tower should be on cooldown immediately after firing")
        assertEquals(0.5f, tower.fireTimer, 0.001f)
        assertEquals(0.15f, tower.recoilTimer, 0.001f)

        // Advance timer by 0.3s
        tower.update(0.3f)
        assertFalse(tower.canFire())
        assertEquals(0.2f, tower.fireTimer, 0.001f)

        // Advance timer by 0.25s (total 0.55s)
        tower.update(0.25f)
        assertTrue(tower.canFire(), "Tower should be ready to fire after cooldown elapses")
    }

    @Test
    fun testThornsJammingMechanic() {
        val tower = Tower(x = 0f, y = 0f, fireRate = 1.0f)
        tower.thornJamTimer = 2.0f
        assertFalse(tower.canFire(), "Jammed tower cannot fire")

        tower.update(1.0f)
        assertFalse(tower.canFire())
        tower.update(1.5f)
        assertTrue(tower.canFire(), "Tower fires once thorn jam expires")
    }
}
