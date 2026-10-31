<div align="center">

# 🧾 Maju Lancar — Kalkulator Kasir

**Aplikasi kasir Android yang ringan, rapi, dan siap dipakai jualan.**
Hitung belanjaan, terima uang pembeli, dan lihat kembalian dalam hitungan detik, lengkap dengan laporan penjualan.

[![Build APK](https://github.com/BagasExpoit/Majulancar/actions/workflows/build-apk.yml/badge.svg)](https://github.com/BagasExpoit/Majulancar/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/BagasExpoit/Majulancar?color=00695C&label=release)](https://github.com/BagasExpoit/Majulancar/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/BagasExpoit/Majulancar/total?color=80CBC4)](https://github.com/BagasExpoit/Majulancar/releases)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)

[⬇️ Unduh APK](https://github.com/BagasExpoit/Majulancar/releases/latest) ·
[🐞 Laporkan Bug](https://github.com/BagasExpoit/Majulancar/issues) ·
[👤 Developer](https://github.com/BagasExpoit)

</div>

---

## ✨ Fitur

| | Fitur | Keterangan |
|---|---|---|
| 💰 | **Input harga & jumlah** | Tambahkan banyak barang ke daftar belanja dalam satu transaksi |
| 🛒 | **Total otomatis** | Total barang dan total belanja dihitung langsung setiap ada perubahan |
| 💵 | **Uang pembeli & kembalian** | Kembalian muncul otomatis. Jika uang kurang, selisihnya ditampilkan |
| ✅ | **Selesaikan transaksi** | Satu ketukan untuk menyimpan transaksi ke laporan dan mulai transaksi baru |
| 📊 | **Laporan penjualan** | Rekap jumlah transaksi, barang terjual, dan pendapatan, dengan filter **Hari Ini** dan **Semua** |
| 📤 | **Bagikan laporan** | Kirim rincian laporan sebagai teks, misalnya lewat WhatsApp |
| 🗑️ | **Kelola riwayat** | Hapus satu transaksi atau seluruh riwayat dengan konfirmasi |
| 🧭 | **Navigasi bawah** | Bilah melayang dengan indikator bergeser: Kasir, Laporan, dan Developer |
| 🌗 | **Tema gelap & terang** | Ganti lewat saklar, default mengikuti pengaturan HP |
| 🇮🇩 | **Format Rupiah** | Pemisah ribuan standar Indonesia, contoh `Rp 1.250.000` |
| 💾 | **Data tersimpan** | Laporan tetap ada walau aplikasi ditutup |

<!--
## 📸 Tampilan
Taruh screenshot di folder docs/ lalu aktifkan baris di bawah:

| Kasir | Laporan | Developer |
|:---:|:---:|:---:|
| <img src="docs/kasir.png" width="220"> | <img src="docs/laporan.png" width="220"> | <img src="docs/developer.png" width="220"> |
-->

## 📲 Instalasi

1. Buka halaman **[Releases](https://github.com/BagasExpoit/Majulancar/releases/latest)**.
2. Unduh file APK terbaru.
3. Buka file tersebut di HP, lalu izinkan **Install dari sumber tidak dikenal** jika diminta.

> **Catatan:** membutuhkan Android 7.0 (API 24) atau lebih baru.
> Jika sebelumnya terpasang versi dengan tanda tangan berbeda (misalnya versi debug), uninstall dulu sebelum memasang versi baru.

## 🧰 Teknologi

- **Kotlin**: bahasa utama
- **Jetpack Compose + Material 3**: antarmuka deklaratif
- **Gradle Kotlin DSL**: konfigurasi build
- **GitHub Actions**: build, uji, dan rilis APK otomatis

## 🗂️ Struktur Proyek

```text
Majulancar/
├── .github/workflows/build-apk.yml    # CI: uji, build debug & release, rilis
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bagas/kasir/MainActivity.kt
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 👨‍💻 Developer

<div align="center">

**Bagas Orcarius**

[![Facebook](https://img.shields.io/badge/Facebook-1877F2?logo=facebook&logoColor=white)](https://facebook.com/willydevops1)
[![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/BagasExpoit)
[![Instagram](https://img.shields.io/badge/Instagram-E4405F?logo=instagram&logoColor=white)](https://www.instagram.com/bagas_samuji74?igsh=bmlscWxqN2pmZnJx)

⭐ Kalau aplikasi ini bermanfaat, beri bintang di repo ini!

<sub>Dibuat dengan Kotlin by Bagas Orcarius</sub>

</div>
