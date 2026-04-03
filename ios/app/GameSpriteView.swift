import SwiftUI
import SpriteKit
import shared

/// Wraps SpriteKit scene in a SwiftUI view for game rendering
struct GameSpriteView: UIViewRepresentable {
    @ObservedObject var viewModel: GameViewModel

    func makeUIView(context: Context) -> SKView {
        let skView = SKView()
        skView.ignoresSiblingOrder = true
        skView.showsFPS = false
        skView.showsNodeCount = false
        let scene = GameScene(size: UIScreen.main.bounds.size, viewModel: viewModel)
        scene.scaleMode = .resizeFill
        skView.presentScene(scene)
        return skView
    }

    func updateUIView(_ uiView: SKView, context: Context) {
        // Scene updates handled by SpriteKit game loop
    }
}

// MARK: - SpriteKit Game Scene

class GameScene: SKScene {
    private weak var viewModel: GameViewModel?
    private var lastUpdateTime: TimeInterval = 0

    // Render layers
    private let terrainLayer = SKNode()
    private let pathLayer = SKNode()
    private let trapLayer = SKNode()
    private let towerLayer = SKNode()
    private let enemyLayer = SKNode()
    private let projectileLayer = SKNode()
    private let effectLayer = SKNode()

    // Cached nodes for efficient updates
    private var towerNodes: [String: SKNode] = [:]
    private var enemyNodes: [String: SKNode] = [:]
    private var trapNodes: [String: SKNode] = [:]

    // Color palette matching Android version
    private let grassColor = UIColor(red: 0.3, green: 0.6, blue: 0.2, alpha: 1)
    private let pathColor = UIColor(red: 0.6, green: 0.5, blue: 0.3, alpha: 1)
    private let waterColor = UIColor(red: 0.2, green: 0.4, blue: 0.8, alpha: 0.6)

    init(size: CGSize, viewModel: GameViewModel) {
        self.viewModel = viewModel
        super.init(size: size)
        backgroundColor = grassColor

        // Set engine screen dimensions
        viewModel.engine.screenW = Float(size.width)
        viewModel.engine.screenH = Float(size.height)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) not supported")
    }

    override func didMove(to view: SKView) {
        // Add render layers in order (back to front)
        addChild(terrainLayer)
        addChild(pathLayer)
        addChild(trapLayer)
        addChild(towerLayer)
        addChild(enemyLayer)
        addChild(projectileLayer)
        addChild(effectLayer)

        drawTerrain()
    }

    // MARK: - Touch handling

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let touch = touches.first, let vm = viewModel else { return }
        let loc = touch.location(in: self)
        vm.handleTap(x: Float(loc.x), y: Float(loc.y))
    }

    // MARK: - Game loop

    override func update(_ currentTime: TimeInterval) {
        guard let vm = viewModel else { return }

        let dt: TimeInterval
        if lastUpdateTime == 0 {
            dt = 1.0 / 60.0
        } else {
            dt = min(currentTime - lastUpdateTime, 1.0 / 30.0) // Cap at 30fps minimum
        }
        lastUpdateTime = currentTime

        vm.update(dt: Float(dt))

        renderPaths()
        renderTraps()
        renderTowers()
        renderEnemies()
        renderBase()
    }

    // MARK: - Rendering

    private func drawTerrain() {
        let bg = SKSpriteNode(color: grassColor, size: size)
        bg.position = CGPoint(x: size.width / 2, y: size.height / 2)
        bg.zPosition = -10
        terrainLayer.addChild(bg)
    }

    private func renderPaths() {
        pathLayer.removeAllChildren()
        guard let engine = viewModel?.engine else { return }

        let pathWidth: CGFloat = 36
        for pathway in engine.pathways {
            let points = pathway.points
            guard points.count >= 2 else { continue }

            for i in 0..<(points.count - 1) {
                let p1 = points[i]
                let p2 = points[i + 1]
                let dx = CGFloat(p2.x - p1.x)
                let dy = CGFloat(p2.y - p1.y)
                let length = sqrt(dx * dx + dy * dy)

                let segment = SKSpriteNode(color: pathColor, size: CGSize(width: length, height: pathWidth))
                segment.position = CGPoint(
                    x: CGFloat(p1.x) + dx / 2,
                    y: CGFloat(p1.y) + dy / 2
                )
                segment.zRotation = atan2(dy, dx)
                segment.zPosition = -5
                pathLayer.addChild(segment)
            }
        }
    }

    private func renderTraps() {
        trapLayer.removeAllChildren()
        guard let engine = viewModel?.engine else { return }

        for trap in engine.traps {
            let emoji: String
            switch trap.type {
            case .spike: emoji = "🗡️"
            case .tar: emoji = "🟤"
            case .mine: emoji = "💣"
            default: emoji = "⚠️"
            }
            let label = SKLabelNode(text: emoji)
            label.fontSize = 20
            label.position = CGPoint(x: CGFloat(trap.x), y: CGFloat(trap.y))
            label.zPosition = 1
            trapLayer.addChild(label)
        }
    }

    private func renderTowers() {
        towerLayer.removeAllChildren()
        guard let engine = viewModel?.engine else { return }

        for tower in engine.towers {
            let emoji = towerEmoji(for: tower.type)
            let node = SKLabelNode(text: emoji)
            node.fontSize = 28
            node.position = CGPoint(x: CGFloat(tower.x), y: CGFloat(tower.y))
            node.zPosition = 5

            // Level indicator
            if tower.level > 1 {
                let lvl = SKLabelNode(text: "Lv\(tower.level)")
                lvl.fontSize = 8
                lvl.fontColor = .yellow
                lvl.position = CGPoint(x: 0, y: -18)
                node.addChild(lvl)
            }

            // Range circle (faint)
            let range = SKShapeNode(circleOfRadius: CGFloat(tower.range))
            range.strokeColor = UIColor.white.withAlphaComponent(0.15)
            range.fillColor = .clear
            range.lineWidth = 0.5
            range.position = .zero
            node.addChild(range)

            towerLayer.addChild(node)
        }
    }

    private func renderEnemies() {
        enemyLayer.removeAllChildren()
        guard let engine = viewModel?.engine else { return }

        for enemy in engine.enemies {
            if enemy.hp <= 0 { continue }

            let emoji = enemyEmoji(for: enemy.type)
            let node = SKLabelNode(text: emoji)
            node.fontSize = 22
            node.position = CGPoint(x: CGFloat(enemy.x), y: CGFloat(enemy.y))
            node.zPosition = 10

            // HP bar above enemy
            let hpPct = CGFloat(enemy.hp / enemy.maxHp)
            let barWidth: CGFloat = 24
            let barBg = SKSpriteNode(color: .darkGray, size: CGSize(width: barWidth, height: 3))
            barBg.position = CGPoint(x: 0, y: 16)
            node.addChild(barBg)

            let barFg = SKSpriteNode(
                color: hpPct > 0.5 ? .green : (hpPct > 0.25 ? .orange : .red),
                size: CGSize(width: barWidth * hpPct, height: 3)
            )
            barFg.anchorPoint = CGPoint(x: 0, y: 0.5)
            barFg.position = CGPoint(x: -barWidth / 2, y: 16)
            node.addChild(barFg)

            // Elite crown
            if enemy.isElite {
                let crown = SKLabelNode(text: "👑")
                crown.fontSize = 10
                crown.position = CGPoint(x: 0, y: 22)
                node.addChild(crown)
            }

            enemyLayer.addChild(node)
        }
    }

    private func renderBase() {
        // Remove old base node
        effectLayer.childNode(withName: "base")?.removeFromParent()
        guard let engine = viewModel?.engine else { return }

        let base = SKLabelNode(text: "🏰")
        base.name = "base"
        base.fontSize = 36
        base.position = CGPoint(x: CGFloat(engine.baseX), y: CGFloat(engine.baseY))
        base.zPosition = 8
        effectLayer.addChild(base)
    }

    // MARK: - Emoji mappings

    private func towerEmoji(for type: TowerType) -> String {
        switch type {
        case .arrow: return "🏹"
        case .magic: return "🧨"
        case .cannon: return "💣"
        case .poison: return "☠️"
        case .tesla: return "⚡"
        case .ice: return "❄️"
        case .flame: return "🔥"
        case .necro: return "💀"
        case .ballista: return "🎯"
        case .vortex: return "🌀"
        default: return "🏗️"
        }
    }

    private func enemyEmoji(for type: EnemyType) -> String {
        switch type {
        case .goblin: return "👺"
        case .bat: return "🦇"
        case .slime: return "🟢"
        case .skeleton: return "💀"
        case .spider: return "🕷️"
        case .orc: return "👹"
        case .fastSkeleton: return "💨"
        case .demon: return "😈"
        case .dragon: return "🐉"
        case .armoredGolem: return "🪨"
        default: return "👾"
        }
    }
}
