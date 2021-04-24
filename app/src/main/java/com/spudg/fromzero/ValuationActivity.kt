package com.spudg.fromzero

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.fromzero.databinding.ActivityValuationBinding
import com.spudg.fromzero.databinding.DayMonthYearPickerBinding
import com.spudg.fromzero.databinding.DialogAddValuationBinding
import java.util.*

class ValuationActivity : AppCompatActivity() {

    private lateinit var bindingValuation: ActivityValuationBinding
    private lateinit var bindingAddValuation: DialogAddValuationBinding

    private lateinit var bindingDMYPicker: DayMonthYearPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingValuation = ActivityValuationBinding.inflate(layoutInflater)
        val view = bindingValuation.root
        setContentView(view)

        setUpValuationList(Globals.alSelected)

        bindingValuation.addValuation.setOnClickListener {
            addValuation()
        }

        bindingValuation.backToMainFromAL.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun getValuationList(al: Int): ArrayList<ValuationModel> {
        val dbHandler = ValuationHandler(this, null)
        val result = dbHandler.getValuationsForAL(al)
        dbHandler.close()
        return result
    }

    private fun setUpValuationList(al: Int) {
        if (getValuationList(al).size > 0) {
            bindingValuation.rvValuations.visibility = View.VISIBLE
            bindingValuation.tvNoValuations.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingValuation.rvValuations.layoutManager = manager
            val assetAdapter = ValuationAdapter(this, getValuationList(al))
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

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddValuation.date.text = "$dayPicked $monthPicked $yearPicked"

        bindingAddValuation.date.setOnClickListener {
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
                bindingAddValuation.date.text = "$dayPicked $monthPicked $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYPicker.dmypDay.wrapSelectorWheel = true
            bindingDMYPicker.dmypMonth.wrapSelectorWheel = true
            bindingDMYPicker.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddValuation.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked)

            val value = bindingAddValuation.etValue.text.toString()
            val date = calendar.timeInMillis.toString()

            if (value.isNotEmpty()) {

                val valuationHandler = ValuationHandler(this, null)

                valuationHandler.addValuation(ValuationModel(0, Globals.alSelected, value, date))

                Toast.makeText(this, "Valuation added.", Toast.LENGTH_LONG).show()

                setUpValuationList(Globals.alSelected)
                addDialog.dismiss()

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

}