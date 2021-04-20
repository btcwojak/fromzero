package com.spudg.fromzero

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.fromzero.databinding.AssetLiabilityRowBinding
import java.text.SimpleDateFormat
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

        with(holder) {
            val al = items[position]

            if (context is MainActivity) {
                binding.value.text = context.getALValue(al)
            }
            binding.colour.setBackgroundColor(al.colour.toInt())
            binding.name.text = al.name
            binding.note.text = al.note

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