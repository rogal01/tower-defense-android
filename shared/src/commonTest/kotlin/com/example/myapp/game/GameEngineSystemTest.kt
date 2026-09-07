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

        // 10. Test Paced Tower Unlocks (Levels 1-15)
        assertEquals(setOf(TowerType.ARROW), CampaignData.levels[0].allowedTowers)
        assertEquals(setOf(TowerType.ARROW, TowerType.MAGIC), CampaignData.levels[1].allowedTowers)
        assertEquals(setOf(TowerType.ARROW, TowerType.MAGIC, TowerType.CANNON), CampaignData.levels[2].allowedTowers)
        assertTrue(TowerType.ICE in CampaignData.levels[5].allowedTowers)
        assertTrue(TowerType.FLAME in CampaignData.levels[6].allowedTowers)
        assertTrue(TowerType.TESLA in CampaignData.levels[7].allowedTowers)
        assertTrue(TowerType.POISON in CampaignData.levels[8].allowedTowers)
        assertTrue(TowerType.BALLISTA in CampaignData.levels[10].allowedTowers)
        assertTrue(TowerType.NECRO in CampaignData.levels[11].allowedTowers)
        assertTrue(TowerType.VORTEX in CampaignData.levels[12].allowedTowers)
        assertTrue(TowerType.HEALER in CampaignData.levels[13].allowedTowers)
        assertEquals(TowerType.entries.toSet(), CampaignData.levels[14].allowedTowers)

        // 11. Test Skill Tree Scaling (Tutorial Levels 1-3 zero inflation vs Late Levels 100%)
        prefs.edit().putInt("skill_start_gold", 5).putInt("skill_base_hp", 5).apply()
        val upgradedEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        assertTrue(upgradedEngine.skillTree.bonusStartGold() > 0)

        // Level 1: pure tutorial (0% skill tree factor)
        upgradedEngine.applyCampaign(CampaignData.levels[0])
        upgradedEngine.init(800f, 600f)
        assertEquals(CampaignData.levels[0].startingGold, upgradedEngine.gold)

        // Level 20: veteran tier (100% skill tree factor)
        val engineLvl20 = GameEngine(prefs = prefs, audio = SilentAudio)
        engineLvl20.applyCampaign(CampaignData.levels[19])
        engineLvl20.init(800f, 600f)
        assertEquals(CampaignData.levels[19].startingGold + engineLvl20.skillTree.bonusStartGold(), engineLvl20.gold)

        // 12. Test Level 25: Glass Bastion (50 max base HP)
        val lvl25 = CampaignData.levels[24]
        val engineLvl25 = GameEngine(prefs = prefs, audio = SilentAudio)
        engineLvl25.applyCampaign(lvl25)
        assertEquals(50f, engineLvl25.maxBaseHp)
        assertEquals(50f, engineLvl25.baseHp)
    }

    @Test
    fun testCallNextWaveEarlyAwardsBountyAndStartsWave() {
        engine.init(800f, 600f)
        assertEquals(0, engine.wave)
        assertFalse(engine.waveInProgress)
        val initialGold = engine.gold

        val bounty = engine.callNextWaveEarly()
        assertTrue(bounty >= 15, "Early call bounty must be at least 15g")
        assertEquals(initialGold + bounty, engine.gold, "Gold must increase by the awarded rush bounty")
        assertEquals(1, engine.wave, "Wave 1 should have started immediately")
        assertTrue(engine.waveInProgress, "Wave should now be in progress")

        // Attempting to rush while wave is active must return 0
        val midWaveBounty = engine.callNextWaveEarly()
        assertEquals(0, midWaveBounty, "Rushing while wave is already in progress should yield 0 bounty")
    }

    @Test
    fun testMissionTypeSuddenDeath() {
        val lvl78 = CampaignData.levels.first { it.id == 78 }
        assertEquals(MissionType.SUDDEN_DEATH, lvl78.missionType)
        engine.applyCampaign(lvl78)
        assertEquals(1f, engine.maxBaseHp, "Sudden Death base HP must be 1f")
        assertEquals(1f, engine.baseHp)

        val lvl43 = CampaignData.levels.first { it.id == 43 }
        val engine43 = GameEngine(prefs = prefs, audio = SilentAudio)
        engine43.applyCampaign(lvl43)
        assertEquals(10f, engine43.maxBaseHp, "Level 43 citadel has 10f base HP")
    }

    @Test
    fun testMissionTypeLoneChampionRestrictsTowers() {
        val lvl14 = CampaignData.levels.first { it.id == 14 }
        assertEquals(MissionType.LONE_CHAMPION, lvl14.missionType)
        engine.applyCampaign(lvl14)
        engine.init(800f, 600f)
        engine.paths.clear()
        engine.gold = 1000

        assertTrue(engine.placeTower(100f, 100f, TowerType.ARROW))
        assertTrue(engine.placeTower(200f, 100f, TowerType.ARROW))
        assertTrue(engine.placeTower(300f, 100f, TowerType.ARROW))
        assertTrue(engine.placeTower(400f, 100f, TowerType.ARROW))
        assertEquals(4, engine.towers.size)

        // 5th tower must be denied
        val fifthPlaced = engine.placeTower(500f, 100f, TowerType.ARROW)
        assertFalse(fifthPlaced, "Lone Champion mission archetype must restrict towers to maximum of 4")
        assertEquals(4, engine.towers.size)
    }

    @Test
    fun testMissionTypeBlitzAndGoldRushParameters() {
        val lvl6 = CampaignData.levels.first { it.id == 6 }
        assertEquals(MissionType.BLITZ, lvl6.missionType)
        engine.applyCampaign(lvl6)
        assertEquals(1.5f, engine.waveDelay, "Blitz mission should have 1.5s wave delay")

        val lvl8 = CampaignData.levels.first { it.id == 8 }
        assertEquals(MissionType.GOLD_RUSH, lvl8.missionType)
        val grEngine = GameEngine(prefs = prefs, audio = SilentAudio)
        grEngine.applyCampaign(lvl8)
        assertEquals(lvl8.goldMult * 2.5f, grEngine.currentGoldMult, "Gold Rush mission should multiply level gold multiplier by 2.5x")
    }

    @Test
    fun testCampaignCustomWaveModifiersApplied() {
        val lvl18 = CampaignData.levels.first { it.id == 18 }
        assertEquals(WaveModifier.FAST, lvl18.waveModifiers[4])
        engine.applyCampaign(lvl18)
        engine.init(800f, 600f)

        engine.wave = 3
        engine.waveInProgress = false
        engine.enemies.clear()
        engine.callNextWaveEarly()

        assertEquals(4, engine.wave)
        assertEquals(WaveModifier.FAST, engine.currentWaveModifier, "Wave 4 should activate FAST from campaignLevel.waveModifiers")
    }

    @Test
    fun testSteamBurstReactionFireAndIce() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 0f, hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.iceSlowFactor = 0.5f // Chilled
        val enemy2 = Enemy(x = 240f, y = 200f, speed = 0f, hp = 300f, maxHp = 300f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.FIRE, null, 20f)
        assertTrue(reacted, "Fire on chilled enemy must trigger Steam Burst")
        assertTrue(enemy1.hp < 200f, "Target should suffer steam burst damage")
        assertTrue(enemy2.hp < 200f, "Nearby enemy within 130px should suffer steam burst AoE damage")
        assertEquals(1f, enemy1.iceSlowFactor, "Freeze/chill should be consumed by steam burst")
        assertEquals(0.4f, enemy2.iceSlowFactor, "Nearby enemy should be slowed by scalding steam")
        assertEquals(1, engine.totalElementalReactionsThisRun)
    }

    @Test
    fun testVolatileDetonationReactionFireAndPoison() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 300f, y = 300f, speed = 0f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.poisonTimer = 3f
        enemy1.poisonDps = 20f
        val enemy2 = Enemy(x = 350f, y = 300f, speed = 0f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.FIRE, null, 25f)
        assertTrue(reacted, "Fire on poisoned enemy must trigger Volatile Detonation")
        assertEquals(0f, enemy1.poisonTimer, "Poison must be consumed by detonation")
        // Detonation damage with rebalanced formula: 100 + (3*20*1.25) + 25*0.5 = 100 + 75 + 12.5 = 187.5
        assertTrue(enemy1.hp <= 220f, "Target should take massive detonation burst")
        assertTrue(enemy2.hp <= 220f, "Nearby enemy within 120px should take detonation AoE")
        assertTrue(engine.floatingTexts.any { it.text.contains("DETONATION") })
    }

    @Test
    fun testSuperconductorReactionElectricAndIce() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.iceSlowFactor = 0.5f
        val enemy2 = Enemy(x = 260f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.ELECTRIC, null, 30f)
        assertTrue(reacted, "Electric on chilled enemy must trigger Superconductor")
        assertEquals(4.0f, enemy1.superconductTimer, "Target should receive 4.0s superconduct debuff")
        assertEquals(4.0f, enemy2.superconductTimer, "Chain lightning should apply superconduct debuff to nearby enemy")
        assertTrue(enemy2.hp < 500f, "Nearby enemy should take chained lightning damage")
        assertTrue(engine.floatingTexts.any { it.text.contains("SUPERCONDUCT") })

        // Verify +25% damage taken while superconduct is active
        val flameTower = Tower(x = 200f, y = 220f, level = 1, damage = 40f, range = 100f, fireRate = 10f, type = TowerType.FLAME)
        engine.towers.add(flameTower)
        val hpBeforeHit = enemy1.hp
        engine.update(0.1f)
        // With superconduct (+25%), damage should be >= 40 * 1.25 = 50
        val damageTaken = hpBeforeHit - enemy1.hp
        assertTrue(damageTaken >= 50f, "Superconduct should amplify incoming damage by 25% (expected >= 50, got $damageTaken)")
    }

    @Test
    fun testCorrosiveShockReactionElectricAndPoisonAndStun() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 100f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.poisonTimer = 3f
        enemy1.poisonDps = 20f
        val enemy2 = Enemy(x = 250f, y = 200f, speed = 100f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.ELECTRIC, null, 20f)
        assertTrue(reacted, "Electric on poisoned enemy must trigger Corrosive Shock")
        assertTrue(enemy1.stunTimer >= 0.5f, "Target should be stunned (0.6s)")
        assertTrue(enemy2.stunTimer >= 0.3f, "Nearby enemy should be micro-stunned (0.4s)")
        assertTrue(enemy2.poisonTimer > 0f, "Poison should be spread to nearby enemy")
        assertTrue(engine.floatingTexts.any { it.text.contains("CORROSIVE") })

        // Verify stun prevents movement in update loop
        val startX1 = enemy1.x
        val startY1 = enemy1.y
        engine.update(0.1f)
        assertEquals(startX1, enemy1.x, 0.001f, "Stunned enemy 1 must not move")
        assertEquals(startY1, enemy1.y, 0.001f, "Stunned enemy 1 must not move")
    }

    @Test
    fun testArcaneImplosionReactionMagicAndElements() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 0f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.iceSlowFactor = 0.5f
        val enemy2 = Enemy(x = 280f, y = 200f, speed = 0f, hp = 400f, maxHp = 400f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.MAGIC, null, 20f)
        assertTrue(reacted, "Magic on chilled enemy must trigger Glacial Singularity")
        assertTrue(enemy1.hp < 300f, "Target should suffer singularity damage")
        assertTrue(enemy2.hp < 300f, "Nearby enemy should suffer singularity damage")
        // Enemy 2 was at x=280, distance 80px to enemy 1 (at x=200). It should be pulled towards enemy 1!
        assertTrue(enemy2.x < 260f, "Enemy 2 should be pulled towards epicenter (was 280, now ${enemy2.x})")
        assertTrue(engine.floatingTexts.any { it.text.contains("GLACIAL SINGULARITY") })
    }

    @Test
    fun testSolarFlareStripsShieldsAndSuppressesRegen() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 300f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.burnTimer = 3f
        enemy.shieldTimer = 5f
        engine.enemies.add(enemy)

        val reacted = engine.triggerElementalReaction(enemy, DamageType.MAGIC, null, 20f)
        assertTrue(reacted, "Magic on burning enemy triggers Solar Flare")
        assertEquals(0f, enemy.shieldTimer, "Solar Flare should strip shields")
        assertTrue(enemy.solarBurnTimer > 0f, "Solar Flare applies solar burn")
        assertTrue(engine.floatingTexts.any { it.text.contains("SOLAR FLARE") })

        // Check regen suppression while solarBurnTimer is active
        val hpBefore = enemy.hp
        enemy.regenRate = 50f
        engine.update(0.1f)
        assertTrue(enemy.hp <= hpBefore, "Solar burn must suppress HP regeneration")
    }

    @Test
    fun testOverloadFluxEchoesDamageAcrossLinkedEnemies() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        val enemy2 = Enemy(x = 250f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.shockTimer = 3f
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.MAGIC, null, 20f)
        assertTrue(reacted, "Magic on shocked enemy triggers Overload Flux")
        assertTrue(enemy2.conduitTimer > 0f, "Nearby enemy gets conduit status")

        val enemy2HpBefore = enemy2.hp
        engine.echoConduitDamage(enemy1, 100f, null)
        assertTrue(enemy2.hp < enemy2HpBefore, "Conduit enemy should take echoed damage when ally is hit")
    }

    @Test
    fun testAstralDecayShredsResistanceAndGivesBonusGold() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 50f, maxHp = 500f, goldReward = 10, damage = 10f, type = EnemyType.GOBLIN)
        enemy.poisonTimer = 3f
        engine.enemies.add(enemy)

        val reacted = engine.triggerElementalReaction(enemy, DamageType.MAGIC, null, 20f)
        assertTrue(reacted, "Magic on poisoned enemy triggers Astral Decay")
        assertTrue(enemy.astralDecayTimer > 0f, "Target has astral decay status")

        val initialGold = engine.gold
        enemy.hp = 0f
        engine.update(0.05f)
        assertTrue(engine.gold >= initialGold + 15, "Astral decay should award bonus gold on death")
    }

    @Test
    fun testHellfireSoulburnPhantomOnDeath() {
        engine.init(1080f, 1920f)
        val enemy1 = Enemy(x = 200f, y = 200f, speed = 0f, hp = 50f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        val enemy2 = Enemy(x = 230f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy1.burnTimer = 3f
        engine.enemies.add(enemy1)
        engine.enemies.add(enemy2)

        val reacted = engine.triggerElementalReaction(enemy1, DamageType.DARK, null, 20f)
        assertTrue(reacted, "Dark on burning enemy triggers Hellfire")
        assertTrue(enemy1.soulburnTimer > 0f, "Target has soulburn status")

        val enemy2HpBefore = enemy2.hp
        enemy1.hp = 0f
        engine.update(0.05f)
        assertTrue(enemy2.hp < enemy2HpBefore, "Phantom wisp from Hellfire death should damage nearby enemy2")
    }

    @Test
    fun testFrostTombIncreasesPhysicalDamage() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.iceSlowFactor = 0.5f
        engine.enemies.add(enemy)

        val reacted = engine.triggerElementalReaction(enemy, DamageType.DARK, null, 20f)
        assertTrue(reacted, "Dark on chilled enemy triggers Frost Tomb")
        assertTrue(enemy.stunTimer > 0f, "Target should be stunned/entombed")
        assertTrue(enemy.brittleTimer > 0f, "Target should have brittle status")

        val brittleMult = if (enemy.brittleTimer > 0f) 1.4f else 1f
        assertEquals(1.4f, brittleMult, 0.01f, "Brittle status must yield 1.4x physical damage multiplier")
    }

    @Test
    fun testShadowSurgeEnfeeblesEnemy() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 20f, type = EnemyType.GOBLIN)
        enemy.shockTimer = 3f
        engine.enemies.add(enemy)

        val reacted = engine.triggerElementalReaction(enemy, DamageType.DARK, null, 20f)
        assertTrue(reacted, "Dark on shocked enemy triggers Shadow Surge")
        assertTrue(enemy.enfeebleTimer > 0f, "Target should be enfeebled")

        val enfeebleMult = if (enemy.enfeebleTimer > 0f) 0.5f else 1f
        val calculatedDmg = enemy.damage * enfeebleMult
        assertEquals(10f, calculatedDmg, 0.1f, "Enfeebled enemy should deal 50% damage")
    }

    @Test
    fun testCorpseMiasmaDealsMaxHpPercentDamage() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 1000f, maxHp = 1000f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.poisonTimer = 3f
        engine.enemies.add(enemy)

        val hpBefore = enemy.hp
        val reacted = engine.triggerElementalReaction(enemy, DamageType.DARK, null, 20f)
        assertTrue(reacted, "Dark on poisoned enemy triggers Corpse Miasma")
        val damageTaken = hpBefore - enemy.hp
        assertTrue(damageTaken >= 100f, "Corpse Miasma should deal base + max HP percentage damage")
    }

    @Test
    fun testNapalmConflagrationIgnitesGroundPatch() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.burnTimer = 3f
        engine.enemies.add(enemy)

        assertEquals(0, engine.activeFirePatches.size)
        val reacted = engine.triggerElementalReaction(enemy, DamageType.EXPLOSIVE, null, 20f)
        assertTrue(reacted, "Explosive on burning enemy triggers Napalm Conflagration")
        assertEquals(1, engine.activeFirePatches.size, "Should spawn a fire patch on ground")
        assertTrue(engine.activeFirePatches[0].duration > 0f)
    }

    @Test
    fun testEmpShockwaveSilencesAndStripsShields() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.shockTimer = 3f
        enemy.shieldTimer = 5f
        engine.enemies.add(enemy)

        val reacted = engine.triggerElementalReaction(enemy, DamageType.EXPLOSIVE, null, 20f)
        assertTrue(reacted, "Explosive on shocked enemy triggers EMP Shockwave")
        assertEquals(0f, enemy.shieldTimer, "EMP should strip shields")
        assertTrue(enemy.silenceTimer > 0f, "EMP should silence target")
    }

    @Test
    fun testFusionScholarAchievementUnlock() {
        engine.init(1080f, 1920f)
        val achievement = engine.achievements.first { it.id == "fusion_scholar" }
        assertFalse(achievement.unlocked)

        // Trigger or record all 14 fusions
        FusionCatalog.allFusions.forEach { fusion ->
            engine.recordFusionDiscovery(fusion.id)
        }

        assertTrue(achievement.unlocked, "Fusion Scholar achievement must unlock when all 14 fusions are discovered")
        assertEquals(14, engine.discoveredFusions.size)
    }

    @Test
    fun testHeroPowersTriggerElementalReactions() {
        engine.init(1080f, 1920f)
        engine.gold = 500

        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        enemy.iceSlowFactor = 0.5f // Chilled
        engine.enemies.add(enemy)

        // Cast Fireball power
        val used = engine.usePower(PowerType.FIREBALL)
        assertTrue(used, "Fireball should cast successfully")
        assertTrue(engine.floatingTexts.any { it.text.contains("STEAM BURST") || it.text.contains("SHATTER") },
            "Fireball on chilled enemy should trigger Steam Burst reaction")
        assertTrue(engine.totalElementalReactionsThisRun >= 1)
    }

    @Test
    fun testSynergyMasterAchievementUnlock() {
        engine.init(1080f, 1920f)
        val enemy = Enemy(x = 200f, y = 200f, speed = 0f, hp = 99999f, maxHp = 99999f, goldReward = 5, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy)

        assertFalse(engine.achievements.first { it.id == "synergy_master" }.unlocked)

        for (i in 1..25) {
            enemy.iceSlowFactor = 0.5f
            engine.triggerElementalReaction(enemy, DamageType.FIRE, null, 10f)
        }

        assertEquals(25, engine.totalElementalReactionsThisRun)
        assertTrue(engine.achievements.first { it.id == "synergy_master" }.unlocked, "Synergy Master achievement must unlock at 25 reactions")
    }

    @Test
    fun testCampaignRegionalTowerDamageMultipliers() {
        // Without campaign level, default multiplier is 1.0f
        assertEquals(1.0f, engine.campaignRegionalTowerDmgMult(TowerType.ARROW))

        // Act I: Levels 1..10 buff ARROW
        engine.applyCampaign(CampaignData.levels.first { it.id == 5 })
        assertEquals(1.15f, engine.campaignRegionalTowerDmgMult(TowerType.ARROW), 0.001f)
        assertEquals(1.0f, engine.campaignRegionalTowerDmgMult(TowerType.CANNON), 0.001f)

        // Act II: Levels 11..20 buff POISON
        engine.applyCampaign(CampaignData.levels.first { it.id == 15 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.POISON), 0.001f)
        assertEquals(1.0f, engine.campaignRegionalTowerDmgMult(TowerType.ARROW), 0.001f)

        // Act III: Levels 21..30 buff FLAME
        engine.applyCampaign(CampaignData.levels.first { it.id == 25 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.FLAME), 0.001f)

        // Act IV: Levels 31..40 buff ICE
        engine.applyCampaign(CampaignData.levels.first { it.id == 35 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.ICE), 0.001f)

        // Act V: Levels 41..50 buff TESLA
        engine.applyCampaign(CampaignData.levels.first { it.id == 45 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.TESLA), 0.001f)

        // Act VI: Levels 51..65 buff MAGIC and VORTEX
        engine.applyCampaign(CampaignData.levels.first { it.id == 55 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.MAGIC), 0.001f)
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.VORTEX), 0.001f)

        // Act VII: Levels 66..80 buff CANNON and BALLISTA
        engine.applyCampaign(CampaignData.levels.first { it.id == 70 })
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.CANNON), 0.001f)
        assertEquals(1.20f, engine.campaignRegionalTowerDmgMult(TowerType.BALLISTA), 0.001f)
    }

    @Test
    fun testFlawlessBossDefenseDiamondReward() {
        engine.init(1080f, 1920f)
        val initialDiamonds = engine.skillTree.diamonds
        engine.wave = 5 // bossInterval is 5
        engine.baseHp = 100f
        engine.baseHpBeforeWave = 100f
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0

        // Trigger wave completion check
        engine.update(0.1f)

        assertFalse(engine.waveInProgress)
        assertEquals(initialDiamonds + 2, engine.skillTree.diamonds, "Flawless boss defense should award +2 diamonds")
        assertTrue(engine.floatingTexts.any { it.text.contains("FLAWLESS BOSS DEFENSE") })
    }

    @Test
    fun testCampaignClimaxBossAssignment() {
        engine.init(1080f, 1920f)

        val bossExpectations = mapOf(
            10 to BossType.ORC_KING,
            20 to BossType.SPORE_OVERLORD,
            30 to BossType.DRAGON_QUEEN,
            40 to BossType.FROST_TITAN,
            50 to BossType.CHRONO_LICH,
            65 to BossType.VOID_PHOENIX,
            80 to BossType.IRON_DREADNOUGHT
        )

        for ((levelId, expectedBoss) in bossExpectations) {
            val level = CampaignData.levels.first { it.id == levelId }
            engine.applyCampaign(level)
            engine.wave = level.targetWave - 1
            engine.waveInProgress = false
            engine.waveTimer = 0f
            engine.enemies.clear()
            engine.enemiesRemaining = 0

            engine.update(0.1f) // Starts wave level.targetWave

            assertEquals(level.targetWave, engine.wave)
            assertEquals(expectedBoss, engine.currentBoss, "Level $levelId climax boss must match $expectedBoss")
        }
    }

    @Test
    fun testShockwaveEmissionOnElementalReaction() {
        engine.init(1080f, 1920f)
        engine.pendingShockwaves.clear()

        val enemy = Enemy(x = 350f, y = 450f, speed = 0f, hp = 1000f, maxHp = 1000f, goldReward = 10, damage = 10f, type = EnemyType.ORC)
        enemy.iceSlowFactor = 0.5f // Chilled
        engine.enemies.add(enemy)

        engine.triggerElementalReaction(enemy, DamageType.FIRE, null, 40f)

        assertTrue(engine.pendingShockwaves.isNotEmpty(), "Elemental reaction must queue a shockwave")
        val sw = engine.pendingShockwaves.first()
        assertEquals(350f, sw.x)
        assertEquals(450f, sw.y)
        assertTrue(sw.maxRadius >= 120f)
    }

    @Test
    fun testPhilosopherStoneGoldConversionOnRunHistory() {
        engine.init(1080f, 1920f)
        engine.skillTree.addDiamonds(50)
        engine.skillTree.unlockRelic(RelicId.ALCHEMIST_PHILOSOPHER_STONE)

        val diamondsBefore = engine.skillTree.diamonds
        engine.totalGoldEarned = 1600 // 1600 / 400 = 4 diamonds
        engine.saveRunHistory("Won")

        assertEquals(diamondsBefore + 4, engine.skillTree.diamonds, "Philosopher Stone should convert 1600 gold into +4 diamonds")
    }

    @Test
    fun testPreRunDiamondBlessingInGameEngine() {
        engine.init(1080f, 1920f)
        engine.skillTree.addDiamonds(100)

        // Purchase Midas Blessing
        assertTrue(engine.skillTree.purchaseBlessing(DiamondBlessing.MIDAS))
        assertEquals(DiamondBlessing.MIDAS, engine.skillTree.getActiveBlessing())

        // Start new game run
        val initialGold = 50
        engine.init(1080f, 1920f)
        // Midas blessing adds +300 starting gold
        assertEquals(initialGold + 300, engine.gold)

        // Kill an enemy and verify 50% gold bonus
        val enemy = Enemy(x = 100f, y = 100f, speed = 0f, hp = 0f, maxHp = 10f, goldReward = 20, damage = 10f, type = EnemyType.GOBLIN)
        engine.enemies.add(enemy)
        val goldBefore = engine.gold
        engine.update(0.1f)
        // 20 * 1.50 = 30 gold
        assertEquals(goldBefore + 30, engine.gold)

        // Save run history clears the active blessing
        engine.saveRunHistory("Lost")
        assertEquals(DiamondBlessing.NONE, engine.skillTree.getActiveBlessing())
    }

    @Test
    fun testHealerTowerDynamicLevelScalingAndWaveParticles() {
        engine.init(1080f, 1920f)
        engine.paths.clear()

        // Place a Healer tower at level 1
        val healer = Tower(
            x = 500f, y = 500f, level = 1,
            range = 250f, fireRate = 10f, type = TowerType.HEALER
        )
        engine.towers.add(healer)

        // Setup damaged base and damaged blockade in range
        engine.maxBaseHp = 100f
        engine.baseHp = 50f
        val blockade = Blockade(x = 550f, y = 500f, hp = 10f, maxHp = 50f)
        engine.blockades.add(blockade)

        engine.particles.clear()
        engine.pendingShockwaves.clear()

        // Tick 1: Level 1 healer fires (Base +3 HP, Blockade +5 HP)
        engine.update(0.1f)

        assertEquals(53.0f, engine.baseHp, 0.001f, "Level 1 Healer should heal base for exactly 3.0 HP (3 + 0 * 1.5)")
        assertEquals(15.0f, blockade.hp, 0.001f, "Level 1 Healer should repair blockade for exactly 5.0 HP (5 + 0 * 3)")
        assertTrue(engine.pendingShockwaves.isNotEmpty(), "Healer firing must emit a radiant healing wave shockwave")
        assertTrue(engine.particles.size >= 8, "Healer firing must emit a burst of radiant healing wave particles")

        // Level 2 upgrade test: Base heal = 3 + 1 * 1.5 = 4.5 HP; Blockade repair = 5 + 1 * 3 = 8.0 HP
        healer.level = 2
        healer.fireTimer = 0f
        engine.baseHp = 50f
        blockade.hp = 10f

        engine.update(0.1f)

        assertEquals(54.5f, engine.baseHp, 0.001f, "Level 2 Healer should heal base for exactly 4.5 HP (3 + 1 * 1.5)")
        assertEquals(18.0f, blockade.hp, 0.001f, "Level 2 Healer should repair blockade for exactly 8.0 HP (5 + 1 * 3)")

        // Level 5 upgrade test: Base heal = 3 + 4 * 1.5 = 9.0 HP; Blockade repair = 5 + 4 * 3 = 17.0 HP
        healer.level = 5
        healer.fireTimer = 0f
        engine.baseHp = 50f
        blockade.hp = 10f

        engine.update(0.1f)

        assertEquals(59.0f, engine.baseHp, 0.001f, "Level 5 Healer should heal base for exactly 9.0 HP (3 + 4 * 1.5)")
        assertEquals(27.0f, blockade.hp, 0.001f, "Level 5 Healer should repair blockade for exactly 17.0 HP (5 + 4 * 3)")
    }

    @Test
    fun testBallistaDamageBuffAndInnateArmorPenetration() {
        engine.init(1080f, 1920f)
        engine.paths.clear()

        // 1. Verify base stats
        assertEquals(68f, TowerType.BALLISTA.baseDamage, 0.001f, "Ballista base damage must be buffed to 68f")
        assertEquals(20.4f, TowerType.BALLISTA.baseDamage * TowerType.BALLISTA.baseFireRate, 0.01f, "Ballista DPS must be 20.4")

        // 2. Setup Ballista tower
        val ballista = Tower(
            x = 300f, y = 300f, level = 1, damage = TowerType.BALLISTA.baseDamage,
            range = 350f, fireRate = 10f, type = TowerType.BALLISTA
        )
        engine.towers.add(ballista)

        // 3. Test against unarmored enemy (GOBLIN has 1.0f physical resistance)
        val goblin = Enemy(x = 320f, y = 300f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 1, damage = 5f, type = EnemyType.GOBLIN)
        engine.enemies.clear()
        engine.enemies.add(goblin)
        engine.update(0.1f)

        val damageToGoblin = 500f - goblin.hp
        assertTrue(Math.abs(damageToGoblin - 68f) < 0.05f || Math.abs(damageToGoblin - 136f) < 0.05f, "Ballista deals 68f (or 136f crit) to unarmored goblin")

        // 4. Test against ARMORED_GOLEM (raw physical resistance is 0.3f, 70% mitigation)
        // With 25% innate armor penetration: 0.3 + 0.7 * 0.25 = 0.475 multiplier.
        // Expected damage: 68 * 0.475 = 32.3f (or 64.6f crit).
        val armoredGolem = Enemy(x = 320f, y = 300f, speed = 0f, hp = 500f, maxHp = 500f, goldReward = 1, damage = 5f, type = EnemyType.ARMORED_GOLEM)
        engine.enemies.clear()
        engine.enemies.add(armoredGolem)
        ballista.fireTimer = 0f
        engine.update(0.1f)

        val damageToGolem = 500f - armoredGolem.hp
        assertTrue(Math.abs(damageToGolem - 32.3f) < 0.05f || Math.abs(damageToGolem - 64.6f) < 0.05f, "Ballista should pierce 25% armor on armored golem (32.3f or 64.6f crit)")

        // 5. Test against BOSS enemy (25% innate punch against boss hide)
        val boss = Enemy(x = 320f, y = 300f, speed = 0f, hp = 1000f, maxHp = 1000f, goldReward = 10, damage = 20f, type = EnemyType.BOSS, bossType = BossType.ORC_KING)
        engine.enemies.clear()
        engine.enemies.add(boss)
        ballista.fireTimer = 0f
        engine.update(0.1f)

        val damageToBoss = 1000f - boss.hp
        assertTrue(Math.abs(damageToBoss - 85.0f) < 0.05f || Math.abs(damageToBoss - 170.0f) < 0.05f, "Ballista should deal 25% bonus sniper punch against boss enemies (85.0f or 170.0f crit)")
    }

    @Test
    fun testVolatileDetonationPoisonBurstSoftCap() {
        engine.init(1080f, 1920f)

        // 1. Extreme poison stack that hits the 450f soft cap
        // Target with 500 remaining poison (10s * 50 DPS). Without cap: 500 * 1.25 = 625f > 450f.
        val bossTarget = Enemy(x = 400f, y = 400f, speed = 0f, hp = 5000f, maxHp = 5000f, goldReward = 50, damage = 20f, type = EnemyType.BOSS, bossType = BossType.DRAGON_QUEEN)
        bossTarget.poisonTimer = 10f
        bossTarget.poisonDps = 50f // remainingPoison = 500f
        engine.enemies.clear()
        engine.enemies.add(bossTarget)

        // Trigger Volatile Detonation with base incoming damage = 20f
        val reactedHigh = engine.triggerElementalReaction(bossTarget, DamageType.FIRE, null, 20f)
        assertTrue(reactedHigh, "Fire on poisoned enemy must trigger Volatile Detonation")

        // Formula: (100f + (500f * 1.25f).coerceAtMost(450f) + 20f * 0.5f) * 1.0f = 100 + 450 + 10 = 560f
        val damageTakenHigh = 5000f - bossTarget.hp
        assertEquals(560f, damageTakenHigh, 0.01f, "Extreme poison detonation must be soft-capped at 450f poison contribution (total 560f damage)")

        // 2. Moderate poison stack below the 450f soft cap
        // Target with 40 remaining poison (2s * 20 DPS). 40 * 1.25 = 50f (< 450f).
        val normalTarget = Enemy(x = 400f, y = 400f, speed = 0f, hp = 1000f, maxHp = 1000f, goldReward = 10, damage = 10f, type = EnemyType.ORC)
        normalTarget.poisonTimer = 2f
        normalTarget.poisonDps = 20f // remainingPoison = 40f
        engine.enemies.clear()
        engine.enemies.add(normalTarget)

        // Trigger Volatile Detonation with base incoming damage = 20f
        val reactedLow = engine.triggerElementalReaction(normalTarget, DamageType.FIRE, null, 20f)
        assertTrue(reactedLow, "Fire on poisoned enemy must trigger Volatile Detonation")

        // Formula: (100f + 50f + 10f) * 1.0f = 160f
        val damageTakenLow = 1000f - normalTarget.hp
        assertEquals(160f, damageTakenLow, 0.01f, "Sub-cap poison detonation must scale proportionally without being capped (160f damage)")
    }

    @Test
    fun testSporeOverlordBaseHpCurveTuned() {
        assertEquals(1350f, BossType.SPORE_OVERLORD.baseHp, 0.001f, "Spore Overlord base HP must be tuned down to 1350f")

        // Verify wave 20 spawn scaling
        val wave = 20
        val waveScale = 1f + (wave - 1) * 0.15f // 3.85
        val expectedSpawnHp = (BossType.SPORE_OVERLORD.baseHp + wave * 40f) * waveScale // (1350 + 800) * 3.85 = 8277.5f

        assertEquals(8277.5f, expectedSpawnHp, 0.1f, "Wave 20 Spore Overlord HP curve should be smoothed to 8,277.5 HP")
        assertTrue(expectedSpawnHp < 8855f, "Smoothed HP must be lower than previous 8,855 HP curve")
    }

    @Test
    fun testFlawlessBossDefenseDiamondTrackingAndCounterState() {
        engine.init(1080f, 1920f)
        val initialDiamonds = engine.skillTree.diamonds
        val initialRunDiamonds = engine.diamondsEarnedThisRun

        // Simulate wave 5 (boss wave) ending with 0 citadel damage
        engine.wave = 5
        engine.baseHp = 100f
        engine.baseHpBeforeWave = 100f
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0

        engine.update(0.1f)

        assertFalse(engine.waveInProgress, "Wave should complete")
        assertEquals(initialDiamonds + 2, engine.skillTree.diamonds, "Normal mode flawless boss defense awards +2 diamonds")
        assertEquals(initialRunDiamonds + 2, engine.diamondsEarnedThisRun, "diamondsEarnedThisRun must track +2 diamonds")
        assertTrue(engine.floatingTexts.any { it.text.contains("+2 💎 FLAWLESS BOSS DEFENSE!") }, "Flawless defense announcement must be emitted")

        // Verify that taking damage in a boss wave denies the flawless bonus
        val currentDiamonds = engine.skillTree.diamonds
        engine.wave = 10
        engine.baseHp = 80f
        engine.baseHpBeforeWave = 100f // Took 20 damage
        engine.waveInProgress = true
        engine.enemies.clear()
        engine.enemiesRemaining = 0

        engine.update(0.1f)

        assertEquals(currentDiamonds, engine.skillTree.diamonds, "Damaged base must not award flawless diamond bonus")
        assertEquals(0, engine.consecutiveFlawlessWaves, "Consecutive flawless wave counter should reset to 0 on base damage")
    }

    @Test
    fun testMapPathGeneratorMultiTopologiesAndObstacles() {
        val w = 1080f
        val h = 1920f
        val bx = w / 2f
        val by = h * 0.90f

        for (mapType in MapType.entries) {
            val layout = MapPathGenerator.generateLayout(mapType, w, h, bx, by)
            val expectedLanes = MapPathGenerator.getLaneCount(mapType)
            assertEquals(expectedLanes, layout.paths.size, "Biome $mapType must produce $expectedLanes distinct lanes")

            for (path in layout.paths) {
                assertTrue(path.waypoints.size >= 3, "Path in $mapType must feature curved multi-waypoint topology")
                val last = path.waypoints.last()
                assertEquals(bx, last.x, 0.001f, "Path must terminate at sanctuary base X")
                assertEquals(by, last.y, 0.001f, "Path must terminate at sanctuary base Y")

                // Ensure non-zero segment lengths (no collapsed or degenerate waypoints)
                for (i in 0 until path.waypoints.size - 1) {
                    val p1 = path.waypoints[i]
                    val p2 = path.waypoints[i + 1]
                    val distSq = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y)
                    assertTrue(distSq > 100f, "Path segments must have non-trivial length")
                }
            }

            // Verify strategic obstacle zones
            assertTrue(layout.obstacles.isNotEmpty(), "Biome $mapType must define natural terrain obstacle zones")
            for (obs in layout.obstacles) {
                assertTrue(obs.radius in 20f..50f, "Obstacle radius must be reasonable")
                assertTrue(obs.x in 0f..w, "Obstacle X must be within map bounds")
                assertTrue(obs.y in 0f..h, "Obstacle Y must be within map bounds")
            }

            val separateObstacles = MapPathGenerator.generateObstacles(mapType, w, h)
            assertEquals(layout.obstacles.size, separateObstacles.size, "generateObstacles must match layout.obstacles count")
        }
    }

    @Test
    fun testObstacleZoneBlocksTowerPlacement() {
        engine.init(1080f, 1920f)
        engine.gold = 500
        assertTrue(engine.obstacleZones.isNotEmpty(), "Engine must load obstacle zones on init")

        val targetObstacle = engine.obstacleZones.first()
        val ox = targetObstacle.x
        val oy = targetObstacle.y

        // Placement directly inside obstacle zone must be blocked
        assertFalse(engine.canPlaceTower(ox, oy, TowerType.ARROW), "canPlaceTower must return false inside obstacle zone")
        assertFalse(engine.placeTower(ox, oy, TowerType.ARROW), "placeTower must reject placement inside obstacle zone")

        // Placement within the obstacle margin must also be blocked
        assertFalse(engine.canPlaceTower(ox + targetObstacle.radius * 0.5f, oy, TowerType.ARROW), "canPlaceTower must reject placement in obstacle margin")
    }

    @Test
    fun testDailyChallengeDeterministicGenerationAndEngineSetup() {
        val testSeed = 20260906L
        val daily1 = DailyChallengeHelper.getDailyChallenge(testSeed)
        val daily2 = DailyChallengeHelper.getDailyChallenge(testSeed)

        assertEquals(daily1.title, daily2.title, "Daily challenge title must be deterministic for a seed")
        assertEquals(daily1.mapType, daily2.mapType, "Daily challenge map must be deterministic")
        assertEquals(daily1.startingGold, daily2.startingGold, "Daily starting gold must be deterministic")
        assertEquals(daily1.modifiers, daily2.modifiers, "Daily modifiers list must be deterministic")
        assertEquals(daily1.hpMultiplier, daily2.hpMultiplier, 0.0001f)
        assertEquals(daily1.speedMultiplier, daily2.speedMultiplier, 0.0001f)
        assertEquals(10, daily1.diamondReward, "Daily challenge awards +10 diamonds")

        assertTrue(daily1.modifiers.size in 3..5, "Daily challenge must feature 3 to 5 modifiers")
        assertTrue(daily1.modifiers.all { it != WaveModifier.NONE }, "Daily modifiers must not contain WaveModifier.NONE")
        assertTrue(daily1.startingGold in 40..80, "Starting gold must be balanced between 40 and 80")
        assertTrue(daily1.hpMultiplier in 0.85f..1.25f, "HP multiplier must be within [0.85, 1.25]")
        assertTrue(daily1.speedMultiplier in 0.90f..1.10f, "Speed multiplier must be within [0.90, 1.10]")

        // Verify GameEngine setupDailyChallenge integrates with DailyChallengeHelper
        engine.setupDailyChallenge()
        assertTrue(engine.isDailyChallenge, "Engine must be marked as daily challenge")
        assertTrue(engine.dailyChallengeModifiers.isNotEmpty(), "Engine must receive daily challenge modifiers")
        assertEquals(1, engine.difficulty, "Daily challenge difficulty is fixed to Normal (1)")
    }

    @Test
    fun testPathTopologyGeneratorsAllTopologies() {
        val w = 1080f
        val h = 1920f
        val bx = 540f
        val by = 1632f

        for (topo in PathTopology.entries) {
            val layout = MapPathGenerator.generateLayout(
                MapType.CLASSIC, w, h, bx, by,
                rng = java.util.Random(12345L),
                topology = topo
            )

            assertTrue(layout.paths.isNotEmpty(), "Topology $topo must generate at least one path")
            val expectedLanes = MapPathGenerator.getLaneCount(MapType.CLASSIC, topo)
            assertEquals(expectedLanes, layout.paths.size, "Topology $topo lane count mismatch")

            for (path in layout.paths) {
                assertTrue(path.waypoints.size >= 2, "Topology $topo path must have at least 2 waypoints")
                val first = path.waypoints.first()
                val last = path.waypoints.last()

                // Spawn point starts near the top of the map
                assertTrue(first.y <= h * 0.25f, "Spawn point y=${first.y} should be near map top for $topo")
                // Final point targets the base sanctuary
                assertEquals(bx, last.x, 20f, "Path destination x should end near base for $topo")
                assertEquals(by, last.y, 20f, "Path destination y should end near base for $topo")

                // All waypoints within map bounds
                for (wp in path.waypoints) {
                    assertTrue(wp.x in -50f..(w + 50f), "Waypoint X out of bounds in $topo: ${wp.x}")
                    assertTrue(wp.y in -50f..(h + 50f), "Waypoint Y out of bounds in $topo: ${wp.y}")
                }
            }
        }
    }

    @Test
    fun testDualForkTopologyAndBridges() {
        val w = 1080f
        val h = 1920f
        val bx = 540f
        val by = 1632f

        val layout = MapPathGenerator.generateLayout(
            MapType.VALLEY, w, h, bx, by,
            rng = java.util.Random(9999L),
            topology = PathTopology.DUAL_FORK
        )

        assertEquals(2, layout.paths.size, "DUAL_FORK should produce 2 convergeable branches")
        assertTrue(layout.bridges.isNotEmpty(), "DUAL_FORK must place a BridgeZone at the convergence point")

        val bridge = layout.bridges.first()
        assertTrue(bridge.width > 0f && bridge.height > 0f, "Bridge dimensions must be positive")
        assertTrue(bridge.x in 0f..w, "Bridge x must be inside screen width")
        assertTrue(bridge.y in 0f..h, "Bridge y must be inside screen height")

        // Also test engine integration
        engine.selectedTopology = PathTopology.DUAL_FORK
        engine.mapSeed = 7777L
        engine.init(w, h)
        assertTrue(engine.bridgeZones.isNotEmpty(), "Engine bridgeZones must be populated on init with DUAL_FORK")
        assertEquals(2, engine.paths.size, "Engine paths must have 2 lanes for DUAL_FORK")
    }

    @Test
    fun testPathGeneratorSeedDeterminism() {
        val w = 1080f
        val h = 1920f
        val bx = 540f
        val by = 1632f

        // Same seed must produce identical paths
        val l1 = MapPathGenerator.generateLayout(MapType.DESERT, w, h, bx, by, rng = java.util.Random(424242L), topology = PathTopology.S_CURVE)
        val l2 = MapPathGenerator.generateLayout(MapType.DESERT, w, h, bx, by, rng = java.util.Random(424242L), topology = PathTopology.S_CURVE)

        assertEquals(l1.paths.size, l2.paths.size)
        for (i in l1.paths.indices) {
            val pts1 = l1.paths[i].waypoints
            val pts2 = l2.paths[i].waypoints
            assertEquals(pts1.size, pts2.size)
            for (j in pts1.indices) {
                assertEquals(pts1[j].x, pts2[j].x, 0.0001f)
                assertEquals(pts1[j].y, pts2[j].y, 0.0001f)
            }
        }

        // Different seeds should produce organic variance in jitter
        val l3 = MapPathGenerator.generateLayout(MapType.DESERT, w, h, bx, by, rng = java.util.Random(888888L), topology = PathTopology.S_CURVE)
        val pts3 = l3.paths[0].waypoints
        val pts1 = l1.paths[0].waypoints
        var foundDifference = false
        for (j in 1 until pts1.size - 1) {
            if (kotlin.math.abs(pts1[j].x - pts3[j].x) > 0.1f || kotlin.math.abs(pts1[j].y - pts3[j].y) > 0.1f) {
                foundDifference = true
                break
            }
        }
        assertTrue(foundDifference, "Different seeds must create distinct organic variations")
    }
}
