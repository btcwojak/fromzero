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

    private lateinit var bindingMYPicker: MonthYearPickerBinding

    private val gbpFormatter: NumberFormat = DecimalFormat("#,##0")

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

        bindingMain.aboutBtn.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setUpChart() {
        val valuationHandler = ValuationHandler(this, null)
        val valuations = valuationHandler.getAllValuations()

        val earliestValuationDate = valuations.minByOrNull { it.date }!!.date
        val calEarly = Calendar.getInstance()
        calEarly.timeInMillis = earliestValuationDate.toLong()
        val earliestMonth = calEarly.get(Calendar.MONTH) + 1
        val earliestYear = calEarly.get(Calendar.YEAR)
        val earliestMonthNo = (earliestYear * 12) + earliestMonth

        val latestValuationDate = valuations.maxByOrNull { it.date }!!.date
        val calLate = Calendar.getInstance()
        calLate.timeInMillis = latestValuationDate.toLong()
        val latestMonth = calLate.get(Calendar.MONTH)
        val latestYear = calLate.get(Calendar.YEAR)
        val latestMonthNo = (latestYear * 12) + latestMonth

        val numberOfXAxis = latestMonthNo - earliestMonthNo + 2

        if (numberOfXAxis > 1) {
            bindingMain.nwChart.visibility = View.VISIBLE
            bindingMain.tvNoDataForChart.visibility = View.GONE
            val xAxisLabels = arrayListOf<String>()
            val yAxisLabels = arrayListOf<String>()
            repeat(numberOfXAxis) {
                if (((it + earliestMonth) % 12).toString().toInt() == 0) {
                    xAxisLabels.add(Globals.getShortMonth(12))
                } else {
                    xAxisLabels.add(Globals.getShortMonth((it + earliestMonth) % 12))
                }
                yAxisLabels.add(valuationHandler.getNetWorthForMonthYear(it + earliestMonthNo - 1))
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
            chartLine.xAxis.setDrawLabels(false)
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
            bindingMain.nwChart.visibility = View.GONE
            bindingMain.tvNoDataForChart.visibility = View.VISIBLE
            Log.e("mainActivity", "Chart not processed as there is not enough data.")
        }


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
        result.sortByDescending { getLatestValuationForAL(it.id).toFloat() }
        dbHandler.close()
        return result
    }

    private fun getLiabilityList(): ArrayList<ALModel> {
        val dbHandler = ALHandler(this, null)
        val result = dbHandler.getAllLiabilities()
        result.sortBy { getLatestValuationForAL(it.id).toFloat() }
        dbHandler.close()
        return result
    }

    fun getLatestValuationForAL(id: Int): String {
        val valuationHandler = ValuationHandler(this, null)
        return valuationHandler.getLatestValuationForAL(id)
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
            if (runningLiabilityTotal != 0F) {
                bindingMain.liabilityTotal.text = gbpFormatter.format(-runningLiabilityTotal)
            } else {
                bindingMain.liabilityTotal.text = gbpFormatter.format(runningLiabilityTotal)
            }


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

        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddAsset.date.text = getString(
            R.string.month_year,
            Globals.getShortMonth(monthPicked),
            yearPicked.toString()
        )

        bindingAddAsset.date.setOnClickListener {
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

            bindingMYPicker.mypMonth.value = monthPicked
            bindingMYPicker.mypYear.value = yearPicked

            bindingMYPicker.mypMonth.displayedValues = Globals.monthsShortArray

            bindingMYPicker.mypMonth.setOnValueChangedListener { _, _, newVal ->
                monthPicked = newVal
            }

            bindingMYPicker.mypYear.setOnValueChangedListener { _, _, newVal ->
                yearPicked = newVal
            }

            bindingMYPicker.submitDmy.setOnClickListener {
                bindingAddAsset.date.text =
                    getString(
                        R.string.month_year,
                        Globals.getShortMonth(monthPicked),
                        yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingMYPicker.mypMonth.wrapSelectorWheel = true
            bindingMYPicker.mypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddAsset.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, 1)

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
                setUpChart()
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

        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddLiability.date.text = getString(
            R.string.month_year,
            Globals.getShortMonth(monthPicked),
            yearPicked.toString()
        )

        bindingAddLiability.date.setOnClickListener {
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

            bindingMYPicker.mypMonth.value = monthPicked
            bindingMYPicker.mypYear.value = yearPicked

            bindingMYPicker.mypMonth.displayedValues = Globals.monthsShortArray

            bindingMYPicker.mypMonth.setOnValueChangedListener { _, _, newVal ->
                monthPicked = newVal
            }

            bindingMYPicker.mypYear.setOnValueChangedListener { _, _, newVal ->
                yearPicked = newVal
            }

            bindingMYPicker.submitDmy.setOnClickListener {
                bindingAddLiability.date.text =
                    getString(
                        R.string.month_year,
                        Globals.getShortMonth(monthPicked),
                        yearPicked.toString()
                    )
                changeDateDialog.dismiss()
            }

            bindingMYPicker.mypMonth.wrapSelectorWheel = true
            bindingMYPicker.mypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddLiability.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, 1)

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
                setUpChart()
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
        val alHandler = ALHandler(this, null)
        val valuation = valuationHandler.getLatestValuationForAL(al.id)
        return if (alHandler.isAsset(al.id)) {
            valuation
        } else {
            if (valuation.toFloat() < 0) {
                (valuation.toFloat() * -1).toString()
            } else {
                "0.00"
            }
        }
    }


}