package com.spudg.fromzero

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spudg.fromzero.databinding.ValuationRowBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ValuationAdapter(private val context: Context, private val items: ArrayList<ValuationModel>) :
    RecyclerView.Adapter<ValuationAdapter.ValuationViewHolder>() {

    inner class ValuationViewHolder(val binding: ValuationRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValuationViewHolder {
        val binding = ValuationRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ValuationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ValuationViewHolder, position: Int) {

        val formatter: NumberFormat = DecimalFormat("#,##0.00")

        with(holder) {
            val valuation = items[position]

            binding.value.text = formatter.format(valuation.value.toFloat())
            val cal = Calendar.getInstance()
            cal.timeInMillis = valuation.date.toLong()
            binding.date.text =
                "${Globals.getShortMonth(cal.get(Calendar.MONTH) + 1)} ${
                    cal.get(Calendar.YEAR)
                }"

            binding.mainRowLayout.setOnClickListener {
                if (context is ValuationActivity) {
                    context.updateValuation(valuation)
                }
            }

            binding.mainRowLayout.setOnLongClickListener {
                if (context is ValuationActivity) {
                    context.deleteValuation(valuation)
                }
                true
            }

        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}