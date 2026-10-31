package com.majulancar.kasir.ui.menu

import com.majulancar.kasir.data.MenuCategory
import com.majulancar.kasir.data.MenuItem
import com.majulancar.kasir.data.Store

/** One row of the menu list: hero header, category header, or a menu item card. */
sealed interface MenuRow {
    data class Hero(val store: Store) : MenuRow
    data class Category(val category: MenuCategory) : MenuRow
    data class Item(val item: MenuItem, val emoji: String) : MenuRow
}
