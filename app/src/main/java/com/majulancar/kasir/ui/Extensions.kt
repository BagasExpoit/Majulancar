package com.majulancar.kasir.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.majulancar.kasir.R

/** Opens an http(s) URL in whichever app handles it (browser, WhatsApp, Maps, Telegram...). */
fun Context.openUrl(url: String) {
    if (url.isBlank()) return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.error_cannot_open_link, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}
