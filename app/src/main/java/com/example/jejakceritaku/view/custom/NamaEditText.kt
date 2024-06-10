package com.example.jejakceritaku.view.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.jejakceritaku.R

class NamaEditText : AppCompatEditText {
    private var isNameValid: Boolean = false
    private lateinit var profile: Drawable

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        profile  = ContextCompat.getDrawable(context, R.drawable.ic_person)!!
        onShowVisibilityIcon(profile)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val name = text?.trim()
                if (name.isNullOrEmpty()) {
                    isNameValid = false
                    error = resources.getString(R.string.enter_name)
                } else {
                    isNameValid = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun onShowVisibilityIcon(icon: Drawable) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
    }
}