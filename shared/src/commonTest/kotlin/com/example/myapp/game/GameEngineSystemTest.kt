package com.example.myapp.game

import kotlin.test.*

class GameEngineSystemTest {

    private lateinit var prefs: FakeGamePreferences
    private lateinit var engine: GameEngine

    @BeforeTest
    fun setUp() {
        prefs = FakeGamePreferences()
        engine = GameEngine(prefs = prefs, audio = SilentAudio)
    }

    @Test
    fun testEngineInitialState() {
        assertEquals(0, engine.wave)
        assertEquals(50, engine.gold)
        assertEquals(100f, engine.baseHp)
        assertEquals(100f, engine.maxBaseHp)
        assertFalse(engine.gameOver)
        assertFalse(engine.isPaused)
        assertTrue(engine.towers.isEmpty())
        assertTrue(engine.enemies.isEmpty())
        assertTrue(engine.player.hp > 0f)
    }

    @Test
    fun testTowerPlacementAndGoldDeduction() {
        engine.gold = 200
        val cost = engine.getTowerCost(TowerType.ARROW)

        // Clear paths so placement succeeds anywhere
        engine.paths.clear()

        val placed = engine.placeTower(200f, 300f, TowerType.ARROW)
        assertTrue(placed, "Tower placement should succeed with sufficient gold and valid location")
        assertEquals(200 - cost, engine.gold)
        assertEquals(1, engine.towers.size)
        assertEquals(TowerType.ARROW, engine.towers.first().type)
    }

    @Test
    fun testTowerPlacementInsufficientGold() {
        engine.gold = 10
        val placed = engine.placeTower(200f, 300f, TowerType.CANNON)
        assertFalse(placed, "Tower placement must fail when player cannot afford it")
        assertEquals(0, engine.towers.size)
    }

    @Test
    fun testTowerSellRefundCalculation() {
        engine.paths.clear()
        engine.gold = 300
        engine.placeTower(200f, 300f, TowerType.ARROW)
        val tower = engine.towers.first()
        val goldBeforeSell = engine.gold

        val refund = (tower.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
        assertTrue(refund > 0)

        val sold = engine.sellTower(tower)
        assertTrue(sold)
        assertEquals(0, engine.towers.size)
        assertEquals(goldBeforeSell + refund, engine.gold)
    }

    @Test
    fun testPlayerDamageUpgradeCaps() {
        engine.gold = 50_000 // Unlimited gold for testing

        val initialDmg = engine.player.attackDamage
        assertEquals(1, engine.playerDamageLevel)

        // Upgrade until capped
        var upgrades = 0
        while (engine.upgradePlayerDamage()) {
            upgrades++
        }

        assertEquals(GameEngine.MAX_PLAYER_DAMAGE_LEVEL, engine.playerDamageLevel)
        assertFalse(engine.upgradePlayerDamage(), "Cannot upgrade beyond MAX_PLAYER_DAMAGE_LEVEL")
        assertTrue(engine.player.attackDamage > initialDmg)
    }

    @Test
    fun testPlayerSpeedUpgradeCapAndClamping() {
        engine.gold = 50_000

        while (engine.upgradePlayerSpeed()) {
            // keep upgrading
        }

        assertEquals(GameEngine.MAX_PLAYER_SPEED_LEVEL, engine.playerSpeedLevel)
        assertFalse(engine.upgradePlayerSpeed(), "Cannot upgrade beyond MAX_PLAYER_SPEED_LEVEL")
        assertTrue(engine.player.speed <= 525f, "Player speed must be hard-clamped at 525 px/sec")
    }

    @Test
    fun testPlayerHpAndBaseHpUpgradeCaps() {
        engine.gold = 100_000

        while (engine.upgradePlayerHp()) {}
        assertEquals(GameEngine.MAX_PLAYER_HP_LEVEL, engine.playerHpLevel)
        assertFalse(engine.upgradePlayerHp())

        engine.gold = 100_000
        while (engine.upgradeBaseHp()) {}
        assertEquals(GameEngine.MAX_BASE_HP_LEVEL, engine.baseHpLevel)
        assertFalse(engine.upgradeBaseHp())
    }

    @Test
    fun testPowersActivationAndCooldowns() {
        engine.gold = 200

        // Spawn a dummy enemy
        val testEnemy = Enemy(
            x = 200f, y = 200f, speed = 50f,
            hp = 100f, maxHp = 100f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )
        engine.enemies.add(testEnemy)

        // Trigger fireball power
        val used = engine.usePower(PowerType.FIREBALL)
        assertTrue(used, "Fireball power should trigger successfully")
        assertTrue(testEnemy.hp < 100f, "Enemies should take damage from fireball")

        // Immediately attempting again must fail due to cooldown
        val immediateRetry = engine.usePower(PowerType.FIREBALL)
        assertFalse(immediateRetry, "Power cannot be used while on cooldown")
    }

    @Test
    fun testPauseAndResume() {
        assertFalse(engine.isPaused)
        engine.isPaused = true
        assertTrue(engine.isPaused)
        engine.isPaused = false
        assertFalse(engine.isPaused)
    }

    @Test
    fun testCampaignApplicationAndStarCriteria() {
        val level1 = CampaignData.levels.first { it.id == 1 }
        engine.applyCampaign(level1)

        assertEquals(level1, engine.campaignLevel)
        assertEquals(level1.startingGold, engine.gold)
        assertFalse(engine.campaignVictory)

        assertTrue(engine.isTowerAllowed(TowerType.ARROW))
        assertFalse(engine.isTowerAllowed(TowerType.TESLA))

        // Level 1 star scores
        assertTrue(level1.star2Score >= 50)
        assertTrue(level1.star3Score > level1.star2Score)
    }

    @Test
    fun testPickEndlessMilestoneBuff() {
        val initialDmgMult = engine.endlessBuffTowerDmg
        val initialGoldMult = engine.endlessBuffGold

        engine.pickEndlessBuff(EndlessBuff.TOWER_DMG_UP)
        assertEquals(initialDmgMult + 0.20f, engine.endlessBuffTowerDmg)
        assertTrue(engine.endlessBuffs.contains(EndlessBuff.TOWER_DMG_UP))

        engine.pickEndlessBuff(EndlessBuff.GOLD_BONUS)
        assertEquals(initialGoldMult + 0.30f, engine.endlessBuffGold)
        assertTrue(engine.endlessBuffs.contains(EndlessBuff.GOLD_BONUS))
    }

    @Test
    fun testBaseShieldAndAegisOfDawnDamageAbsorption() {
        engine.baseHp = 100f
        engine.baseShield = 50f

        // Damage base by 30 -> shield absorbs fully
        engine.damageBase(30f)
        assertEquals(20f, engine.baseShield)
        assertEquals(100f, engine.baseHp)

        // Damage base by 40 -> remaining 20 shield absorbed, 20 damage penetrates to baseHp
        engine.damageBase(40f)
        assertEquals(0f, engine.baseShield)
        assertEquals(80f, engine.baseHp)
    }

    @Test
    fun testZephyrGreavesPassiveSpeedAndGaleAura() {
        prefs.edit().putBoolean(RelicId.ZEPHYR_GREAVES.prefKey, true).apply()
        val zephyrEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        zephyrEngine.init(1080f, 1920f)

        assertTrue(zephyrEngine.isZephyrUnlocked)

        // Add an enemy close to the player
        val enemy = Enemy(
            x = zephyrEngine.player.x + 50f,
            y = zephyrEngine.player.y + 50f,
            speed = 100f,
            hp = 100f,
            maxHp = 100f,
            goldReward = 10,
            damage = 5f,
            type = EnemyType.GOBLIN
        )
        zephyrEngine.enemies.add(enemy)

        // Update engine frame
        zephyrEngine.update(0.1f)

        // Player speed multiplier is +35%
        assertEquals(1.35f, zephyrEngine.player.speedMultiplier)

        // Enemy within 180f receives Gale Aura slow (25% reduction -> 0.75x)
        assertEquals(0.75f, enemy.iceSlowFactor)
    }

    @Test
    fun testPlayerJoystickVelocityAndStop() {
        val testEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        testEngine.init(1080f, 1920f)
        GameEngineHolder.engine = testEngine

        val startX = testEngine.player.x
        val startY = testEngine.player.y

        // Set joystick moving right (+X) at 100% magnitude
        testEngine.player.setVelocity(1f, 0f, 1f)
        testEngine.player.update(0.2f)

        assertTrue(testEngine.player.x > startX)
        assertEquals(startY, testEngine.player.y, 0.01f)

        // Stop moving
        val stoppedX = testEngine.player.x
        testEngine.player.stopMoving()
        testEngine.player.update(0.2f)

        assertEquals(stoppedX, testEngine.player.x, 0.01f)
    }

    @Test
    fun testPlayerRiverSliding() {
        val testEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        testEngine.init(1080f, 1920f)
        // Configure vertical river hazard between x=400 and x=600
        testEngine.riverChecker = { x, _ -> x in 400f..600f }
        GameEngineHolder.engine = testEngine

        // Place player just to the left of the river at (390, 500)
        testEngine.player.x = 390f
        testEngine.player.y = 500f
        testEngine.player.stopMoving()

        // Push joystick diagonally up-right towards river: dx > 0, dy < 0
        testEngine.player.setVelocity(0.707f, -0.707f, 1f)
        testEngine.player.update(0.1f)

        // Player must NOT enter the river (river starts at x=400f)
        assertTrue(testEngine.player.x < 400f)
        assertFalse(testEngine.isPointOnRiver(testEngine.player.x, testEngine.player.y))
        // And player MUST slide along the unobstructed vertical bank (y < 500)
        assertTrue(testEngine.player.y < 500f)
    }

    @Test
    fun testDemolitionSatchelTrapUses() {
        prefs.edit().putBoolean(RelicId.DEMOLITION_SATCHEL.prefKey, true).apply()
        val satchelEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        satchelEngine.init(1080f, 1920f)
        satchelEngine.gold = 500

        // Clear paths and add a path waypoint near (300, 300)
        satchelEngine.paths.clear()
        satchelEngine.paths.add(GamePath(listOf(GamePoint(300f, 300f), GamePoint(300f, 500f))))

        val placed = satchelEngine.placeTrap(300f, 320f, TrapType.SPIKE)
        assertTrue(placed)
        val trap = satchelEngine.traps.first()
        // Default spike uses is 5 + wave/5 = 5. With Demolition Satchel (+2) it is 7!
        assertEquals(7, trap.maxUses)
    }

    @Test
    fun testMapPathGeneratorAllBiomes() {
        val w = 1080f
        val h = 1920f
        val bx = w / 2f
        val by = h * 0.90f

        for (mapType in MapType.entries) {
            val paths = MapPathGenerator.generate(mapType, w, h, bx, by)
            val expectedLanes = MapPathGenerator.getLaneCount(mapType)
            assertEquals(expectedLanes, paths.size, "Map $mapType should have $expectedLanes paths")

            for (path in paths) {
                assertTrue(path.waypoints.size >= 2, "Path in $mapType must have at least 2 points")
                val lastPoint = path.waypoints.last()
                assertEquals(bx, lastPoint.x, 0.001f, "Path in $mapType should terminate at base X")
                assertEquals(by, lastPoint.y, 0.001f, "Path in $mapType should terminate at base Y")
            }
        }

        val volcanoCenter = MapPathGenerator.getVolcanoCenter(w, h)
        assertEquals(w * 0.50f, volcanoCenter.first, 0.001f)
        assertEquals(h * 0.40f, volcanoCenter.second, 0.001f)

        val crossroadsCenter = MapPathGenerator.getCrossroadsCenter(w, h)
        assertEquals(w * 0.50f, crossroadsCenter.first, 0.001f)
        assertEquals(h * 0.45f, crossroadsCenter.second, 0.001f)
    }

    @Test
    fun testWeatherEventProperties() {
        for (event in WeatherEvent.entries) {
            assertTrue(event.emoji.isNotBlank(), "Weather $event should have an emoji")
            assertTrue(event.displayName.isNotBlank(), "Weather $event should have a display name")
            assertTrue(event.description.isNotBlank(), "Weather $event should have a description")
        }

        assertEquals(2.0f, WeatherEvent.BLOOD_MOON.goldMultiplier)
        assertEquals(1.15f, WeatherEvent.BLOOD_MOON.enemySpeedMultiplier)
        assertEquals(2, WeatherEvent.BLOOD_MOON.bonusDiamondsOnClear)

        assertTrue(WeatherEvent.THUNDERSTORM.hasHeavenlyLightning)

        assertEquals(0.65f, WeatherEvent.SOLAR_ECLIPSE.spellCooldownMultiplier)
        assertEquals(1.30f, WeatherEvent.SOLAR_ECLIPSE.heroAttackSpeedMultiplier)
    }

    @Test
    fun testBloodMoonGoldMultiplierAndDiamondReward() {
        val testEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        testEngine.init(1080f, 1920f)
        testEngine.currentWeather = WeatherEvent.BLOOD_MOON

        val enemy = Enemy(
            x = 200f, y = 200f, speed = 50f,
            hp = 0f, maxHp = 100f, goldReward = 10, damage = 10f,
            type = EnemyType.GOBLIN
        )
        testEngine.enemies.add(enemy)
        val initialGold = testEngine.gold

        // Update to trigger enemy death processing
        testEngine.update(0.1f)
        // With 2x gold from Blood Moon, gold should increase by at least 20g (base 10 * 2)
        val goldEarned = testEngine.gold - initialGold
        assertTrue(goldEarned >= 20, "Enemy kill during Blood Moon must yield at least 2x gold, got $goldEarned")
    }

    @Test
    fun testThunderstormHeavenlyLightningStrikes() {
        val testEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        testEngine.init(1080f, 1920f)
        testEngine.currentWeather = WeatherEvent.THUNDERSTORM
        testEngine.waveInProgress = true

        val enemy = Enemy(
            x = 300f, y = 300f, speed = 40f,
            hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f,
            type = EnemyType.ORC
        )
        testEngine.enemies.add(enemy)

        // Set lightning timer to trigger on next tick
        testEngine.weatherLightningTimer = 0.05f
        testEngine.update(0.1f)

        // Heavenly lightning should have struck
        assertNotNull(testEngine.lastLightningTarget, "Thunderstorm should acquire and strike enemy target")
        assertTrue(testEngine.lightningFlashTimer > 0f, "Screen flash timer should be active")
        assertTrue(enemy.hp < 300f, "Enemy should take damage from lightning strike")
        assertTrue(enemy.deepFreezeTimer > 0f, "Enemy should be stunned by lightning strike")
    }

    @Test
    fun testSolarEclipseSpellCooldownBuff() {
        val testEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        testEngine.init(1080f, 1920f)

        // Test normal cooldown reduction
        testEngine.currentWeather = WeatherEvent.CLEAR
        testEngine.powerCooldowns[PowerType.FIREBALL] = 10f
        testEngine.update(1.0f)
        val normalRemaining = testEngine.getPowerCooldown(PowerType.FIREBALL)
        assertEquals(9.0f, normalRemaining, 0.05f)

        // Test Solar Eclipse cooldown reduction (1.4x CDR rate)
        testEngine.currentWeather = WeatherEvent.SOLAR_ECLIPSE
        testEngine.powerCooldowns[PowerType.FIREBALL] = 10f
        testEngine.update(1.0f)
        val eclipseRemaining = testEngine.getPowerCooldown(PowerType.FIREBALL)
        // 10 - 1.0 * 1.4 = 8.6
        assertEquals(8.6f, eclipseRemaining, 0.05f)
        assertTrue(eclipseRemaining < normalRemaining, "Solar Eclipse must reduce cooldowns faster")
    }

    @Test
    fun testMerchantShopTriggerEvery3Waves() {
        engine.init(1080f, 1920f)
        engine.wave = 3
        engine.waveInProgress = true
        engine.enemiesRemaining = 0
        engine.enemies.clear()

        // Wave completes
        engine.update(0.1f)

        assertFalse(engine.waveInProgress)
        assertTrue(engine.merchantShopPending, "Merchant shop should be pending on wave 3")
        assertTrue(engine.isPaused, "Engine should pause for merchant shop draft")
        assertEquals(3, engine.merchantShopChoices.size, "Merchant shop must offer exactly 3 wares")
    }

    @Test
    fun testMerchantDraftAndGoldDeduction() {
        engine.init(1080f, 1920f)
        engine.gold = 150
        val card = MerchantCatalog.allCards.first { it.id == MerchantItemId.THERMAL_SHOCK }
        
        // Cannot purchase if insufficient gold
        engine.gold = 50
        val purchasedFailed = engine.draftMerchantCard(card)
        assertFalse(purchasedFailed, "Purchase should fail if player lacks gold")
        assertFalse(engine.hasMerchantItem(card.id))

        // Purchase with sufficient gold
        engine.gold = 200
        val purchased = engine.draftMerchantCard(card)
        assertTrue(purchased, "Purchase should succeed with enough gold")
        assertEquals(200 - card.cost, engine.gold)
        assertTrue(engine.hasMerchantItem(card.id))
        assertFalse(engine.merchantShopPending)
        assertFalse(engine.isPaused)
    }

    @Test
    fun testGlassCannonAndBloodOfferingEffects() {
        engine.init(1080f, 1920f)
        engine.gold = 100
        engine.baseHp = 100f
        engine.maxBaseHp = 100f

        val glassCannon = MerchantCatalog.allCards.first { it.id == MerchantItemId.GLASS_CANNON }
        assertTrue(engine.draftMerchantCard(glassCannon))
        assertTrue(engine.hasMerchantItem(MerchantItemId.GLASS_CANNON))
        assertEquals(75f, engine.maxBaseHp, "Glass cannon should reduce max base HP")
        assertEquals(75f, engine.baseHp)

        val bloodOffering = MerchantCatalog.allCards.first { it.id == MerchantItemId.BLOOD_OFFERING }
        val goldBefore = engine.gold
        assertTrue(engine.draftMerchantCard(bloodOffering))
        assertEquals(50f, engine.baseHp, "Blood offering should deduct 25 HP")
        assertEquals(goldBefore + 180, engine.gold, "Blood offering should grant 180 gold")
    }

    @Test
    fun testTemporaryBoosterLifespan() {
        engine.init(1080f, 1920f)
        engine.gold = 100
        val booster = MerchantCatalog.allCards.first { it.id == MerchantItemId.MIDAS_TONIC }
        assertTrue(engine.draftMerchantCard(booster))
        assertTrue(engine.hasMerchantItem(MerchantItemId.MIDAS_TONIC))
        assertEquals(3, engine.activeTemporaryMerchantItems[MerchantItemId.MIDAS_TONIC])

        // Advance 1 wave
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        engine.waveInProgress = false
        engine.waveTimer = 0f
        engine.update(0.1f) // starts wave
        assertEquals(2, engine.activeTemporaryMerchantItems[MerchantItemId.MIDAS_TONIC])

        // Advance 2nd wave
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        engine.waveInProgress = false
        engine.waveTimer = 0f
        engine.update(0.1f)
        assertEquals(1, engine.activeTemporaryMerchantItems[MerchantItemId.MIDAS_TONIC])

        // Advance 3rd wave — booster should expire
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        engine.waveInProgress = false
        engine.waveTimer = 0f
        engine.update(0.1f)
        assertFalse(engine.hasMerchantItem(MerchantItemId.MIDAS_TONIC), "Booster must expire after 3 waves")
    }

    @Test
    fun testThermalShockSynergyProc() {
        engine.init(1080f, 1920f)
        engine.activePermanentMerchantItems.add(MerchantItemId.THERMAL_SHOCK)

        val enemy1 = Enemy(
            x = 200f, y = 200f, speed = 0f,
            hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )
        // Enemy is frozen via deepFreezeTimer
        enemy1.deepFreezeTimer = 2.0f

        val enemy2 = Enemy(
            x = 240f, y = 200f, speed = 0f,
            hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )

        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val flameTower = Tower(
            x = 200f, y = 220f, level = 1,
            damage = 20f, range = 150f, fireRate = 10f,
            type = TowerType.FLAME
        )
        engine.towers.add(flameTower)

        // Trigger tower attack update
        engine.update(0.2f)

        // AoE shatter burst (140 dmg) should hit both enemies
        assertTrue(enemy1.hp < 150f, "Direct hit with Thermal Shock should shatter primary target")
        assertTrue(enemy2.hp < 200f, "AoE splash with Thermal Shock should damage nearby target")
        assertTrue(engine.floatingTexts.any { it.text.contains("SHATTER") }, "Floating text should display SHATTER")
    }

    @Test
    fun testNeurotoxinChainSynergyProc() {
        engine.init(1080f, 1920f)
        engine.activePermanentMerchantItems.add(MerchantItemId.NEUROTOXIN_CHAIN)

        val enemy1 = Enemy(
            x = 200f, y = 200f, speed = 0f,
            hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )
        enemy1.poisonTimer = 3.0f
        enemy1.poisonDps = 15f

        val enemy2 = Enemy(
            x = 230f, y = 200f, speed = 0f,
            hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )

        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val teslaTower = Tower(
            x = 200f, y = 220f, level = 1,
            damage = 25f, range = 150f, fireRate = 10f,
            type = TowerType.TESLA
        )
        engine.towers.add(teslaTower)

        engine.update(0.2f)

        assertTrue(enemy2.hp < 300f, "Neurotoxin chain should detonate and damage nearby target")
        assertTrue(enemy2.poisonTimer > 0f, "Neurotoxin chain should spread poison to nearby target")
        assertTrue(engine.floatingTexts.any { it.text.contains("TOXIN BURST") })
    }

    @Test
    fun testMerchantAndSynergyAchievements() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)
        engine.gold = 500

        val card1 = MerchantCatalog.allCards.first { it.id == MerchantItemId.ALCHEMIST_ELIXIR }
        val card2 = MerchantCatalog.allCards.first { it.id == MerchantItemId.MIDAS_TONIC }
        val card3 = MerchantCatalog.allCards.first { it.id == MerchantItemId.FORTRESS_AEGIS }

        engine.draftMerchantCard(card1)
        assertTrue(prefs.getBoolean("ach_merchant_first", false), "Drafting first item should unlock merchant_first")
        assertTrue(prefs.getBoolean("ach_booster_alchemist", false), "Drafting booster should unlock booster_alchemist")
        assertFalse(prefs.getBoolean("ach_merchant_trio", false), "1 purchase should not unlock merchant_trio")

        engine.draftMerchantCard(card2)
        engine.draftMerchantCard(card3)
        assertTrue(prefs.getBoolean("ach_merchant_trio", false), "3 purchases should unlock merchant_trio")
    }

    @Test
    fun testWeatherAndIronWallAchievements() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)

        // Simulate 5 flawless waves
        for (i in 1..5) {
            engine.isPaused = false
            engine.merchantShopPending = false
            engine.wave = i
            engine.baseHp = 100f
            engine.baseHpBeforeWave = 100f
            engine.waveInProgress = true
            engine.enemies.clear()
            engine.enemiesRemaining = 0
            engine.update(0.1f)
        }
        assertTrue(prefs.getBoolean("ach_iron_wall_5", false), "5 consecutive flawless waves should unlock iron_wall_5")

        // Test Thunderstorm clear
        engine.isPaused = false
        engine.merchantShopPending = false
        engine.wave = 6
        engine.currentWeather = WeatherEvent.THUNDERSTORM
        engine.baseHp = 100f
        engine.baseHpBeforeWave = 100f
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        engine.update(0.1f)
        assertTrue(prefs.getBoolean("ach_weather_thunder", false), "Flawless thunderstorm should unlock weather_thunder")

        // Test Blood Moon clear
        engine.isPaused = false
        engine.merchantShopPending = false
        engine.wave = 7
        engine.currentWeather = WeatherEvent.BLOOD_MOON
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        engine.update(0.1f)
        assertTrue(prefs.getBoolean("ach_weather_bloodmoon", false), "Blood moon clear should unlock weather_bloodmoon")
    }

    @Test
    fun testHeroAndComboAchievements() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)

        // Hero crits
        engine.playerCritsThisRun = 14
        val dummyEnemy = Enemy(
            x = 100f, y = 100f, speed = 0f,
            hp = 100f, maxHp = 100f, goldReward = 5, damage = 10f,
            type = EnemyType.GOBLIN
        )
        engine.player.x = 100f
        engine.player.y = 110f
        engine.player.attackTimer = 0f
        engine.endlessBuffCritChance = 1.0f // guaranteed crit
        engine.enemies.add(dummyEnemy)
        engine.update(0.1f)
        assertTrue(prefs.getBoolean("ach_hero_crits", false), "15 crits should unlock hero_crits")

        // Wave 75
        engine.wave = 74
        val startNextWaveMethod = engine.javaClass.getDeclaredMethod("startNextWave")
        startNextWaveMethod.isAccessible = true
        startNextWaveMethod.invoke(engine)
        assertEquals(75, engine.wave)
        assertTrue(prefs.getBoolean("ach_wave_75", false), "Reaching wave 75 should unlock wave_75")
    }

    @Test
    fun testCampaignObjectivesEvaluation() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)

        // PERFECT_BASE: baseDamageTakenThisRun == 0
        engine.baseDamageTakenThisRun = 0f
        assertTrue(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.PERFECT_BASE, descEn = "", descPl = "")))
        engine.baseDamageTakenThisRun = 10f
        assertFalse(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.PERFECT_BASE, descEn = "", descPl = "")))

        // NO_TOWERS_SOLD
        engine.towersSoldThisRun = 0
        assertTrue(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.NO_TOWERS_SOLD, descEn = "", descPl = "")))
        engine.towersSoldThisRun = 1
        assertFalse(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.NO_TOWERS_SOLD, descEn = "", descPl = "")))

        // MAX_TOWERS_PLACED
        engine.towersPlacedThisRun = 4
        assertTrue(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.MAX_TOWERS_PLACED, targetValue = 4, descEn = "", descPl = "")))
        engine.towersPlacedThisRun = 5
        assertFalse(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.MAX_TOWERS_PLACED, targetValue = 4, descEn = "", descPl = "")))

        // FORBIDDEN_TOWER
        engine.towersPlacedTypes.clear()
        engine.towersPlacedTypes.add(TowerType.ARROW)
        assertTrue(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.FORBIDDEN_TOWER, forbiddenTower = TowerType.TESLA, descEn = "", descPl = "")))
        engine.towersPlacedTypes.add(TowerType.TESLA)
        assertFalse(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.FORBIDDEN_TOWER, forbiddenTower = TowerType.TESLA, descEn = "", descPl = "")))

        // HERO_SLAYS_BOSS
        engine.heroKilledBoss = false
        assertFalse(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.HERO_SLAYS_BOSS, descEn = "", descPl = "")))
        engine.heroKilledBoss = true
        assertTrue(engine.evaluateCampaignObjective(CampaignObjective(ObjectiveType.HERO_SLAYS_BOSS, descEn = "", descPl = "")))
    }

    @Test
    fun testHeroicCampaignDifficultyAndReward() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)
        val level = CampaignData.levels[0]

        // Normal mode
        engine.applyCampaign(level, isHeroic = false)
        assertFalse(engine.isHeroicMode)

        // Heroic mode
        engine.applyCampaign(level, isHeroic = true)
        assertTrue(engine.isHeroicMode)

        // Check boss telegraph duration in heroic mode
        val boss = Enemy(
            x = 100f, y = 100f, speed = 20f, hp = 500f, maxHp = 500f,
            goldReward = 50, damage = 20f, type = EnemyType.BOSS, bossType = BossType.FROST_TITAN
        )
        engine.startBossTelegraph(boss, BossAbility.TITAN_STOMP)
        assertEquals(1.2f, boss.telegraphDuration, "Heroic telegraph duration should be 1.2s instead of 1.5s")

        // Trigger campaign victory on heroic
        engine.wave = level.targetWave
        engine.baseDamageTakenThisRun = 0f
        engine.baseHpBeforeWave = engine.baseHp
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0
        val initialDiamonds = prefs.getInt("diamonds", 0)

        engine.update(0.1f)

        assertTrue(prefs.getBoolean("campaign_${level.id}_heroic", false), "Heroic clear flag should be set")
        assertTrue(prefs.getBoolean("ach_campaign_heroic_first", false), "Heroic first achievement should be unlocked")
        val earnedDiamonds = prefs.getInt("diamonds", 0) - initialDiamonds
        // level.diamondReward + 4 (for 3 stars) + 5 (heroic bounty)
        assertEquals(level.diamondReward + 4 + 5, earnedDiamonds)
    }

    @Test
    fun testExpandedCampaignLevelsIntegrityAndMechanics() {
        val prefs = FakeGamePreferences()
        val engine = GameEngine(prefs = prefs, audio = SilentAudio)

        // 1. Verify exact count is 80 levels
        assertEquals(80, CampaignData.levels.size, "Campaign should feature 80 total handcrafted levels")

        // 2. Verify sequential ordering and non-blank fields
        for ((index, level) in CampaignData.levels.withIndex()) {
            assertEquals(index + 1, level.id)
            assertTrue(level.title.isNotBlank())
            assertTrue(level.description.isNotBlank())
            assertTrue(level.emoji.isNotBlank())
            assertTrue(level.targetWave >= 3)
            assertTrue(level.startingGold >= 20)
            assertTrue(level.diamondReward > 0)
            assertNotNull(level.mapType)
            assertNotNull(level.objective1)
            assertNotNull(level.objective2)
            assertNotNull(level.objective3)
        }

        // 3. Test Level 43: Glass Citadel (10 max base HP)
        val lvl43 = CampaignData.levels[42]
        engine.applyCampaign(lvl43)
        assertEquals(10f, engine.baseHp)
        assertEquals(10f, engine.maxBaseHp)

        // 4. Test Level 53: Tempest Wing (Thunderstorm)
        val lvl53 = CampaignData.levels[52]
        engine.applyCampaign(lvl53)
        assertEquals(WeatherEvent.THUNDERSTORM, engine.currentWeather)

        // 5. Test Level 62: Solar Eclipse
        val lvl62 = CampaignData.levels[61]
        engine.applyCampaign(lvl62)
        assertEquals(WeatherEvent.SOLAR_ECLIPSE, engine.currentWeather)

        // 6. Test Level 63: Lone Champion Hero buff
        val lvl63 = CampaignData.levels[62]
        val heroDamageBefore = engine.player.attackDamage
        engine.applyCampaign(lvl63)
        assertTrue(engine.player.attackDamage > heroDamageBefore * 2f)

        // 7. Test Level 64: Blood Moon Rampage
        val lvl64 = CampaignData.levels[63]
        engine.applyCampaign(lvl64)
        assertEquals(WeatherEvent.BLOOD_MOON, engine.currentWeather)

        // 8. Test Level 69: Boss Gauntlet (bossInterval == 1)
        val lvl69 = CampaignData.levels[68]
        engine.applyCampaign(lvl69)
        assertEquals(1, engine.bossInterval)

        // 9. Test Level 78: Glacial Perma-Death (no repairs, 0g sell refund)
        val lvl78 = CampaignData.levels[77]
        engine.applyCampaign(lvl78)
        engine.baseHp = 80f
        engine.gold = 500
        assertFalse(engine.repairBase(), "Base repairs must be forbidden on Level 78 Perma-Death")

        val tower = Tower(x = 100f, y = 100f, type = TowerType.ARROW)
        engine.towers.add(tower)
        val goldBeforeSell = engine.gold
        engine.sellTower(tower)
        assertEquals(goldBeforeSell, engine.gold, "Tower selling on Level 78 must yield 0 gold refund")
    }
}
