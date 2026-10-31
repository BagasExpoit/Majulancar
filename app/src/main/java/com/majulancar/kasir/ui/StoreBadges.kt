package com.majulancar.kasir.ui

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.majulancar.kasir.R
import com.majulancar.kasir.data.Store
import com.majulancar.kasir.data.StoreHours
import java.util.Calendar

/**
 * Row of info chips (Google Maps rating, opening hours with live
 * open/closed status, WhatsApp contact). Used on the Menu hero and the
 * About header so the data is read from menu.json in a single place.
 */
object StoreBadges {

    fun bind(group: ChipGroup, store: Store) {
        val ctx = group.context
        group.removeAllViews()

        group.addView(Chip(ctx).apply {
            text = ctx.getString(
                R.string.badge_rating,
                store.rating.value,
                store.rating.reviewCount,
                store.rating.source
            )
            setOnClickListener { ctx.openUrl(store.location.mapsUrl) }
        })

        val open = isOpenNow(store.hours)
        group.addView(Chip(ctx).apply {
            val status = when (open) {
                true -> ctx.getString(R.string.status_open)
                false -> ctx.getString(R.string.status_closed)
                null -> null
            }
            text = if (status != null) {
                ctx.getString(R.string.badge_hours, store.hours.label, status)
            } else {
                store.hours.label
            }
            if (open != null) {
                setTextColor(
                    ContextCompat.getColor(ctx, if (open) R.color.basil else R.color.danger)
                )
            }
            isClickable = false
            isFocusable = false
        })

        if (store.phone.isNotBlank()) {
            group.addView(Chip(ctx).apply {
                text = store.phone
                setOnClickListener { ctx.openUrl(store.whatsappUrl) }
            })
        }
        group.visibility = View.VISIBLE
    }

    /** True/false when [hours] parse correctly, null when the format is unexpected. */
    private fun isOpenNow(hours: StoreHours): Boolean? {
        val open = toMinutes(hours.open) ?: return null
        val close = toMinutes(hours.close) ?: return null
        val now = Calendar.getInstance()
        val minutesNow = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return minutesNow >= open && minutesNow < close
    }

    private fun toMinutes(hhmm: String): Int? {
        val parts = hhmm.split(":")
        if (parts.size != 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().toIntOrNull() ?: return null
        return h * 60 + m
    }
}
