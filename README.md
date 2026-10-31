# Kasir Mie Ayam Maju Lancar — Android (Kotlin + Java)

Versi Android native dari situs `majulancar.biz.id`. **Tanpa halaman login dan tanpa Cloudflare Turnstile.**

## Fitur & navigasi (bottom bar)
| Tab | Isi |
|---|---|
| 🍜 Menu | Filter kategori, atur jumlah, tambah ke keranjang, bar ringkasan keranjang |
| 🛒 Keranjang | Ubah jumlah/hapus, total, pembayaran **QRIS dinamis** (nominal otomatis) atau **Tunai** + kembalian |
| 📊 Omzet | Total/hari ini, rincian QRIS vs tunai, riwayat 100 transaksi terakhir, reset (dengan konfirmasi) |
| ℹ️ Tentang | Lokasi (Maps), kontak WhatsApp/Telegram, developer (Facebook, Instagram), info keamanan |

Tombol 🌙/☀️ di pojok kanan atas mengganti tema gelap/terang (pilihan diingat; sebelum dipilih mengikuti sistem).

## Keamanan
- **Tanpa izin apa pun** (termasuk INTERNET) → tidak ada data yang bisa keluar lewat jaringan.
- Omzet & keranjang terenkripsi **AES-256-GCM**, kunci di **Android Keystore** (StrongBox bila ada); nama berkas ikut diautentikasi (AAD).
- Harga **selalu dibaca dari katalog** (`Catalog.kt`), bukan dari data keranjang; total dihitung ulang saat checkout.
- Penjaga integritas: bila riwayat gagal dimuat, penulisan dinonaktifkan agar data lama tak tertimpa; data rusak dikarantina, tidak dihapus.
- QRIS: payload di-parse sebagai TLV dan divalidasi (struktur, ASCII, CRC) sebelum diubah; payload tidak valid tidak pernah dimodifikasi.
- `FLAG_SECURE` (anti screenshot/perekam layar, termasuk dialog pembayaran), proteksi tapjacking, `allowBackup=false` + `dataExtractionRules`, cleartext dilarang.
- Tautan keluar hanya https ke allowlist (`SafeLinks`).
- Release: R8 (minify + shrink), log v/d/i dibuang, metadata dependensi tidak disertakan.

> Karena login dihapus, **siapa pun yang memegang HP dalam keadaan terbuka bisa melihat omzet.** Pakai kunci layar HP.

## Build lokal
Butuh JDK 17 + Android SDK (platform 35). Gradle wrapper tidak disertakan (berkas biner); buat sekali dengan `gradle wrapper --gradle-version 8.10.2`, lalu:
```bash
./gradlew :app:testReleaseUnitTest :app:assembleRelease
```

## Penandatanganan & rilis di GitHub Actions
1. `./scripts/generate-keystore.sh` (sekali, di komputer Anda; **simpan cadangan keystore, jangan di-commit**).
2. Isi 4 secret repo (perintah `gh secret set …` dicetak skrip): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
3. Push ke `main` → workflow `Android Release` menjalankan unit test, membangun APK + AAB, memverifikasi tanda tangan, dan mengunggahnya sebagai artifact.
4. Push tag `v*` (mis. `git tag v1.0.0 && git push --tags`) → APK bertanda tangan juga dipublikasikan sebagai GitHub Release.

Tanpa secret, workflow tetap berjalan tetapi menghasilkan APK **unsigned** (tidak bisa dipasang) dan menampilkan peringatan.

## Mengubah data toko
Menu, harga, nomor WhatsApp, dan payload QRIS ada di `app/src/main/kotlin/.../data/Catalog.kt` (awalnya di-generate dari `data/menu.json`).
