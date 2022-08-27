package com.example.connect.Login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connect.MainActivity
import com.example.connect.R
import com.example.connect.Signup.signup
import com.example.connect.utilities.Constants
import com.example.connect.utilities.SharedPref
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_otp.*

class otpActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var otpid: String
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var user: FirebaseUser
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        supportActionBar?.hide()

        val num = intent.getStringExtra("phno")
        txtview1.text = "Verifying "+num
        auth = Firebase.auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@otpActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                progressDialog.dismiss()
                otpid = verificationId
                resendToken = token
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(num!!)       // Phone number to verify
            .setTimeout(0L, java.util.concurrent.TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        button.setOnClickListener { //confirm otp button
            if (appCompatEditText2.text.toString().isEmpty())
                Toast.makeText(this@otpActivity, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            else if (appCompatEditText2.text.toString().length != 6)
                Toast.makeText(this@otpActivity, "Please enter correct OTP", Toast.LENGTH_SHORT)
                    .show()
            else {
                val cred =
                    PhoneAuthProvider.getCredential(otpid, appCompatEditText2.text.toString())
                signInWithPhoneAuthCredential(cred)
            }
        }

        resendBtn.setOnClickListener {
            if (resendToken != null) {
                progressDialog = createProgressDialog("Re-sending the verification code", false)
                progressDialog.show()

                val options = PhoneAuthOptions.Builder(auth)
                    .setPhoneNumber(num)       // Phone number to verify
                    .setTimeout(0L, java.util.concurrent.TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .setForceResendingToken(resendToken!!)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

        showTimer(60000)
        progressDialog = createProgressDialog("Sending the verification code", false)
        progressDialog.show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this){ task->
            if(task.isSuccessful){
                user = task.getResult().user!!
                Toast.makeText(this@otpActivity, "Verified", Toast.LENGTH_SHORT).show()
                SharedPref(this@otpActivity).setString(Constants.USER_ID, user.uid)
                startActivity(Intent(this@otpActivity, signup::class.java))
                finish()
            }
            else{
                Toast.makeText(this@otpActivity, "Failed to verify", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimer(milliSecinFuture: Long) {
        resendBtn.isEnabled = false
        object : CountDownTimer(milliSecinFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendBtn.isEnabled = false
                counter.isEnabled = true
                counter.text = "Seconds remaining: " + millisUntilFinished / 1000
            }

            override fun onFinish() {
                counter.isEnabled = false
                resendBtn.isEnabled = true
            }

        }.start()
    }
}

fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}
