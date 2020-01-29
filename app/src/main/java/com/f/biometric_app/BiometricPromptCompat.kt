package com.f.biometric_app

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import javax.security.auth.callback.Callback

class BiometricPromptCompat private constructor(
    private val context: Context,
    private val title: String,
    private val subtitle: String,
    private val description: String,
    private val btnText: String,
    private val authListener: OnAuthenticateListener
) {
    fun authenticate(callback: Callback) {
        when {
            !hasSDKSupport() -> {
                return callback.onSDKNotSupported()
            }
            !hasHardwareSupport(context) -> {
                return callback.onHardwareNotDetected()
            }
            !isPermissionsGranted(context) -> {
                return callback.onPermissionsNotGranted()
            }
            !isFingerprintAvailable(context) -> {
                return callback.onBiometricAuthNotAvailable()
            }
        }

        if (hasBiometricPromptSupport()) {
            showBiometricPromptV28(authListener)
        } else {
            showBiometricPromptV23(authListener)
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresPermission(android.Manifest.permission.USE_BIOMETRIC)
    private fun showBiometricPromptV28(listener: OnAuthenticateListener) {
        BiometricPrompt.Builder(context)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButton(btnText, context.mainExecutor, DialogInterface.OnClickListener { dialogInterface, _ ->
                if (dialogInterface != null) dialogInterface.dismiss()
                listener.onAuthCancelled()
            })
            .build()
            .authenticate(CancellationSignal(), context.mainExecutor, AuthenticationCallback(listener))
    }

    private fun showBiometricPromptV23(listener: OnAuthenticateListener) {
        com.f.biometric_app.BiometricPrompt.Builder(context)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setButtonNegative(btnText)
            .build()
            .authenticate(listener)
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var subtitle: String? = null
        private var description: String? = null
        private var btnText: String = context.getString(R.string.cancel)
        private var authListener: BiometricPromptCompat.OnAuthenticateListener? = null

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): Builder {
            this.subtitle = subtitle
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setNegativeButtonText(text: String): Builder {
            this.btnText = text
            return this
        }

        fun addOnAuthenticationListener(authListener: BiometricPromptCompat.OnAuthenticateListener): Builder {
            this.authListener = authListener
            return this
        }

        fun build(): BiometricPromptCompat {
            val authListener = requireNotNull(authListener) { "OnAuthenticateListener cannot be null" }
            val title = requireNotNull(title) { "title cannot be null" }
            val subtitle = requireNotNull(subtitle) { "subTitle cannot be null" }
            val description = requireNotNull(description) { "description cannot be null" }

            return BiometricPromptCompat(context, title, subtitle, description, btnText, authListener)
        }
    }

    interface Callback {
        fun onSDKNotSupported()
        fun onHardwareNotDetected()
        fun onBiometricAuthNotAvailable()
        fun onPermissionsNotGranted()
        fun onError(error: String)
    }

    interface OnAuthenticateListener {
        fun onAuthSuccess()
        fun onAuthFailure()
        fun onAuthCancelled()
        fun onAuthHelp(helpCode: Int, helpString: CharSequence)
        fun onAuthError(errorCode: Int, errString: CharSequence)
    }

    companion object {
        fun hasSDKSupport() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        @RequiresPermission(android.Manifest.permission.USE_FINGERPRINT)
        fun hasHardwareSupport(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                context.getSystemService(FingerprintManager::class.java).isHardwareDetected
            } else {
                FingerprintManagerCompat.from(context).isHardwareDetected
            }
        }

        @RequiresPermission(android.Manifest.permission.USE_FINGERPRINT)
        fun isFingerprintAvailable(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                context.getSystemService(FingerprintManager::class.java).hasEnrolledFingerprints()
            } else {
                FingerprintManagerCompat.from(context).hasEnrolledFingerprints()
            }
        }

        fun hasBiometricPromptSupport() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

        fun isPermissionsGranted(context: Context): Boolean {
            return if (hasBiometricPromptSupport())
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.USE_BIOMETRIC
                ) == PackageManager.PERMISSION_GRANTED
            else
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.USE_FINGERPRINT
                ) == PackageManager.PERMISSION_GRANTED
        }
    }


}