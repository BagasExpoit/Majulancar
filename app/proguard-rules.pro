# Hapus log verbose/debug/info dari build release.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Pertahankan nomor baris untuk stack trace (dipetakan lewat mapping.txt yang diunggah CI).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
