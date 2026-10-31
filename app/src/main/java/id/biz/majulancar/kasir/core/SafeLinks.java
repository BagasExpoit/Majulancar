package id.biz.majulancar.kasir.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Allowlist tautan keluar. Aplikasi hanya boleh membuka https:// ke host yang
 * didaftarkan di sini, tanpa userinfo (user:pass@host) dan tanpa port non-standar.
 */
public final class SafeLinks {

    private static final Set<String> HOSTS = new HashSet<>(Arrays.asList(
            "www.google.com",
            "wa.me",
            "t.me",
            "www.facebook.com",
            "facebook.com",
            "www.instagram.com"
    ));

    private SafeLinks() {
    }

    public static boolean isAllowed(String url) {
        if (url == null || url.length() > 2048) {
            return false;
        }
        final URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        if (uri.getRawUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || !HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            return false;
        }
        // Google hanya untuk Maps, bukan seluruh domain google.com.
        if ("www.google.com".equalsIgnoreCase(host)) {
            String path = uri.getPath();
            return path != null && path.startsWith("/maps");
        }
        return true;
    }
}
