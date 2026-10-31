package com.majulancar.kasir.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.majulancar.kasir.R
import com.majulancar.kasir.app
import com.majulancar.kasir.data.PaymentMethod
import com.majulancar.kasir.databinding.FragmentCartBinding
import com.majulancar.kasir.ui.toast
import com.majulancar.kasir.util.Formatter

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private var paymentDialog: PaymentDialog? = null

    private val adapter = CartAdapter(
        onMinus = { app.cartRepository.updateQuantity(it.id, -1) },
        onPlus = { app.cartRepository.updateQuantity(it.id, +1) },
        onRemove = {
            app.cartRepository.remove(it.id)
            requireContext().toast(R.string.cart_item_removed)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        app.cartRepository.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val empty = items.isEmpty()
            binding.emptyState.isVisible = empty
            binding.rvCart.isVisible = !empty
            binding.summaryCard.isVisible = !empty
            binding.tvTotal.text = Formatter.rupiah(items.sumOf { it.subtotal })
        }

        binding.btnClear.setOnClickListener {
            app.cartRepository.clear()
            requireContext().toast(R.string.cart_cleared)
        }

        binding.btnSeeMenu.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                .selectedItemId = R.id.menuFragment
        }

        binding.btnCheckout.setOnClickListener { startCheckout() }
    }

    private fun startCheckout() {
        val cart = app.cartRepository
        val total = cart.total
        if (cart.current.isEmpty() || total <= 0) {
            requireContext().toast(R.string.cart_empty_toast)
            return
        }
        val store = app.menuRepository.load().store
        paymentDialog = PaymentDialog(requireContext(), store, total) { method, cashReceived ->
            finishPayment(method, cashReceived)
        }.also { it.show() }
    }

    private fun finishPayment(method: PaymentMethod, cashReceived: Long?) {
        val cart = app.cartRepository
        val items = cart.current
        if (items.isEmpty()) return

        val sale = app.salesRepository.record(items, method, cashReceived)
        cart.clear()

        val message = if (method == PaymentMethod.CASH) {
            getString(R.string.pay_success_cash, Formatter.number(sale.change ?: 0L))
        } else {
            getString(R.string.pay_success_qris)
        }
        requireContext().toast(message)
    }

    override fun onDestroyView() {
        paymentDialog?.dismiss()
        paymentDialog = null
        binding.rvCart.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
