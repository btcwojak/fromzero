package com.spudg.fromzero

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import java.text.NumberFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingAssetLiability: DialogAssetLiabilityBinding

    private lateinit var bindingAddAsset: DialogAddAssetBinding
    private lateinit var bindingAddLiability: DialogAddLiabilityBinding

    private lateinit var bindingDMYPicker: DayMonthYearPickerBinding

    private val gbpFormatter: NumberFormat = DecimalFormat("#,##0")
    private val gbpFormatterP: NumberFormat = DecimalFormat("#,##0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

        setUpAssetList()
        setUpLiabilityList()
        setUpNetWorthHeading()
        setUpChart()


        bindingMain.addAssetLiability.setOnClickListener {
            val assetLiabilityDialog = Dialog(this, R.style.Theme_Dialog)
            assetLiabilityDialog.setCancelable(false)
            bindingAssetLiability = DialogAssetLiabilityBinding.inflate(layoutInflater)
            val viewAL = bindingAssetLiability.root
            assetLiabilityDialog.setContentView(viewAL)
            assetLiabilityDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            bindingAssetLiability.asset.setOnClickListener {
                addAsset()
                assetLiabilityDialog.dismiss()
            }
            bindingAssetLiability.liability.setOnClickListener {
                addLiability()
                assetLiabilityDialog.dismiss()
            }
            bindingAssetLiability.tvCancel.setOnClickListener {
                assetLiabilityDialog.dismiss()
            }
            assetLiabilityDialog.show()
        }


    }

    private fun setUpChart() {
        val valuationHandler = ValuationHandler(this, null)
        val valuations = valuationHandler.getAllValuations()

        val earliestValuationDate = valuations.sortedBy { it.date }.first().date
        val calEarly = Calendar.getInstance()
        calEarly.timeInMillis = earliestValuationDate.toLong()
        val earliestMonth = calEarly.get(Calendar.MONTH) + 1
        val earliestYear = calEarly.get(Calendar.YEAR)
        val earliestMonthNo = (earliestYear * 12) + earliestMonth

        val latestValuationDate = valuations.sortedBy { it.date }.last().date
        val calLate = Calendar.getInstance()
        calLate.timeInMillis = latestValuationDate.toLong()
        val latestMonth = calLate.get(Calendar.MONTH) + 1
        val latestYear = calLate.get(Calendar.YEAR)
        val latestMonthNo = (latestYear * 12) + latestMonth

        val numberOfXAxis = latestMonthNo - earliestMonthNo + 1

        val xAxisLabels = arrayListOf<String>()
        val yAxisLabels = arrayListOf<String>()
        repeat(numberOfXAxis) {
            if (((it + earliestMonth) % 12).toString().toInt() == 0) {
                xAxisLabels.add(Globals.getShortMonth(12))
            } else {
                xAxisLabels.add(Globals.getShortMonth((it + earliestMonth) % 12))
            }
            yAxisLabels.add(valuationHandler.getAveNetWorthForMonthYear(it + earliestMonthNo))
            Log.e("test", xAxisLabels[it])
            Log.e("test", yAxisLabels[it])
        }

        val lineEntries: ArrayList<BarEntry> = arrayListOf()

        for (i in 0 until yAxisLabels.size) {
            lineEntries.add(BarEntry(i.toFloat(), yAxisLabels[i].toFloat()))
        }


        val dataSetLine = LineDataSet(lineEntries as List<Entry>?, "")
        val dataLine = LineData(dataSetLine)
        dataSetLine.color = R.color.colorAccent

        val chartLine: LineChart = bindingMain.nwChart
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
        chartLine.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) {
                    xAxisLabels[value.toInt()]
                } else {
                    ""
                }
            }
        }
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


    }

    private fun setUpNetWorthHeading() {

        val alHandler = ALHandler(this, null)
        val valuationHandler = ValuationHandler(this, null)

        var runningAssetTotal = 0F
        for (asset in alHandler.getAllAssets()) {
            runningAssetTotal += valuationHandler.getLatestValuationForAL(asset.id).toFloat()
        }

        var runningLiabilityTotal = 0F
        for (liability in alHandler.getAllLiabilities()) {
            runningLiabilityTotal += valuationHandler.getLatestValuationForAL(liability.id)
                .toFloat()
        }
        bindingMain.latestNetWorth.text =
            gbpFormatter.format(runningAssetTotal + runningLiabilityTotal)
    }

    private fun getAssetList(): ArrayList<ALModel> {
        val dbHandler = ALHandler(this, null)
        val result = dbHandler.getAllAssets()
        dbHandler.close()
        return result
    }

    private fun getLiabilityList(): ArrayList<ALModel> {
        val dbHandler = ALHandler(this, null)
        val result = dbHandler.getAllLiabilities()
        dbHandler.close()
        return result
    }

    private fun setUpAssetList() {
        if (getAssetList().size > 0) {
            bindingMain.rvAssets.visibility = View.VISIBLE
            bindingMain.assetTitle.visibility = View.VISIBLE
            bindingMain.assetTotal.visibility = View.VISIBLE
            bindingMain.tvNoAssetsLiabilities.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingMain.rvAssets.layoutManager = manager
            val assetAdapter = ALAdapter(this, getAssetList())
            bindingMain.rvAssets.adapter = assetAdapter

            val alHandler = ALHandler(this, null)
            val valuationHandler = ValuationHandler(this, null)
            var runningAssetTotal = 0F
            for (asset in alHandler.getAllAssets()) {
                runningAssetTotal += valuationHandler.getLatestValuationForAL(asset.id).toFloat()
            }
            bindingMain.assetTotal.text = gbpFormatter.format(runningAssetTotal)

        } else {
            bindingMain.rvAssets.visibility = View.GONE
            bindingMain.assetTitle.visibility = View.GONE
            bindingMain.assetTotal.visibility = View.GONE
            if (getLiabilityList().size > 0) {
                bindingMain.tvNoAssetsLiabilities.visibility = View.GONE
            } else {
                bindingMain.tvNoAssetsLiabilities.visibility = View.VISIBLE
            }
        }

    }

    private fun setUpLiabilityList() {
        if (getLiabilityList().size > 0) {
            bindingMain.rvLiabilities.visibility = View.VISIBLE
            bindingMain.liabilityTitle.visibility = View.VISIBLE
            bindingMain.liabilityTotal.visibility = View.VISIBLE
            bindingMain.tvNoAssetsLiabilities.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingMain.rvLiabilities.layoutManager = manager
            val liabilityAdapter = ALAdapter(this, getLiabilityList())
            bindingMain.rvLiabilities.adapter = liabilityAdapter

            val alHandler = ALHandler(this, null)
            val valuationHandler = ValuationHandler(this, null)
            var runningLiabilityTotal = 0F
            for (liability in alHandler.getAllLiabilities()) {
                runningLiabilityTotal += valuationHandler.getLatestValuationForAL(liability.id)
                    .toFloat()
            }
            bindingMain.liabilityTotal.text = gbpFormatter.format(runningLiabilityTotal)

        } else {
            bindingMain.rvLiabilities.visibility = View.GONE
            bindingMain.liabilityTitle.visibility = View.GONE
            bindingMain.liabilityTotal.visibility = View.GONE
            if (getAssetList().size > 0) {
                bindingMain.tvNoAssetsLiabilities.visibility = View.GONE
            } else {
                bindingMain.tvNoAssetsLiabilities.visibility = View.VISIBLE
            }
        }
    }

    private fun addAsset() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddAsset = DialogAddAssetBinding.inflate(layoutInflater)
        val view = bindingAddAsset.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingAddAsset.colourPicker.showOldCenterColor = false

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddAsset.date.text = "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"

        bindingAddAsset.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYPicker = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYPicker.dmypDay.maxValue = 30
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYPicker.dmypDay.maxValue = 29
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYPicker.dmypDay.maxValue = 28
                bindingDMYPicker.dmypDay.minValue = 1
            } else {
                bindingDMYPicker.dmypDay.maxValue = 31
                bindingDMYPicker.dmypDay.minValue = 1
            }

            bindingDMYPicker.dmypMonth.maxValue = 12
            bindingDMYPicker.dmypMonth.minValue = 1
            bindingDMYPicker.dmypYear.maxValue = 2999
            bindingDMYPicker.dmypYear.minValue = 1000

            bindingDMYPicker.dmypDay.value = dayPicked
            bindingDMYPicker.dmypMonth.value = monthPicked
            bindingDMYPicker.dmypYear.value = yearPicked

            bindingDMYPicker.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYPicker.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYPicker.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYPicker.dmypDay.maxValue = 30
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 == 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 != 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                } else {
                    bindingDMYPicker.dmypDay.maxValue = 31
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYPicker.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYPicker.submitDmy.setOnClickListener {
                bindingAddAsset.date.text =
                    "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYPicker.dmypDay.wrapSelectorWheel = true
            bindingDMYPicker.dmypMonth.wrapSelectorWheel = true
            bindingDMYPicker.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddAsset.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked)

            val name = bindingAddAsset.etName.text.toString()
            val value = bindingAddAsset.etValue.text.toString()
            val note: String = if (bindingAddAsset.etNote.text != null) {
                bindingAddAsset.etNote.text.toString()
            } else {
                ""
            }
            val colour = bindingAddAsset.colourPicker.color.toString()
            val date = calendar.timeInMillis.toString()

            if (name.isNotEmpty() && value.isNotEmpty()) {

                val alHandler = ALHandler(this, null)
                val valuationHandler = ValuationHandler(this, null)

                alHandler.addAL(ALModel(0, 1, name, note, colour))
                valuationHandler.addValuation(
                    ValuationModel(
                        0,
                        alHandler.getLatestALID(),
                        value,
                        date
                    )
                )

                Toast.makeText(this, "Asset added.", Toast.LENGTH_LONG).show()

                setUpAssetList()
                setUpNetWorthHeading()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

        }
        addDialog.show()

        bindingAddAsset.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }
    }

    private fun addLiability() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddLiability = DialogAddLiabilityBinding.inflate(layoutInflater)
        val view = bindingAddLiability.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingAddLiability.colourPicker.showOldCenterColor = false

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddLiability.date.text =
            "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"

        bindingAddLiability.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYPicker = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYPicker.dmypDay.maxValue = 30
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYPicker.dmypDay.maxValue = 29
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYPicker.dmypDay.maxValue = 28
                bindingDMYPicker.dmypDay.minValue = 1
            } else {
                bindingDMYPicker.dmypDay.maxValue = 31
                bindingDMYPicker.dmypDay.minValue = 1
            }

            bindingDMYPicker.dmypMonth.maxValue = 12
            bindingDMYPicker.dmypMonth.minValue = 1
            bindingDMYPicker.dmypYear.maxValue = 2999
            bindingDMYPicker.dmypYear.minValue = 1000

            bindingDMYPicker.dmypDay.value = dayPicked
            bindingDMYPicker.dmypMonth.value = monthPicked
            bindingDMYPicker.dmypYear.value = yearPicked

            bindingDMYPicker.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYPicker.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYPicker.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYPicker.dmypDay.maxValue = 30
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 == 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 != 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                } else {
                    bindingDMYPicker.dmypDay.maxValue = 31
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYPicker.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYPicker.submitDmy.setOnClickListener {
                bindingAddLiability.date.text =
                    "$dayPicked ${Globals.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYPicker.dmypDay.wrapSelectorWheel = true
            bindingDMYPicker.dmypMonth.wrapSelectorWheel = true
            bindingDMYPicker.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddLiability.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked)

            val name = bindingAddLiability.etName.text.toString()
            val value = bindingAddLiability.etValue.text.toString()
            val note: String = if (bindingAddLiability.etNote.text != null) {
                bindingAddLiability.etNote.text.toString()
            } else {
                ""
            }
            val colour = bindingAddLiability.colourPicker.color.toString()
            val date = calendar.timeInMillis.toString()

            if (name.isNotEmpty() && value.isNotEmpty()) {

                val alHandler = ALHandler(this, null)
                val valuationHandler = ValuationHandler(this, null)

                alHandler.addAL(ALModel(0, 0, name, note, colour))
                valuationHandler.addValuation(
                    ValuationModel(
                        0,
                        alHandler.getLatestALID(),
                        (value.toFloat() * -1).toString(),
                        date
                    )
                )

                Toast.makeText(this, "Liability added.", Toast.LENGTH_LONG).show()

                setUpLiabilityList()
                setUpNetWorthHeading()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

        }
        addDialog.show()

        bindingAddLiability.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }
    }

    fun selectAL(al: ALModel) {
        Globals.alSelected = al.id
        val intent = Intent(this, ValuationActivity::class.java)
        startActivity(intent)
    }

    fun getALValue(al: ALModel): String {
        val valuationHandler = ValuationHandler(this, null)
        return valuationHandler.getLatestValuationForAL(al.id)
    }


}