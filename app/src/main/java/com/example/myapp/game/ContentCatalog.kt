package com.example.myapp.game

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
)

object ContentCatalog {

    val achievementDefs: List<AchievementDef> = listOf(
        AchievementDef("first_kill", "First Blood", "Kill your first enemy", "\uD83D\uDDE1\uFE0F"),
        AchievementDef("wave_5", "Survivor", "Reach wave 5", "\uD83D\uDEE1\uFE0F"),
        AchievementDef("wave_10", "Veteran", "Reach wave 10", "\u2694\uFE0F"),
        AchievementDef("wave_20", "Legend", "Reach wave 20", "\uD83D\uDC51"),
        AchievementDef("wave_30", "Immortal", "Reach wave 30", "\uD83C\uDFC6"),
        AchievementDef("wave_50", "Mythic", "Reach wave 50", "\uD83C\uDF1F"),
        AchievementDef("kills_50", "Slayer", "Kill 50 enemies", "\uD83D\uDC80"),
        AchievementDef("kills_200", "Destroyer", "Kill 200 enemies", "\uD83D\uDD25"),
        AchievementDef("kills_500", "Annihilator", "Kill 500 enemies", "\uD83D\uDCA5"),
        AchievementDef("combo_10", "Combo King", "Get a 10x combo", "\uD83D\uDD17"),
        AchievementDef("combo_20", "Combo God", "Get a 20x combo", "\u26D3\uFE0F"),
        AchievementDef("boss_kill", "Boss Slayer", "Kill your first boss", "\u2620\uFE0F"),
        AchievementDef("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "\uD83D\uDC09"),
        AchievementDef("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "\uD83D\uDC32"),
        AchievementDef("5_towers", "Architect", "Place 5 towers", "\uD83C\uDFD7\uFE0F"),
        AchievementDef("10_towers", "Fortress", "Place 10 towers", "\uD83C\uDFF0"),
        AchievementDef("all_tower_types", "Arsenal", "Place all tower types", "\uD83C\uDFAF"),
        AchievementDef("use_power", "Sorcerer", "Use a power for the first time", "\u2728"),
        AchievementDef("max_tower", "Master Builder", "Upgrade a tower to level 5", "\u2B06\uFE0F"),
        AchievementDef("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0"),
        AchievementDef("rich_1000", "Millionaire", "Have 1000 gold at once", "\uD83E\uDD11"),
        AchievementDef("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "\uD83C\uDFE6"),
        AchievementDef("score_1000", "Score Chaser", "Reach 1000 score", "\uD83D\uDCCA"),
        AchievementDef("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "\uD83D\uDC8E"),
        AchievementDef("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "\uD83D\uDC8E"),
        AchievementDef("repaired_3", "Mechanic", "Repair the base 3 times in a run", "\uD83D\uDD27"),
        AchievementDef("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "\uD83C\uDF96\uFE0F"),
        AchievementDef("endless_10", "Endurance", "Reach wave 10 in endless mode", "\u267E\uFE0F"),
        AchievementDef("kills_1000", "Genocide", "Kill 1000 enemies in one run", "\uD83D\uDC7B"),
        AchievementDef("wave_100", "Centurion", "Reach wave 100", "\u2694\uFE0F"),
        AchievementDef("no_damage", "Untouchable", "Complete a wave without base taking damage", "\uD83D\uDEE1\uFE0F"),
        AchievementDef("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "\uD83D\uDCA8"),
        AchievementDef("all_powers", "Elementalist", "Use all 4 powers in one run", "\uD83C\uDF0A"),
        AchievementDef("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "\u2764\uFE0F"),
        AchievementDef("campaign_5", "Campaigner", "Complete 5 campaign levels", "\uD83D\uDDFA\uFE0F"),
        AchievementDef("campaign_10", "Strategist", "Complete 10 campaign levels", "\uD83C\uDFC5"),
        AchievementDef("campaign_all", "Conqueror", "Complete all campaign levels", "\uD83D\uDC51"),
        AchievementDef("campaign_no_damage", "Flawless", "Beat a campaign level without base damage", "\uD83D\uDEE1\uFE0F"),
        AchievementDef("campaign_3star", "Perfectionist", "Beat 5 campaign levels at full base HP", "\u2B50")
    )

    fun createAchievements(): MutableList<Achievement> =
        achievementDefs.map { Achievement(it.id, it.title, it.description, it.emoji) }.toMutableList()
}
