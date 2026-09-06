package com.example.myapp

import com.example.myapp.game.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AudioAndDataIntegrityTest {

    @Test
    fun testSoundEffectEnumIntegrity() {
        // Ensure all core sound cues are defined
        val requiredSounds = listOf(
            SfxType.TOWER_PLACE,
            SfxType.TOWER_UPGRADE,
            SfxType.TOWER_SELL,
            SfxType.ENEMY_DIE,
            SfxType.PLAYER_ATTACK,
            SfxType.PLAYER_UPGRADE,
            SfxType.POWER_FIREBALL,
            SfxType.POWER_FREEZE,
            SfxType.ACHIEVEMENT,
            SfxType.UI_CLICK,
            SfxType.WAVE_START,
            SfxType.WEATHER_THUNDER,
            SfxType.WEATHER_BLOOD_MOON,
            SfxType.WEATHER_ECLIPSE,
            SfxType.HERO_SPECIAL,
            SfxType.REWARD_CHEST,
            SfxType.STUN_ZAP
        )

        for (sound in requiredSounds) {
            assertNotNull(sound)
            assertTrue(sound.name.isNotBlank())
        }
    }

    @Test
    fun testTowerSpecializationMetadata() {
        for (spec in TowerSpecialization.entries) {
            assertNotNull(spec.displayName)
            assertNotNull(spec.description)
            if (spec != TowerSpecialization.NONE) {
                assertTrue(spec.displayName.isNotBlank())
                assertTrue(spec.description.isNotBlank())
                assertTrue(spec.emoji.isNotBlank())
            }
        }
    }

    @Test
    fun testWaveModifierDefinitions() {
        for (mod in WaveModifier.entries) {
            assertNotNull(mod.displayName)
            assertNotNull(mod.description)
            if (mod != WaveModifier.NONE) {
                assertTrue(mod.displayName.isNotBlank())
                assertTrue(mod.description.isNotBlank())
            }
        }
    }

    @Test
    fun testEnemyTypeColorAndEmojiIntegrity() {
        for (type in EnemyType.entries) {
            assertTrue(type.emoji.isNotBlank(), "Enemy type ${type.name} should have an emoji")
            assertTrue(type.color != 0, "Enemy type ${type.name} should have a non-zero color")
        }
    }

    @Test
    fun testRelicDefinitionsIntegrity() {
        for (relic in RelicId.entries) {
            assertTrue(relic.title.isNotBlank(), "Relic ${relic.name} should have a title")
            assertTrue(relic.description.isNotBlank(), "Relic ${relic.name} should have a description")
            assertTrue(relic.emoji.isNotBlank(), "Relic ${relic.name} should have an emoji")
            assertTrue(relic.diamondCost > 0, "Relic ${relic.name} should have a diamond cost > 0")
            assertTrue(relic.prefKey.startsWith("relic_unlocked_"), "Relic ${relic.name} should have prefKey")
        }
    }

    private class SimpleMockPrefs : GamePreferences {
        override fun getInt(key: String, default: Int): Int = default
        override fun getLong(key: String, default: Long): Long = default
        override fun getFloat(key: String, default: Float): Float = default
        override fun getBoolean(key: String, default: Boolean): Boolean = default
        override fun getString(key: String, default: String): String = default
        override fun edit(): GamePreferences.Editor = object : GamePreferences.Editor {
            override fun putInt(key: String, value: Int) = this
            override fun putLong(key: String, value: Long) = this
            override fun putFloat(key: String, value: Float) = this
            override fun putBoolean(key: String, value: Boolean) = this
            override fun putString(key: String, value: String) = this
            override fun apply() {}
        }
    }

    @Test
    fun testAchievementDefinitionsIntegrity() {
        val engine = GameEngine(prefs = SimpleMockPrefs(), audio = SilentAudio)
        val stringAchs = GameStrings.achievements()

        // Verify all GameEngine achievements have string counterparts
        val stringAchIds = stringAchs.map { it.id }.toSet()
        for (ach in engine.achievements) {
            assertTrue(stringAchIds.contains(ach.id), "Achievement ${ach.id} must exist in GameStrings")
            assertTrue(ach.diamondReward > 0, "Achievement ${ach.id} must offer a diamond reward > 0")
        }
    }

    @Test
    fun testMapThemeAllBiomes() {
        for (mapType in MapType.entries) {
            val theme = MapTheme.forType(mapType)
            assertEquals(mapType, theme.mapType)
            assertTrue(theme.skyTopDay != 0)
            assertTrue(theme.skyBotDay != 0)
            assertTrue(theme.groundTopDay != 0)
            assertTrue(theme.groundBotDay != 0)
            assertTrue(theme.pathColor != 0)
            assertNotNull(theme.ambientParticleType)
        }
    }

    @Test
    fun testAllSfxGeneratorsProduceValidWaveforms() {
        // Synthesize raw PCM waveforms for every SfxType to ensure non-empty, non-clipping waveforms
        for (sfx in SfxType.entries) {
            val samples = SoundManager.generateSamples(sfx)
            assertNotNull(samples, "SoundManager should generate samples for ${sfx.name}")
            assertTrue(samples.isNotEmpty(), "Sound ${sfx.name} should have non-empty samples")

            var hasSignal = false
            var clippedSamples = 0
            for (s in samples) {
                if (s != 0.toShort()) hasSignal = true
                if (s == Short.MAX_VALUE || s == Short.MIN_VALUE) clippedSamples++
            }
            assertTrue(hasSignal, "Sound ${sfx.name} generated only zeroes")
            val clipRatio = clippedSamples.toDouble() / samples.size
            assertTrue(clipRatio < 0.05, "Sound ${sfx.name} is excessively hard-clipped (${clipRatio * 100}%)")
        }
    }

    @Test
    fun testMusicManagerTrackAndWeatherIntegrity() {
        MusicManager.updateGameState(WeatherEvent.THUNDERSTORM, true, false)
        assertEquals(WeatherEvent.THUNDERSTORM, MusicManager.currentWeather)
        assertTrue(MusicManager.isWaveActive)

        MusicManager.updateGameState(WeatherEvent.BLOOD_MOON, true, true)
        assertEquals(WeatherEvent.BLOOD_MOON, MusicManager.currentWeather)
        assertTrue(MusicManager.isBossPresent)

        MusicManager.updateGameState(WeatherEvent.CLEAR, false, false)
        assertEquals(WeatherEvent.CLEAR, MusicManager.currentWeather)
        assertTrue(!MusicManager.isWaveActive)
    }
}
