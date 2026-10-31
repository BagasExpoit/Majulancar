package id.biz.majulancar.kasir

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.biz.majulancar.kasir.ui.AppRoot
import id.biz.majulancar.kasir.ui.AppViewModel
import id.biz.majulancar.kasir.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Blokir screenshot, perekam layar, dan thumbnail "Recent apps".
        // Harus dipasang sebelum konten pertama digambar.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Tolak sentuhan saat jendela tertutup overlay (mitigasi tapjacking).
        window.decorView.filterTouchesWhenObscured = true
        setContent {
            val override by viewModel.themeOverride.collectAsStateWithLifecycle()
            val dark = override ?: isSystemInDarkTheme()

            // Ikon status bar & navigation bar mengikuti tema APLIKASI (bukan tema sistem),
            // supaya tetap terbaca saat pengguna memilih tema berbeda dari sistem.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose { }
            }

            AppTheme(darkTheme = dark) {
                AppRoot(viewModel, darkTheme = dark)
            }
        }
    }
}
