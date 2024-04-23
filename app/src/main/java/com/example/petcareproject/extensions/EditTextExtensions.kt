package com.example.petcareproject.extensions

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.petcareproject.R

fun EditText.makeInput(): String {
    return this.text.toString().trim();
}

fun Array<EditText>.setupFieldListeners(context: Context) {
    this.forEach { editText ->
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.isEmpty()) {
                editText.shakeView(context)
            }
        }
    }
}


fun View.shakeView(context: Context) {
    val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake)
    this.startAnimation(shakeAnimation)
}

fun Array<EditText>.strokeField(context: Context, backgroundColor: Int, strokeColor: Int) {
    this.forEach { editText ->
        val density = context.resources.displayMetrics.density
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(context, backgroundColor)) // Background color
            cornerRadius = 10 * density // Convert 10dp to pixels
            setStroke(1 * density.toInt(), ContextCompat.getColor(context, strokeColor)) // Stroke color
        }
        editText.background = background
        editText.setTextColor(ContextCompat.getColorStateList(context, R.color.pet)) // This needs a valid color resource

    }
}
