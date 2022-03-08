package com.example.geschenkapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.util.*

class RegisterActivity : AppCompatActivity() {

    lateinit var datepicker: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


    }
    private fun setDate(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var mt = "0"
        var d = "0"

        datepicker = findViewById(R.id.tvDateOfBirth)
        datepicker.setOnClickListener{
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener{ view, mYear, mMonth, mDay ->
                val month2 = mMonth + 1
                if(month2<10){
                    mt = "0" + month2
                } else{
                    mt = month2.toString()
                }
                if(mDay<10){
                    d = "0" + mDay
                } else{
                    d = mDay.toString()
                }
                datepicker.setText(""+ mYear + "-" + mt + "-"+ d )
            }, year, month, day)
            dpd.show()
        }
    }
}