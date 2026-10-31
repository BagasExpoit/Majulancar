#!/usr/bin/env bash
# Membuat keystore penandatangan rilis. Jalankan SEKALI di komputer Anda sendiri.
# Password diminta secara interaktif oleh keytool (tidak muncul di layar maupun riwayat shell).
set -euo pipefail

OUT="${1:-release.jks}"
ALIAS="${2:-majulancar}"

if [ -e "$OUT" ]; then
  echo "Berkas '$OUT' sudah ada — tidak ditimpa. Hapus/ganti nama dulu bila memang ingin membuat baru." >&2
  exit 1
fi

keytool -genkeypair -v \
  -keystore "$OUT" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 \
  -validity 10000

chmod 600 "$OUT"

cat <<MSG

Keystore dibuat: $OUT  (alias: $ALIAS)

PENTING: simpan cadangannya di tempat aman di luar repo. Jika keystore hilang,
Anda tidak bisa merilis pembaruan untuk aplikasi yang sama. JANGAN commit berkas ini.

Langkah berikutnya (butuh GitHub CLI 'gh' yang sudah login), dari folder repo:

  gh secret set KEYSTORE_BASE64   < <(base64 -w0 "$OUT")     # macOS: base64 -i "$OUT" | gh secret set KEYSTORE_BASE64
  gh secret set KEY_ALIAS         --body "$ALIAS"
  gh secret set KEYSTORE_PASSWORD   # akan meminta nilai secara interaktif
  gh secret set KEY_PASSWORD        # akan meminta nilai secara interaktif

MSG
