package com.example.habizen.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habizen.databinding.ItemOnboardingBinding
import com.example.habizen.data.OnboardingPage

class OnboardingAdapter(private val pages: List<OnboardingPage>) : 
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingViewHolder(private val binding: ItemOnboardingBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPage) {
            binding.apply {
                tvTitle.text = page.title
                tvDescription.text = page.description

                // Load drawable via ContextCompat to avoid inflation issues and preserve colors
                val drawable = androidx.core.content.ContextCompat.getDrawable(
                    ivIllustration.context,
                    page.illustrationRes
                )

                // Ensure no tint is applied so the drawable appears as designed
                ivIllustration.imageTintList = null
                ivIllustration.setImageDrawable(drawable)
            }
        }
    }
}
