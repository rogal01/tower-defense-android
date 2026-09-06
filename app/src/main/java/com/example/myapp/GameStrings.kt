package com.example.myapp

import android.content.Context

/**
 * Provides all user-visible game strings in the current language.
 * Call GameStrings.init(context) once, then use GameStrings.get() everywhere.
 */
object GameStrings {
    private var lang: String = "en"

    fun init(context: Context) {
        lang = LocaleHelper.getLanguage(context)
    }

    val isPl get() = lang == "pl"

    // ─── Main Menu ───
    val welcomeCommander get() = if (isPl) "Witaj, Dowódco!" else "Welcome, Commander!"
    fun highScoreFmt(score: Int, wave: Int) =
        if (isPl) "⭐ Najlepszy wynik: $score  |  Najlepsza fala: $wave"
        else "⭐ High Score: $score  |  Best Wave: $wave"
    fun endlessRecordFmt(wave: Int) =
        if (isPl) "♾️ Rekord nieskończoności: Fala $wave"
        else "♾️ Endless Record: Wave $wave"
    fun bossRushRecordFmt(bosses: Int) =
        if (isPl) "💀 Rekord rajdu bossów: $bosses bossów"
        else "💀 Boss Rush Record: $bosses bosses"
    fun diamondsFmt(d: Int) =
        if (isPl) "💎 $d Diamentów" else "💎 $d Diamonds"

    // Endless popup
    val endlessModeTitle get() = if (isPl) "♾️ Tryb nieskończony" else "♾️ Endless Mode"
    val difficultySection get() = if (isPl) "\n⚔️ Trudność" else "\n⚔️ Difficulty"
    val mapSection get() = if (isPl) "\n🗺️ Mapa" else "\n🗺️ Map"
    val diffEasy get() = if (isPl) "😊 Łatwy" else "😊 Easy"
    val diffNormal get() = if (isPl) "⚔️ Normalny" else "⚔️ Normal"
    val diffHard get() = if (isPl) "💀 Trudny" else "💀 Hard"
    val btnPlay get() = if (isPl) "🎮 Graj" else "🎮 Play"
    val btnCancel get() = if (isPl) "Anuluj" else "Cancel"

    // Settings
    val resetTitle get() = if (isPl) "Resetuj postęp" else "Reset Progress"
    val resetMessage get() = if (isPl) "To usunie wszystkie wyniki i osiągnięcia. Czy jesteś pewien?" else "This will erase all high scores and achievements. Are you sure?"
    val resetBtn get() = if (isPl) "Resetuj" else "Reset"

    // ─── In-Game HUD (GameView) ───
    val victory get() = if (isPl) "⭐ ZWYCIĘSTWO! ⭐" else "⭐ VICTORY! ⭐"
    val gameOver get() = if (isPl) "💀 KONIEC GRY" else "💀 GAME OVER"
    val bossRushOver get() = if (isPl) "💀 RAJD BOSSÓW ZAKOŃCZONY" else "💀 BOSS RUSH OVER"
    val endlessOver get() = if (isPl) "♾️ NIESKOŃCZONOŚĆ ZAKOŃCZONA" else "♾️ ENDLESS OVER"
    val randomizerOver get() = if (isPl) "🎲 LOSOWY ZAKOŃCZONY" else "🎲 RANDOMIZER OVER"
    val tapRestart get() = if (isPl) "Dotknij, aby zrestartować" else "Tap to restart"
    val tapContinue get() = if (isPl) "Dotknij, aby kontynuować" else "Tap to continue"
    val paused get() = if (isPl) "⏸️ PAUZA" else "⏸️ PAUSED"
    val tapResume get() = if (isPl) "Dotknij ▶️ aby wznowić" else "Tap ▶️ to resume"
    val newHighScore get() = if (isPl) "⭐ NOWY REKORD! ⭐" else "⭐ NEW HIGH SCORE! ⭐"
    val newEndlessRecord get() = if (isPl) "⭐ NOWY REKORD NIESKOŃCZONOŚCI! ⭐" else "⭐ NEW ENDLESS RECORD! ⭐"
    val newBossRushRecord get() = if (isPl) "⭐ NOWY REKORD RAJDU BOSSÓW! ⭐" else "⭐ NEW BOSS RUSH RECORD! ⭐"
    val maxStars get() = if (isPl) "Maksimum gwiazdek!" else "Max stars!"
    fun scoreFmt(s: Int) = if (isPl) "Wynik: $s" else "Score: $s"
    fun diamondsEarned(d: Int) = if (isPl) "💎 +$d Diamentów!" else "💎 +$d Diamonds!"
    fun diamondsEarnedLong(d: Int) = if (isPl) "💎 +$d Diamentów zdobytych!" else "💎 +$d Diamonds earned!"

    // Pause overlay
    fun pauseWaveScore(w: Int, s: Int) = if (isPl) "Fala: $w  |  Wynik: $s" else "Wave: $w  |  Score: $s"
    fun pauseKillsGold(k: Int, g: Int) = if (isPl) "Zabicia: $k  |  Złoto: $g" else "Kills: $k  |  Gold: $g"
    fun pauseTowersCombo(t: Int, c: Int) = if (isPl) "Wieże: $t  |  Najlepsze combo: ${c}x" else "Towers: $t  |  Combo Best: ${c}x"
    fun pauseDiamonds(d: Int) = if (isPl) "💎 $d Diamentów" else "💎 $d Diamonds"

    // Game over stats
    fun bossesDefeatedScore(b: Int, s: Int) = if (isPl) "Pokonani bossowie: $b  |  Wynik: $s" else "Bosses defeated: $b  |  Score: $s"
    fun waveScore(w: Int, s: Int) = if (isPl) "Fala: $w  |  Wynik: $s" else "Wave: $w  |  Score: $s"
    fun killsCombo(k: Int, c: Int) = if (isPl) "Zabicia: $k  |  Najlepsze combo: ${c}x" else "Kills: $k  |  Best Combo: ${c}x"
    fun highScoreRecord(hs: Int, hw: Int) = if (isPl) "Najlepszy wynik: $hs  |  Najlepsza fala: $hw" else "High Score: $hs  |  Best Wave: $hw"
    fun endlessRecord(w: Int) = if (isPl) "Rekord nieskończoności: Fala $w" else "Endless Record: Wave $w"
    fun bossRushRecord(b: Int) = if (isPl) "Rekord rajdu bossów: $b bossów" else "Boss Rush Record: $b bosses"

    // Tower stats popup
    val towerDps get() = if (isPl) "OPS" else "DPS"
    val towerKills get() = if (isPl) "Zabicia" else "Kills"
    val towerDamage get() = if (isPl) "Obrażenia" else "Damage"
    val abilityReady get() = if (isPl) "GOTOWA" else "READY"

    // Tower placement
    fun placeTower(emoji: String, name: String) =
        if (isPl) "Dotknij, aby postawić $emoji wieżę" else "Tap to place $emoji tower"
    val placeBlockade get() = if (isPl) "Dotknij ścieżkę, aby postawić 🪨 barykadę" else "Tap path to place 🪨 blockade"

    // Wave preview
    fun enemyCountFmt(count: Int) = if (isPl) "$count wrogów:" else "$count enemies:"
    val bossAbilityMinions get() = if (isPl) "minionki" else "minions"

    // Achievement banner
    val achievementUnlocked get() = if (isPl) "Osiągnięcie odblokowane!" else "Achievement Unlocked!"

    // FPS
    fun fpsFmt(fps: Int) = "$fps FPS"

    // ─── Tutorial ───
    val tutBackBtn get() = if (isPl) "⬅ Wróć" else "⬅ Back"
    val tutNextBtn get() = if (isPl) "Dalej ➡" else "Next ➡"
    val tutGotIt get() = if (isPl) "Rozumiem!" else "Got it!"

    fun tutorialPages(): List<Pair<String, String>> {
        if (!isPl) return listOf(
            "🏰 BASICS" to """
Your goal is to defend your base from waves of enemies.

• Tap or drag on the map to move your hero. The hero automatically attacks nearby enemies.
• Your base has a health bar at the bottom. If it reaches 0, game over!
• Gold 💰 is earned from killing enemies. Use it to build towers, upgrades, and powers.
• Diamonds 💎 are rare currency earned from boss kills and lucky drops. Spend them in the Skill Tree.
• Between waves you earn gold interest (5% of current gold) and a wave completion bonus.
""",
            "🏗️ TOWERS" to """
Place towers by tapping a tower button, then tapping an open area on the field.

🏹 Arrow (30g) — Fast, cheap, physical damage. Ability: Volley (5 rapid shots)
🧨 Magic (60g) — High magic damage, good range. Ability: Arcane Blast (AoE)
💣 Cannon (100g) — Slow but explosive. Ability: Napalm (area burn)
☠️ Poison (80g) — Damage-over-time. Ability: Plague (poisons all enemies)
⚡ Tesla (120g) — Chain lightning. Ability: Overcharge (chains to all)
❄️ Ice (70g) — Slows enemies in range (no projectiles). Ability: Deep Freeze (stun)
🔥 Flame (90g) — Burns enemies over time (fire DoT). Ability: Inferno (mass burn)
💀 Necro (110g) — Dark damage, 3× vs low HP. Ability: Soul Harvest (execute <15%)
🎯 Ballista (140g) — Extreme range sniper. Ability: Siege Shot (massive hit)
🌀 Vortex (100g) — Pulls enemies toward tower. Ability: Singularity (mass pull)

• Tap a placed tower to select it, then use ⬆ TOWER to upgrade or 💸 Sell.
• Use 🎯 to change targeting: Close → First → Last → Strong.
• Same-type towers near each other get a synergy bonus (+10% damage per neighbor, max 30%).
""",
            "⚡ POWERS & CONTROLS" to """
Powers cost gold and have cooldowns.

🔥 Fireball (40g, 8s CD) — Damages ALL enemies on screen.
❄️ Freeze (30g, 12s CD) — Slows all enemies to 20% speed for 4 seconds.
💚 Heal (25g, 15s CD) — Restores 50 HP to your base.
⚡ Lightning (50g, 10s CD) — Chain lightning hits the 5 closest enemies for 80 damage each.

🕹️ Virtual Joystick — Touch and steer on the left side of the screen for fluid 360° hero movement.
""",
            "⬆️ UPGRADES" to """
Hero upgrades (costs scale with level):

⚔️ ATK — +5 hero attack damage per level
👟 SPD — +30 hero movement speed per level
❤️ HP — +25 hero max HP per level
🏰 BASE — +30 base max HP per level

🔧 Repair (20g) — Restores 30 base HP.

Tower upgrades:
Select a tower, then press ⬆ TOWER. Each level costs (level × 50g) and improves damage (+40%), range (+15), and fire rate (+15%).
""",
            "👹 ENEMIES & BOSSES" to """
Enemy types appear as waves progress:
• Goblin (wave 1+) — Weak, common fodder
• Bat (wave 1+) — Fast but fragile
• Slime (wave 1+) — Slow, moderate HP
• Skeleton (wave 2+) — Resists physical, weak to magic
• Spider (wave 2+) — Fast, scatters webs on death
• Orc (wave 3+) — Tanky, slow, resists physical
• Fast Skeleton (wave 4+) — Very fast, low HP
• Demon (wave 5+) — Resists poison, weak to ice
• Dragon (wave 7+) — Resists physical, weak to ice/magic
• Armored Golem (wave 8+) — Heavy armor, weak to explosives/magic

Every 5th wave is a BOSS wave with unique abilities:
• Orc King — Charges at base
• Lich Lord / Spider Queen — Summons minions
• Demon Prince — AoE tower damage
• Dragon Queen — Roar buffs minion speed
• Shadow Wraith — Teleports ahead on path
• Slime King — Splits into clones at low HP
• Vampire Lord — Drains your gold
• Frost Titan — Quake slows towers
• Stone Golem — Temporary shield (70% damage reduction)

👑 Elite enemies (gold crown) appear every 5th non-boss wave from wave 5 onward — 3× HP, 2× gold.
""",
            "🌙 ADVANCED MECHANICS" to """
• Day/Night Cycle: Every 8 waves the cycle switches. Night = enemies +20% HP, towers -10% range.
• Combo System: Kill enemies quickly to build combos. 5+ combo gives bonus gold.
• Critical Hits: 12% chance for towers to deal 2× damage (red CRIT! text).
• Gold Interest: 5% interest on gold between waves — hoarding pays off!
• Wave Modifiers: Some waves have random effects (Fast, Armored, Regen, Swarm, Ghostly, Treasure, Rally).
• Damage Resistances: Each enemy type takes more/less damage from certain tower types. Match your towers to the enemies!
• Skill Tree: Spend diamonds on permanent upgrades that persist across runs.

🎮 Speed: Use the ▶ buttons to play at 2× or 3× speed. Press ⏸ to pause.
""",
            "🗺️ GAME MODES" to """
• Campaign: Story-based levels that introduce mechanics one by one. Beat each level to unlock the next. Earn diamonds and stars based on your score.
• Endless: Infinite waves with scaling difficulty. Choose your difficulty (Easy/Normal/Hard) and map, then test how far you can go. Separate high score tracking.
• Boss Rush: A boss every wave. Fewer minions, more boss fights.
• Daily Challenge: Unique modifiers each day. Same seed for everyone.
• Randomizer: Everything is scrambled! Tower costs, power cooldowns, enemy stats, starting gold — all randomized each run.

🗺️ Maps: Classic, Valley, Crossroads, Desert, Snow, Lava (volcanic), Enchanted (magical forest). Each has unique path layouts and terrain.

🏆 Complete achievements for extra goals, check stats to track your lifetime progress!
""",
            "⚔️ DAMAGE GUIDE" to """
Each tower deals a specific damage type. Enemies have resistances and weaknesses:

🏹 Physical — Good vs: Goblins, Bats, Slimes. Weak vs: Skeletons, Orcs, Golems.
🧨 Magic — Good vs: Skeletons, Golems, Dragons. Weak vs: Demons.
💣 Explosive — Good vs: Golems, Orcs (groups). Weak vs: Bats (too fast).
☠️ Poison — Good vs: Orcs, Slimes (slow). Weak vs: Demons (immune).
⚡ Electric — Good vs: Bats, Spiders (chains). Weak vs: Golems (grounded).
❄️ Ice — Good vs: Demons, Dragons (slows). Weak vs: Golems (no damage).
🔥 Fire — Good vs: Spiders, Slimes (burns). Weak vs: Dragons (fire resist).
💀 Dark — Good vs: Low HP enemies (execute). Weak vs: Bosses (high HP).

💡 Tip: Mix tower types for best coverage! Synergy bonuses help same-type clusters.
"""
        )
        // Polish
        return listOf(
            "🏰 PODSTAWY" to """
Twoim celem jest obrona bazy przed falami wrogów.

• Dotknij lub przeciągnij po mapie, aby poruszyć bohatera. Bohater automatycznie atakuje pobliskich wrogów.
• Twoja baza ma pasek zdrowia na dole. Jeśli spadnie do 0 — koniec gry!
• Złoto 💰 zdobywasz za zabijanie wrogów. Używaj go do budowy wież, ulepszeń i mocy.
• Diamenty 💎 to rzadka waluta z bossów i szczęśliwych dropów. Wydaj je w Drzewku umiejętności.
• Między falami dostajesz odsetki od złota (5%) i bonus za ukończenie fali.
""",
            "🏗️ WIEŻE" to """
Stawiaj wieże dotykając przycisku wieży, a potem wolnego pola.

🏹 Łucznicza (30g) — Szybka, tania, obrażenia fizyczne. Umiejętność: Salwa (5 szybkich strzałów)
🧨 Magiczna (60g) — Wysokie obrażenia magiczne. Umiejętność: Podmuch Arkany (AoE)
💣 Armatnia (100g) — Wolna, ale wybuchowa. Umiejętność: Napalm (obszarowe podpalanie)
☠️ Trująca (80g) — Obrażenia w czasie. Umiejętność: Plaga (zatruwanie wszystkich)
⚡ Tesli (120g) — Łańcuchowe błyskawice. Umiejętność: Przeładowanie (łańcuch na wszystkich)
❄️ Lodowa (70g) — Spowalnia wrogów w zasięgu. Umiejętność: Głębokie zamrożenie (ogłuszenie)
🔥 Płomienna (90g) — Podpala wrogów (DoT). Umiejętność: Inferno (masowe podpalanie)
💀 Nekro (110g) — Ciemne obrażenia, 3× na niskie HP. Umiejętność: Żniwa Dusz (egzekucja <15%)
🎯 Balisty (140g) — Ekstremalny zasięg. Umiejętność: Strzał oblężniczy (potężne trafienie)
🌀 Wir (100g) — Przyciąga wrogów. Umiejętność: Osobliwość (masowe przyciąganie)

• Dotknij postawionej wieży, aby ją wybrać, potem użyj ⬆ WIEŻA do ulepszenia lub 💸 Sprzedaj.
• Użyj 🎯 by zmienić cel: Bliski → Pierwszy → Ostatni → Silny.
• Wieże tego samego typu blisko siebie dostają bonus synergii (+10% obrażeń, max 30%).
""",
            "⚡ MOCE I STEROWANIE" to """
Moce kosztują złoto i mają czas odnowienia.

🔥 Kula ognia (40g, 8s CD) — Zadaje obrażenia WSZYSTKIM wrogom na ekranie.
❄️ Zamrożenie (30g, 12s CD) — Spowalnia wszystkich wrogów do 20% prędkości na 4 sekundy.
💚 Leczenie (25g, 15s CD) — Przywraca 50 HP bazy.
⚡ Błyskawica (50g, 10s CD) — Łańcuchowa błyskawica trafia 5 najbliższych wrogów po 80 obrażeń.

🕹️ Wirtualny Joystick — Dotknij i steruj w lewej połowie ekranu, by płynnie poruszać bohaterem w 360°.
""",
            "⬆️ ULEPSZENIA" to """
Ulepszenia bohatera (koszt rośnie z poziomem):

⚔️ ATK — +5 obrażeń ataku bohatera na poziom
👟 SPD — +30 prędkości ruchu bohatera na poziom
❤️ HP — +25 maks. HP bohatera na poziom
🏰 BAZA — +30 maks. HP bazy na poziom

🔧 Naprawa (20g) — Przywraca 30 HP bazy.

Ulepszenia wież:
Wybierz wieżę, naciśnij ⬆ WIEŻA. Każdy poziom kosztuje (poziom × 50g), poprawia obrażenia (+40%), zasięg (+15) i szybkość strzelania (+15%).
""",
            "👹 WROGOWIE I BOSSOWIE" to """
Typy wrogów pojawiają się z postępem fal:
• Goblin (fala 1+) — Słaby, pospolity
• Nietoperz (fala 1+) — Szybki, ale kruchy
• Śluz (fala 1+) — Wolny, średnie HP
• Szkielet (fala 2+) — Odporny na fizyczne, słaby na magię
• Pająk (fala 2+) — Szybki, sieje pajęczyny po śmierci
• Ork (fala 3+) — Wytrzymały, wolny, odporny na fizyczne
• Szybki Szkielet (fala 4+) — Bardzo szybki, niskie HP
• Demon (fala 5+) — Odporny na truciznę, słaby na lód
• Smok (fala 7+) — Odporny na fizyczne, słaby na lód/magię
• Pancerny Golem (fala 8+) — Ciężki pancerz, słaby na eksplozje/magię

Co 5. fala to fala BOSSA z unikalnymi zdolnościami:
• Król Orków — Szarżuje na bazę
• Władca Liszów / Królowa Pająków — Przyzywa sługi
• Książę Demonów — Obrażenia AoE wieżom
• Królowa Smoków — Ryk przyspiesza sługi
• Widmowy Rycerz — Teleportuje się po ścieżce
• Król Śluzów — Dzieli się na klony przy niskim HP
• Wampirzy Lord — Kradnie twoje złoto
• Lodowy Tytan — Trzęsienie spowalnia wieże
• Kamienny Golem — Tymczasowa tarcza (70% redukcja)

👑 Elitarni wrogowie (złota korona) pojawiają się co 5 fal od fali 5 — 3× HP, 2× złota.
""",
            "🌙 ZAAWANSOWANE MECHANIKI" to """
• Cykl dnia/nocy: Co 8 fal cykl się zmienia. Noc = wrogowie +20% HP, wieże -10% zasięgu.
• System combo: Zabijaj szybko, by budować combo. 5+ combo daje bonus złota.
• Trafienia krytyczne: 12% szans na 2× obrażeń wieży (czerwony tekst KRYT!).
• Odsetki od złota: 5% odsetek między falami — gromadzenie się opłaca!
• Modyfikatory fal: Losowe efekty (Szybkie, Opancerzone, Regenerujące, Rój, Widmowe, Skarbowe, Rajd).
• Odporności na obrażenia: Każdy typ wroga otrzymuje więcej/mniej obrażeń od różnych wież. Dopasuj wieże!
• Drzewko umiejętności: Wydaj diamenty na trwałe ulepszenia działające między grami.

🎮 Prędkość: Użyj przycisków ▶ do gry w 2× lub 3× prędkości. Naciśnij ⏸ aby wstrzymać.
""",
            "🗺️ TRYBY GRY" to """
• Kampania: Poziomy z fabułą, wprowadzające mechaniki. Odblokuj kolejne wygrywając. Zdobywaj diamenty i gwiazdki.
• Nieskończony: Nieskończone fale ze skalującą się trudnością. Wybierz trudność i mapę, sprawdź jak daleko zajdziesz.
• Rajd Bossów: Boss co falę. Mniej sług, więcej walki z bossami.
• Wyzwanie dnia: Unikalne modyfikatory każdego dnia. Ten sam seed dla wszystkich.
• Losowy: Wszystko jest losowe! Koszty wież, czasy odnowienia, statystyki wrogów — losowane każdą grę.

🗺️ Mapy: Klasyczna, Dolina, Skrzyżowanie, Pustynia, Śnieg, Lawa (wulkaniczna), Zaczarowana (magiczny las). Każda ma unikalne ścieżki.

🏆 Wykonuj osiągnięcia, sprawdzaj statystyki, śledź postęp!
""",
            "⚔️ PRZEWODNIK OBRAŻEŃ" to """
Każda wieża zadaje określony typ obrażeń. Wrogowie mają odporności i słabości:

🏹 Fizyczne — Dobre vs: Gobliny, Nietoperze, Szlamy. Słabe vs: Szkielety, Orki, Golemy.
🧨 Magiczne — Dobre vs: Szkielety, Golemy, Smoki. Słabe vs: Demony.
💣 Wybuchowe — Dobre vs: Golemy, Orki (grupy). Słabe vs: Nietoperze (za szybkie).
☠️ Trucizna — Dobre vs: Orki, Szlamy (wolne). Słabe vs: Demony (odporne).
⚡ Elektryczne — Dobre vs: Nietoperze, Pająki (łańcuch). Słabe vs: Golemy (uziemione).
❄️ Lód — Dobre vs: Demony, Smoki (spowalnia). Słabe vs: Golemy (brak obrażeń).
🔥 Ogień — Dobre vs: Pająki, Szlamy (pali). Słabe vs: Smoki (odporność na ogień).
💀 Ciemność — Dobre vs: Wrogowie z niskim HP. Słabe vs: Bossowie (wysokie HP).

💡 Porada: Mieszaj typy wież! Bonusy synergii pomagają grupom tego samego typu.
"""
        )
    }

    // ─── Stats Activity ───
    fun statTotalKills(k: Int) = if (isPl) "🗡️ Zabicia ogółem: $k" else "🗡️ Total Kills: $k"
    fun statBossesDefeated(b: Int) = if (isPl) "☠️ Pokonani bossowie: $b" else "☠️ Bosses Defeated: $b"
    fun statBestCombo(c: Int) = if (isPl) "🔗 Najlepsze combo: ${c}x" else "🔗 Best Combo: ${c}x"
    fun statTowersPlaced(t: Int) = if (isPl) "🏰 Postawione wieże: $t" else "🏰 Towers Placed: $t"
    val statNoFavorite get() = if (isPl) "Brak" else "None yet"
    fun statFavoriteTower(name: String) = if (isPl) "❤️ Ulubiona wieża: $name" else "❤️ Favorite Tower: $name"
    fun statGamesPlayed(g: Int) = if (isPl) "🎮 Rozegranych gier: $g" else "🎮 Games Played: $g"
    fun statTotalWaves(w: Int) = if (isPl) "🌊 Fale ogółem: $w" else "🌊 Total Waves: $w"
    fun statLifetimeScore(s: Int) = if (isPl) "⭐ Wynik łączny: $s" else "⭐ Lifetime Score: $s"
    fun statLifetimeGold(g: Int) = if (isPl) "💰 Złoto łączne: $g" else "💰 Lifetime Gold: $g"
    fun statHighScore(s: Int) = if (isPl) "⭐ Najlepszy wynik: $s" else "⭐ High Score: $s"
    fun statBestWave(w: Int) = if (isPl) "⚔️ Najlepsza fala: $w" else "⚔️ Best Wave: $w"
    fun statEndlessRecord(w: Int) = if (isPl) "♾️ Rekord nieskończoności: Fala $w" else "♾️ Endless Record: Wave $w"
    fun statBossRushRecord(b: Int) = if (isPl) "💀 Rekord rajdu bossów: $b bossów" else "💀 Boss Rush Record: $b bosses"
    fun statDiamonds(d: Int) = if (isPl) "💎 Diamenty: $d" else "💎 Diamonds: $d"

    // ─── Achievements ───
    fun achievementProgress(unlocked: Int, total: Int) =
        if (isPl) "$unlocked / $total Odblokowane" else "$unlocked / $total Unlocked"

    val tabPassiveSkills get() = if (isPl) "🏰 Cytadela" else "🏰 Citadel"
    val tabCitadelPassives get() = if (isPl) "🏰 Cytadela" else "🏰 Citadel"
    val tabElementalAlchemy get() = if (isPl) "⚗️ Alchemia i Runy" else "⚗️ Alchemy & Runes"
    val tabRelicVault get() = if (isPl) "🏺 Skarbiec Relikwii" else "🏺 Relic Vault"
    val relicVaultSubtitle get() = if (isPl) "Potężne, stałe artefakty zmieniające mechaniki gry" else "Powerful permanent artifacts with game-changing powers"
    val alchemySubtitle get() = if (isPl) "Potęga 14 fuzji żywiołów oraz błogosławieństwa przed kolejnym biegiem" else "Harness 14 elemental fusions and pre-run diamond blessings"
    val citadelSubtitle get() = if (isPl) "Pasywne ulepszenia obrony bazy, bohatera i uzbrojenia" else "Permanent defenses, hero prowess, and armory upgrades"
    val blessingSectionTitle get() = if (isPl) "✨ Przedbiegowe Błogosławieństwa Diamentów" else "✨ Pre-Run Diamond Blessings"
    val blessingSectionDesc get() = if (isPl) "Wybierz jednorazową runę na kolejny bieg. Efekt znika po zakończeniu gry." else "Select a one-run blessing for your next battle. Cleared when run ends."
    val blessingActiveBadge get() = if (isPl) "AKTYWNE NA KOLEJNY BIEG ✓" else "ACTIVE FOR NEXT RUN ✓"
    val blessingSelectBtn get() = if (isPl) "AKTYWUJ" else "ACTIVATE"
    val blessingRemoveBtn get() = if (isPl) "USUŃ" else "REMOVE"
    val alchemySkillsHeader get() = if (isPl) "🧪 Mistrzostwo Fuzji Żywiołów" else "🧪 Elemental Fusion Mastery"
    val combatSkillsHeader get() = if (isPl) "🛡️ Fortyfikacje Cytadeli" else "🛡️ Citadel Fortifications"
    val relicActive get() = if (isPl) "AKTYWNY ✓" else "ACTIVE ✓"
    fun relicUnlockBtn(cost: Int) = if (isPl) "ODBLOKUJ \uD83D\uDC8E $cost" else "UNLOCK \uD83D\uDC8E $cost"
    val claimAllBtn get() = if (isPl) "\uD83C\uDF81 Odbierz wszystko" else "\uD83C\uDF81 Claim All"
    fun claimBtn(reward: Int) = if (isPl) "ODBIERZ \uD83D\uDC8E $reward" else "CLAIM \uD83D\uDC8E $reward"
    val claimedBtn get() = if (isPl) "ODEBRANO ✓" else "CLAIMED ✓"
    fun claimedAllToast(count: Int, diamonds: Int) =
        if (isPl) "Odebrano $count nagród! +$diamonds \uD83D\uDC8E" else "Claimed $count rewards! +$diamonds \uD83D\uDC8E"

    fun getLocalizedRelicTitle(relic: com.example.myapp.game.RelicId): String = when (relic) {
        com.example.myapp.game.RelicId.ZEPHYR_GREAVES -> if (isPl) "Nagoleniki Zefiru" else relic.title
        com.example.myapp.game.RelicId.ARTEMIS_QUIVER -> if (isPl) "Kołczan Artemidy" else relic.title
        com.example.myapp.game.RelicId.CHRONO_HOURGLASS -> if (isPl) "Klepsydra Chrono" else relic.title
        com.example.myapp.game.RelicId.AEGIS_OF_DAWN -> if (isPl) "Egida Brzasku" else relic.title
        com.example.myapp.game.RelicId.DEMOLITION_SATCHEL -> if (isPl) "Torba Minerska" else relic.title
        com.example.myapp.game.RelicId.MIDAS_CRUCIBLE -> if (isPl) "Tygiel Midasa" else relic.title
        com.example.myapp.game.RelicId.PRISMATIC_CATALYST -> if (isPl) "Pryzmatyczny Katalizator" else relic.title
        com.example.myapp.game.RelicId.GRIMOIRE_OF_CONDUIT -> if (isPl) "Grimuar Przewodnika" else relic.title
        com.example.myapp.game.RelicId.ASTRAL_HARVESTER -> if (isPl) "Żniwiarz Astralny" else relic.title
        com.example.myapp.game.RelicId.TITAN_WARHORN -> if (isPl) "Róg Wojenny Tytana" else relic.title
        com.example.myapp.game.RelicId.CHRONO_SINGULARITY -> if (isPl) "Osobliwość Chrono" else relic.title
        com.example.myapp.game.RelicId.ALCHEMIST_PHILOSOPHER_STONE -> if (isPl) "Kamień Filozoficzny" else relic.title
    }

    fun getLocalizedRelicDesc(relic: com.example.myapp.game.RelicId): String = when (relic) {
        com.example.myapp.game.RelicId.ZEPHYR_GREAVES -> if (isPl) "+35% prędkości ruchu bohatera. Emituje Aurę Wichru spowalniającą pobliskich wrogów o 25%." else relic.description
        com.example.myapp.game.RelicId.ARTEMIS_QUIVER -> if (isPl) "Ataki bohatera wystrzeliwują potrójną salwę strzał z +15% szansą na trafienie krytyczne." else relic.description
        com.example.myapp.game.RelicId.CHRONO_HOURGLASS -> if (isPl) "-25% czasu odnowienia czarów (Kula Ognia, Lód, Błyskawica). Krytyki wywołują 2s globalne spowolnienie." else relic.description
        com.example.myapp.game.RelicId.AEGIS_OF_DAWN -> if (isPl) "Baza otrzymuje 100 HP boskiej tarczy co falę. Aura bohatera regeneruje bazę o 3 HP co 5s." else relic.description
        com.example.myapp.game.RelicId.DEMOLITION_SATCHEL -> if (isPl) "Wszystkie pułapki (Kolce, Smoła, Miny) zyskują +2 ładunki oraz +40% obrażeń i zasięgu wybuchu." else relic.description
        com.example.myapp.game.RelicId.MIDAS_CRUCIBLE -> if (isPl) "+100 złota na start, +30% złota za zabójstwa i +25% większa szansa na drop diamentów." else relic.description
        com.example.myapp.game.RelicId.PRISMATIC_CATALYST -> if (isPl) "Wszystkie 14 fuzji żywiołów zadaje +40% obrażeń, ma +25% obszaru i 20% szansy na upuszczenie +1 diamentu." else relic.description
        com.example.myapp.game.RelicId.GRIMOIRE_OF_CONDUIT -> if (isPl) "Strumień Przeciążenia wiąże do 7 wrogów naraz i odbija 60% obrażeń na wszystkich połączonych." else relic.description
        com.example.myapp.game.RelicId.ASTRAL_HARVESTER -> if (isPl) "Ogniki Rozkładu Astralnego i Piekielnego Ognia zadają 2.5× obrażeń i wciągają pobliskich wrogów w próżnię." else relic.description
        com.example.myapp.game.RelicId.TITAN_WARHORN -> if (isPl) "Cięcia miecza bohatera ranią wszystkich wrogów w łuku 140° z fizycznym odrzutem o 45px." else relic.description
        com.example.myapp.game.RelicId.CHRONO_SINGULARITY -> if (isPl) "Wydłuża czas reakcji na telegrafy bossów o +1.2s i spowalnia ładowanie zdolności elit o 40%." else relic.description
        com.example.myapp.game.RelicId.ALCHEMIST_PHILOSOPHER_STONE -> if (isPl) "Każde 400 złota zdobyte w trakcie gry transmutuje w +1 diament po zakończeniu (do +15 💎 na bieg)." else relic.description
    }

    fun getLocalizedBlessingTitle(blessing: com.example.myapp.game.DiamondBlessing): String = when (blessing) {
        com.example.myapp.game.DiamondBlessing.NONE -> if (isPl) "Brak Błogosławieństwa" else blessing.title
        com.example.myapp.game.DiamondBlessing.MIDAS -> if (isPl) "Błogosławieństwo Midasa" else blessing.title
        com.example.myapp.game.DiamondBlessing.CATALYST -> if (isPl) "Błogosławieństwo Katalizatora" else blessing.title
        com.example.myapp.game.DiamondBlessing.HIGH_ROLLER -> if (isPl) "Pakt Wysokich Stawek" else blessing.title
    }

    fun getLocalizedBlessingDesc(blessing: com.example.myapp.game.DiamondBlessing): String = when (blessing) {
        com.example.myapp.game.DiamondBlessing.NONE -> if (isPl) "Startujesz z podstawowymi parametrami bez bonusów i kar." else blessing.description
        com.example.myapp.game.DiamondBlessing.MIDAS -> if (isPl) "+300 złota na start oraz +50% złota za każdego zabitego wroga." else blessing.description
        com.example.myapp.game.DiamondBlessing.CATALYST -> if (isPl) "Wszystkie 14 fuzji żywiołów zadaje +50% obrażeń i ma powiększony promień wybuchu." else blessing.description
        com.example.myapp.game.DiamondBlessing.HIGH_ROLLER -> if (isPl) "Wrogowie mają +25% HP i szybkości, ale Elity i Bossowie dają 3× WIĘCEJ DIAMENTÓW!" else blessing.description
    }

    fun getLocalizedSkillName(id: String): String = when (id) {
        "start_gold" -> if (isPl) "Złoty Start" else "Golden Start"
        "base_hp" -> if (isPl) "Umocniona Baza" else "Fortified Base"
        "player_damage" -> if (isPl) "Ostre Ostrze" else "Sharp Blade"
        "player_speed" -> if (isPl) "Szybkie Stopy" else "Swift Feet"
        "player_hp" -> if (isPl) "Twarda Skóra" else "Tough Skin"
        "tower_damage" -> if (isPl) "Mistrzostwo Wież" else "Tower Mastery"
        "gold_bonus" -> if (isPl) "Łowca Skarbów" else "Treasure Hunter"
        "diamond_luck" -> if (isPl) "Magnes na Diamenty" else "Diamond Magnet"
        "wave_bonus" -> if (isPl) "Weteran Wojny" else "War Veteran"
        "attack_range" -> if (isPl) "Sokole Oko" else "Eagle Eye"
        "ice_power" -> if (isPl) "Władca Mrozu" else "Frost Mastery"
        "ability_cd" -> if (isPl) "Szybkie Czary" else "Quick Cast"
        "sell_bonus" -> if (isPl) "Targowanie" else "Haggler"
        "resist_pierce" -> if (isPl) "Kruszenie Pancerza" else "Armor Break"
        "wave_modifier" -> if (isPl) "Szczęśliwe Fale" else "Lucky Waves"
        "prestige_gold" -> if (isPl) "Dotyk Midasa" else "Midas Touch"
        "fusion_potency" -> if (isPl) "Potęga Fuzji" else "Fusion Potency"
        "catalyst_radius" -> if (isPl) "Promień Katalizatora" else "Catalyst Radius"
        "conduit_resonance" -> if (isPl) "Łącze Przewodnika" else "Conduit Link"
        "status_duration" -> if (isPl) "Wydłużone Cierpienie" else "Affliction Mastery"
        "citadel_barrier" -> if (isPl) "Egida Cytadeli" else "Citadel Aegis"
        "trap_overhaul" -> if (isPl) "Inżynieria Polowa" else "Combat Engineering"
        "hero_critical" -> if (isPl) "Precyzyjny Cios" else "Precision Strike"
        else -> id
    }

    fun getLocalizedSkillDesc(id: String): String = when (id) {
        "start_gold" -> if (isPl) "+15 złota na start na poziom" else "+15 starting gold per level"
        "base_hp" -> if (isPl) "+20 maks. HP bazy na poziom" else "+20 base HP per level"
        "player_damage" -> if (isPl) "+3 obrażeń ataku bohatera na poziom" else "+3 starting attack damage"
        "player_speed" -> if (isPl) "+20 prędkości ruchu na poziom" else "+20 starting move speed"
        "player_hp" -> if (isPl) "+15 maks. HP bohatera na poziom" else "+15 starting player HP"
        "tower_damage" -> if (isPl) "+8% obrażeń wszystkich wież na poziom" else "+8% tower damage per level"
        "gold_bonus" -> if (isPl) "+10% złota za zabójstwa na poziom" else "+10% gold from kills per level"
        "diamond_luck" -> if (isPl) "+5% szansy na drop diamentu na poziom" else "+5% diamond drop chance per level"
        "wave_bonus" -> if (isPl) "+3 złota bonusu za każdą falę" else "+3 gold per wave bonus"
        "attack_range" -> if (isPl) "+15 zasięgu ataku bohatera" else "+15 starting attack range"
        "ice_power" -> if (isPl) "+10% spowolnienia wież lodowych na poziom" else "+10% ice tower slow per level"
        "ability_cd" -> if (isPl) "-5% czasu odnowienia umiejętności na poziom" else "-5% ability cooldown per level"
        "sell_bonus" -> if (isPl) "+10% zwrotu przy sprzedaży wieży na poziom" else "+10% tower sell value per level"
        "resist_pierce" -> if (isPl) "+5% przebicia odporności na poziom" else "+5% resistance pierce per level"
        "wave_modifier" -> if (isPl) "Lepsze i bogatsze modyfikatory fal" else "Better wave modifier chances"
        "prestige_gold" -> if (isPl) "+5% złota na poziom prestiżu" else "+5% gold per prestige level"
        "fusion_potency" -> if (isPl) "+12% obrażeń od fuzji żywiołów na poziom" else "+12% elemental fusion damage per level"
        "catalyst_radius" -> if (isPl) "+10% promienia wybuchu fuzji na poziom" else "+10% fusion blast & effect radius per level"
        "conduit_resonance" -> if (isPl) "+6% obrażeń odbitych w Strumieniu Przeciążenia na poziom" else "+6% echoed damage on Overload Flux per level"
        "status_duration" -> if (isPl) "+0.8s do czasu trwania wszystkich podpaleń i osłabień" else "+0.8s duration to all elemental burns & debuffs"
        "citadel_barrier" -> if (isPl) "+30 energii tarczy bazy na każdą falę za poziom" else "+30 starting wave energy shield for base per level"
        "trap_overhaul" -> if (isPl) "+20% obrażeń pułapek i +1 dodatkowe użycie na poziom" else "+20% trap damage & +1 max use per level"
        "hero_critical" -> if (isPl) "+5% szansy na krytyk bohatera (2.5× obrażeń) na poziom" else "+5% hero crit chance (2.5x damage) per level"
        else -> ""
    }

    fun actLore(actNumber: Int): String = if (isPl) {
        when (actNumber) {
            1 -> "🌲 Zielone łąki i pagórki królestwa. Orkowe hordy testują twoje pierwsze linie obrony."
            2 -> "🌋 Złowrogie bagna spowite toksyczną mgłą i rojami zarodników z głębin."
            3 -> "❄️ Starożytna krypta smoczych władców wśród rzek płynnej lawy i bazaltowych skał."
            4 -> "☣️ Spaczone lodowce i mroźne przełęcze strzeżone przez prastarych tytanów lodu."
            5 -> "🔥 Piekielny bastion na szczycie wulkanu, gdzie czas i rzeczywistość ulegają spaczeniu."
            6 -> "🌌 Kosmiczne szczeliny otchłani, z których wyłaniają się nienarodzone byty próżni."
            7 -> "👑 Ostateczna twierdza imperium, atakowana przez gigantyczne machiny wojenne."
            else -> ""
        }
    } else {
        when (actNumber) {
            1 -> "🌲 Green kingdom meadows and rolling hills. The greenskin hordes test your initial battlements."
            2 -> "🌋 Perilous swamplands choked with virulent spore blossoms and toxic miasma."
            3 -> "❄️ Scorched volcanic caverns where ancient dragon broods slumber among magma rivers."
            4 -> "☣️ Sub-zero mountain glaciers haunted by primeval frost titans and bitter winds."
            5 -> "🔥 Infernal Bastion at the summit of the volcanic rift, warping spacetime itself."
            6 -> "🌌 Cosmic abyssal rifts where ancient void entities rupture the mortal plane."
            7 -> "👑 The empire's grand apex citadel under direct siege by colossal war dreadnoughts."
            else -> ""
        }
    }

    fun actRegionalPerk(actNumber: Int): String = if (isPl) {
        when (actNumber) {
            1 -> "🎯 Premia regionalna: Wieże Łucznicze +15% obrażeń"
            2 -> "☠️ Premia regionalna: Wieże Trujące +20% obrażeń"
            3 -> "🔥 Premia regionalna: Wieże Płomienne +20% obrażeń"
            4 -> "❄️ Premia regionalna: Wieże Lodowe +20% obrażeń"
            5 -> "⚡ Premia regionalna: Wieże Tesli +20% obrażeń"
            6 -> "🔮 Premia regionalna: Wieże Magiczne i Wiru +20% obrażeń"
            7 -> "💣 Premia regionalna: Wieże Armatnie i Balisty +20% obrażeń"
            else -> ""
        }
    } else {
        when (actNumber) {
            1 -> "🎯 Regional Perk: Arrow Towers +15% Damage"
            2 -> "☠️ Regional Perk: Poison Towers +20% Damage"
            3 -> "🔥 Regional Perk: Flame Towers +20% Damage"
            4 -> "❄️ Regional Perk: Ice Towers +20% Damage"
            5 -> "⚡ Regional Perk: Tesla Towers +20% Damage"
            6 -> "🔮 Regional Perk: Magic & Vortex Towers +20% Damage"
            7 -> "💣 Regional Perk: Cannon & Ballista Towers +20% Damage"
            else -> ""
        }
    }

    data class AchDef(val id: String, val title: String, val description: String, val emoji: String, val diamondReward: Int = 10)

    fun achievements(): List<AchDef> {
        if (!isPl) return listOf(
            AchDef("first_kill", "First Blood", "Kill your first enemy", "🗡️", 5),
            AchDef("wave_5", "Survivor", "Reach wave 5", "🌊", 10),
            AchDef("wave_10", "Veteran", "Reach wave 10", "⭐", 15),
            AchDef("wave_20", "Legend", "Reach wave 20", "🏆", 25),
            AchDef("wave_30", "Immortal", "Reach wave 30", "💀", 35),
            AchDef("wave_50", "Mythic", "Reach wave 50", "🔥", 50),
            AchDef("kills_50", "Slayer", "Kill 50 enemies", "⚔️", 10),
            AchDef("kills_200", "Destroyer", "Kill 200 enemies", "💣", 20),
            AchDef("kills_500", "Annihilator", "Kill 500 enemies", "☠️", 30),
            AchDef("combo_10", "Combo King", "Get a 10x combo", "🔗", 10),
            AchDef("combo_20", "Combo God", "Get a 20x combo", "⛓️", 20),
            AchDef("boss_kill", "Boss Slayer", "Kill your first boss", "👹", 15),
            AchDef("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "🐉", 30),
            AchDef("5_towers", "Architect", "Place 5 towers", "🏗️", 10),
            AchDef("10_towers", "Fortress", "Place 10 towers", "🏰", 20),
            AchDef("all_tower_types", "Arsenal", "Place all tower types", "🎯", 25),
            AchDef("use_power", "Sorcerer", "Use a power for the first time", "🧙", 5),
            AchDef("max_tower", "Master Builder", "Upgrade a tower to level 5", "⬆️", 15),
            AchDef("rich", "Rich", "Have 500 gold at once", "💰", 15),
            AchDef("rich_1000", "Millionaire", "Have 1000 gold at once", "💎", 25),
            AchDef("score_1000", "Score Chaser", "Reach 1000 score", "🎯", 15),
            AchDef("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "💎", 20),
            AchDef("repaired_3", "Mechanic", "Repair the base 3 times in a run", "🔧", 10),
            AchDef("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "🌟", 20),
            AchDef("endless_10", "Endurance", "Reach wave 10 in endless mode", "♾️", 15),
            AchDef("kills_1000", "Genocide", "Kill 1000 enemies in one run", "💀", 40),
            AchDef("wave_100", "Centurion", "Reach wave 100", "💯", 100),
            AchDef("no_damage", "Untouchable", "Complete a wave without base taking damage", "🛡️", 20),
            AchDef("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "⚡", 25),
            AchDef("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "👑", 50),
            AchDef("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "⛏️", 40),
            AchDef("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "🏦", 35),
            AchDef("all_powers", "Elementalist", "Use all 4 powers in one run", "🌈", 20),
            AchDef("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "❤️‍🔥", 35),
            AchDef("campaign_5", "Campaigner", "Complete 5 campaign levels", "🗺️", 25),
            AchDef("campaign_10", "Strategist", "Complete 10 campaign levels", "🏅", 35),
            AchDef("campaign_all", "Conqueror", "Complete all campaign levels", "👑", 75),
            AchDef("campaign_no_damage", "Flawless", "Beat a campaign level without base damage", "🛡️", 25),
            AchDef("campaign_3star", "Perfectionist", "Get 3 stars on 5 campaign levels", "⭐", 30),
            AchDef("trap_first", "Trapper", "Place your first trap", "🪤", 10),
            AchDef("trap_10", "Minefield", "Place 10 traps in one run", "💣", 20),
            AchDef("mine_triple", "Triple Threat", "Kill 3 enemies with one mine", "💥", 25),
            AchDef("bounty_first", "Bounty Hunter", "Complete your first bounty", "🎯", 15),
            AchDef("bounty_all", "Bounty King", "Complete all 3 bounties in one run", "👑", 30),
            AchDef("volcano_win", "Volcanic Victory", "Reach wave 15 on Volcano map", "🌋", 25),
            AchDef("combo_30", "Unstoppable", "Get a 30x combo", "🔥", 30),
            AchDef("combo_50", "Godlike", "Get a 50x combo", "⚡", 50),
            AchDef("streak_no_tower", "Lone Wolf", "Reach wave 5 with no towers", "🐺", 35),
            AchDef("all_maps", "Cartographer", "Play on all 8 maps", "🌍", 30),
            AchDef("boss_rush_5", "Gauntlet", "Defeat 5 bosses in Boss Rush", "🗡️", 30),
            AchDef("randomizer_win", "Chaos Master", "Reach wave 15 in Randomizer", "🎲", 30),
            AchDef("ability_all", "Tactician", "Use abilities on 5 tower types in one run", "✨", 25),
            AchDef("prestige_first", "Reborn", "Prestige for the first time", "👑", 50),
            AchDef("score_5000", "High Roller", "Reach 5000 score", "🏅", 30),
            AchDef("score_10000", "Legendary Score", "Reach 10000 score", "🥇", 60),
            AchDef("boss_leviathan", "Storm Tamer", "Defeat the Storm Leviathan", "⚡", 25),
            AchDef("boss_phoenix", "Phoenix Reborn", "Defeat the Void Phoenix in both forms", "🪶", 25),
            AchDef("boss_spore", "Mycology Expert", "Defeat the Spore Overlord", "🍄", 25),
            AchDef("boss_chrono", "Time Warden", "Defeat the Chrono Lich", "⏳", 25),
            AchDef("boss_dreadnought", "Siege Breaker", "Destroy the Iron Dreadnought", "🤖", 25),
            AchDef("spec_first", "Master Artisan", "Specialize your first Level 5 tower", "⚡", 15),
            AchDef("spec_trio", "Grand Architect", "Specialize 3 towers in a single run", "🏛️", 30),
            AchDef("milestone_first", "Destiny's Boon", "Select your first Endless Milestone Buff", "🌟", 15),
            AchDef("milestone_trio", "Ascended Champion", "Pick 3 Milestone Buffs in one run", "✨", 30),
            AchDef("relic_first", "Ancient Reliquary", "Unlock your first Legendary Relic", "🏺", 20),
            AchDef("relic_3", "Treasury of Ancients", "Unlock 3 Legendary Relics", "👑", 50),
            AchDef("campaign_3star_10", "Grand Strategist", "Earn 3 stars on 10 campaign levels", "⭐", 50),
            AchDef("endless_25", "Abyssal Challenger", "Reach wave 25 in Endless mode", "🌊", 30),
            AchDef("endless_50", "Titan of the Endless", "Reach wave 50 in Endless mode", "🔥", 60),
            AchDef("boss_rush_10", "Colosseum God", "Defeat 10 bosses in Boss Rush", "⚔️", 50),
            AchDef("merchant_first", "First Barter", "Draft your first item from the Wandering Merchant", "🛒", 15),
            AchDef("merchant_trio", "Bazaar Master", "Draft 3 items from the Wandering Merchant in one run", "⚖️", 30),
            AchDef("booster_alchemist", "Elixir Draught", "Draft any 3-wave booster potion", "🧪", 15),
            AchDef("synergy_proc", "Elemental Fusion", "Trigger an Elemental Synergy in combat", "💥", 25),
            AchDef("synergy_master", "Elemental Catalyst", "Trigger 25 Elemental Reactions in a single game", "🔮", 35),
            AchDef("fusion_scholar", "Fusion Scholar", "Discover all 14 Elemental & Arcane Fusions", "📜", 50),
            AchDef("pact_survivor", "Devil's Bargain", "Survive 5 waves while bound to a High-Stakes Pact", "📜", 30),
            AchDef("greed_curse_diamonds", "Avarice Reward", "Earn bonus diamonds through the Curse of Greed", "😈", 25),
            AchDef("weather_thunder", "Lightning Rod", "Clear a Thunderstorm wave without losing Base HP", "🌩️", 25),
            AchDef("weather_bloodmoon", "Blood Moon Vanguard", "Survive an enraged wave during a Blood Moon", "🌕", 25),
            AchDef("weather_eclipse", "Solar Aegis", "Clear a Solar Eclipse wave", "🌑", 25),
            AchDef("hero_slayer_50", "Frontline Champion", "Slay 50 enemies directly with your hero in one run", "🗡️", 20),
            AchDef("hero_crits", "Lethal Strikes", "Land 15 critical strikes with the hero in one run", "🎯", 20),
            AchDef("iron_wall_5", "Iron Bastion", "Clear 5 consecutive waves without taking base damage", "🏰", 30),
            AchDef("combo_75", "Combo Overlord", "Reach a 75x kill combo", "🔥", 40),
            AchDef("combo_100", "Transcendent Combo", "Reach a 100x kill combo", "⚡", 60),
            AchDef("wave_75", "Abyssal Conqueror", "Reach wave 75 in any mode", "🔱", 50),
            AchDef("kills_2500", "Harbinger of Ruin", "Eliminate 2500 enemies in a single run", "💀", 50),
            AchDef("campaign_heroic_first", "Heroic Champion", "Clear a campaign mission on Heroic difficulty", "💀", 35),
            AchDef("boss_slayer_hero", "Regicide", "Land the final blow on a Boss with the Hero", "⚔️", 25)
        )
        return listOf(
            AchDef("first_kill", "Pierwsze trafienie", "Zabij pierwszego wroga", "🗡️", 5),
            AchDef("wave_5", "Ocalały", "Dotrzyj do fali 5", "🌊", 10),
            AchDef("wave_10", "Weteran", "Dotrzyj do fali 10", "⭐", 15),
            AchDef("wave_20", "Legenda", "Dotrzyj do fali 20", "🏆", 25),
            AchDef("wave_30", "Nieśmiertelny", "Dotrzyj do fali 30", "💀", 35),
            AchDef("wave_50", "Mityczny", "Dotrzyj do fali 50", "🔥", 50),
            AchDef("kills_50", "Zabójca", "Zabij 50 wrogów", "⚔️", 10),
            AchDef("kills_200", "Niszczyciel", "Zabij 200 wrogów", "💣", 20),
            AchDef("kills_500", "Anihilator", "Zabij 500 wrogów", "☠️", 30),
            AchDef("combo_10", "Król combo", "Zdobądź 10× combo", "🔗", 10),
            AchDef("combo_20", "Bóg combo", "Zdobądź 20× combo", "⛓️", 20),
            AchDef("boss_kill", "Pogromca bossów", "Zabij swojego pierwszego bossa", "👹", 15),
            AchDef("5_bosses", "Łowca bossów", "Zabij 5 bossów w jednym podejściu", "🐉", 30),
            AchDef("5_towers", "Architekt", "Postaw 5 wież", "🏗️", 10),
            AchDef("10_towers", "Forteca", "Postaw 10 wież", "🏰", 20),
            AchDef("all_tower_types", "Arsenał", "Postaw wszystkie typy wież", "🎯", 25),
            AchDef("use_power", "Czarodziej", "Użyj mocy po raz pierwszy", "🧙", 5),
            AchDef("max_tower", "Mistrz budowniczy", "Ulepsz wieżę do poziomu 5", "⬆️", 15),
            AchDef("rich", "Bogacz", "Miej 500 złota naraz", "💰", 15),
            AchDef("rich_1000", "Milioner", "Miej 1000 złota naraz", "💎", 25),
            AchDef("score_1000", "Łowca punktów", "Zdobądź 1000 punktów", "🎯", 15),
            AchDef("diamond_10", "Kolekcjoner", "Zdobądź 10 diamentów w jednym podejściu", "💎", 20),
            AchDef("repaired_3", "Mechanik", "Napraw bazę 3 razy w jednym podejściu", "🔧", 10),
            AchDef("upgrade_all", "Wszechstronny", "Kup wszystkie 4 ulepszenia", "🌟", 20),
            AchDef("endless_10", "Wytrzymałość", "Dotrzyj do fali 10 w trybie nieskończonym", "♾️", 15),
            AchDef("kills_1000", "Ludobójca", "Zabij 1000 wrogów w jednym podejściu", "💀", 40),
            AchDef("wave_100", "Centurion", "Dotrzyj do fali 100", "💯", 100),
            AchDef("no_damage", "Nietknięty", "Ukończ falę bez obrażeń bazy", "🛡️", 20),
            AchDef("speed_demon", "Demon prędkości", "Wygraj falę 10 na prędkości 3×", "⚡", 25),
            AchDef("10_bosses", "Legenda bossów", "Zabij 10 bossów w jednym podejściu", "👑", 50),
            AchDef("diamond_50", "Kopalnia diamentów", "Zdobądź 50 diamentów w jednym podejściu", "⛏️", 40),
            AchDef("gold_hoarder", "Skarbiec", "Miej 2000 złota naraz", "🏦", 35),
            AchDef("all_powers", "Elementalista", "Użyj wszystkich 4 mocy w jednym podejściu", "🌈", 20),
            AchDef("survivor_1hp", "Ostatnia szansa", "Wygraj falę z bazą na 1 HP", "❤️‍🔥", 35),
            AchDef("campaign_5", "Kampanijczyk", "Ukończ 5 poziomów kampanii", "🗺️", 25),
            AchDef("campaign_10", "Strateg", "Ukończ 10 poziomów kampanii", "🏅", 35),
            AchDef("campaign_all", "Zdobywca", "Ukończ wszystkie poziomy kampanii", "👑", 75),
            AchDef("campaign_no_damage", "Bezbłędny", "Ukończ poziom kampanii bez obrażeń bazy", "🛡️", 25),
            AchDef("campaign_3star", "Perfekcjonista", "Zdobądź 3 gwiazdki na 5 poziomach kampanii", "⭐", 30),
            AchDef("trap_first", "Łowca", "Postaw swoją pierwszą pułapkę", "🪤", 10),
            AchDef("trap_10", "Pole minowe", "Postaw 10 pułapek w jednym podejściu", "💣", 20),
            AchDef("mine_triple", "Potrójne zagrożenie", "Zabij 3 wrogów jedną miną", "💥", 25),
            AchDef("bounty_first", "Łowca nagród", "Wykonaj swoje pierwsze zlecenie", "🎯", 15),
            AchDef("bounty_all", "Król zleceń", "Wykonaj wszystkie 3 zlecenia w jednym podejściu", "👑", 30),
            AchDef("volcano_win", "Wulkaniczna wiktoria", "Dotrzyj do fali 15 na mapie Wulkan", "🌋", 25),
            AchDef("combo_30", "Nie do zatrzymania", "Zdobądź 30× combo", "🔥", 30),
            AchDef("combo_50", "Boski", "Zdobądź 50× combo", "⚡", 50),
            AchDef("streak_no_tower", "Samotny wilk", "Dotrzyj do fali 5 bez wież", "🐺", 35),
            AchDef("all_maps", "Kartograf", "Zagraj na wszystkich 8 mapach", "🌍", 30),
            AchDef("boss_rush_5", "Rękawica", "Pokonaj 5 bossów w Rajdzie Bossów", "🗡️", 30),
            AchDef("randomizer_win", "Mistrz chaosu", "Dotrzyj do fali 15 w trybie Losowym", "🎲", 30),
            AchDef("ability_all", "Taktyk", "Użyj umiejętności 5 typów wież w jednym podejściu", "✨", 25),
            AchDef("prestige_first", "Odrodzony", "Zdobądź prestiż po raz pierwszy", "👑", 50),
            AchDef("score_5000", "Gracz wysokich stawek", "Zdobądź 5000 punktów", "🏅", 30),
            AchDef("score_10000", "Legendarny wynik", "Zdobądź 10000 punktów", "🥇", 60),
            AchDef("boss_leviathan", "Poskromiciel Burz", "Pokonaj Burzowego Lewiatana", "⚡", 25),
            AchDef("boss_phoenix", "Odrodzony Feniks", "Pokonaj Feniksa Pustki w obu formach", "🪶", 25),
            AchDef("boss_spore", "Mistrz Mykologii", "Pokonaj Władcę Zarodników", "🍄", 25),
            AchDef("boss_chrono", "Strażnik Czasu", "Pokonaj Chrono Licza", "⏳", 25),
            AchDef("boss_dreadnought", "Pogromca Żelaza", "Zniszcz Żelaznego Pancernika", "🤖", 25),
            AchDef("spec_first", "Mistrz Rzemiosła", "Wyspecjalizuj swoją pierwszą wieżę poziomu 5", "⚡", 15),
            AchDef("spec_trio", "Wielki Architekt", "Wyspecjalizuj 3 wieże w jednym podejściu", "🏛️", 30),
            AchDef("milestone_first", "Dar Przeznaczenia", "Wybierz swój pierwszy Kamień Milowy w Nieskończoności", "🌟", 15),
            AchDef("milestone_trio", "Wzniesiony Czempion", "Wybierz 3 Kamienie Milowe w jednym podejściu", "✨", 30),
            AchDef("relic_first", "Starożytny Relikwiarz", "Odblokuj swoją pierwszą Legendarną Relikwię", "🏺", 20),
            AchDef("relic_3", "Skarbiec Przodków", "Odblokuj 3 Legendarne Relikwie", "👑", 50),
            AchDef("campaign_3star_10", "Wielki Strateg", "Zdobądź 3 gwiazdki na 10 poziomach kampanii", "⭐", 50),
            AchDef("endless_25", "Otchłanny Rywal", "Dotrzyj do fali 25 w trybie nieskończonym", "🌊", 30),
            AchDef("endless_50", "Tytan Nieskończoności", "Dotrzyj do fali 50 w trybie nieskończonym", "🔥", 60),
            AchDef("boss_rush_10", "Bóg Koloseum", "Pokonaj 10 bossów w Rajdzie Bossów", "⚔️", 50),
            AchDef("merchant_first", "Pierwszy handel", "Wybierz swój pierwszy przedmiot od Wędrownego Kupca", "🛒", 15),
            AchDef("merchant_trio", "Mistrz bazaru", "Wybierz 3 przedmioty od Wędrownego Kupca w jednym podejściu", "⚖️", 30),
            AchDef("booster_alchemist", "Eliksir alchemika", "Wypij dowolną 3-falową miksturę wzmacniającą", "🧪", 15),
            AchDef("synergy_proc", "Elementarna fuzja", "Aktywuj elementarną synergię podczas walki", "💥", 25),
            AchDef("synergy_master", "Elementarny katalizator", "Aktywuj 25 reakcji elementarnych w jednej grze", "🔮", 35),
            AchDef("fusion_scholar", "Uczony fuzji", "Odkryj wszystkie 14 elementarnych i tajemnych fuzji", "📜", 50),
            AchDef("pact_survivor", "Cyrograf", "Przetrwaj 5 fal będąc związanym Ryzykownym Paktem", "📜", 30),
            AchDef("greed_curse_diamonds", "Nagroda chciwości", "Zdobądź dodatkowe diamenty dzięki Klątwie Chciwości", "😈", 25),
            AchDef("weather_thunder", "Piorunochron", "Ukończ falę Burzy z piorunami bez utraty punktów bazy", "🌩️", 25),
            AchDef("weather_bloodmoon", "Strażnik Krwawego Księżyca", "Przetrwaj wściekłą falę podczas Krwawego Księżyca", "🌕", 25),
            AchDef("weather_eclipse", "Słoneczny aegis", "Przetrwaj falę podczas Zaćmienia Słońca", "🌑", 25),
            AchDef("hero_slayer_50", "Czempion frontu", "Pokonaj 50 wrogów bezpośrednio swoim bohaterem w jednym podejściu", "🗡️", 20),
            AchDef("hero_crits", "Zabójcze ciosy", "Wykonaj 15 ciosów krytycznych bohaterem w jednym podejściu", "🎯", 20),
            AchDef("iron_wall_5", "Żelazny bastion", "Ukończ 5 kolejnych fal bez utraty zdrowia bazy", "🏰", 30),
            AchDef("combo_75", "Władca kombosów", "Osiągnij combo 75x", "🔥", 40),
            AchDef("combo_100", "Transcendencja", "Osiągnij mityczne combo 100x", "⚡", 60),
            AchDef("wave_75", "Zdobywca otchłani", "Dotrzyj do fali 75 w dowolnym trybie", "🔱", 50),
            AchDef("kills_2500", "Zwiastun zguby", "Wyeliminuj 2500 wrogów w jednym podejściu", "💀", 50),
            AchDef("campaign_heroic_first", "Heroiczny czempion", "Ukończ misję kampanii na poziomie Heroicznym", "💀", 35),
            AchDef("boss_slayer_hero", "Królobójca", "Zadaj ostateczny cios bossowi swoim bohaterem", "⚔️", 25)
        )
    }

    // ─── Skill Tree ───
    fun skillDiamonds(d: Int) = "💎 $d"
    fun skillPrestigeLevel(lvl: Int, bonus: Int) =
        if (isPl) "👑 Poziom prestiżu $lvl — +$bonus% bonus obrażeń"
        else "👑 Prestige Level $lvl — +${bonus}% damage bonus"
    val skillPage1 get() = if (isPl) "Strona 1 — Podstawowe" else "Page 1 — Core Skills"
    val skillPage2 get() = if (isPl) "Strona 2 — Prestiżowe" else "Page 2 — Prestige Skills"
    fun skillPrestigeBtn(cost: Int) = if (isPl) "👑 PRESTIŻ (💎 $cost)" else "👑 PRESTIGE (💎 $cost)"
    val skillPrestigeLocked get() = if (isPl) "🔒 Zmaksuj umiejętności, by odblokować prestiż" else "🔒 Max all skills to prestige"
    fun skillPrestigeDone(lvl: Int) =
        if (isPl) "👑 Prestiż $lvl! Strona 1 zresetowana, bonus odblokowany!"
        else "👑 Prestige $lvl! Page 1 reset, bonus unlocked!"
    val skillPage2Locked get() = if (isPl) "🔒 Zdobądź prestiż, by odblokować stronę 2" else "🔒 Prestige to unlock Page 2 skills"
    val skillMax get() = if (isPl) "MAX" else "MAX"

    // ─── Campaign ───
    fun campaignProgress(completed: Int, total: Int, stars: Int, maxStars: Int) =
        if (isPl) "$completed / $total Ukończone  |  ⭐ $stars / $maxStars"
        else "$completed / $total Completed  |  ⭐ $stars / $maxStars"
    val campaignTitle get() = if (isPl) "📜 Kampania" else "📜 Campaign"
    fun campaignSurvive(waves: Int) = if (isPl) "Przetrwaj $waves fal" else "Survive $waves waves"

    // ─── Toast messages (MainActivity) ───
    fun towerPlacementToast(emoji: String, name: String, cost: Int) =
        if (isPl) "Dotknij, aby postawić $emoji $name (${cost}g)" else "Tap to place $emoji $name (${cost}g)"
    val notEnoughGold get() = if (isPl) "Za mało złota!" else "Not enough gold!"
    fun cooldownFmt(cd: Int) = if (isPl) "Odnowienie: ${cd}s" else "Cooldown: ${cd}s"
    val fireballUsed get() = if (isPl) "🔥 Kula ognia! Obrażenia obszarowe!" else "🔥 Fireball! AoE damage!"
    fun fireballNeedGold(cost: Int) = if (isPl) "Potrzeba ${cost}g!" else "Need ${cost}g!"
    val freezeUsed get() = if (isPl) "❄️ Zamrożenie! Wrogowie spowolnieni!" else "❄️ Freeze! Enemies slowed!"
    fun freezeNeedGold(cost: Int) = if (isPl) "Potrzeba ${cost}g!" else "Need ${cost}g!"
    val healUsed get() = if (isPl) "💚 Leczenie! Baza +50 HP!" else "💚 Heal! Base +50 HP!"
    fun healNeedGold(cost: Int) = if (isPl) "Potrzeba ${cost}g!" else "Need ${cost}g!"
    val lightningUsed get() = if (isPl) "⚡ Błyskawica! Łańcuchowe obrażenia!" else "⚡ Lightning! Chain damage!"
    fun lightningNeedGold(cost: Int) = if (isPl) "Potrzeba ${cost}g!" else "Need ${cost}g!"
    val joystickHint get() = if (isPl) "🕹️ Steruj joystickiem po lewej stronie" else "🕹️ Use the left joystick to move"
    val baseRepaired get() = if (isPl) "🔧 Baza naprawiona!" else "🔧 Base repaired!"
    val baseFullHp get() = if (isPl) "Baza ma pełne HP!" else "Base is full HP!"
    fun blockadePlacement(cost: Int) = if (isPl) "Dotknij ścieżkę, aby postawić 🪨 Barykadę (${cost}g)" else "Tap a path to place 🪨 Blockade (${cost}g)"
    fun hintToast(hint: String) = "💡 $hint"

    // ─── Upgrade toasts ───
    fun attackUp(cost: Int) = if (isPl) "⚔️ Atak wzmocniony! (${cost}g)" else "⚔️ Attack up! (${cost}g)"
    fun speedUp(cost: Int) = if (isPl) "👟 Prędkość zwiększona! (${cost}g)" else "👟 Speed up! (${cost}g)"
    fun hpUp(cost: Int) = if (isPl) "❤️ HP zwiększone! (${cost}g)" else "❤️ HP up! (${cost}g)"
    fun baseUp(cost: Int) = if (isPl) "🏰 Baza wzmocniona! (${cost}g)" else "🏰 Base up! (${cost}g)"
    fun needGold(cost: Int) = if (isPl) "Potrzeba ${cost}g!" else "Need ${cost}g!"
    fun towerUpgraded(level: Int, cost: Int) = if (isPl) "⬆️ Wieża Poz.${level}! (${cost}g)" else "⬆️ Tower Lv${level}! (${cost}g)"
    val tapTowerFirst get() = if (isPl) "Najpierw dotknij wieżę!" else "Tap a tower first!"
    fun targetMode(label: String) = if (isPl) "Cel: $label" else "Target: $label"
    fun soldTower(value: Int) = if (isPl) "💸 Sprzedano za ${value}g!" else "💸 Sold for ${value}g!"
    fun abilityActivated(name: String) = if (isPl) "✨ $name!" else "✨ $name!"
    fun speedToast(speed: Int) = if (isPl) "Prędkość: ${speed}x" else "Speed: ${speed}x"
    fun milestoneTitle(wave: Int) = if (isPl) "🌟 Fala $wave — Wybierz bonus!" else "🌟 Wave $wave Milestone — Pick a Buff!"

    // ─── HUD labels ───
    fun goldHud(gold: Int) = "💰 $gold"
    fun waveHud(wave: Int) = "⚔️ ${if (isPl) "Fala" else "Wave"} $wave"
    fun diamondsHud(d: Int) = "💎 $d"
    fun killsHud(k: Int) = "💀 $k"
    fun gameOverHud(score: Int, wave: Int) = if (isPl) "💰 KONIEC GRY" else "💰 GAME OVER"
    fun gameOverWaveHud(score: Int, wave: Int) = "⚔️ ${if (isPl) "Fala" else "Wave"} $wave | ${if (isPl) "Wynik" else "Score"} $score"

    // ─── Help Activity ───
    val helpTitle get() = if (isPl) "📖 Pomoc i mechaniki" else "📖 Help & Mechanics"

    // ─── Map names (already localized via MapType, but needed for popup) ───
    fun mapName(name: String): String {
        if (!isPl) return name
        return when (name) {
            "Classic" -> "Klasyczna"
            "Valley" -> "Dolina"
            "Crossroads" -> "Skrzyżowanie"
            "Desert" -> "Pustynia"
            "Snow" -> "Śnieg"
            "Lava" -> "Lawa"
            "Enchanted" -> "Zaczarowana"
            "Volcano" -> "Wulkan"
            else -> name
        }
    }

    val mapSelectionTitle get() = if (isPl) "🗺️ WYBIERZ POLE BITWY" else "🗺️ SELECT BATTLEFIELD"
    val deployToEndless get() = if (isPl) "⚔️ ROZPOCZNIJ BIEG" else "⚔️ START RUN"
    fun mapLanesFmt(lanes: Int): String = if (isPl) "$lanes Linie marszu" else "$lanes Marching Lanes"

    fun mapTacticalTag(mapType: com.example.myapp.game.MapType): String = if (isPl) {
        when (mapType) {
            com.example.myapp.game.MapType.CLASSIC -> "Zrównoważone 3 linie"
            com.example.myapp.game.MapType.VALLEY -> "Wąskie gardła canyonu"
            com.example.myapp.game.MapType.CROSSROADS -> "4-kierunkowy krzyżowy ogień"
            com.example.myapp.game.MapType.DESERT -> "Szybkie wydmy pustynne"
            com.example.myapp.game.MapType.SNOW -> "Lodowe górskie przełęcze"
            com.example.myapp.game.MapType.LAVA -> "Pola wrzącej magmy"
            com.example.myapp.game.MapType.ENCHANTED -> "Mistyczna spirala 4 linii"
            com.example.myapp.game.MapType.VOLCANO -> "Aktywny krater i erupcje"
        }
    } else {
        when (mapType) {
            com.example.myapp.game.MapType.CLASSIC -> "Balanced 3-Lane Front"
            com.example.myapp.game.MapType.VALLEY -> "Tight Canyon Chokepoints"
            com.example.myapp.game.MapType.CROSSROADS -> "4-Way Crossfire Nexus"
            com.example.myapp.game.MapType.DESERT -> "Sweeping Sand Dunes"
            com.example.myapp.game.MapType.SNOW -> "Glacial Mountain Passes"
            com.example.myapp.game.MapType.LAVA -> "Scorched Magma Fields"
            com.example.myapp.game.MapType.ENCHANTED -> "Mystic 4-Lane Spiral"
            com.example.myapp.game.MapType.VOLCANO -> "Active Caldera & Eruptions"
        }
    }

    fun mapDescription(mapType: com.example.myapp.game.MapType): String = if (isPl) {
        when (mapType) {
            com.example.myapp.game.MapType.CLASSIC -> "Żyzne łąki królestwa o zrównoważonym rozkładzie 3 dróg. Idealne pole do budowy klasycznego bastionu."
            com.example.myapp.game.MapType.VALLEY -> "Głęboki skalisty wąwóz z krętymi serpentynami. Wieże obszarowe sieją spustoszenie w wąskich gardłach!"
            com.example.myapp.game.MapType.CROSSROADS -> "Starożytny imperialny trakt krzyżujący się w centrum. Wrogowie atakują ze wszystkich 4 stron świata!"
            com.example.myapp.game.MapType.DESERT -> "Gorące wydmy pustynne. Wrogowie poruszają się szybko szerokimi łukami; wieże spowalniające są kluczem."
            com.example.myapp.game.MapType.SNOW -> "Zmarznięta tundra otoczona lodowymi szczytami. Trzy wąskie przełęcze zbiegające się ku sanktuarium."
            com.example.myapp.game.MapType.LAVA -> "Spalona bazaltowa ziemia pocięta płonącą lawą. Trudny teren o wysokiej gęstości wrogich fal."
            com.example.myapp.game.MapType.ENCHANTED -> "Czarodziejski gaj z 4 spiralnymi ścieżkami wróżek. Nieprzewidywalne natarcia wymagają elastycznej obrony."
            com.example.myapp.game.MapType.VOLCANO -> "Piekielny superwulkan! Wrogowie obchodzą centralny krater, który okresowo wyrzuca strumienie wrzącej magmy!"
        }
    } else {
        when (mapType) {
            com.example.myapp.game.MapType.CLASSIC -> "Lush kingdom meadows with 3 balanced approach lanes. Ideal proving ground for well-rounded defensive layouts."
            com.example.myapp.game.MapType.VALLEY -> "Deep rocky canyon gorge with winding switchbacks. Splashing splash and bomb towers dominate tight chokepoints."
            com.example.myapp.game.MapType.CROSSROADS -> "Ancient highway intersection with enemies converging from 4 directions into a grand central battleground."
            com.example.myapp.game.MapType.DESERT -> "Sun-bleached dunes with long sweeping curves. Enemies advance quickly; Frost and Tar traps are vital."
            com.example.myapp.game.MapType.SNOW -> "Frozen glacial tundra where three narrow icy mountain passes funnel attackers directly toward your base."
            com.example.myapp.game.MapType.LAVA -> "Scorched volcanic crust winding through hazardous magma fissures. Fierce, relentless enemy pressure."
            com.example.myapp.game.MapType.ENCHANTED -> "Mystical twilight fairy grove with 4 spiraling approach vectors. Demands versatile multi-angle defense."
            com.example.myapp.game.MapType.VOLCANO -> "Supervolcano caldera! Three trails loop around an active magma cone that periodically erupts with burning fury!"
        }
    }

    // ─── Help Activity ───
    fun helpText(): String {
        if (!isPl) return """
🏰 BASE
Your base is at the bottom of the map. Enemies follow paths toward it. If an enemy reaches the base, it deals damage. Game over when base HP reaches 0. Upgrade your base HP with the 🏰 BASE button.

🗡️ HERO & JOYSTICK
Your hero auto-attacks the nearest enemy in range. Use the dynamic floating joystick on the left side of the screen for smooth 360° movement. Screen taps on the right place towers, inspect defenses, and trigger powers. Upgrade ATK and SPD using the bottom bar buttons.

🏹 TOWERS
Tap a tower button then tap the map to place it. Towers auto-fire at enemies in range.

• 🏹 Arrow (30g) — Fast physical damage
• 🧨 Magic (60g) — Magic damage, good vs undead
• 💣 Cannon (100g) — Splash explosive damage
• ☠️ Poison (80g) — Damage over time
• ⚡ Tesla (120g) — Chain lightning, electric damage
• ❄️ Ice (70g) — Slows enemies in an aura
• 🔥 Flame (90g) — Burns enemies (fire DoT)
• 💀 Necro (110g) — Dark damage, 3× vs low HP
• 🎯 Ballista (140g) — Extreme range sniper
• 🌀 Vortex (100g) — Pulls enemies toward tower
• 💚 Healer (80g) — Heals base and repairs blockades
Select a tower by tapping it, then use ⬆️ TOWER to upgrade (up to level 5). Use ✨ Ability for a tower's special ability.

🔥 POWERS
• 🔥 Fireball (40g, 8s) — AoE fire damage
• ❄️ Freeze (30g, 12s) — Slows ALL enemies
• 💚 Heal (25g, 15s) — Heals the base
• ⚡ Lightning (50g, 10s) — Chain lightning

👹 ENEMIES
Enemies spawn in waves. Types have different resistances. Elite enemies (crown) appear every 5th non-boss wave with 3× HP, 2× gold. Every 5th wave, a boss spawns with unique abilities.

New enemy types:
• Berserker (wave 12+) — Gets faster as HP drops
• Commander (wave 15+) — Aura buffs nearby enemy speed
• Shapeshifter (wave 18+) — Cycles resistances every 5s

🌙 DAY/NIGHT
Every 8 waves, day/night toggles. Night: enemies +20% HP, towers -10% range.

⚔️ COMBOS
Kill quickly for combo multiplier (up to 5×). Combos increase gold earned.

💎 DIAMONDS
Boss kills and rare drops give diamonds. Spend them in the Skill Tree for permanent upgrades.

⏩ SPEED & PAUSE
Use the 2× and 3× buttons to speed up. Tap Pause to pause.
""".trimIndent()

        return """
🏰 BAZA
Twoja baza jest na dole mapy. Wrogowie podążają ścieżkami w jej kierunku. Koniec gry, gdy HP bazy spadnie do 0. Ulepsz HP bazy przyciskiem 🏰 BAZA.

🗡️ BOHATER I JOYSTICK
Twój bohater automatycznie atakuje najbliższego wroga w zasięgu. Użyj dynamicznego joysticka w lewej części ekranu do płynnego poruszania się. Dotknięcia prawej strony ekranu stawiają wieże, ulepszają obronę i aktywują moce. Ulepszaj ATK i SPD w dolnym pasku.

🏹 WIEŻE
Dotknij przycisk wieży, potem mapę, by ją postawić. Wieże strzelają automatycznie.

• 🏹 Łucznicza (30g) — Szybkie obrażenia fizyczne
• 🧨 Magiczna (60g) — Obrażenia magiczne
• 💣 Armatnia (100g) — Obrażenia wybuchowe
• ☠️ Trująca (80g) — Obrażenia w czasie
• ⚡ Tesli (120g) — Łańcuchowa błyskawica
• ❄️ Lodowa (70g) — Spowalnia wrogów
• 🔥 Płomienna (90g) — Podpala wrogów (DoT)
• 💀 Nekro (110g) — Ciemne obrażenia, 3× na niskie HP
• 🎯 Balisty (140g) — Snajper dalekiego zasięgu
• 🌀 Wir (100g) — Przyciąga wrogów

⬆️ ULEPSZENIA WIEŻ
Dotknij wieżę, użyj ⬆️ WIEŻA do ulepszenia (do poz. 5). Użyj ✨ Umiejętność dla specjalnej zdolności.

🔥 MOCE
• 🔥 Kula ognia (40g, 8s) — Obrażenia AoE
• ❄️ Zamrożenie (30g, 12s) — Spowalnia WSZYSTKICH
• 💚 Leczenie (25g, 15s) — Leczy bazę
• ⚡ Błyskawica (50g, 10s) — Łańcuchowa błyskawica

👹 WROGOWIE
Wrogowie pojawiają się w falach z różnymi odpornościami. Elitarni (korona) co 5 fal z 3× HP. Co 5 falę boss z unikalnymi zdolnościami.

🌙 DZIEŃ/NOC
Co 8 fal cykl się zmienia. Noc: wrogowie +20% HP, wieże -10% zasięgu.

⚔️ COMBO
Zabijaj szybko, by budować combo (do 5×). Combo = więcej złota.

💎 DIAMENTY
Bossowie i rzadkie dropy dają diamenty. Wydawaj w Drzewku umiejętności.

⏩ PRĘDKOŚĆ I PAUZA
Użyj 2× i 3× by przyspieszyć. Dotknij Pauza, by wstrzymać.
""".trimIndent()
    }

    // ─── Traps ───
    fun placeTrapSpike(cost: Int) = if (isPl) "🗡️ Postaw kolce na ścieżce (${cost}g)" else "🗡️ Place Spike on path (${cost}g)"
    fun placeTrapTar(cost: Int) = if (isPl) "🟤 Postaw smołę na ścieżce (${cost}g)" else "🟤 Place Tar on path (${cost}g)"
    fun placeTrapMine(cost: Int) = if (isPl) "💣 Postaw minę na ścieżce (${cost}g)" else "💣 Place Mine on path (${cost}g)"
    val trapPlaceOnPath get() = if (isPl) "Postaw pułapkę na ścieżce" else "Place trap on path"

    // ─── Bounties ───
    val bountyHeader get() = if (isPl) "🎯 Zlecenia" else "🎯 Bounties"
    val bountyDone get() = if (isPl) "✔️ Wykonano!" else "✔️ Done!"

    // ─── Volcano ───
    val volcanoMapName get() = if (isPl) "🌋 Wulkan" else "🌋 Volcano"

    // ─── Run History ───
    val runHistoryTitle get() = if (isPl) "📜 Historia gier" else "📜 Run History"
    val runHistoryEmpty get() = if (isPl) "Brak historii gier" else "No run history yet"
    fun runHistoryEntry(mode: String, wave: Int, score: Int) =
        if (isPl) "$mode — Fala $wave — $score pkt" else "$mode — Wave $wave — $score pts"

    // ─── Weather Events ───
    fun weatherName(event: com.example.myapp.game.WeatherEvent): String = when (event) {
        com.example.myapp.game.WeatherEvent.CLEAR -> if (isPl) "Czyste Niebo" else "Clear Skies"
        com.example.myapp.game.WeatherEvent.BLOOD_MOON -> if (isPl) "Krwawy Księżyc" else "Blood Moon"
        com.example.myapp.game.WeatherEvent.THUNDERSTORM -> if (isPl) "Burza z Piorunami" else "Thunderstorm"
        com.example.myapp.game.WeatherEvent.SOLAR_ECLIPSE -> if (isPl) "Zaćmienie Słońca" else "Solar Eclipse"
    }

    fun weatherBanner(event: com.example.myapp.game.WeatherEvent): String = when (event) {
        com.example.myapp.game.WeatherEvent.CLEAR -> ""
        com.example.myapp.game.WeatherEvent.BLOOD_MOON ->
            if (isPl) "🩸 KRWAWY KSIĘŻYC WSCHODZI! Wrogowie w szale (+15% Spd) • 2× Złoto i Diamenty!"
            else "🩸 BLOOD MOON RISES! Enraged Creeps (+15% Spd) • 2× Gold & Bonus Diamonds!"
        com.example.myapp.game.WeatherEvent.THUNDERSTORM ->
            if (isPl) "⚡ BURZA ROZPĘTANA! Niebiańskie gromy rażą roje potworów!"
            else "⚡ THUNDERSTORM UNLEASHED! Heavenly Lightning Strikes Enemy Swarms!"
        com.example.myapp.game.WeatherEvent.SOLAR_ECLIPSE ->
            if (isPl) "☀️ ZAĆMIENIE SŁOŃCA! Magiczne odnowienie -35% i zwinność Bohatera +30%!"
            else "☀️ SOLAR ECLIPSE ASCENDS! Spell Cooldowns -35% & Hero Swiftness +30%!"
    }

    // ─── Boss Telegraphs & Heroic Campaign ───
    val heroicChallenge get() = if (isPl) "💀 WYZWANIE HEROICZNE" else "💀 HEROIC CHALLENGE"
    val heroicModeDesc get() = if (isPl) "Wrogowie +35% HP, +20% Szybkości • Szybsze telegrafy bossa • Premia +5 💎" else "Enemies +35% HP, +20% Speed • Faster Boss Telegraphs • +5 💎 Bounty"
    val heroicVictoryTitle get() = if (isPl) "💀 HEROICZNE ZWYCIĘSTWO!" else "💀 HEROIC VICTORY!"
    val heroicProgressFmt: (Int, Int) -> String = { cleared, total ->
        if (isPl) "💀 Heroic: $cleared/$total ukończone" else "💀 Heroic: $cleared/$total Cleared"
    }
    fun bossChargingFmt(ability: String, seconds: Float) =
        if (isPl) "⚠️ ŁADOWANIE: $ability (${String.format("%.1f", seconds)}s)"
        else "⚠️ CHARGING: $ability (${String.format("%.1f", seconds)}s)"

    // ─── Fusion Codex ───
    val codexTitle get() = if (isPl) "⚡ KODEX FUZJI" else "⚡ FUSION CODEX"
    val codexSubtitle get() = if (isPl) "Katalog 14 reakcji i syntez żywiołów" else "Catalog of 14 Elemental & Arcane Fusions"
    val codexFilterAll get() = if (isPl) "Wszystkie" else "All"
    val codexFilterElemental get() = if (isPl) "Elementarne" else "Elemental"
    val codexFilterArcane get() = if (isPl) "Tajemne" else "Arcane"
    val codexFilterDark get() = if (isPl) "Mroczne" else "Dark Arts"
    val codexFilterSiege get() = if (isPl) "Oblężnicze" else "Siege"
    val codexStatusDiscovered get() = if (isPl) "✅ ODKRYTA" else "✅ DISCOVERED"
    val codexStatusUndiscovered get() = if (isPl) "🔒 ZABLOKOWANA" else "🔒 UNDISCOVERED"
    val codexHintUndiscovered get() = if (isPl) "Połącz te żywioły na polu bitwy, aby odkryć pełną formułę!" else "Trigger this elemental reaction in combat to unveil the formula!"
    val codexRoleLabel get() = if (isPl) "Rola taktyczna: " else "Tactical Role: "
    val codexRecipeLabel get() = if (isPl) "Połączenie: " else "Combination: "
    val codexClose get() = if (isPl) "Zamknij" else "Close"
    fun codexDiscoveredCount(discovered: Int, total: Int) =
        if (isPl) "Odkryto: $discovered / $total fuzji" else "Discovered: $discovered / $total Fusions"

    fun getLocalizedFusionName(id: String): String = when (id) {
        "steam_burst" -> if (isPl) "Wybuch Pary" else "Steam Burst"
        "volatile_detonation" -> if (isPl) "Niestabilna Detonacja" else "Volatile Detonation"
        "superconductor" -> if (isPl) "Nadprzewodnik" else "Superconductor"
        "corrosive_shock" -> if (isPl) "Żrący Szok" else "Corrosive Shock"
        "solar_flare" -> if (isPl) "Rozbłysk Słoneczny" else "Solar Flare"
        "glacial_singularity" -> if (isPl) "Lodowa Osobliwość" else "Glacial Singularity"
        "overload_flux" -> if (isPl) "Strumień Przeciążenia" else "Overload Flux"
        "astral_decay" -> if (isPl) "Rozkład Astralny" else "Astral Decay"
        "hellfire" -> if (isPl) "Piekielny Ogień" else "Hellfire"
        "frost_tomb" -> if (isPl) "Grobowiec Mrozu" else "Frost Tomb"
        "shadow_surge" -> if (isPl) "Fala Cienia" else "Shadow Surge"
        "corpse_miasma" -> if (isPl) "Miazmat Trupich Jadów" else "Corpse Miasma"
        "napalm_conflagration" -> if (isPl) "Napalmowa Pożoga" else "Napalm Conflagration"
        "emp_shockwave" -> if (isPl) "Fala EMP" else "EMP Shockwave"
        else -> id
    }

    fun getLocalizedFusionDesc(id: String): String = when (id) {
        "steam_burst" -> if (isPl) "Szok termiczny wywołuje eksplozję pary (obszar 130px), która spowalnia okolicznych wrogów na 2s." else "Thermal shock causes an explosive vapor burst (130px AoE) that slows surrounding foes for 2s."
        "volatile_detonation" -> if (isPl) "Podpala toksyczne opary, natychmiast detonując pozostałe obrażenia trucizny w potężny wybuch." else "Ignites concentrated toxic fumes, detonating remaining poison DoT into a massive immediate blast."
        "superconductor" -> if (isPl) "Przewodzący szron przeskakuje na 3 wrogów, nakładając +25% podatności na wszystkie obrażenia przez 4s." else "Conductive frost chains to up to 3 nearby foes, inflicting +25% vulnerability to all damage for 4s."
        "corrosive_shock" -> if (isPl) "Reakcja elektrochemiczna ogłusza cel (0.6s) i rozpryskuje toksyny na okolicznych wrogów." else "Electro-chemical reaction stuns target (0.6s) and splatters virulent toxins across surrounding foes."
        "solar_flare" -> if (isPl) "Słoneczna radiacja zdejmuje tarcze (+50% obrażeń na osłony) i całkowicie blokuje regenerację HP na 3.5s." else "Arcane radiance strips enemy shields (+50% bonus damage against shields) and halts HP regeneration for 3.5s."
        "glacial_singularity" -> if (isPl) "Arktyczny wir przyciąga wrogów w promieniu 150px do środka i zamraża ich w bryle lodu (1.2s ogłuszenia, 90% spowolnienia)." else "Sub-zero vortex pulls foes within 150px inward and flash-freezes them solid (1.2s stun, 90% slow)."
        "overload_flux" -> if (isPl) "Wiąże cel i do 4 wrogów rezonującym łączem — 35% wszelkich otrzymanych obrażeń odbija się na połączonych wrogach przez 4s." else "Binds target and up to 4 nearby enemies into a resonance link, echoing 35% of all damage taken for 4s."
        "astral_decay" -> if (isPl) "Kosmiczny uwiąd zadaje 35 DPS, zwiększa otrzymywane obrażenia o +20% i zapewnia +50% złota oraz widmowy ognik po śmierci." else "Cosmic wither inflicts 35 DPS, +20% damage vulnerability, and grants +50% bonus gold + void wisp on death."
        "hellfire" -> if (isPl) "Przeklęty płomień zadaje 45 DPS. Po śmierci dusza wroga eksploduje mściwym widmem zadającym 150 obrażeń." else "Cursed infernal flame deals 45 DPS. On death, the soul detonates into a vengeful phantom wisp exploding for 150 damage."
        "frost_tomb" -> if (isPl) "Krystalizuje pancerz wroga lodem otchłani (+40% obrażeń fizycznych i wybuchowych, zatrzymana regeneracja HP przez 4s)." else "Abyssal frost crystallizes foe armor, applying +40% physical & explosive vulnerability and halting HP regen for 4s."
        "shadow_surge" -> if (isPl) "Eteryczne wyładowanie ogłusza cel i osłabia go na 5s, zmniejszając o połowę zadawane przez niego obrażenia bazie i barykadom." else "Ethereal static shocks and enfeebles target for 5s, halving all damage dealt against base and barricades."
        "corpse_miasma" -> if (isPl) "Wyzwala nekrotyczny wyziew zadający obrażenia obszarowe powiększone o 3.5% maksymalnego HP wroga oraz 35% spowolnienia." else "Unleashes necrotic pestilence dealing heavy damage plus 3.5% max HP and applying a 35% slow in 140px AoE."
        "napalm_conflagration" -> if (isPl) "Uderzenie uderzeniowe (150px AoE), które podpala podłoże na 3s, zadając 40 DPS przechodzącym wrogom." else "Concussive blast (150px AoE) that leaves behind a scorched fire crater dealing 40 DPS to enemies on it for 3s."
        "emp_shockwave" -> if (isPl) "Impuls elektromagnetyczny usuwa tarcze, przerywa telegrafy i ucisza zdolności elitarnych wrogów na 2.5s." else "Electromagnetic shockwave cleanses energy shields, cancels ability telegraphs, and silences elite abilities for 2.5s."
        else -> ""
    }

    fun getLocalizedTacticalRole(id: String): String = when (id) {
        "steam_burst" -> if (isPl) "Kontrola tłumu i obszar" else "Crowd Control & AoE"
        "volatile_detonation" -> if (isPl) "Zabójczy wybuch" else "Burst Finisher"
        "superconductor" -> if (isPl) "Osłabienie wielu celów" else "Debuff & Multi-Target"
        "corrosive_shock" -> if (isPl) "Ogłuszenie i zatrucie" else "Stun & Toxic Spread"
        "solar_flare" -> if (isPl) "Łamacz tarcz i blokada leczenia" else "Shield Breaker & Anti-Regen"
        "glacial_singularity" -> if (isPl) "Wir grawitacyjny i twarda kontrola" else "Mass Vacuum & Hard CC"
        "overload_flux" -> if (isPl) "Mnożnik obrażeń łańcuchowych" else "Damage Multiplication"
        "astral_decay" -> if (isPl) "Niszczenie bossów i nagroda" else "Boss Shred & Bounty"
        "hellfire" -> if (isPl) "Cel pojedynczy i wybuch duszy" else "Single Target & Phantom Burst"
        "frost_tomb" -> if (isPl) "Kruszenie pancerza i anty-regen" else "Armor Shred & Anti-Regen"
        "shadow_surge" -> if (isPl) "Ochrona bazy i osłabienie" else "Objective Protection"
        "corpse_miasma" -> if (isPl) "Pogromca tanków i elit" else "Tank & Elite Slaying"
        "napalm_conflagration" -> if (isPl) "Blokada terenu i pułapka ognia" else "Area Denial & Hazard"
        "emp_shockwave" -> if (isPl) "Czyszczenie tarcz i uciszenie" else "Shield Purge & Silence"
        else -> ""
    }
}
