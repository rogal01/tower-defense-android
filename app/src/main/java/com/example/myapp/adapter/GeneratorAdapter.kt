package com.example.myapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.GameManager
import com.example.myapp.R
import com.example.myapp.model.Generator

class GeneratorAdapter(
    private val generators: List<Generator>,
    private val gameManager: GameManager,
    private val onBuy: (Generator) -> Unit
) : RecyclerView.Adapter<GeneratorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.text_emoji)
        val name: TextView = view.findViewById(R.id.text_name)
        val level: TextView = view.findViewById(R.id.text_level)
        val goldPerSec: TextView = view.findViewById(R.id.text_gold_per_sec)
        val buyButton: Button = view.findViewById(R.id.btn_buy)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_next_level)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_generator, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gen = generators[position]
        val context = holder.itemView.context

        holder.emoji.text = gen.emoji
        holder.name.text = gen.name
        holder.level.text = "Lv. ${gen.level}"

        if (gen.level > 0) {
            holder.goldPerSec.text = "${GameManager.formatNumber(gen.currentGoldPerSec())}/s"
            holder.goldPerSec.visibility = View.VISIBLE
        } else {
            holder.goldPerSec.visibility = View.GONE
        }

        val cost = gen.nextCost()
        holder.buyButton.text = "\uD83D\uDCB0 ${GameManager.formatNumber(cost)}"

        val canAfford = gameManager.canAfford(gen)
        holder.buyButton.isEnabled = canAfford
        holder.buyButton.alpha = if (canAfford) 1f else 0.5f

        // Progress toward next milestone (every 10 levels)
        val progressInTen = gen.level % 10
        holder.progressBar.progress = progressInTen * 10

        holder.buyButton.setOnClickListener { onBuy(gen) }

        // Dim locked generators (can't afford first purchase)
        if (gen.level == 0 && !canAfford) {
            holder.itemView.alpha = 0.4f
        } else {
            holder.itemView.alpha = 1f
        }
    }

    override fun getItemCount() = generators.size
}
