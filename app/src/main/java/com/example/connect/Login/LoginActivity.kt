package com.example.connect.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.connect.MainActivity
import com.example.connect.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        appCompatEditText.addTextChangedListener {
            nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length<10)
        }
        val ccp = findViewById<CountryCodePicker>(R.id.ccp)
        val ph = findViewById<EditText>(R.id.appCompatEditText)
        ccp.registerCarrierNumberEditText(ph) //create the number with country code

        nextBtn.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setMessage("The number ${ccp.fullNumberWithPlus.trim()} will be verified. Is it correct or " +
                        "would you like to edit the number") //create dialog with the message

                //positive button
                setPositiveButton("OK"){_,_ ->
                    val intent = Intent(this@LoginActivity, otpActivity::class.java)
                    intent.putExtra("phno", ccp.fullNumberWithPlus.trim())
                    startActivity(intent)
                }

                //negative button
                setNegativeButton("EDIT"){dialog, which ->
                    dialog.dismiss()
                }

            }.setCancelable(false)
                .create()
                .show()
        }
    }
}