package com.majulancar.kasir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.majulancar.kasir.databinding.ActivityMainBinding
import com.majulancar.kasir.security.SecurityGuard

/**
 * Single entry point: a NavHost with four bottom-nav tabs
 * (Menu, Keranjang, Omzet, Tentang). No login screen — the app runs
 * locally on the cashier's own device.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Blocks screenshots / screen recording / Recents thumbnail: the app
        // shows QRIS codes and the omzet ledger.
        SecurityGuard.enableScreenshotProtection(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        binding.bottomNav.isItemActiveIndicatorEnabled = false
        binding.bottomNav.setupWithNavController(navHost.navController)

        // Cart item count as a badge on the Keranjang tab.
        val app = application as MajuLancarApp
        app.cartRepository.items.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            if (count > 0) {
                val badge = binding.bottomNav.getOrCreateBadge(R.id.cartFragment)
                badge.isVisible = true
                badge.number = count
            } else {
                binding.bottomNav.removeBadge(R.id.cartFragment)
            }
        }

        if (savedInstanceState == null && SecurityGuard.looksRooted(this)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.root_warning_title)
                .setMessage(R.string.root_warning_message)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }
}
