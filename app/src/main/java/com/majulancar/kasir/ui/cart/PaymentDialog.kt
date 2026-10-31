package com.majulancar.kasir.ui.cart

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.majulancar.kasir.R
import com.majulancar.kasir.data.PaymentMethod
import com.majulancar.kasir.data.Store
import com.majulancar.kasir.databinding.DialogPaymentBinding
import com.majulancar.kasir.util.Formatter
import com.majulancar.kasir.util.QrBitmap
import com.majulancar.kasir.util.Qris

/**
 * Bottom sheet for finishing a payment: QRIS (QR with the total already
 * embedded) or cash (amount received, quick-cash chips, change).
 * Calls [onPaid] only when the cashier taps "Selesai".
 */
class PaymentDialog(
    private val context: Context,
    private val store: Store,
    private val total: Long,
    private val onPaid: (method: PaymentMethod, cashReceived: Long?) -> Unit
) {

    private val dialog = BottomSheetDialog(context)
    private val b = DialogPaymentBinding.inflate(LayoutInflater.from(context))

    private var method = PaymentMethod.QRIS
    private var cashReceived = 0L
    private var formatting = false

    init {
        dialog.setContentView(b.root)
        // The sheet shows a QRIS code: keep it out of screenshots/recordings too.
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true

        b.tvTotalAmount.text = Formatter.rupiah(total)
        b.tvStoreName.text = store.name

        renderQr()
        buildQuickCashChips()

        b.toggleMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            method = if (checkedId == R.id.btnCash) PaymentMethod.CASH else PaymentMethod.QRIS
            render()
        }
        b.toggleMethod.check(R.id.btnQris)

        b.etCash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (formatting || s == null) return
                formatting = true
                val digits = s.toString().filter { it.isDigit() }.take(MAX_DIGITS)
                cashReceived = digits.toLongOrNull() ?: 0L
                val formatted = if (digits.isEmpty()) "" else Formatter.number(cashReceived)
                if (formatted != s.toString()) {
                    s.replace(0, s.length, formatted)
                }
                formatting = false
                renderCash()
            }
        })

        b.btnClose.setOnClickListener { dialog.dismiss() }
        b.btnFinish.setOnClickListener {
            if (method == PaymentMethod.CASH && !isCashSufficient()) return@setOnClickListener
            val received = if (method == PaymentMethod.CASH) cashReceived else null
            dialog.dismiss()
            onPaid(method, received)
        }

        render()
    }

    fun show() = dialog.show()

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }

    private fun isCashSufficient(): Boolean = total > 0 && cashReceived >= total

    private fun render() {
        val isQris = method == PaymentMethod.QRIS
        b.qrisContainer.isVisible = isQris
        b.cashContainer.isVisible = !isQris
        renderCash()
    }

    private fun renderQr() {
        // Amount is embedded so the buyer doesn't type it; falls back to the
        // static payload (manual amount) if the merchant payload is malformed.
        val result = Qris.generateDynamicSafe(store.qrisPayload, total)
        val sizePx = (QR_SIZE_DP * context.resources.displayMetrics.density).toInt()
        b.ivQr.setImageBitmap(QrBitmap.create(result.payload, sizePx))
        b.tvQrisNote.isVisible = result.isDynamic
        b.tvQrisFallback.isVisible = !result.isDynamic
    }

    private fun renderCash() {
        val hasInput = cashReceived > 0
        val sufficient = isCashSufficient()
        val change = cashReceived - total

        b.tvChange.text = if (hasInput) Formatter.rupiah(maxOf(change, 0L)) else "—"
        b.changeBox.setBackgroundResource(
            if (sufficient) R.drawable.bg_change_ok else R.drawable.bg_change_warn
        )
        b.tvChangeWarn.isVisible = hasInput && !sufficient
        if (hasInput && !sufficient) {
            b.tvChangeWarn.text = context.getString(R.string.pay_cash_short, Formatter.rupiah(total - cashReceived))
        }

        b.btnFinish.isEnabled = method == PaymentMethod.QRIS || sufficient
    }

    private fun buildQuickCashChips() {
        b.chipsQuick.removeAllViews()
        for (amount in quickCashAmounts(total)) {
            val chip = Chip(context)
            chip.text = if (amount == total) {
                context.getString(R.string.pay_exact)
            } else {
                Formatter.rupiah(amount)
            }
            chip.setOnClickListener {
                b.etCash.setText(amount.toString())
                b.etCash.setSelection(b.etCash.text?.length ?: 0)
            }
            b.chipsQuick.addView(chip)
        }
    }

    companion object {
        private const val QR_SIZE_DP = 244
        private const val MAX_DIGITS = 12

        /** Plausible cash amounts for [total], mimicking common Rupiah banknotes. */
        fun quickCashAmounts(total: Long): List<Long> {
            if (total <= 0) return emptyList()
            val suggestions = linkedSetOf(total)
            for (d in listOf(5_000L, 10_000L, 20_000L, 50_000L, 100_000L)) {
                if (d >= total) suggestions.add(d)
            }
            for (base in listOf(10_000L, 20_000L, 50_000L, 100_000L)) {
                val rounded = ((total + base - 1) / base) * base
                if (rounded > total) suggestions.add(rounded)
            }
            return suggestions.sorted().take(5)
        }
    }
}
