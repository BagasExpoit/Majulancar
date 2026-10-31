package com.majulancar.kasir

import android.app.Application
import androidx.fragment.app.Fragment
import com.majulancar.kasir.data.CartRepository
import com.majulancar.kasir.data.MenuRepository
import com.majulancar.kasir.data.SalesRepository

/**
 * Application class. Holds the three on-device repositories as lazy
 * singletons (menu catalogue, cart, sales ledger). Nothing here touches
 * the network.
 */
class MajuLancarApp : Application() {
    val menuRepository: MenuRepository by lazy { MenuRepository(this) }
    val cartRepository: CartRepository by lazy { CartRepository(this) }
    val salesRepository: SalesRepository by lazy { SalesRepository(this) }
}

/** Convenience accessor for fragments. */
val Fragment.app: MajuLancarApp
    get() = requireActivity().application as MajuLancarApp
