package com.spudg.fromzero

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.fromzero.databinding.AssetLiabilityRowBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ALAdapter(private val context: Context, private val items: ArrayList<ALModel>) :
    RecyclerView.Adapter<ALAdapter.ALViewHolder>() {

    inner class ALViewHolder(val binding: AssetLiabilityRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ALViewHolder {
        val binding = AssetLiabilityRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ALViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ALViewHolder, position: Int) {

        val formatter: NumberFormat = DecimalFormat("#,##0.00")

        with(holder) {
            val al = items[position]

            if (context is MainActivity) {

                binding.value.text = formatter.format(context.getALValue(al).toFloat())
            }
            binding.colour.setBackgroundColor(al.colour.toInt())
            binding.name.text = al.name
            binding.note.text = al.note

            if (context is MainActivity) {
                if (context.getLatestValuationForAL(al.id).toFloat() == 0F) {
                    binding.value.textSize = 12F
                    binding.name.textSize = 12F
                    binding.note.visibility = View.GONE
                }
            }

            binding.innerRowLayout.setOnClickListener {
                if (context is MainActivity) {
                    context.selectAL(al)
                }

            }

        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}