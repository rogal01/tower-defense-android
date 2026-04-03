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
            "⚡ POWERS & DASH" to """
Powers cost gold and have cooldowns.

🔥 Fireball (40g, 8s CD) — Damages ALL enemies on screen.
❄️ Freeze (30g, 12s CD) — Slows all enemies to 20% speed for 4 seconds.
💚 Heal (25g, 15s CD) — Restores 50 HP to your base.
⚡ Lightning (50g, 10s CD) — Chain lightning hits the 5 closest enemies for 80 damage each.

💨 Dash (free, 8s CD) — Teleport toward your target, dealing AoE damage along the path.
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
            "⚡ MOCE I DASH" to """
Moce kosztują złoto i mają czas odnowienia.

🔥 Kula ognia (40g, 8s CD) — Zadaje obrażenia WSZYSTKIM wrogom na ekranie.
❄️ Zamrożenie (30g, 12s CD) — Spowalnia wszystkich wrogów do 20% prędkości na 4 sekundy.
💚 Leczenie (25g, 15s CD) — Przywraca 50 HP bazy.
⚡ Błyskawica (50g, 10s CD) — Łańcuchowa błyskawica trafia 5 najbliższych wrogów po 80 obrażeń.

💨 Dash (darmowy, 8s CD) — Teleportuj się w kierunku celu, zadając obrażenia obszarowe po drodze.
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

    data class AchDef(val id: String, val title: String, val description: String, val emoji: String)

    fun achievements(): List<AchDef> {
        if (!isPl) return listOf(
            AchDef("first_kill", "First Blood", "Kill your first enemy", "🗡️"),
            AchDef("wave_5", "Survivor", "Reach wave 5", "🌊"),
            AchDef("wave_10", "Veteran", "Reach wave 10", "⭐"),
            AchDef("wave_20", "Legend", "Reach wave 20", "🏆"),
            AchDef("wave_30", "Immortal", "Reach wave 30", "💀"),
            AchDef("wave_50", "Mythic", "Reach wave 50", "🔥"),
            AchDef("kills_50", "Slayer", "Kill 50 enemies", "⚔️"),
            AchDef("kills_200", "Destroyer", "Kill 200 enemies", "💣"),
            AchDef("kills_500", "Annihilator", "Kill 500 enemies", "☠️"),
            AchDef("combo_10", "Combo King", "Get a 10x combo", "🔗"),
            AchDef("combo_20", "Combo God", "Get a 20x combo", "⛓️"),
            AchDef("boss_kill", "Boss Slayer", "Kill your first boss", "👹"),
            AchDef("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "🐉"),
            AchDef("5_towers", "Architect", "Place 5 towers", "🏗️"),
            AchDef("10_towers", "Fortress", "Place 10 towers", "🏰"),
            AchDef("all_tower_types", "Arsenal", "Place all tower types", "🎯"),
            AchDef("use_power", "Sorcerer", "Use a power for the first time", "🧙"),
            AchDef("max_tower", "Master Builder", "Upgrade a tower to level 5", "⬆️"),
            AchDef("rich", "Rich", "Have 500 gold at once", "💰"),
            AchDef("rich_1000", "Millionaire", "Have 1000 gold at once", "💎"),
            AchDef("score_1000", "Score Chaser", "Reach 1000 score", "🎯"),
            AchDef("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "💎"),
            AchDef("repaired_3", "Mechanic", "Repair the base 3 times in a run", "🔧"),
            AchDef("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "🌟"),
            AchDef("endless_10", "Endurance", "Reach wave 10 in endless mode", "♾️"),
            AchDef("kills_1000", "Genocide", "Kill 1000 enemies in one run", "💀"),
            AchDef("wave_100", "Centurion", "Reach wave 100", "💯"),
            AchDef("no_damage", "Untouchable", "Complete a wave without base taking damage", "🛡️"),
            AchDef("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "⚡"),
            AchDef("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "👑"),
            AchDef("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "⛏️"),
            AchDef("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "🏦"),
            AchDef("all_powers", "Elementalist", "Use all 4 powers in one run", "🌈"),
            AchDef("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "❤️‍🔥"),
            AchDef("trap_first", "Trapper", "Place your first trap", "🪤"),
            AchDef("trap_10", "Minefield", "Place 10 traps in one run", "💣"),
            AchDef("mine_triple", "Triple Threat", "Kill 3 enemies with one mine", "💥"),
            AchDef("bounty_first", "Bounty Hunter", "Complete your first bounty", "🎯"),
            AchDef("bounty_all", "Bounty King", "Complete all 3 bounties in one run", "👑"),
            AchDef("volcano_win", "Volcanic Victory", "Reach wave 15 on Volcano map", "🌋"),
            AchDef("combo_30", "Unstoppable", "Get a 30x combo", "🔥"),
            AchDef("combo_50", "Godlike", "Get a 50x combo", "⚡"),
            AchDef("streak_no_tower", "Lone Wolf", "Reach wave 5 with no towers", "🐺"),
            AchDef("all_maps", "Cartographer", "Play on all 8 maps", "🌍"),
            AchDef("boss_rush_5", "Gauntlet", "Defeat 5 bosses in Boss Rush", "🗡️"),
            AchDef("randomizer_win", "Chaos Master", "Reach wave 15 in Randomizer", "🎲"),
            AchDef("ability_all", "Tactician", "Use abilities on 5 tower types in one run", "✨"),
            AchDef("prestige_first", "Reborn", "Prestige for the first time", "👑"),
            AchDef("score_5000", "High Roller", "Reach 5000 score", "🏅"),
            AchDef("score_10000", "Legendary Score", "Reach 10000 score", "🥇")
        )
        return listOf(
            AchDef("first_kill", "Pierwsze trafienie", "Zabij pierwszego wroga", "🗡️"),
            AchDef("wave_5", "Ocalały", "Dotrzyj do fali 5", "🌊"),
            AchDef("wave_10", "Weteran", "Dotrzyj do fali 10", "⭐"),
            AchDef("wave_20", "Legenda", "Dotrzyj do fali 20", "🏆"),
            AchDef("wave_30", "Nieśmiertelny", "Dotrzyj do fali 30", "💀"),
            AchDef("wave_50", "Mityczny", "Dotrzyj do fali 50", "🔥"),
            AchDef("kills_50", "Zabójca", "Zabij 50 wrogów", "⚔️"),
            AchDef("kills_200", "Niszczyciel", "Zabij 200 wrogów", "💣"),
            AchDef("kills_500", "Anihilator", "Zabij 500 wrogów", "☠️"),
            AchDef("combo_10", "Król combo", "Zdobądź 10× combo", "🔗"),
            AchDef("combo_20", "Bóg combo", "Zdobądź 20× combo", "⛓️"),
            AchDef("boss_kill", "Pogromca bossów", "Zabij swojego pierwszego bossa", "👹"),
            AchDef("5_bosses", "Łowca bossów", "Zabij 5 bossów w jednym podejściu", "🐉"),
            AchDef("5_towers", "Architekt", "Postaw 5 wież", "🏗️"),
            AchDef("10_towers", "Forteca", "Postaw 10 wież", "🏰"),
            AchDef("all_tower_types", "Arsenał", "Postaw wszystkie typy wież", "🎯"),
            AchDef("use_power", "Czarodziej", "Użyj mocy po raz pierwszy", "🧙"),
            AchDef("max_tower", "Mistrz budowniczy", "Ulepsz wieżę do poziomu 5", "⬆️"),
            AchDef("rich", "Bogacz", "Miej 500 złota naraz", "💰"),
            AchDef("rich_1000", "Milioner", "Miej 1000 złota naraz", "💎"),
            AchDef("score_1000", "Łowca punktów", "Zdobądź 1000 punktów", "🎯"),
            AchDef("diamond_10", "Kolekcjoner", "Zdobądź 10 diamentów w jednym podejściu", "💎"),
            AchDef("repaired_3", "Mechanik", "Napraw bazę 3 razy w jednym podejściu", "🔧"),
            AchDef("upgrade_all", "Wszechstronny", "Kup wszystkie 4 ulepszenia", "🌟"),
            AchDef("endless_10", "Wytrzymałość", "Dotrzyj do fali 10 w trybie nieskończonym", "♾️"),
            AchDef("kills_1000", "Ludobójca", "Zabij 1000 wrogów w jednym podejściu", "💀"),
            AchDef("wave_100", "Centurion", "Dotrzyj do fali 100", "💯"),
            AchDef("no_damage", "Nietknięty", "Ukończ falę bez obrażeń bazy", "🛡️"),
            AchDef("speed_demon", "Demon prędkości", "Wygraj falę 10 na prędkości 3×", "⚡"),
            AchDef("10_bosses", "Legenda bossów", "Zabij 10 bossów w jednym podejściu", "👑"),
            AchDef("diamond_50", "Kopalnia diamentów", "Zdobądź 50 diamentów w jednym podejściu", "⛏️"),
            AchDef("gold_hoarder", "Skarbiec", "Miej 2000 złota naraz", "🏦"),
            AchDef("all_powers", "Elementalista", "Użyj wszystkich 4 mocy w jednym podejściu", "🌈"),
            AchDef("survivor_1hp", "Ostatnia szansa", "Wygraj falę z bazą na 1 HP", "❤️‍🔥"),
            AchDef("trap_first", "Łowca", "Postaw swoją pierwszą pułapkę", "🪤"),
            AchDef("trap_10", "Pole minowe", "Postaw 10 pułapek w jednym podejściu", "💣"),
            AchDef("mine_triple", "Potrójne zagrożenie", "Zabij 3 wrogów jedną miną", "💥"),
            AchDef("bounty_first", "Łowca nagród", "Wykonaj swoje pierwsze zlecenie", "🎯"),
            AchDef("bounty_all", "Król zleceń", "Wykonaj wszystkie 3 zlecenia w jednym podejściu", "👑"),
            AchDef("volcano_win", "Wulkaniczna wiktoria", "Dotrzyj do fali 15 na mapie Wulkan", "🌋"),
            AchDef("combo_30", "Nie do zatrzymania", "Zdobądź 30× combo", "🔥"),
            AchDef("combo_50", "Boski", "Zdobądź 50× combo", "⚡"),
            AchDef("streak_no_tower", "Samotny wilk", "Dotrzyj do fali 5 bez wież", "🐺"),
            AchDef("all_maps", "Kartograf", "Zagraj na wszystkich 8 mapach", "🌍"),
            AchDef("boss_rush_5", "Rękawica", "Pokonaj 5 bossów w Rajdzie Bossów", "🗡️"),
            AchDef("randomizer_win", "Mistrz chaosu", "Dotrzyj do fali 15 w trybie Losowym", "🎲"),
            AchDef("ability_all", "Taktyk", "Użyj umiejętności 5 typów wież w jednym podejściu", "✨"),
            AchDef("prestige_first", "Odrodzony", "Zdobądź prestiż po raz pierwszy", "👑"),
            AchDef("score_5000", "Gracz wysokich stawek", "Zdobądź 5000 punktów", "🏅"),
            AchDef("score_10000", "Legendarny wynik", "Zdobądź 10000 punktów", "🥇")
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
    fun dashCooldownFmt(cd: Int) = if (isPl) "Dash: ${cd}s odnowienia" else "Dash cooldown: ${cd}s"
    val dashUsed get() = if (isPl) "💨 Dash! Obrażenia obszarowe!" else "💨 Dash! AoE damage!"
    val cantDash get() = if (isPl) "Nie możesz teraz dashować!" else "Can't dash right now!"
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
            else -> name
        }
    }

    // ─── Help Activity ───
    fun helpText(): String {
        if (!isPl) return """
🏰 BASE
Your base is at the bottom of the map. Enemies follow paths toward it. If an enemy reaches the base, it deals damage. Game over when base HP reaches 0. Upgrade your base HP with the 🏰 BASE button.

🗡️ PLAYER
Your hero auto-attacks the nearest enemy in range. Tap anywhere on the map to move. Upgrade ATK (damage) and SPD (movement speed) using the bottom bar buttons.

💨 DASH
Tap the Dash button, then tap a location to teleport there instantly. Deals AoE damage along the dash path. 8-second cooldown.

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

🗡️ GRACZ
Twój bohater automatycznie atakuje najbliższego wroga. Dotknij mapę, by się poruszyć. Ulepszaj ATK (obrażenia) i SPD (prędkość) w dolnym pasku.

💨 DASH
Dotknij Dash, potem lokalizację, by się teleportować. Zadaje obrażenia po drodze. 8s odnowienia.

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
}
