package com.f.biometric_app

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_authenticate.setOnClickListener{
            showAuthDialog()
        }
    }
    private val authListener = object : BiometricPromptCompat.OnAuthenticateListener {
        override fun onAuthSuccess() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(this@MainActivity,"You have passed the fingerPrint authentication",Toast.LENGTH_LONG).show()
            }
        }

        override fun onAuthFailure() {}

        override fun onAuthCancelled() {}

        override fun onAuthHelp(helpCode: Int, helpString: CharSequence) {}

        override fun onAuthError(errorCode: Int, errString: CharSequence) {}
    }

    fun showAuthDialog(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricPromptCompat.Builder(this)
                .setTitle(getString(R.string.fingerprint_dialog_title))
                .setSubtitle("")
                .setDescription(getString(R.string.fingerprint_dialog_description))
                .addOnAuthenticationListener(authListener)
                .build()
                .authenticate(callback)
        }
    }
    private var callback : BiometricPromptCompat.Callback = object :BiometricPromptCompat.Callback{
        override fun onSDKNotSupported() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onHardwareNotDetected() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBiometricAuthNotAvailable() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPermissionsNotGranted() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onError(error: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
