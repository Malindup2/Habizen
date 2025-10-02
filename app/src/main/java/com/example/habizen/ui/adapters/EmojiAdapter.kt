package com.example.habizen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.databinding.ItemEmojiBinding

class EmojiAdapter(
    private val emojis: List<String>,
    private val onEmojiSelected: (String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val binding = ItemEmojiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EmojiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(emojis[position])
    }

    override fun getItemCount(): Int = emojis.size

    inner class EmojiViewHolder(private val binding: ItemEmojiBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: String) {
            binding.tvEmoji.text = emoji
            binding.tvEmoji.setOnClickListener {
                onEmojiSelected(emoji)
            }
        }
    }
}
