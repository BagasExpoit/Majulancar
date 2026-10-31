package id.biz.majulancar.kasir.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitas QRIS dinamis (nominal tertanam) sesuai EMV QR Code Merchant-Presented.
 *
 * Berbeda dengan versi web yang memotong string secara posisional, di sini payload
 * di-parse sebagai TLV tingkat atas dan divalidasi (struktur, panjang, ASCII, CRC)
 * sebelum diubah. Payload yang tidak valid TIDAK pernah dimodifikasi.
 *
 * Perubahan pada payload statis:
 *  - Tag 01 (Point of Initiation) "11" (statis) -> "12" (dinamis)
 *  - Tag 54 (Transaction Amount) disisipkan/diganti sesuai urutan tag
 *  - Tag 63 (CRC16-CCITT, poly 0x1021, init 0xFFFF) dihitung ulang
 */
public final class Qris {

    /** Batas nominal (tag 54 maksimal 13 karakter menurut EMV). */
    public static final long MAX_AMOUNT = 9_999_999_999L;

    private static final int MAX_PAYLOAD_LENGTH = 512;

    private Qris() {
    }

    /** Hasil pembuatan QRIS; bila gagal, payload = payload statis asli dan error terisi. */
    public static final class Result {
        public final String payload;
        public final boolean isDynamic;
        public final String error;

        Result(String payload, boolean isDynamic, String error) {
            this.payload = payload;
            this.isDynamic = isDynamic;
            this.error = error;
        }
    }

    private static final class Tlv {
        final String tag;
        final String value;

        Tlv(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }
    }

    /** CRC16-CCITT (False) -> 4 digit hex huruf besar. Input harus ASCII. */
    public static String crc16(String data) {
        int crc = 0xFFFF;
        for (int i = 0; i < data.length(); i++) {
            crc ^= (data.charAt(i) & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ 0x1021) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return String.format("%04X", crc);
    }

    /** @return null bila payload valid, atau alasan penolakan. */
    public static String validate(String payload) {
        if (payload == null || payload.length() < 20) {
            return "Payload QRIS kosong atau terlalu pendek.";
        }
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            return "Payload QRIS terlalu panjang.";
        }
        for (int i = 0; i < payload.length(); i++) {
            char c = payload.charAt(i);
            if (c < 0x20 || c > 0x7E) {
                return "Payload QRIS mengandung karakter non-ASCII.";
            }
        }
        List<Tlv> tlvs;
        try {
            tlvs = parse(payload);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (tlvs.isEmpty() || !"00".equals(tlvs.get(0).tag) || !"01".equals(tlvs.get(0).value)) {
            return "Payload tidak diawali indikator versi EMV (000201).";
        }
        String poi = find(tlvs, "01");
        if (!"11".equals(poi) && !"12".equals(poi)) {
            return "Tag 01 (metode inisiasi) tidak valid.";
        }
        if (!"ID".equals(find(tlvs, "58"))) {
            return "Tag 58 (Country Code = ID) tidak ditemukan.";
        }
        Tlv last = tlvs.get(tlvs.size() - 1);
        if (!"63".equals(last.tag) || last.value.length() != 4) {
            return "Tag 63 (CRC) tidak ada di akhir payload.";
        }
        String body = payload.substring(0, payload.length() - 4);
        if (!crc16(body).equalsIgnoreCase(last.value)) {
            return "CRC payload QRIS tidak cocok.";
        }
        return null;
    }

    /** Sisipkan nominal. @throws IllegalArgumentException bila payload/nominal tidak valid. */
    public static String generateDynamic(String staticPayload, long amount) {
        String reason = validate(staticPayload);
        if (reason != null) {
            throw new IllegalArgumentException("Payload QRIS tidak valid: " + reason);
        }
        if (amount <= 0 || amount > MAX_AMOUNT) {
            throw new IllegalArgumentException("Nominal transaksi di luar batas yang diizinkan.");
        }

        List<Tlv> source = parse(staticPayload);
        List<Tlv> out = new ArrayList<>();
        boolean amountInserted = false;
        String amountText = Long.toString(amount);

        for (Tlv t : source) {
            if ("63".equals(t.tag) || "54".equals(t.tag)) {
                continue; // CRC lama & nominal lama dibuang
            }
            if (!amountInserted && Integer.parseInt(t.tag) > 54) {
                out.add(new Tlv("54", amountText));
                amountInserted = true;
            }
            if ("01".equals(t.tag)) {
                out.add(new Tlv("01", "12"));
            } else {
                out.add(t);
            }
        }
        if (!amountInserted) {
            out.add(new Tlv("54", amountText));
        }

        StringBuilder sb = new StringBuilder();
        for (Tlv t : out) {
            sb.append(t.tag).append(twoDigits(t.value.length())).append(t.value);
        }
        sb.append("6304");
        String withHeader = sb.toString();
        return withHeader + crc16(withHeader);
    }

    /** Versi aman: gagal -> payload statis (nominal diketik manual oleh pembeli). */
    public static Result generateDynamicSafe(String staticPayload, long amount) {
        try {
            return new Result(generateDynamic(staticPayload, amount), true, null);
        } catch (IllegalArgumentException e) {
            return new Result(staticPayload, false, e.getMessage());
        }
    }

    // ---- internal ----

    static List<Tlv> parse(String payload) {
        List<Tlv> out = new ArrayList<>();
        int i = 0;
        int n = payload.length();
        while (i < n) {
            if (i + 4 > n) {
                throw new IllegalArgumentException("Struktur TLV terpotong.");
            }
            String tag = payload.substring(i, i + 2);
            String lenText = payload.substring(i + 2, i + 4);
            if (!isDigits(tag) || !isDigits(lenText)) {
                throw new IllegalArgumentException("Tag/panjang TLV bukan angka.");
            }
            int len = Integer.parseInt(lenText);
            if (i + 4 + len > n) {
                throw new IllegalArgumentException("Panjang TLV melebihi payload.");
            }
            for (Tlv existing : out) {
                if (existing.tag.equals(tag)) {
                    throw new IllegalArgumentException("Tag TLV ganda: " + tag);
                }
            }
            out.add(new Tlv(tag, payload.substring(i + 4, i + 4 + len)));
            i += 4 + len;
        }
        return out;
    }

    private static String find(List<Tlv> tlvs, String tag) {
        for (Tlv t : tlvs) {
            if (t.tag.equals(tag)) {
                return t.value;
            }
        }
        return null;
    }

    private static boolean isDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static String twoDigits(int n) {
        return (n < 10 ? "0" : "") + n;
    }
}
