package in.jansetu.auth.service;

import java.util.Locale;

/** Normalises login identifiers: mobiles to E.164 (+91…), emails to lower case. */
public final class Identifiers {
    private Identifiers() {
    }

    public static String mobile(String raw) {
        String d = raw.trim();
        if (d.startsWith("+91")) d = d.substring(3);
        if (!d.matches("[6-9]\\d{9}")) throw AuthErrors.badRequest("mobile must be a 10-digit Indian mobile number");
        return "+91" + d;
    }

    public static String email(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isEmail(String identifier) {
        return identifier.contains("@");
    }
}
