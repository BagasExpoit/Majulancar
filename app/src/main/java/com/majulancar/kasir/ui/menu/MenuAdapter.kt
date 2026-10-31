package com.majulancar.kasir.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.majulancar.kasir.R
import com.majulancar.kasir.data.MenuItem
import com.majulancar.kasir.databinding.ItemCategoryBinding
import com.majulancar.kasir.databinding.ItemHeroBinding
import com.majulancar.kasir.databinding.ItemMenuBinding
import com.majulancar.kasir.ui.StoreBadges
import com.majulancar.kasir.util.Formatter

class MenuAdapter(
    private val rows: List<MenuRow>,
    private val onAdd: (item: MenuItem, quantity: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /** Quantity picked with the +/- steppers per menu item, not yet added to the cart. */
    private val pending = mutableMapOf<String, Int>()

    override fun getItemCount(): Int = rows.size

    override fun getItemViewType(position: Int): Int = when (rows[position]) {
        is MenuRow.Hero -> TYPE_HERO
        is MenuRow.Category -> TYPE_CATEGORY
        is MenuRow.Item -> TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HERO -> HeroHolder(ItemHeroBinding.inflate(inflater, parent, false))
            TYPE_CATEGORY -> CategoryHolder(ItemCategoryBinding.inflate(inflater, parent, false))
            else -> ItemHolder(ItemMenuBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is MenuRow.Hero -> (holder as HeroHolder).bind(row)
            is MenuRow.Category -> (holder as CategoryHolder).bind(row)
            is MenuRow.Item -> (holder as ItemHolder).bind(row)
        }
    }

    private class HeroHolder(private val b: ItemHeroBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: MenuRow.Hero) {
            b.tvAddress.text = row.store.address
            StoreBadges.bind(b.badges, row.store)
        }
    }

    private class CategoryHolder(private val b: ItemCategoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: MenuRow.Category) {
            val category = row.category
            b.tvCategoryLabel.text = category.label

            val photo = category.photo
            b.cardBanner.isVisible = photo != null
            if (photo != null) {
                // Banner overlay shows the label without the leading emoji.
                b.tvBannerLabel.text = category.label.replace(LEADING_SYMBOLS, "").trim()
                b.ivBanner.load(photo) {
                    crossfade(true)
                    addHeader("User-Agent", "MajuLancarKasir/1.0 (Android)")
                }
            } else {
                b.ivBanner.setImageDrawable(null)
            }

            val credit = category.photoCredit
            b.tvPhotoCredit.isVisible = photo != null && credit != null
            if (credit != null) {
                b.tvPhotoCredit.text = b.root.context.getString(R.string.photo_credit, credit)
            }
        }
    }

    private inner class ItemHolder(private val b: ItemMenuBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(row: MenuRow.Item) {
            val item = row.item
            b.tvName.text = item.name
            b.tvPrice.text = Formatter.rupiah(item.price)
            b.tvEmoji.text = row.emoji
            b.tvQty.text = (pending[item.id] ?: 0).toString()

            b.btnMinus.setOnClickListener { change(item.id, -1) }
            b.btnPlus.setOnClickListener { change(item.id, +1) }
            b.btnAdd.setOnClickListener {
                // Like the web version: with the stepper at 0, "Tambah" adds one.
                val qty = maxOf(1, pending[item.id] ?: 0)
                onAdd(item, qty)
                pending[item.id] = 0
                b.tvQty.text = "0"
            }
        }

        private fun change(id: String, delta: Int) {
            val next = maxOf(0, (pending[id] ?: 0) + delta)
            pending[id] = next
            b.tvQty.text = next.toString()
        }
    }

    private companion object {
        const val TYPE_HERO = 0
        const val TYPE_CATEGORY = 1
        const val TYPE_ITEM = 2

        /** Everything before the first letter/digit (i.e. the emoji and spaces). */
        val LEADING_SYMBOLS = Regex("^[^\\p{L}\\p{N}]+")
    }
}
