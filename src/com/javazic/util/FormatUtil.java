package com.javazic.util;

public final class FormatUtil {

    private FormatUtil() {}

    public static String formaterDuree(int secondes) {
        int h = secondes / 3600;
        int m = (secondes % 3600) / 60;
        int s = secondes % 60;
        if (h > 0) {
            return String.format("%dh %02dmin %02ds", h, m, s);
        }
        return String.format("%dmin %02ds", m, s);
    }

    public static String formaterDureeCompacte(int secondes) {
        int total = Math.max(0, secondes);
        int h = total / 3600;
        int m = (total % 3600) / 60;
        int s = total % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%d:%02d", m, s);
    }

    public static String tronquer(String texte, int longueurMax) {
        if (texte == null) return "";
        if (texte.length() <= longueurMax) return texte;
        return texte.substring(0, longueurMax - 3) + "...";
    }

    public static String repeter(String s, int n) {
        return s.repeat(Math.max(0, n));
    }

    public static String centrer(String texte, int largeur) {
        if (texte.length() >= largeur) return texte;
        int padding = (largeur - texte.length()) / 2;
        return " ".repeat(padding) + texte;
    }
}
