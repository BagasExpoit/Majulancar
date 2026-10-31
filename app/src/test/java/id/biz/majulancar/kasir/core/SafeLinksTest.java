package id.biz.majulancar.kasir.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SafeLinksTest {

    @Test
    public void allowsKnownHttpsHosts() {
        assertTrue(SafeLinks.isAllowed("https://wa.me/6287710528400?text=Halo%20Mie%20Ayam"));
        assertTrue(SafeLinks.isAllowed("https://t.me/Bagas_ocarius"));
        assertTrue(SafeLinks.isAllowed("https://facebook.com/willydevops1"));
        assertTrue(SafeLinks.isAllowed("https://www.instagram.com/bagas_samuji74?igsh=bmlscWxqN2pmZnJx"));
        assertTrue(SafeLinks.isAllowed("https://www.google.com/maps/place/Dramaga/@-6.5631508,106.7243088,15z"));
    }

    @Test
    public void blocksEverythingElse() {
        String[] bad = {
                "http://wa.me/6282", "javascript:alert(1)", "intent://x#Intent;scheme=http;end",
                "file:///etc/passwd", "content://x", "https://evil.com/", "https://wa.me.evil.com/",
                "https://wa.me@evil.com/", "https://user:pw@wa.me/x", "https://wa.me:8443/x",
                "https://www.google.com/search?q=x", "https://www.google.com/",
                "https://github.com/BagasExpoit", "", null,
        };
        for (String url : bad) {
            assertFalse("harus diblokir: " + url, SafeLinks.isAllowed(url));
        }
    }
}
