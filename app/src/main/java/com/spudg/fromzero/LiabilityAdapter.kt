package com.spudg.fromzero

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.fromzero.databinding.AssetLiabilityRowBinding
import java.text.SimpleDateFormat
import java.util.*

class LiabilityAdapter(private val context: Context, private val items: ArrayList<LiabilityModel>) :
        RecyclerView.Adapter<LiabilityAdapter.LiabilityViewHolder>() {

    inner class LiabilityViewHolder(val binding: AssetLiabilityRowBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiabilityViewHolder {
        val binding = AssetLiabilityRowBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return LiabilityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LiabilityViewHolder, position: Int) {

        with(holder) {
            val liability = items[position]

            binding.value.text = liability.value
            binding.colour.setBackgroundColor(liability.colour.toInt())
            binding.name.text = liability.name
            binding.date.text = liability.date

            binding.mainRowLayout.setOnClickListener {
                if (context is MainActivity) {
                    //context.updateLiability(liability)
                }
            }

            binding.mainRowLayout.setOnLongClickListener {
                if (context is MainActivity) {
                    //context.deleteLiability(liability)
                }
                true
            }

        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}