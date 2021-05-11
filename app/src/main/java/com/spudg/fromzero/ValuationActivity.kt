package com.spudg.fromzero

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.spudg.fromzero.databinding.*
import java.text.DecimalFormat
import java.util.*

class ValuationActivity : AppCompatActivity() {

    private lateinit var bindingValuation: ActivityValuationBinding
    private lateinit var bindingAddValuation: DialogAddValuationBinding
    private lateinit var bindingUpdateValuation: DialogUpdateValuationBinding
    private lateinit var bindingDeleteValuation: DialogDeleteValuationBinding
    private lateinit var bindingUpdateAL: DialogUpdateAlBinding
    private lateinit var bindingDeleteAL: DialogDeleteAlBinding

    private lateinit var bindingMYPicker: MonthYearPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingValuation = ActivityValuationBinding.inflate(layoutInflater)
        val view = bindingValuation.root
        setContentView(view)

        setUpTitles()
        setUpValuationList()
        setUpChart()

        bindingValuation.addValuation.setOnClickListener {
            addValuation()
        }

        bindingValuation.alUpdate.setOnClickListener {
            updateAL()
        }

        bindingValuation.alDelete.setOnClickListener {
            deleteAL()
        }

        bindingValuation.backToMainFromAL.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun setUpChart() {


        if (getValuationList(Globals.alSelected).size > 1) {
            bindingValuation.valuationChart.visibility = View.VISIBLE
            bindingValuation.tvNoDataForChart.visibility = View.GONE

            val valuationHandler = ValuationHandler(this, null)
            val valuations = valuationHandler.getValuationsForAL(Globals.alSelected)
            val lineEntries: ArrayList<BarEntry> = arrayListOf()

            valuations.sortBy { it.date }

            for (i in 0 until valuations.size) {
                lineEntries.add(BarEntry(i.toFloat(), valuations[i].value.toFloat()))
            }


            val dataSetLine = LineDataSet(lineEntries as List<Entry>?, "")
            val dataLine = LineData(dataSetLine)
            dataSetLine.color = R.color.colorAccent

            val chartLine: LineChart = bindingValuation.valuationChart
            if (lineEntries.size > 0) {
                chartLine.data = dataLine
            }

            dataLine.setDrawValues(false)

            dataSetLine.setDrawFilled(true)
            dataSetLine.fillDrawable = ContextCompat.getDrawable(this, R.drawable.gradient)

            dataSetLine.setDrawCircles(false)

            chartLine.animateY(800)
            chartLine.setNoDataText("No valuations added yet.")
            chartLine.setNoDataTextColor(0xff000000.toInt())
            chartLine.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartLine.xAxis.setDrawGridLines(false)
            chartLine.xAxis.setDrawLabels(false)
            chartLine.axisLeft.setDrawLabels(false)
            chartLine.axisLeft.setDrawGridLines(false)
            chartLine.xAxis.setDrawAxisLine(false)
            chartLine.axisLeft.setDrawAxisLine(false)
            chartLine.setTouchEnabled(false)
            chartLine.axisRight.isEnabled = false
            chartLine.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chartLine.legend.isEnabled = false
            chartLine.description.isEnabled = false

            dataLine.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        val mFormat = DecimalFormat("###,###,##0.00")
                        mFormat.format(super.getFormattedValue(value).toFloat())
                    } else {
                        ""
                    }
                }
            })

            val l: Legend = chartLine.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)

            chartLine.invalidate()
        } else {
            bindingValuation.valuationChart.visibility = View.GONE
            bindingValuation.tvNoDataForChart.visibility = View.VISIBLE
        }


    }

    private fun deleteAL() {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        bindingDeleteAL = DialogDeleteAlBinding.inflate(layoutInflater)
        val view = bindingDeleteAL.root
        deleteDialog.setContentView(view)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val valuationHandler = ValuationHandler(this, null)
        val alHandler = ALHandler(this, null)

        if (alHandler.isAsset(Globals.alSelected)) {
            bindingDeleteAL.deleteALTitle.text = getString(R.string.delete_asset)
            bindingDeleteAL.deleteALWarning.text = getString(R.string.delete_asset_warn)
        } else {
            bindingDeleteAL.deleteALTitle.text = getString(R.string.delete_liability)
            bindingDeleteAL.deleteALWarning.text = getString(R.string.delete_liability_warn)
        }

        bindingDeleteAL.tvDelete.setOnClickListener {

            val associatedValuations = valuationHandler.getValuationsForAL(Globals.alSelected)
            for (valuation in associatedValuations) {
                valuationHandler.deleteValuation(valuationHandler.getValuation(valuation.id))
            }

            alHandler.deleteAL(alHandler.getAL(Globals.alSelected))

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            Toast.makeText(this, "Valuation deleted.", Toast.LENGTH_LONG).show()

            setUpValuationList()
            valuationHandler.close()
            deleteDialog.dismiss()
        }

        bindingDeleteAL.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    private fun updateAL() {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        bindingUpdateAL = DialogUpdateAlBinding.inflate(layoutInflater)
        val view = bindingUpdateAL.root
        updateDialog.setContentView(view)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val alHandler = ALHandler(this, null)
        val originalAL = alHandler.getAL(Globals.alSelected)

        bindingUpdateAL.etName.setText(originalAL.name)
        bindingUpdateAL.etNote.setText(originalAL.note)
        bindingUpdateAL.colourPicker.color = originalAL.colour.toInt()
        bindingUpdateAL.colourPicker.showOldCenterColor = false

        if (originalAL.al == 1) {
            bindingUpdateAL.updateALTitle.text = getString(R.string.update_asset)
        } else {
            bindingUpdateAL.updateALTitle.text = getString(R.string.update_liability)
        }

        bindingUpdateAL.tvUpdate.setOnClickListener {
            val name = bindingUpdateAL.etName.text.toString()
            val note = bindingUpdateAL.etNote.text.toString()
            val colour = bindingUpdateAL.colourPicker.color.toString()
            val updatedAL = ALModel(originalAL.id, originalAL.al, name, note, colour)
            if (name.isNotEmpty()) {
                alHandler.updateAL(updatedAL)
                setUpTitles()
                updateDialog.dismiss()
                if (originalAL.al == 1) {
                    Toast.makeText(this, "Asset updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Liability updated.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bindingUpdateAL.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    private fun setUpTitles() {

        val dbHandler = ALHandler(this, null)

        bindingValuation.alTitle.text = dbHandler.getALName(Globals.alSelected)
        if (dbHandler.isAsset(Globals.alSelected)) {
            bindingValuation.assetOrLiability.text = getString(R.string.asset)
        } else {
            bindingValuation.assetOrLiability.text = getString(R.string.liability)
        }

        bindingValuation.alColour.setBackgroundColor(
            dbHandler.getALColour(Globals.alSelected).toInt()
        )

    }

    fun isAsset(alID: Int): Boolean {
        val alHandler = ALHandler(this, null)
        return alHandler.isAsset(alID)
    }

    private fun getValuationList(al: Int): ArrayList<ValuationModel> {
        val dbHandler = ValuationHandler(this, null)
        val result = dbHandler.getValuationsForAL(al)
        dbHandler.close()
        return result
    }

    private fun setUpValuationList() {
        if (getValuationList(Globals.alSelected).size > 0) {
            bindingValuation.rvValuations.visibility = View.VISIBLE
            bindingValuation.tvNoValuations.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingValuation.rvValuations.layoutManager = manager
            val assetAdapter = ValuationAdapter(this, getValuationList(Globals.alSelected))
            bindingValuation.rvValuations.adapter = assetAdapter
        } else {
            bindingValuation.rvValuations.visibility = View.GONE
            bindingValuation.tvNoValuations.visibility = View.VISIBLE
        }

    }

    private fun addValuation() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddValuation = DialogAddValuationBinding.inflate(layoutInflater)
        val view = bindingAddValuation.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var monthPicked = Calendar.getInstance()[Calendar.MONTH]
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddValuation.date.text = getString(
            R.string.month_year,
            Globals.getShortMonth(monthPicked + 1),
            yearPicked.toString()
        )

        bindingAddValuation.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingMYPicker = MonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingMYPicker.mypMonth.maxValue = 12
            bindingMYPicker.mypMonth.minValue = 1
            bindingMYPicker.mypYear.maxValue = 2999
            bindingMYPicker.mypYear.minValue = 1000

            bindingMYPicker.mypMonth.value = monthPicked + 1
            bindingMYPicker.mypYear.value = yearPicked

            bindingMYPicker.mypMonth.displayedValues = Globals.monthsShortArray

            bindingMYPicker.mypMonth.setOnValueChangedListener { _, _, newVal ->
                monthPicked = newVal - 1
            }

            bindingMYPicker.mypYear.setOnValueChangedListener { _, _, newVal ->
                yearPicked = newVal
            }

            bindingMYPicker.submitDmy.setOnClickListener {
                bindingAddValuation.date.text =
                    getString(
                        R.string.month_year,
                        Globals.getShortMonth(monthPicked + 1),
                        yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingMYPicker.mypMonth.wrapSelectorWheel = true
            bindingMYPicker.mypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddValuation.tvAdd.setOnClickListener {

            val valuationHandler = ValuationHandler(this, null)
            val alHandler = ALHandler(this, null)

            var valExistsForMonthYear = false

            val valsForCheck = valuationHandler.getValuationsForAL(Globals.alSelected)
            for (valuation in valsForCheck) {
                val calForCheck = Calendar.getInstance()
                calForCheck.timeInMillis = valuation.date.toLong()
                if (calForCheck.get(Calendar.MONTH) == monthPicked && calForCheck.get(Calendar.YEAR) == yearPicked) {
                    valExistsForMonthYear = true
                }
            }

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked, 1)

            val value: String = if (alHandler.isAsset(Globals.alSelected)) {
                bindingAddValuation.etValue.text.toString()
            } else {
                ((bindingAddValuation.etValue.text.toString().toFloat()) * -1).toString()
            }

            val date = calendar.timeInMillis.toString()

            if (value.isNotEmpty()) {

                if (!valExistsForMonthYear) {

                    valuationHandler.addValuation(
                        ValuationModel(
                            0,
                            Globals.alSelected,
                            value,
                            date
                        )
                    )

                    Toast.makeText(this, "Valuation added.", Toast.LENGTH_LONG).show()

                    setUpValuationList()
                    setUpChart()
                    valuationHandler.close()
                    addDialog.dismiss()
                } else {
                    Toast.makeText(
                        this,
                        "A valuation already exists for this month.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

        }
        addDialog.show()

        bindingAddValuation.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }
    }

    fun updateValuation(valuation: ValuationModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        bindingUpdateValuation = DialogUpdateValuationBinding.inflate(layoutInflater)
        val view = bindingUpdateValuation.root
        updateDialog.setContentView(view)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val alHandler = ALHandler(this, null)
        val valuationHandler = ValuationHandler(this, null)

        val cal = Calendar.getInstance()
        cal.timeInMillis = valuation.date.toLong()
        var monthPicked = cal.get(Calendar.MONTH)
        var yearPicked = cal.get(Calendar.YEAR)

        if (alHandler.isAsset(Globals.alSelected)) {
            bindingUpdateValuation.etValue.setText((valuation.value.toFloat()).toString())
        } else {
            bindingUpdateValuation.etValue.setText((valuation.value.toFloat() * -1).toString())
        }

        bindingUpdateValuation.date.text = getString(
            R.string.month_year,
            Globals.getShortMonth(monthPicked + 1),
            yearPicked.toString()
        )

        bindingUpdateValuation.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingMYPicker = MonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingMYPicker.mypMonth.maxValue = 12
            bindingMYPicker.mypMonth.minValue = 1
            bindingMYPicker.mypYear.maxValue = 2999
            bindingMYPicker.mypYear.minValue = 1000

            bindingMYPicker.mypMonth.value = monthPicked + 1
            bindingMYPicker.mypYear.value = yearPicked

            bindingMYPicker.mypMonth.displayedValues = Globals.monthsShortArray

            bindingMYPicker.mypMonth.setOnValueChangedListener { _, _, newVal ->
                monthPicked = newVal - 1
            }

            bindingMYPicker.mypYear.setOnValueChangedListener { _, _, newVal ->
                yearPicked = newVal
            }

            bindingMYPicker.submitDmy.setOnClickListener {
                bindingUpdateValuation.date.text =
                    getString(
                        R.string.month_year,
                        Globals.getShortMonth(monthPicked + 1),
                        yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingMYPicker.mypMonth.wrapSelectorWheel = true
            bindingMYPicker.mypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingUpdateValuation.tvUpdate.setOnClickListener {

            var valExistsForMonthYear = false

            val calForExisting = Calendar.getInstance()
            calForExisting.timeInMillis = valuation.date.toLong()

            val calForUpdate = Calendar.getInstance()
            calForUpdate.set(yearPicked, monthPicked, 1)
            val date = calForUpdate.timeInMillis.toString()

            if ((monthPicked != calForExisting.get(Calendar.MONTH)) || (yearPicked != calForExisting.get(
                    Calendar.YEAR
                ))
            ) {
                val valsForCheck = valuationHandler.getValuationsForAL(Globals.alSelected)
                for (valCheck in valsForCheck) {
                    val calForCheck = Calendar.getInstance()
                    calForCheck.timeInMillis = valCheck.date.toLong()
                    if (((calForCheck.get(Calendar.MONTH) == monthPicked) && (calForCheck.get(
                            Calendar.YEAR
                        ) == yearPicked))
                    ) {
                        valExistsForMonthYear = true
                    }
                }
            }

            val value: String = if (alHandler.isAsset(Globals.alSelected)) {
                bindingUpdateValuation.etValue.text.toString()
            } else {
                (bindingUpdateValuation.etValue.text.toString().toFloat() * -1).toString()
            }

            if (value.isNotEmpty()) {

                if (!valExistsForMonthYear) {

                    valuationHandler.updateValuation(
                        ValuationModel(
                            valuation.id,
                            Globals.alSelected,
                            value,
                            date
                        )
                    )

                    Toast.makeText(this, "Valuation updated.", Toast.LENGTH_LONG).show()

                    setUpValuationList()
                    setUpChart()
                    valuationHandler.close()
                    updateDialog.dismiss()
                } else {
                    Toast.makeText(
                        this,
                        "A valuation already exists for this month.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

        }

        bindingUpdateValuation.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteValuation(valuation: ValuationModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        bindingDeleteValuation = DialogDeleteValuationBinding.inflate(layoutInflater)
        val view = bindingDeleteValuation.root
        deleteDialog.setContentView(view)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDeleteValuation.tvDelete.setOnClickListener {
            val valuationHandler = ValuationHandler(this, null)
            valuationHandler.deleteValuation(ValuationModel(valuation.id, 0, "", ""))

            Toast.makeText(this, "Valuation deleted.", Toast.LENGTH_LONG).show()

            setUpValuationList()
            setUpChart()
            valuationHandler.close()
            deleteDialog.dismiss()
        }

        bindingDeleteValuation.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()

    }

}