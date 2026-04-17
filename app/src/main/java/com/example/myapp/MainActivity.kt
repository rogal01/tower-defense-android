package com.example.myapp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.example.myapp.databinding.ActivityMainBinding

class MainActivity : ImmersiveActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var uiController: MainGameUiController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SoundManager.init(this)
        GameStrings.init(this)

        val engine = binding.gameView.getEngine()
        MainGameSessionConfigurator(binding, engine).apply(intent, this)

        uiController = MainGameUiController(this, binding)
        uiController.bind(onExitToMenu = ::finish)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!uiController.handleBackPressed()) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            },
        )
    }

    override fun onPause() {
        super.onPause()
        uiController.onPause()
    }

    override fun onResume() {
        super.onResume()
        uiController.onResume()
    }
}
