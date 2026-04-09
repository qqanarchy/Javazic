package com.javazic.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseur JSON minimal pour les reponses de l'API Jamendo.
 * Gere uniquement la structure : {"headers":{...},"results":[{...},{...}]}
 */
public final class JsonParser {

    private JsonParser() {}

    /**
     * Extrait le tableau "results" d'une reponse JSON Jamendo
     * et retourne chaque objet sous forme de Map cle/valeur.
     */
    public static List<Map<String, String>> parseResultats(String json) {
        List<Map<String, String>> resultats = new ArrayList<>();
        if (json == null || json.isEmpty()) return resultats;

        // Trouver le debut du tableau "results"
        int idx = json.indexOf("\"results\"");
        if (idx == -1) return resultats;

        idx = json.indexOf('[', idx);
        if (idx == -1) return resultats;

        // Extraire chaque objet {...} du tableau
        int profondeur = 0;
        int debutObjet = -1;

        for (int i = idx; i < json.length(); i++) {
            char c = json.charAt(i);

            // Sauter les chaines entre guillemets
            if (c == '"') {
                i = finDeChaine(json, i);
                continue;
            }

            if (c == '{') {
                profondeur++;
                if (profondeur == 1) {
                    debutObjet = i;
                }
            } else if (c == '}') {
                if (profondeur == 1 && debutObjet >= 0) {
                    String objet = json.substring(debutObjet, i + 1);
                    resultats.add(parseObjet(objet));
                    debutObjet = -1;
                }
                profondeur--;
            } else if (c == ']' && profondeur <= 1) {
                break;
            }
        }

        return resultats;
    }

    /**
     * Parse un objet JSON plat en Map cle/valeur (valeurs toujours String).
     */
    private static Map<String, String> parseObjet(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = json.indexOf('{') + 1;

        while (i < json.length()) {
            // Chercher la prochaine cle
            int debutCle = json.indexOf('"', i);
            if (debutCle == -1) break;

            int finCle = json.indexOf('"', debutCle + 1);
            if (finCle == -1) break;

            String cle = json.substring(debutCle + 1, finCle);

            // Chercher le ':' apres la cle
            int deuxPoints = json.indexOf(':', finCle);
            if (deuxPoints == -1) break;

            // Extraire la valeur
            int debutValeur = deuxPoints + 1;
            while (debutValeur < json.length() && json.charAt(debutValeur) == ' ') {
                debutValeur++;
            }

            if (debutValeur >= json.length()) break;

            String valeur;
            char premier = json.charAt(debutValeur);

            if (premier == '"') {
                // Valeur chaine
                int finValeur = finDeChaine(json, debutValeur);
                valeur = json.substring(debutValeur + 1, finValeur);
                // Desechapper les caracteres
                valeur = valeur.replace("\\\"", "\"").replace("\\\\", "\\")
                        .replace("\\/", "/").replace("\\n", "\n");
                i = finValeur + 1;
            } else if (premier == '{') {
                // Sous-objet : on le saute
                int fin = trouverFinBloc(json, debutValeur, '{', '}');
                valeur = "";
                i = fin + 1;
            } else if (premier == '[') {
                // Sous-tableau : on le saute
                int fin = trouverFinBloc(json, debutValeur, '[', ']');
                valeur = "";
                i = fin + 1;
            } else if (premier == 'n' && json.startsWith("null", debutValeur)) {
                valeur = "";
                i = debutValeur + 4;
            } else {
                // Valeur numerique ou booleenne
                int finValeur = debutValeur;
                while (finValeur < json.length()) {
                    char cv = json.charAt(finValeur);
                    if (cv == ',' || cv == '}' || cv == ']') break;
                    finValeur++;
                }
                valeur = json.substring(debutValeur, finValeur).trim();
                i = finValeur;
            }

            map.put(cle, valeur);

            // Avancer apres la virgule
            while (i < json.length() && (json.charAt(i) == ',' || json.charAt(i) == ' ')) {
                i++;
            }
        }

        return map;
    }

    /**
     * Trouve la fin d'une chaine JSON en gerant les caracteres echappes.
     * Retourne l'index du guillemet fermant.
     */
    private static int finDeChaine(String json, int debutGuillemet) {
        for (int i = debutGuillemet + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') {
                i++; // sauter le caractere echappe
            } else if (c == '"') {
                return i;
            }
        }
        return json.length() - 1;
    }

    /**
     * Trouve la fin d'un bloc (objet ou tableau) en gerant l'imbrication.
     */
    private static int trouverFinBloc(String json, int debut, char ouvrant, char fermant) {
        int profondeur = 0;
        for (int i = debut; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                i = finDeChaine(json, i);
            } else if (c == ouvrant) {
                profondeur++;
            } else if (c == fermant) {
                profondeur--;
                if (profondeur == 0) return i;
            }
        }
        return json.length() - 1;
    }
}
