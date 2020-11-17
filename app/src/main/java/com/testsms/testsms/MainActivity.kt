package com.testsms.testsms


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var auth: FirebaseAuth
    val TIME_OUT = 60
    var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    var mVerificationId: String? = ""

    var mResendToken: ForceResendingToken? = null

    var editTextMobile: EditText? = null

    var buttonContinue: Button? = null

    var editOtpNumber: EditText? = null

    var buttonsubmit: Button? = null

    var buttonreSend: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editTextMobile = findViewById(R.id.editTextMobile)
        buttonContinue = findViewById(R.id.buttonContinue)
        editOtpNumber = findViewById(R.id.editOtpNumber)
        buttonsubmit = findViewById(R.id.buttonsubmit)
        buttonreSend=findViewById(R.id.buttonreSend)

        auth = FirebaseAuth.getInstance()

        buttonContinue!!.setOnClickListener(this)
        buttonsubmit!!.setOnClickListener(this)
        buttonreSend!!.setOnClickListener(this)
    }


    private fun sendVerificationCode(mobile: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobile)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun reSendVerificationCode(mobile: String, resendToken: ForceResendingToken) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobile)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks)//OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)// resend token
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //Getting the code sent by SMS
                val code = phoneAuthCredential.smsCode

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (code != null) {
                    editOtpNumber!!.setText(code)
                    //verifying the code
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                mVerificationId = s
                mResendToken = forceResendingToken
            }
        }


    private fun verifyVerificationCode(otp: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, otp)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this@MainActivity,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        //verification successful we will start the profile activity
                        /*  val intent = Intent(this@VerifyPhoneActivity, ProfileActivity::class.java)
                          intent.flags =
                              Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                          startActivity(intent)*/

                        Toast.makeText(this, "sucessfull", Toast.LENGTH_SHORT).show()

                    } else {

                        //verification unsuccessful.. display an error message
                        var message = "Somthing is wrong, we will fix it soon..."
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered..."
                        }
                        val snackbar = Snackbar.make(
                            findViewById(R.id.parent),
                            message, Snackbar.LENGTH_LONG
                        )
                        snackbar.setAction(
                            "Dismiss"
                        ) { TODO("Not yet implemented") }
                        snackbar.show()
                    }
                })
    }

    override fun onClick(v: View?) {


        when (v!!.id)
        {
            R.id.buttonContinue ->
            {

                if(editTextMobile!=null)
                {
                    sendVerificationCode(editTextMobile!!.text.toString())
                }

            }

            R.id.buttonsubmit ->
            {
                if(editOtpNumber!=null)
                {
                    verifyVerificationCode(editOtpNumber!!.text.toString())
                }
            }

            R.id.buttonreSend ->
            {
                if(editTextMobile!=null)
                {
                    reSendVerificationCode(editTextMobile!!.text.toString(),mResendToken!!)
                }
            }


        }
    }

}