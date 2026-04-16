package com.example.myapp.game

/** Singleton holder for GameEngine reference, for use in Player logic. */
object GameEngineHolder {
    var engine: GameEngine? = null
}