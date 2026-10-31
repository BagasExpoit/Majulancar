# Maju Lancar Kasir (Android)

Aplikasi kasir offline untuk **Mie Ayam Maju Lancar Mas Tri** — port native Android dari
situs web `majulancar.biz.id`. Semua data (keranjang & omzet) tersimpan terenkripsi di
perangkat (`EncryptedSharedPreferences`), tanpa server, tanpa login.

Fitur: Menu (3 kategori, stepper jumlah) · Keranjang · Bayar **QRIS** (QR sudah memuat
nominal total) atau **Tunai** (uang diterima, uang pas/nominal cepat, kembalian) ·
Rekap Omzet (total, hari ini, per metode, riwayat 100 transaksi, reset) · Tentang
(lokasi, WhatsApp, Telegram admin, developer). Tema terang & gelap otomatis.

## Cara build

**A. Android Studio (paling mudah)**
1. `File > Open` → pilih folder proyek ini, tunggu Gradle sync.
2. `Build > Build APK(s)` (debug), atau `Build > Generate Signed Bundle / APK` untuk rilis.
3. Hasil: `app/build/outputs/apk/...`

Butuh JDK 17 + Android SDK 34 (Android Studio sudah membawanya). Proyek ini tidak menyertakan
`gradlew`; Android Studio akan memakai Gradle bawaannya. Untuk command line:
`gradle wrapper --gradle-version 8.9` sekali saja, lalu `./gradlew assembleDebug`.

**B. GitHub Actions**
Push ke GitHub → tab *Actions* → workflow **Build APK** → unduh artifact `MajuLancarKasir-apk`.

**Tanda tangan rilis (opsional).** Tanpa secrets, rilis ditandatangani dengan debug keystore
(bisa diinstal, tapi bukan untuk Play Store). Untuk keystore sendiri, isi secrets
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
(`base64 -w0 my.keystore`). Jangan commit keystore.

## Struktur
- `assets/menu.json` — data toko, menu, harga, payload QRIS statis (ubah di sini saja).
- `util/Qris.kt` — sisip nominal ke QRIS (tag 54, indikator 11→12, hitung ulang CRC16).
- `data/` — model, repository menu/keranjang/omzet, penyimpanan terenkripsi.
- `ui/` — Menu, Keranjang (+ `PaymentDialog`), Omzet, Tentang.
- `security/SecurityGuard.java` — FLAG_SECURE (blok screenshot), peringatan root.

## Catatan
- Layar dilindungi `FLAG_SECURE`: screenshot/rekam layar diblokir (QRIS & omzet).
- Foto kategori dimuat dari Wikimedia Commons (HTTPS) — perlu internet; aplikasi tetap
  berfungsi tanpa foto.
- QR dibuat level koreksi **M** (lebih mudah dipindai daripada H pada payload sepanjang ini).
