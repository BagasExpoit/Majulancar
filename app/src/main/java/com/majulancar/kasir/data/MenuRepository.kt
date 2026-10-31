package com.majulancar.kasir.data

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Reads the store's menu catalogue from `assets/menu.json`.
 *
 * Deliberately uses the built-in `org.json` parser instead of pulling in
 * Gson/Moshi: one fewer third-party dependency to audit, and the schema
 * here is small and stable. The JSON file itself is the exact same
 * merchant data (`data/menu.json`) the original web app used — nothing
 * about the catalogue, prices or the QRIS payload was changed.
 */
class MenuRepository(private val context: Context) {

    @Volatile
    private var cached: MenuData? = null

    fun load(): MenuData {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val json = readAssetFile("menu.json")
            val parsed = parse(JSONObject(json))
            cached = parsed
            return parsed
        }
    }

    private fun readAssetFile(name: String): String {
        context.assets.open(name).use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                return reader.readText()
            }
        }
    }

    private fun parse(root: JSONObject): MenuData {
        val storeJson = root.getJSONObject("store")
        val hoursJson = storeJson.getJSONObject("hours")
        val ratingJson = storeJson.getJSONObject("rating")
        val locationJson = storeJson.getJSONObject("location")

        val store = Store(
            name = storeJson.getString("name"),
            address = storeJson.getString("address"),
            adminContactUrl = storeJson.optString("adminContactUrl"),
            phone = storeJson.optString("phone"),
            whatsappUrl = storeJson.optString("whatsappUrl"),
            hours = StoreHours(
                label = hoursJson.optString("label"),
                open = hoursJson.optString("open"),
                close = hoursJson.optString("close")
            ),
            rating = StoreRating(
                value = ratingJson.optDouble("value", 0.0),
                reviewCount = ratingJson.optInt("reviewCount", 0),
                source = ratingJson.optString("source")
            ),
            qrisPayload = storeJson.getString("qrisPayload"),
            location = StoreLocation(
                lat = locationJson.optDouble("lat", 0.0),
                lng = locationJson.optDouble("lng", 0.0),
                zoom = locationJson.optInt("zoom", 15),
                mapsUrl = locationJson.optString("mapsUrl")
            )
        )

        val categoriesJson = root.getJSONObject("categories")
        val categories = mutableListOf<MenuCategory>()
        val keys = categoriesJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val catJson = categoriesJson.getJSONObject(key)
            val itemsJson = catJson.getJSONArray("items")
            val items = mutableListOf<MenuItem>()
            for (i in 0 until itemsJson.length()) {
                val itemJson = itemsJson.getJSONObject(i)
                items.add(
                    MenuItem(
                        id = itemJson.getString("id"),
                        name = itemJson.getString("name"),
                        price = itemJson.getLong("price")
                    )
                )
            }
            categories.add(
                MenuCategory(
                    key = key,
                    label = catJson.optString("label", key),
                    photo = catJson.optString("photo").ifBlank { null },
                    photoCredit = catJson.optString("photoCredit").ifBlank { null },
                    items = items
                )
            )
        }

        return MenuData(store = store, categories = categories)
    }
}
