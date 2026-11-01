package id.biz.majulancar.kasir.data

// BERKAS INI DIBUAT DARI data/menu.json (situs web asli). Ubah harga/menu di sini bila perlu.
// Harga di aplikasi SELALU diambil dari katalog ini, bukan dari data keranjang.

object StoreInfo {
    const val NAME = "MIE AYAM MAJU LANCAR MAS TRI"
    const val ADDRESS = "Jl. Babakan Leuwikopo, Depan Polsek Dramaga"
    const val PHONE = "+6287710528400"
    const val WHATSAPP_URL = "https://wa.me/6287710528400?text=Halo%20Mie%20Ayam%20Maju%20Lancar%2C%20saya%20mau%20tanya%20menu%20%F0%9F%98%8A"
    const val ADMIN_CONTACT_URL = "https://t.me/Bagas_ocarius"
    const val HOURS_LABEL = "Setiap hari 08.00–22.00"
    const val RATING_SOURCE = "GoogleMaps"
    const val MAPS_URL = "https://www.google.com/maps/place/CPPF%2BPPM+Mie+Ayam+Bakso+pakdhe+Trimin,+Babakan,+Kec.+Dramaga,+Kabupaten+Bogor,+Jawa+Barat+16680,+Indonesia/@-6.5631508,106.7243088,15z/data=!4m6!3m5!1s0x2e69c5a58163e1c5:0x4f9452ffd732c8a2!8m2!3d-6.5631508!4d106.7243088!16s%2Fg%2F11ty4v7k6r?utm_campaign=ml-ardl&g_ep=Eg1tbF8yMDI2MDcyOF8wIJvbDyoASAJQAQ%3D%3D"
    const val QRIS_STATIC_PAYLOAD = "00020101021126570011ID.DANA.WWW011893600915345573715302094557371530303UMI51440014ID.CO.QRIS.WWW0215ID10232667059130303UMI5204581253033605802ID5925MIE AYAM MAJU LANCAR MAS 6010Kab. Bogor610516680630469EE"
    const val OPEN_MINUTES = 480
    const val CLOSE_MINUTES = 1320
    const val RATING_VALUE = 5.0
    const val RATING_COUNT = 6
}

object Catalog {
    val categories: List<MenuCategory> = listOf(
        MenuCategory(key = "makanan", label = "Menu Mie Ayam", emoji = "🍜", items = listOf(
            MenuItem("bakso_kecil", "Mie ayam + Bakso (Kecil)", 12000L),
            MenuItem("bakso_jumbo", "Mie ayam + Bakso (Jumbo)", 18000L),
            MenuItem("bakso_urat", "Mie ayam + Bakso (Urat)", 15000L),
            MenuItem("bakso_telur", "Mie ayam + Bakso (Telur)", 15000L),
            MenuItem("mie_ayam_2", "Mie ayam", 10000L),
        )),
        MenuCategory(key = "tambahan", label = "Menu Bakso", emoji = "🍥", items = listOf(
            MenuItem("extra_bakso_urat", "Bakso Urat", 10000L),
            MenuItem("extra_bakso_jumbo", "Bakso Jumbo", 15000L),
            MenuItem("extra_bakso_telur", "Bakso Telur", 10000L),
            MenuItem("extra_bakso_urat_telur", "Bakso Urat + Telur", 15000L),
        )),
        MenuCategory(key = "minuman", label = "Menu Minuman", emoji = "🥤", items = listOf(
            MenuItem("es_teh", "Es Teh", 3000L),
            MenuItem("es_teh_tawar", "Es Teh Tawar", 2000L),
            MenuItem("teh_pucuk", "Teh Pucuk", 5000L),
            MenuItem("le_minerale", "Le Minerale", 5000L),
            MenuItem("nutrisari", "Nutrisari", 5000L),
            MenuItem("es_kelapa", "Es Kelapa", 5000L),
            MenuItem("teh_manis_hangat", "Teh Manis Hangat", 3000L),
        )),
    )

    private val byId: Map<String, MenuItem> = categories.flatMap { it.items }.associateBy { it.id }

    fun find(id: String): MenuItem? = byId[id]
}
