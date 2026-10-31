package com.majulancar.kasir.security;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.WindowManager;

import java.io.File;

/**
 * Small, dependency-free collection of runtime hardening checks.
 *
 * These are defense-in-depth, best-effort measures — not a substitute for
 * server-side verification (this app doesn't have a server at all; cart
 * and omzet data never leave the device, see {@code SecurePrefs}). Root
 * detection in particular is inherently heuristic and easy to defeat by a
 * determined attacker; it is only used here to show a one-time warning on
 * obviously-rooted devices, never to block app usage outright.
 */
public final class SecurityGuard {

    private SecurityGuard() {
    }

    /**
     * Prevents the screen from being captured in the Recents thumbnail,
     * screenshots, or screen-recording, for any activity showing a QRIS
     * code or the omzet ledger (financial data).
     */
    public static void enableScreenshotProtection(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );
    }

    /** True if this build was compiled as debuggable (should always be false for release). */
    public static boolean isDebuggableBuild(android.content.Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * Heuristic, best-effort root detection: looks for common su binaries
     * and root-management packages. Returns false (i.e. "assume not
     * rooted") on any uncertainty rather than ever throwing.
     */
    public static boolean looksRooted(android.content.Context context) {
        if (checkSuBinaries()) return true;
        if (checkRootPackages(context)) return true;
        return checkBuildTags();
    }

    private static boolean checkSuBinaries() {
        String[] paths = {
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/su",
                "/system/bin/.ext/.su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRootPackages(android.content.Context context) {
        String[] pkgs = {
                "com.topjohnwu.magisk",
                "eu.chainfire.supersu",
                "com.noshufou.android.su",
                "com.koushikdutta.superuser"
        };
        PackageManager pm = context.getPackageManager();
        for (String pkg : pkgs) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
                // not installed — expected on a non-rooted device
            }
        }
        return false;
    }

    private static boolean checkBuildTags() {
        String tags = Build.TAGS;
        return tags != null && tags.contains("test-keys");
    }
}
