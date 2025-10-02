package com.example.habizen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.data.Achievement
import com.example.habizen.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size

    inner class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            binding.tvEmoji.text = achievement.emoji
            binding.tvTitle.text = achievement.title
            binding.tvDescription.text = achievement.description

            if (achievement.isUnlocked) {
                binding.imgStatus.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
                binding.tvProgress.visibility = View.GONE
            } else if (achievement.target > 0) {
                binding.imgStatus.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
                binding.progress.max = achievement.target
                binding.progress.setProgressCompat(achievement.progress, true)
                binding.tvProgress.visibility = View.VISIBLE
                binding.tvProgress.text = "${achievement.progress}/${achievement.target}"
            } else {
                binding.imgStatus.visibility = View.GONE
                binding.progress.visibility = View.GONE
                binding.tvProgress.visibility = View.GONE
            }
        }
    }
}
