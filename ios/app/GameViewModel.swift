import Foundation
import shared

/// Bridges between the shared Kotlin GameEngine and Swift UI
class GameViewModel: ObservableObject {
    // Core HUD state
    @Published var wave: Int = 1
    @Published var gold: Int = 100
    @Published var diamonds: Int = 0
    @Published var gameOver: Bool = false
    @Published var score: Int = 0
    @Published var baseHp: Float = 100
    @Published var maxBaseHp: Float = 100
    @Published var comboCount: Int = 0
    @Published var totalKills: Int = 0
    @Published var isPaused: Bool = false
    @Published var waveInProgress: Bool = false

    // Placement mode
    enum PlacementMode: Equatable {
        case none
        case tower(TowerType)
        case trap(TrapType)
        case blockade
    }
    @Published var placementMode: PlacementMode = .none

    // Speed
    @Published var speedMultiplier: Int = 1

    private let prefs: GamePreferences
    private let audio: GameAudio
    let engine: GameEngine

    init() {
        prefs = IosGamePreferences(suiteName: "tower_defense_prefs")
        audio = SilentAudio.shared  // TODO: implement iOS audio
        engine = GameEngine(prefs: prefs, audio: audio)
    }

    // MARK: - Tower placement

    func selectTower(_ type: TowerType) {
        placementMode = .tower(type)
    }

    func selectTrap(_ type: TrapType) {
        placementMode = .trap(type)
    }

    func selectBlockade() {
        placementMode = .blockade
    }

    func cancelPlacement() {
        placementMode = .none
    }

    func handleTap(x: Float, y: Float) {
        switch placementMode {
        case .tower(let type):
            let _ = engine.placeTower(x: x, y: y, type: type)
            placementMode = .none
        case .trap(let type):
            let _ = engine.placeTrap(x: x, y: y, type: type)
            placementMode = .none
        case .blockade:
            // Blockade uses engine method if available
            placementMode = .none
        case .none:
            break
        }
    }

    // MARK: - Powers

    func usePower(_ type: PowerType) {
        let _ = engine.usePower(type: type)
    }

    // MARK: - Game controls

    func togglePause() {
        engine.isPaused = !engine.isPaused
    }

    func cycleSpeed() {
        speedMultiplier = speedMultiplier >= 3 ? 1 : speedMultiplier + 1
    }

    func restart() {
        engine.restart()
        speedMultiplier = 1
    }

    // MARK: - Upgrades

    func upgradeAttack() {
        // Engine handles cost check internally
    }

    func upgradeSpeed() {
    }

    func upgradeHp() {
    }

    func upgradeBase() {
    }

    func repairBase() {
    }

    // MARK: - Game loop

    /// Called by the game loop each frame
    func update(dt: Float) {
        let scaledDt = dt * Float(speedMultiplier)
        engine.update(dt: scaledDt)

        // Sync published properties
        wave = Int(engine.wave)
        gold = Int(engine.gold)
        diamonds = Int(engine.skillTree.diamonds)
        score = Int(engine.score)
        baseHp = engine.baseHp
        maxBaseHp = engine.maxBaseHp
        comboCount = Int(engine.comboCount)
        totalKills = Int(engine.totalKills)
        isPaused = engine.isPaused
        waveInProgress = engine.waveInProgress
        gameOver = engine.gameOver
    }
}
