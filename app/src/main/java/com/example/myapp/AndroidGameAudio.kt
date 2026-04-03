package com.example.myapp

import com.example.myapp.game.GameAudio
import com.example.myapp.game.SfxType

/** Android audio adapter — delegates to the SoundManager singleton */
class AndroidGameAudio : GameAudio {
    override fun play(sfx: SfxType) = SoundManager.play(sfx)
}
