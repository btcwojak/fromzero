package com.spudg.fromzero

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.spudg.fromzero.databinding.ActivityMainBinding
import com.spudg.fromzero.databinding.DayMonthYearPickerBinding
import com.spudg.fromzero.databinding.DialogAddAssetBinding
import com.spudg.fromzero.databinding.DialogAssetLiabilityBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var bindingMain: ActivityMainBinding
    private lateinit var bindingAssetLiability: DialogAssetLiabilityBinding
    private lateinit var bindingAddAsset: DialogAddAssetBinding
    private lateinit var bindingDMYPicker: DayMonthYearPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain = ActivityMainBinding.inflate(layoutInflater)
        val view = bindingMain.root
        setContentView(view)

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
                //addLiability()
                assetLiabilityDialog.dismiss()
            }
            assetLiabilityDialog.show()
        }


    }

    private fun addAsset() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddAsset = DialogAddAssetBinding.inflate(layoutInflater)
        val view = bindingAddAsset.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddAsset.date.text = "$dayPicked $monthPicked $yearPicked"

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
                bindingAddAsset.date.text = "$dayPicked $monthPicked $yearPicked"
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

                val assetHandler = AssetHandler(this, null)
                assetHandler.addAsset(AssetModel(0, name, value, note, colour, date))

                Toast.makeText(this, "Asset added.", Toast.LENGTH_LONG).show()
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







}