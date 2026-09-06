package com.example.myapp.game

import kotlin.test.*

class EnemySystemTest {

    @Test
    fun testEnemyInitializationAndDeath() {
        val enemy = Enemy(
            x = 50f, y = 50f,
            speed = 80f,
            hp = 100f, maxHp = 100f,
            goldReward = 10,
            damage = 15f,
            type = EnemyType.GOBLIN
        )

        assertEquals(100f, enemy.hp)
        assertFalse(enemy.isDead())
        assertEquals(50f, enemy.distanceTo(80f, 90f), 0.01f)

        enemy.hp -= 40f
        assertEquals(60f, enemy.hp)
        assertFalse(enemy.isDead())

        enemy.hp -= 65f
        assertTrue(enemy.isDead(), "Enemy should be dead when HP <= 0")
    }

    @Test
    fun testClassicEnemyResistances() {
        // Skeleton: weak to magic/explosive/fire, resistant to physical/dark
        assertEquals(0.5f, EnemyResistances.getMultiplier(EnemyType.SKELETON, DamageType.PHYSICAL))
        assertEquals(1.5f, EnemyResistances.getMultiplier(EnemyType.SKELETON, DamageType.MAGIC))
        assertEquals(1.3f, EnemyResistances.getMultiplier(EnemyType.SKELETON, DamageType.EXPLOSIVE))
        assertEquals(1.3f, EnemyResistances.getMultiplier(EnemyType.SKELETON, DamageType.FIRE))
        assertEquals(0.7f, EnemyResistances.getMultiplier(EnemyType.SKELETON, DamageType.DARK))

        // Demon: weak to ice, resistant to fire/dark
        assertEquals(1.5f, EnemyResistances.getMultiplier(EnemyType.DEMON, DamageType.ICE))
        assertEquals(0.3f, EnemyResistances.getMultiplier(EnemyType.DEMON, DamageType.FIRE))

        // Dragon: weak to ice/dark, resistant to physical/fire
        assertEquals(1.4f, EnemyResistances.getMultiplier(EnemyType.DRAGON, DamageType.ICE))
        assertEquals(0.4f, EnemyResistances.getMultiplier(EnemyType.DRAGON, DamageType.FIRE))
        assertEquals(0.6f, EnemyResistances.getMultiplier(EnemyType.DRAGON, DamageType.PHYSICAL))
    }

    @Test
    fun testNewMonsterExpansionResistances() {
        // 1. Necromancer: weak to explosive & physical, resistant to dark & magic
        assertEquals(1.4f, EnemyResistances.getMultiplier(EnemyType.NECROMANCER, DamageType.EXPLOSIVE), 0.01f)
        assertEquals(1.2f, EnemyResistances.getMultiplier(EnemyType.NECROMANCER, DamageType.PHYSICAL), 0.01f)
        assertEquals(0.4f, EnemyResistances.getMultiplier(EnemyType.NECROMANCER, DamageType.DARK), 0.01f)
        assertEquals(0.7f, EnemyResistances.getMultiplier(EnemyType.NECROMANCER, DamageType.MAGIC), 0.01f)

        // 2. Ghost: vulnerable to magic & tesla, resistant to physical, explosive, dark
        assertEquals(1.6f, EnemyResistances.getMultiplier(EnemyType.GHOST, DamageType.MAGIC), 0.01f)
        assertEquals(1.5f, EnemyResistances.getMultiplier(EnemyType.GHOST, DamageType.ELECTRIC), 0.01f)
        assertEquals(0.25f, EnemyResistances.getMultiplier(EnemyType.GHOST, DamageType.PHYSICAL), 0.01f)
        assertEquals(0.4f, EnemyResistances.getMultiplier(EnemyType.GHOST, DamageType.EXPLOSIVE), 0.01f)

        // 3. Magma Crab: weak to ice, near immune to fire, resistant to physical & poison
        assertEquals(1.8f, EnemyResistances.getMultiplier(EnemyType.MAGMA_CRAB, DamageType.ICE), 0.01f)
        assertEquals(0.1f, EnemyResistances.getMultiplier(EnemyType.MAGMA_CRAB, DamageType.FIRE), 0.01f)
        assertEquals(0.6f, EnemyResistances.getMultiplier(EnemyType.MAGMA_CRAB, DamageType.PHYSICAL), 0.01f)

        // 4. Harpy: weak to physical & electric, evades ground explosive blasts
        assertEquals(1.3f, EnemyResistances.getMultiplier(EnemyType.HARPY, DamageType.PHYSICAL), 0.01f)
        assertEquals(1.4f, EnemyResistances.getMultiplier(EnemyType.HARPY, DamageType.ELECTRIC), 0.01f)
        assertEquals(0.4f, EnemyResistances.getMultiplier(EnemyType.HARPY, DamageType.EXPLOSIVE), 0.01f)

        // 5. Treant: extremely vulnerable to fire, resistant to physical & electric
        assertEquals(1.8f, EnemyResistances.getMultiplier(EnemyType.TREANT, DamageType.FIRE), 0.01f)
        assertEquals(0.7f, EnemyResistances.getMultiplier(EnemyType.TREANT, DamageType.PHYSICAL), 0.01f)
        assertEquals(0.6f, EnemyResistances.getMultiplier(EnemyType.TREANT, DamageType.ELECTRIC), 0.01f)
    }

    @Test
    fun testBerserkerRageScaling() {
        val berserker = Enemy(
            x = 0f, y = 0f, speed = 70f,
            hp = 100f, maxHp = 100f,
            goldReward = 10, damage = 10f,
            type = EnemyType.BERSERKER
        )

        // Full HP -> Rage multiplier is 1.0x
        assertEquals(1.0f, berserker.berserkerRage, 0.01f)

        // Half HP -> Rage multiplier is 1 + (0.5 * 1.5) = 1.75x
        berserker.hp = 50f
        assertEquals(1.75f, berserker.berserkerRage, 0.01f)

        // Near zero HP -> Rage multiplier approaches 2.5x
        berserker.hp = 0f
        assertEquals(2.5f, berserker.berserkerRage, 0.01f)
    }

    @Test
    fun testShapeshifterDynamicPhases() {
        // Phase 0: resistant to Physical / Explosive, weak to Magic / Dark
        assertEquals(0.3f, EnemyResistances.getShapeshifterMultiplier(0, DamageType.PHYSICAL))
        assertEquals(1.4f, EnemyResistances.getShapeshifterMultiplier(0, DamageType.MAGIC))

        // Phase 1: resistant to Magic / Dark, weak to Fire / Ice
        assertEquals(0.3f, EnemyResistances.getShapeshifterMultiplier(1, DamageType.MAGIC))
        assertEquals(1.4f, EnemyResistances.getShapeshifterMultiplier(1, DamageType.FIRE))

        // Phase 2: resistant to Fire / Ice, weak to Electric
        assertEquals(0.3f, EnemyResistances.getShapeshifterMultiplier(2, DamageType.FIRE))
        assertEquals(1.4f, EnemyResistances.getShapeshifterMultiplier(2, DamageType.ELECTRIC))
    }

    @Test
    fun testEliteAbilitiesAndBosses() {
        val elite = Enemy(
            x = 0f, y = 0f, speed = 80f, hp = 300f, maxHp = 300f,
            goldReward = 20, damage = 20f, type = EnemyType.ORC
        )
        elite.isElite = true
        elite.eliteAbility = EliteAbility.SHIELD
        elite.shieldTimer = 4.0f

        assertTrue(elite.isElite)
        assertEquals(EliteAbility.SHIELD, elite.eliteAbility)
        assertTrue(elite.shieldTimer > 0f)

        // Verify all 15 Boss types have valid minion types, stats, and abilities
        assertEquals(15, BossType.entries.size, "Total bosses should be expanded to 15")
        for (boss in BossType.entries) {
            assertNotNull(boss.displayName)
            assertNotNull(boss.minionType)
            assertTrue(boss.baseHp > 0f)
            assertTrue(boss.baseSpeed > 0f)
            assertNotNull(boss.ability)
        }
    }

    @Test
    fun testVoidPhoenixRebirthMechanic() {
        val engine = GameEngine(prefs = FakeGamePreferences(), audio = SilentAudio)
        val phoenix = Enemy(
            x = 200f, y = 200f, speed = 40f,
            hp = 500f, maxHp = 500f, goldReward = 200, damage = 50f,
            type = EnemyType.BOSS, bossType = BossType.VOID_PHOENIX
        )
        engine.enemies.add(phoenix)

        assertFalse(phoenix.hasReborn, "Phoenix should not have reborn initially")

        // First fatal blow
        phoenix.hp = 0f
        engine.update(0.05f)

        assertTrue(phoenix.hasReborn, "Phoenix must reincarnate after first fatal blow")
        assertFalse(phoenix.isDead(), "Phoenix must be alive after rebirth")
        assertEquals(225f, phoenix.hp, 0.5f, "Reborn Phoenix HP should be 45% of maxHp")

        // Second fatal blow
        phoenix.hp = 0f
        engine.update(0.05f)
        assertTrue(phoenix.deathProcessed, "Phoenix must die permanently after second fatal blow")
    }

    @Test
    fun testStormLeviathanEmpDisruptsTowers() {
        val engine = GameEngine(prefs = FakeGamePreferences(), audio = SilentAudio)
        val tower1 = Tower(x = 200f, y = 200f, level = 1, type = TowerType.ARROW)
        val tower2 = Tower(x = 220f, y = 200f, level = 1, type = TowerType.TESLA)
        engine.towers.add(tower1)
        engine.towers.add(tower2)

        val leviathan = Enemy(
            x = 200f, y = 200f, speed = 25f,
            hp = 1000f, maxHp = 1000f, goldReward = 240, damage = 65f,
            type = EnemyType.BOSS, bossType = BossType.STORM_LEVIATHAN,
            bossAbilityTimer = 0.01f, bossAbilityCooldown = 9f
        )
        engine.enemies.add(leviathan)

        assertEquals(0f, tower1.fireTimer)
        assertEquals(0f, tower2.fireTimer)

        // Trigger ability via engine update: first step triggers telegraph, subsequent steps finish charging
        engine.update(0.05f)
        assertTrue(leviathan.isTelegraphing, "Leviathan should telegraph EMP blast")
        engine.update(1.6f)

        assertTrue(tower1.fireTimer >= 2.0f, "EMP blast must jam tower 1")
        assertTrue(tower2.fireTimer >= 2.0f, "EMP blast must jam tower 2")
    }

    @Test
    fun testBossTelegraphChargingAndExecution() {
        val engine = GameEngine(prefs = FakeGamePreferences(), audio = SilentAudio)
        val boss = Enemy(
            x = 200f, y = 200f, speed = 20f,
            hp = 500f, maxHp = 500f, goldReward = 100, damage = 30f,
            type = EnemyType.BOSS, bossType = BossType.FROST_TITAN,
            bossAbilityTimer = 0.05f
        )
        engine.enemies.add(boss)

        assertFalse(boss.isTelegraphing)
        engine.startBossTelegraph(boss, BossAbility.TITAN_STOMP)
        assertTrue(boss.isTelegraphing)
        assertEquals(BossAbility.TITAN_STOMP, boss.pendingAbility)
        assertEquals(1.5f, boss.telegraphDuration)
        assertEquals(1.5f, boss.telegraphTimer)

        // Advance time by 0.5s: still charging
        engine.update(0.5f)
        assertTrue(boss.isTelegraphing)
        assertTrue(boss.telegraphTimer < 1.5f && boss.telegraphTimer > 0f)

        // Advance remaining time: telegraph finishes and ability executes
        engine.update(1.1f)
        assertFalse(boss.isTelegraphing)
    }

    @Test
    fun testBossHealthPhaseTriggers() {
        val engine = GameEngine(prefs = FakeGamePreferences(), audio = SilentAudio)
        val boss = Enemy(
            x = 200f, y = 200f, speed = 20f,
            hp = 1000f, maxHp = 1000f, goldReward = 100, damage = 30f,
            type = EnemyType.BOSS, bossType = BossType.FROST_TITAN,
            bossAbilityTimer = 999f
        )
        engine.enemies.add(boss)

        assertEquals(1, boss.currentPhase)
        assertFalse(boss.phase75Triggered)

        // Drop below 75% HP
        boss.hp = 740f
        engine.update(0.05f)
        assertTrue(boss.phase75Triggered, "75% HP phase should trigger")
        assertEquals(2, boss.currentPhase)
        assertTrue(boss.isTelegraphing)
        assertEquals(BossAbility.SHIELD, boss.pendingAbility)

        // Clear telegraph
        boss.isTelegraphing = false

        // Drop below 50% HP
        boss.hp = 490f
        engine.update(0.05f)
        assertTrue(boss.phase50Triggered, "50% HP phase should trigger")
        assertEquals(3, boss.currentPhase)
        assertTrue(boss.isTelegraphing)
        assertEquals(BossAbility.SUMMON, boss.pendingAbility)

        // Clear telegraph
        boss.isTelegraphing = false

        // Drop below 25% HP
        boss.hp = 240f
        engine.update(0.05f)
        assertTrue(boss.phase25Triggered, "25% HP phase should trigger")
        assertEquals(4, boss.currentPhase)
        assertTrue(boss.isTelegraphing)
        assertEquals(BossAbility.TITAN_STOMP, boss.pendingAbility)
    }

    @Test
    fun testTitanStompStunsNearbyTowers() {
        val engine = GameEngine(prefs = FakeGamePreferences(), audio = SilentAudio)
        val nearTower = Tower(x = 220f, y = 200f, level = 1, type = TowerType.ARROW)
        val farTower = Tower(x = 600f, y = 600f, level = 1, type = TowerType.ARROW)
        engine.towers.add(nearTower)
        engine.towers.add(farTower)

        val titan = Enemy(
            x = 200f, y = 200f, speed = 20f,
            hp = 1000f, maxHp = 1000f, goldReward = 100, damage = 30f,
            type = EnemyType.BOSS, bossType = BossType.FROST_TITAN
        )
        engine.enemies.add(titan)

        assertEquals(0f, nearTower.stunTimer)
        assertTrue(nearTower.canFire())

        engine.executeBossAbility(titan, 0.05f, BossAbility.TITAN_STOMP)

        assertEquals(3.0f, nearTower.stunTimer)
        assertFalse(nearTower.canFire(), "Stunned tower cannot fire")
        assertEquals(0f, farTower.stunTimer, "Far tower should not be stunned")
        assertTrue(farTower.canFire())

        // Update tower to tick down stunTimer
        nearTower.update(1.0f)
        assertEquals(2.0f, nearTower.stunTimer)
        assertFalse(nearTower.canFire())

        nearTower.update(2.5f)
        assertEquals(0f, nearTower.stunTimer)
        assertTrue(nearTower.canFire(), "Tower can fire once stun wears off")
    }
}
