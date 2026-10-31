package com.majulancar.kasir.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.majulancar.kasir.R
import com.majulancar.kasir.app
import com.majulancar.kasir.databinding.FragmentMenuBinding
import com.majulancar.kasir.ui.toast

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = app.menuRepository.load()
        val rows = buildList<MenuRow> {
            add(MenuRow.Hero(data.store))
            for (category in data.categories) {
                add(MenuRow.Category(category))
                val emoji = CATEGORY_EMOJI[category.key] ?: DEFAULT_EMOJI
                for (item in category.items) add(MenuRow.Item(item, emoji))
            }
        }

        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.adapter = MenuAdapter(rows) { item, quantity ->
            app.cartRepository.add(item, quantity)
            requireContext().toast(getString(R.string.cart_added, quantity, item.name))
        }
    }

    override fun onDestroyView() {
        binding.rvMenu.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val DEFAULT_EMOJI = "🍜"
        val CATEGORY_EMOJI = mapOf(
            "makanan" to "🍜",
            "tambahan" to "🍥",
            "minuman" to "🥤"
        )
    }
}
