import SwiftUI
import shared

/// Main game container — hosts the SpriteKit game view with full HUD
struct GameContainerView: View {
    @StateObject private var viewModel = GameViewModel()

    var body: some View {
        ZStack {
            // Game rendering area
            GameSpriteView(viewModel: viewModel)
                .ignoresSafeArea()

            // HUD overlay
            VStack(spacing: 0) {
                // ─── Top bar: wave, score, combo, gold, diamonds ───
                HStack {
                    HudBadge(text: "⚔️ Wave \(viewModel.wave)", color: .white)

                    if viewModel.score > 0 {
                        HudBadge(text: "⭐ \(viewModel.score)", color: .white)
                    }
                    if viewModel.comboCount >= 2 {
                        HudBadge(text: "🔥 \(viewModel.comboCount)x", color: .orange)
                    }

                    Spacer()

                    HudBadge(text: "💰 \(viewModel.gold)", color: .yellow)
                    HudBadge(text: "💎 \(viewModel.diamonds)", color: .cyan)
                }
                .padding(.horizontal, 8)
                .padding(.top, 4)

                // ─── Base HP bar ───
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.gray.opacity(0.4))
                            .frame(height: 8)
                        RoundedRectangle(cornerRadius: 4)
                            .fill(hpColor)
                            .frame(width: geo.size.width * CGFloat(viewModel.baseHp / max(viewModel.maxBaseHp, 1)), height: 8)
                    }
                }
                .frame(height: 8)
                .padding(.horizontal, 8)
                .padding(.top, 4)

                Spacer()

                // ─── Game over / paused overlay ───
                if viewModel.gameOver {
                    VStack(spacing: 8) {
                        Text("💀 GAME OVER")
                            .font(.largeTitle).bold()
                            .foregroundColor(.red)
                        Text("Wave: \(viewModel.wave)  |  Score: \(viewModel.score)")
                            .foregroundColor(.white)
                        Text("Kills: \(viewModel.totalKills)")
                            .foregroundColor(.white)
                        Button("🔄 Restart") { viewModel.restart() }
                            .font(.title2).bold()
                            .padding(.horizontal, 24)
                            .padding(.vertical, 10)
                            .background(Color.green.opacity(0.8))
                            .foregroundColor(.white)
                            .cornerRadius(12)
                    }
                    .padding(20)
                    .background(Color.black.opacity(0.7))
                    .cornerRadius(16)

                    Spacer()
                }

                if viewModel.isPaused && !viewModel.gameOver {
                    VStack(spacing: 8) {
                        Text("⏸️ PAUSED")
                            .font(.largeTitle).bold()
                            .foregroundColor(.white)
                        Text("Wave: \(viewModel.wave)  |  Score: \(viewModel.score)")
                            .foregroundColor(.white)
                    }
                    .padding(20)
                    .background(Color.black.opacity(0.7))
                    .cornerRadius(16)

                    Spacer()
                }

                // ─── Powers row ───
                HStack(spacing: 6) {
                    PowerButton(emoji: "🔥", name: "Fire", cost: 40) {
                        viewModel.usePower(.fireball)
                    }
                    PowerButton(emoji: "❄️", name: "Freeze", cost: 30) {
                        viewModel.usePower(.freeze)
                    }
                    PowerButton(emoji: "💚", name: "Heal", cost: 25) {
                        viewModel.usePower(.heal)
                    }
                    PowerButton(emoji: "⚡", name: "Bolt", cost: 50) {
                        viewModel.usePower(.lightning)
                    }

                    Spacer()

                    // Speed & pause controls
                    Button(action: { viewModel.cycleSpeed() }) {
                        Text("▶\(viewModel.speedMultiplier)x")
                            .font(.caption).bold()
                            .frame(width: 44, height: 36)
                            .background(Color.blue.opacity(0.7))
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                    Button(action: { viewModel.togglePause() }) {
                        Text(viewModel.isPaused ? "▶️" : "⏸")
                            .font(.caption)
                            .frame(width: 36, height: 36)
                            .background(Color.gray.opacity(0.7))
                            .cornerRadius(8)
                    }
                }
                .padding(.horizontal, 8)

                // ─── Trap buttons row ───
                HStack(spacing: 6) {
                    TrapButton(emoji: "🗡️", name: "Spike", cost: 30) {
                        viewModel.selectTrap(.spike)
                    }
                    TrapButton(emoji: "🟤", name: "Tar", cost: 40) {
                        viewModel.selectTrap(.tar)
                    }
                    TrapButton(emoji: "💣", name: "Mine", cost: 60) {
                        viewModel.selectTrap(.mine)
                    }

                    Spacer()

                    // Utility buttons
                    ActionButton(emoji: "🪨", label: "Block") {
                        viewModel.selectBlockade()
                    }
                    ActionButton(emoji: "🔧", label: "Repair") {
                        viewModel.repairBase()
                    }
                }
                .padding(.horizontal, 8)
                .padding(.top, 4)

                // ─── Tower buttons row ───
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        TowerButton(name: "Arrow", cost: 30, emoji: "🏹") {
                            viewModel.selectTower(.arrow)
                        }
                        TowerButton(name: "Magic", cost: 60, emoji: "🧨") {
                            viewModel.selectTower(.magic)
                        }
                        TowerButton(name: "Cannon", cost: 100, emoji: "💣") {
                            viewModel.selectTower(.cannon)
                        }
                        TowerButton(name: "Poison", cost: 80, emoji: "☠️") {
                            viewModel.selectTower(.poison)
                        }
                        TowerButton(name: "Tesla", cost: 120, emoji: "⚡") {
                            viewModel.selectTower(.tesla)
                        }
                        TowerButton(name: "Ice", cost: 70, emoji: "❄️") {
                            viewModel.selectTower(.ice)
                        }
                        TowerButton(name: "Flame", cost: 90, emoji: "🔥") {
                            viewModel.selectTower(.flame)
                        }
                        TowerButton(name: "Necro", cost: 110, emoji: "💀") {
                            viewModel.selectTower(.necro)
                        }
                        TowerButton(name: "Ballista", cost: 140, emoji: "🎯") {
                            viewModel.selectTower(.ballista)
                        }
                        TowerButton(name: "Vortex", cost: 100, emoji: "🌀") {
                            viewModel.selectTower(.vortex)
                        }
                    }
                    .padding(.horizontal, 8)
                }
                .padding(.vertical, 4)

                // ─── Upgrade buttons row ───
                HStack(spacing: 6) {
                    ActionButton(emoji: "⚔️", label: "ATK") { viewModel.upgradeAttack() }
                    ActionButton(emoji: "👟", label: "SPD") { viewModel.upgradeSpeed() }
                    ActionButton(emoji: "❤️", label: "HP") { viewModel.upgradeHp() }
                    ActionButton(emoji: "🏰", label: "BASE") { viewModel.upgradeBase() }
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.bottom, 8)
            }
        }
    }

    private var hpColor: Color {
        let pct = viewModel.baseHp / max(viewModel.maxBaseHp, 1)
        if pct > 0.5 { return .green }
        if pct > 0.25 { return .orange }
        return .red
    }
}

// MARK: - Reusable HUD components

struct HudBadge: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text)
            .font(.caption).bold()
            .foregroundColor(color)
            .padding(.horizontal, 6)
            .padding(.vertical, 3)
            .background(Color.black.opacity(0.5))
            .cornerRadius(6)
    }
}

struct TowerButton: View {
    let name: String
    let cost: Int
    let emoji: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 1) {
                Text(emoji).font(.title3)
                Text("\(cost)g")
                    .font(.system(size: 9))
                    .foregroundColor(.white)
            }
            .frame(width: 52, height: 48)
            .background(Color.gray.opacity(0.7))
            .cornerRadius(8)
        }
    }
}

struct PowerButton: View {
    let emoji: String
    let name: String
    let cost: Int
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 1) {
                Text(emoji).font(.body)
                Text("\(cost)g")
                    .font(.system(size: 8))
                    .foregroundColor(.white)
            }
            .frame(width: 44, height: 40)
            .background(Color.purple.opacity(0.6))
            .cornerRadius(8)
        }
    }
}

struct TrapButton: View {
    let emoji: String
    let name: String
    let cost: Int
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 1) {
                Text(emoji).font(.body)
                Text("\(cost)g")
                    .font(.system(size: 8))
                    .foregroundColor(.white)
            }
            .frame(width: 44, height: 40)
            .background(Color.brown.opacity(0.6))
            .cornerRadius(8)
        }
    }
}

struct ActionButton: View {
    let emoji: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 1) {
                Text(emoji).font(.body)
                Text(label)
                    .font(.system(size: 8))
                    .foregroundColor(.white)
            }
            .frame(width: 44, height: 40)
            .background(Color.gray.opacity(0.6))
            .cornerRadius(8)
        }
    }
}
