# Maju Lancar Kasir — R8/ProGuard rules.
# Data is parsed with org.json (platform class), so no model keep rules needed.

# Tink (used by androidx.security:security-crypto) references optional
# annotation / HTTP classes that are not on the classpath.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
-keep class com.google.crypto.tink.** { *; }

# Coil / OkHttp optional platform classes
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
