package com.example.habizen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.databinding.ItemHabitBinding
import com.example.habizen.data.Habit

class HabitsAdapter(
    private val habits: List<Habit>,
    private val onHabitChecked: (Habit, Boolean) -> Unit,
    private val onEditClicked: (Habit) -> Unit,
    private val onDeleteClicked: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                tvHabitEmoji.text = habit.emoji
                tvHabitName.text = habit.name
                tvHabitGoal.text = "Goal: ${habit.goal}"
                
                // Set progress
                val progress = if (habit.goal > 0) {
                    (habit.currentProgress.toFloat() / habit.goal * 100).toInt()
                } else {
                    0
                }
                progressBar.progress = progress
                tvProgress.text = "${habit.currentProgress}/${habit.goal} completed"
                
                // Set completion status
                cbCompleted.isChecked = habit.isCompletedToday()
                
                // Click listeners
                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onHabitChecked(habit, isChecked)
                }
                
                tvEdit.setOnClickListener {
                    onEditClicked(habit)
                }
                
                tvDelete.setOnClickListener {
                    onDeleteClicked(habit)
                }
                
                // Increment progress on card click (if not completed)
                root.setOnClickListener {
                    if (!habit.isCompletedToday() && habit.currentProgress < habit.goal) {
                        val updatedHabit = habit.copy(currentProgress = habit.currentProgress + 1)
                        onHabitChecked(updatedHabit, habit.currentProgress + 1 >= habit.goal)
                    }
                }
            }
        }
    }
}
