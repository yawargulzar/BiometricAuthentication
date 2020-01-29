package com.f.biometric_app

import android.app.ProgressDialog.show
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.util.Log
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricPromptinternal
class BiometricPrompt(
    private var context: Context,
    private var builder: Builder
) {
    companion object {
        private const val TAG = "BiometricPrompt"
        private val KEY_NAME = UUID.randomUUID().toString()
        private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
    }

    private var keyStore: KeyStore? = getKeystore()
    @RequiresApi(Build.VERSION_CODES.M)
    private var keyGenerator: KeyGenerator? = getKeyGenerator()
    private var cipher: Cipher? = null
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null
    private var dialog: BiometricDialog? = null

    private fun getKeystore() = try {
        KeyStore.getInstance(KEY_STORE_PROVIDER)?.apply { load(null) }
    } catch (e: GeneralSecurityException) {
        Log.e(TAG, e.message)
        null
    } catch (exc: IOException) {
        exc.printStackTrace()
        null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenerator() = try {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_PROVIDER)?.apply {
            init(
                KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            generateKey()
        }
    } catch (e: GeneralSecurityException) {
        Log.e(TAG, e.message)
        null
    } catch (exc: IOException) {
        exc.printStackTrace()
        null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun authenticate(authListener: BiometricPromptCompat.OnAuthenticateListener) {
        if (initCipher()) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                val cryptoObject = FingerprintManager.CryptoObject(cipher ?: return)
                val fingerprintManager = context.getSystemService(FingerprintManager::class.java)
                fingerprintManager.authenticate(
                    cryptoObject,
                    android.os.CancellationSignal(),
                    0,
                    object : FingerprintManager.AuthenticationCallback() {
                        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errMsgId, errString)
                            dialog?.setStatus(errString?.toString(), R.color.colorError)
                            authListener.onAuthError(errMsgId, errString ?: "")
                        }

                        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            dialog?.dismiss()
                            authListener.onAuthSuccess()
                        }

                        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                            super.onAuthenticationHelp(helpMsgId, helpString)
                            dialog?.setStatus(helpString?.toString())
                            authListener.onAuthHelp(helpMsgId, helpString ?: "")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            dialog?.setStatus(
                                context.getString(R.string.fingerprint_not_recognized),
                                R.color.colorError
                            )
                            authListener.onAuthFailure()
                        }
                    },
                    null
                )
            } else {
                cryptoObject = FingerprintManagerCompat.CryptoObject(cipher ?: return)
                val fingerprintManagerCompat = FingerprintManagerCompat.from(context)
                fingerprintManagerCompat.authenticate(
                    cryptoObject, 0, CancellationSignal(),
                    object : FingerprintManagerCompat.AuthenticationCallback() {
                        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errMsgId, errString)
                            dialog?.setStatus(errString?.toString(), R.color.colorError)
                            authListener.onAuthError(errMsgId, errString ?: "")
                        }

                        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            dialog?.dismiss()
                            authListener.onAuthSuccess()
                        }

                        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                            super.onAuthenticationHelp(helpMsgId, helpString)
                            dialog?.setStatus(helpString?.toString())
                            authListener.onAuthHelp(helpMsgId, helpString ?: "")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            dialog?.setStatus(
                                context.getString(R.string.fingerprint_not_recognized),
                                R.color.colorError
                            )
                            authListener.onAuthFailure()
                        }
                    }, null
                )
            }

            showDialog()
        }
    }
    private fun showDialog() {
        val title = requireNotNull(builder.title) { "title cannot be null" }
        val subtitle = requireNotNull(builder.subtitle) { "subTitle cannot be null" }
        val description = requireNotNull(builder.description) { "description cannot be null" }

        dialog = BiometricDialog.Builder(context)
            .setTitle(title)
            .setSubTitle(subtitle)
            .setButtonNegative(builder.btnNegativeText)
            .setDescription(description)
            .build()
            .apply { show () }

    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun initCipher(): Boolean {
        try {
            val transformation =
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            cipher = Cipher.getInstance(transformation)

            keyStore?.let {
                it.load(null)
                val key = it.getKey(KEY_NAME, null) as SecretKey
                cipher?.init(Cipher.ENCRYPT_MODE, key) ?: return false
                return true
            } ?: return false
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }


    class Builder(private val context: Context) {
        var title: String? = null
            private set
        var subtitle: String? = null
            private set
        var description: String? = null
            private set
        var btnNegativeText: String = context.getString(R.string.cancel)
            private set

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

        fun setButtonNegative(text: String): Builder {
            this.btnNegativeText = text
            return this
        }

        fun build() = BiometricPrompt(context, this)

    }


}


