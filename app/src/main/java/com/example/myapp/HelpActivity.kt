package com.example.myapp

import android.os.Bundle
import com.example.myapp.databinding.ActivityHelpBinding

class HelpActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        binding.textHelpContent.text = GameStrings.helpText()

        binding.btnBack.setOnClickListener { finish() }
    }
}
