package com.example.habizen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.databinding.ItemMoodEntryBinding
import com.example.habizen.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onDeleteClicked: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    inner class MoodViewHolder(private val binding: ItemMoodEntryBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                tvMoodEmoji.text = moodEntry.emoji
                tvMoodName.text = moodEntry.moodName
                
                // Format timestamp
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = Date(moodEntry.timestamp)
                
                tvTimestamp.text = timeFormat.format(date)
                tvDate.text = dateFormat.format(date)
                
                // Show note if available
                if (moodEntry.note.isNotEmpty()) {
                    tvMoodNote.text = "\"${moodEntry.note}\""
                    tvMoodNote.visibility = View.VISIBLE
                } else {
                    tvMoodNote.visibility = View.GONE
                }
                
                tvDelete.setOnClickListener {
                    onDeleteClicked(moodEntry)
                }
            }
        }
    }
}
