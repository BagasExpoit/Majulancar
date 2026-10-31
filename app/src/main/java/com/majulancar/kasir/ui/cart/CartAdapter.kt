package com.majulancar.kasir.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.majulancar.kasir.R
import com.majulancar.kasir.data.CartItem
import com.majulancar.kasir.databinding.ItemCartBinding
import com.majulancar.kasir.util.Formatter

class CartAdapter(
    private val onMinus: (CartItem) -> Unit,
    private val onPlus: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItem) {
            val ctx = b.root.context
            b.tvName.text = item.name
            b.tvUnitPrice.text = "${Formatter.rupiah(item.price)} ${ctx.getString(R.string.cart_unit_suffix)}"
            b.tvLine.text = ctx.getString(
                R.string.cart_line,
                item.quantity,
                Formatter.rupiah(item.price),
                Formatter.rupiah(item.subtotal)
            )
            b.tvSubtotal.text = Formatter.rupiah(item.subtotal)
            b.tvQty.text = item.quantity.toString()
            b.btnMinus.setOnClickListener { onMinus(item) }
            b.btnPlus.setOnClickListener { onPlus(item) }
            b.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean =
            oldItem == newItem
    }
}
