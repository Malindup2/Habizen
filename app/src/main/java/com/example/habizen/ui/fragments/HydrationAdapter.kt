package com.example.habizen.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.R
import com.example.habizen.data.HydrationData
import com.example.habizen.databinding.ItemHydrationBinding

class HydrationAdapter(
    private val onDeleteClick: (HydrationData) -> Unit
) : ListAdapter<HydrationData, HydrationAdapter.HydrationViewHolder>(HydrationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HydrationViewHolder {
        val binding = ItemHydrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HydrationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HydrationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateEntries(entries: List<HydrationData>) {
        submitList(entries)
    }

    inner class HydrationViewHolder(
        private val binding: ItemHydrationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hydrationData: HydrationData) {
            binding.apply {
                tvAmount.text = "${hydrationData.amount}ml"
                tvTime.text = hydrationData.time
                tvDate.text = hydrationData.date
                
                // Set appropriate icon based on amount
                val iconRes = when {
                    hydrationData.amount <= 250 -> R.drawable.ic_water_drop
                    hydrationData.amount <= 500 -> R.drawable.ic_water_glass
                    else -> R.drawable.ic_water_bottle
                }
                ivWaterIcon.setImageResource(iconRes)
                
                btnDelete.setOnClickListener {
                    onDeleteClick(hydrationData)
                }
            }
        }
    }

    private class HydrationDiffCallback : DiffUtil.ItemCallback<HydrationData>() {
        override fun areItemsTheSame(oldItem: HydrationData, newItem: HydrationData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HydrationData, newItem: HydrationData): Boolean {
            return oldItem == newItem
        }
    }
}
