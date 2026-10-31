package com.majulancar.kasir.ui.omzet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.majulancar.kasir.R
import com.majulancar.kasir.app
import com.majulancar.kasir.data.PaymentMethod
import com.majulancar.kasir.data.Sale
import com.majulancar.kasir.databinding.FragmentOmzetBinding
import com.majulancar.kasir.databinding.ItemHistoryBinding
import com.majulancar.kasir.databinding.ViewStatCardBinding
import com.majulancar.kasir.ui.toast
import com.majulancar.kasir.util.Formatter

class OmzetFragment : Fragment() {

    private var _binding: FragmentOmzetBinding? = null
    private val binding get() = _binding!!

    private var confirmReset = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOmzetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnReset.setOnClickListener { onResetClicked() }
    }

    override fun onResume() {
        super.onResume()
        cancelResetConfirmation()
        load()
    }

    private fun onResetClicked() {
        if (!confirmReset) {
            confirmReset = true
            binding.btnReset.setText(R.string.omzet_reset_confirm_btn)
            requireContext().toast(R.string.omzet_reset_confirm)
            binding.btnReset.postDelayed({ cancelResetConfirmation() }, CONFIRM_TIMEOUT_MS)
            return
        }
        app.salesRepository.reset()
        cancelResetConfirmation()
        requireContext().toast(R.string.omzet_reset_done)
        load()
    }

    private fun cancelResetConfirmation() {
        confirmReset = false
        _binding?.btnReset?.setText(R.string.omzet_reset)
    }

    private fun load() {
        val summary = app.salesRepository.summary()

        val stats = listOf(
            R.string.omzet_total to Formatter.rupiah(summary.totalOmzet),
            R.string.omzet_today to Formatter.rupiah(summary.omzetHariIni),
            R.string.omzet_transactions to "${summary.totalTransaksi}x",
            R.string.omzet_today_transactions to "${summary.transaksiHariIni}x",
            R.string.omzet_qris to "${Formatter.rupiah(summary.omzetQris)} (${summary.transaksiQris}x)",
            R.string.omzet_cash to "${Formatter.rupiah(summary.omzetTunai)} (${summary.transaksiTunai}x)"
        )

        val statsContainer = binding.statsContainer
        statsContainer.removeAllViews()
        for (pair in stats.chunked(2)) {
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.HORIZONTAL
            for ((labelRes, value) in pair) {
                val card = ViewStatCardBinding.inflate(layoutInflater, row, false)
                card.tvLabel.setText(labelRes)
                card.tvValue.text = value
                row.addView(card.root)
            }
            statsContainer.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val history = binding.historyContainer
        history.removeAllViews()
        binding.tvEmpty.isVisible = summary.riwayat.isEmpty()
        for (sale in summary.riwayat) {
            history.addView(bindSale(sale, history))
        }
    }

    private fun bindSale(sale: Sale, parent: ViewGroup): View {
        val b = ItemHistoryBinding.inflate(layoutInflater, parent, false)
        b.tvId.text = sale.id
        b.tvTime.text = Formatter.waktu(sale.createdAtEpochMillis)
        b.tvTotal.text = Formatter.rupiah(sale.total)
        b.tvMethod.setText(
            if (sale.paymentMethod == PaymentMethod.CASH) R.string.pay_method_cash else R.string.pay_method_qris
        )
        b.tvItems.text = sale.items.joinToString("\n") {
            "• ${it.quantity} x ${Formatter.rupiah(it.price)} (${it.name}) = ${Formatter.rupiah(it.subtotal)}"
        }

        val received = sale.cashReceived
        val change = sale.change
        val showCash = sale.paymentMethod == PaymentMethod.CASH && received != null && change != null
        b.tvCashInfo.isVisible = showCash
        if (showCash && received != null && change != null) {
            b.tvCashInfo.text = getString(
                R.string.omzet_cash_info,
                Formatter.rupiah(received),
                Formatter.rupiah(change)
            )
        }
        return b.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val CONFIRM_TIMEOUT_MS = 5_000L
    }
}
