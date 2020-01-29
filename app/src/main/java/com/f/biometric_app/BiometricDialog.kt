package com.f.biometric_app

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.TextView

internal class BiometricDialog private constructor(
    private var mContext: Context,
    @StyleRes val theme: Int = R.style.BottomSheetDialogTheme
) : BottomSheetDialog(mContext, theme) {

    private lateinit var tvStatus: TextView

    init {
        mContext = mContext.applicationContext
    }

    private fun createView(
        title: String,
        subTitle: String,
        description: String,
        btnText: String
    ): BiometricDialog {
        val view = layoutInflater.inflate(R.layout.bottom_sheet, findViewById(android.R.id.content))
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvSubtitle = view.findViewById<TextView>(R.id.tv_sub_title)
        val tvDescription = view.findViewById<TextView>(R.id.tv_description)
        tvStatus = view.findViewById(R.id.tv_status)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        tvTitle.text = title
        tvSubtitle.text = subTitle
        tvDescription.text = description
        btnCancel.text = btnText

        resetStatus()

        btnCancel.setOnClickListener {
            dismiss()
        }

        return this
    }

    fun setStatus(message: String?, @ColorRes textColor: Int = R.color.textColorLight) {
        message?.let {
            tvStatus.text = it
            tvStatus.visibility = View.VISIBLE
            tvStatus.setTextColor(ContextCompat.getColor(context, textColor))
        }
    }

    override fun dismiss() {
        resetStatus()
        super.dismiss()
    }

    fun resetStatus() {
        tvStatus.text = context.getString(R.string.status_hint)
        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.textColorLight))
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var subTitle: String? = null
        private var description: String? = null
        private var btnNegativeText: String = context.getString(R.string.cancel)

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setSubTitle(subTitle: String): Builder {
            this.subTitle = subTitle
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setButtonNegative(text: String?): Builder {
            text?.let { this.btnNegativeText = it }
            return this
        }

        fun build(): BiometricDialog {
            val title = requireNotNull(title) { "title cannot be null" }
            val subTitle = requireNotNull(subTitle) { "subTitle cannot be null" }
            val description = requireNotNull(description) { "description cannot be null" }

            return BiometricDialog(context).createView(title, subTitle, description, btnNegativeText)
        }
    }

}
