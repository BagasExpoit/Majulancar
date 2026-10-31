package com.majulancar.kasir.ui.about

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.majulancar.kasir.R
import com.majulancar.kasir.app
import com.majulancar.kasir.databinding.FragmentAboutBinding
import com.majulancar.kasir.ui.StoreBadges
import com.majulancar.kasir.ui.openUrl
import java.util.Calendar

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val store = app.menuRepository.load().store

        binding.tvStoreName.text = store.name
        StoreBadges.bind(binding.badges, store)

        binding.tvAddress.text = store.address
        binding.btnMaps.setOnClickListener { ctx.openUrl(store.location.mapsUrl) }

        binding.btnWhatsapp.text = getString(R.string.about_whatsapp_label, store.phone)
        binding.btnWhatsapp.setOnClickListener { ctx.openUrl(store.whatsappUrl) }
        binding.btnTelegram.setOnClickListener { ctx.openUrl(store.adminContactUrl) }

        binding.btnFacebook.setOnClickListener { ctx.openUrl(URL_FACEBOOK) }
        binding.btnInstagram.setOnClickListener { ctx.openUrl(URL_INSTAGRAM) }
        binding.btnGithub.setOnClickListener { ctx.openUrl(URL_GITHUB) }

        val year = Calendar.getInstance().get(Calendar.YEAR)
        binding.tvFooter.text = getString(R.string.about_copyright, year, store.name)
        binding.tvVersion.text = getString(R.string.about_version, versionName())
    }

    private fun versionName(): String = try {
        val ctx = requireContext()
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "-"
    } catch (e: PackageManager.NameNotFoundException) {
        "-"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val URL_FACEBOOK = "https://facebook.com/willydevops1"
        const val URL_INSTAGRAM = "https://www.instagram.com/bagas_samuji74?igsh=bmlscWxqN2pmZnJx"
        const val URL_GITHUB = "https://github.com/BagasExpoit"
    }
}
