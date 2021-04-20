package com.spudg.fromzero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.fromzero.databinding.ActivityMainBinding
import com.spudg.fromzero.databinding.ActivityValuationBinding
import java.util.ArrayList

class ValuationActivity : AppCompatActivity() {

    private lateinit var bindingValuation: ActivityValuationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingValuation = ActivityValuationBinding.inflate(layoutInflater)
        val view = bindingValuation.root
        setContentView(view)

        setUpValuationList(Globals.alSelected)

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

}