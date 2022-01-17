package dev.lazarev.calltasks.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(text: CharSequence, long: Boolean = true){
    val duration = if(long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, text, duration).show()
}