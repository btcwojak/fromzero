package com.spudg.fromzero

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.fromzero.databinding.AssetLiabilityRowBinding
import java.text.SimpleDateFormat
import java.util.*

class AssetAdapter(private val context: Context, private val items: ArrayList<AssetModel>) :
        RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(val binding: AssetLiabilityRowBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = AssetLiabilityRowBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {

        with(holder) {
            val asset = items[position]

            binding.value.text = asset.value
            binding.colour.setBackgroundColor(asset.colour.toInt())
            binding.name.text = asset.name
            binding.date.text = asset.date

            binding.mainRowLayout.setOnClickListener {
                if (context is MainActivity) {
                    //context.updateAsset(asset)
                }
            }

            binding.mainRowLayout.setOnLongClickListener {
                if (context is MainActivity) {
                    //context.deleteAsset(asset)
                }
                true
            }

        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}