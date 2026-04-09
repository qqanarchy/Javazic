package com.javazic.service;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Genre;
import com.javazic.model.Morceau;
import com.javazic.model.Source;
import com.javazic.util.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service Apple iTunes / Apple Music previews.
 */
public class AppleItunesService {

    private static final String SEARCH_URL = "https://itunes.apple.com/search";
    private static final String LOOKUP_URL = "https://itunes.apple.com/lookup";
    private static final String LEGACY_RSS_TOP_SONGS_URL =
            "https://itunes.apple.com/%s/rss/topsongs/limit=%d/json";
    private static final String RSS_TOP_SONGS_URL =
            "https://rss.marketingtools.apple.com/api/v2/%s/music/most-played/%d/songs.json";
    private static final String STOREFRONT = "fr";
    private static final int DEFAULT_LIMIT = 20;
    private static final int GLOBAL_ARTISTS_LIMIT = 3;
    private static final int GLOBAL_ALBUMS_LIMIT = 3;
    private static final int GLOBAL_TRACKS_LIMIT = 4;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final RemoteMediaRegistry remoteRegistry;

    public AppleItunesService(RemoteMediaRegistry remoteRegistry) {
        this.remoteRegistry = remoteRegistry;
    }

    public List<Morceau> rechercherMorceaux(String query) {
        return rechercherMorceaux(query, DEFAULT_LIMIT);
    }

    public List<Album> rechercherAlbums(String query) {
        return rechercherAlbums(query, DEFAULT_LIMIT);
    }

    public List<Artiste> rechercherArtistes(String query) {
        return rechercherArtistes(query, DEFAULT_LIMIT);
    }

    public ProviderSearchResults rechercherGlobal(String query) {
        if (query == null || query.isBlank()) {
            return ProviderSearchResults.vide();
        }
        return new ProviderSearchResults(
                rechercherArtistes(query, GLOBAL_ARTISTS_LIMIT),
                rechercherAlbums(query, GLOBAL_ALBUMS_LIMIT),
                rechercherMorceaux(query, GLOBAL_TRACKS_LIMIT)
        );
    }

    public List<Morceau> getTitresTendance(int limit) {
        List<Morceau> resultats = chargerTendancesDepuisFluxMarketing(limit);
        if (!resultats.isEmpty()) {
            return resultats;
        }
        return chargerTendancesDepuisFluxLegacy(limit);
    }

    public List<Morceau> getMorceauxAlbum(int albumId) {
        Album album = remoteRegistry.getAlbum(albumId);
        if (album == null || album.getSource() != Source.APPLE_ITUNES || album.getSourceId().isEmpty()) {
            return new ArrayList<>();
        }

        List<Morceau> morceaux = extraireMorceaux(appelApi(LOOKUP_URL,
                "id=" + encoder(album.getSourceId())
                        + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                        + "&entity=song"));
        for (Morceau morceau : morceaux) {
            morceau.setAlbum(album);
        }
        return morceaux;
    }

    public List<Morceau> getMorceauxArtiste(int artisteId) {
        Artiste artiste = remoteRegistry.getArtiste(artisteId);
        if (artiste == null || artiste.getSource() != Source.APPLE_ITUNES || artiste.getSourceId().isEmpty()) {
            return new ArrayList<>();
        }

        return extraireMorceaux(appelApi(LOOKUP_URL,
                "id=" + encoder(artiste.getSourceId())
                        + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                        + "&entity=song&limit=" + DEFAULT_LIMIT));
    }

    public Morceau getMorceau(int id) {
        Morceau morceau = remoteRegistry.getMorceau(id);
        return morceau != null && morceau.getSource() == Source.APPLE_ITUNES ? morceau : null;
    }

    public Album getAlbum(int id) {
        Album album = remoteRegistry.getAlbum(id);
        return album != null && album.getSource() == Source.APPLE_ITUNES ? album : null;
    }

    public Artiste getArtiste(int id) {
        Artiste artiste = remoteRegistry.getArtiste(id);
        return artiste != null && artiste.getSource() == Source.APPLE_ITUNES ? artiste : null;
    }

    private List<Morceau> rechercherMorceaux(String query, int limit) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        String params = "term=" + encoder(query)
                + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                + "&media=music&entity=song&limit=" + limit;
        return extraireMorceaux(appelApi(SEARCH_URL, params));
    }

    private List<Album> rechercherAlbums(String query, int limit) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        String params = "term=" + encoder(query)
                + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                + "&media=music&entity=album&limit=" + limit;
        String json = appelApi(SEARCH_URL, params);
        List<Album> resultats = new ArrayList<>();
        if (json == null) {
            return resultats;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            Album album = creerOuMettreAJourAlbum(map);
            if (album != null) {
                resultats.add(album);
            }
        }
        return resultats;
    }

    private List<Artiste> rechercherArtistes(String query, int limit) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        String params = "term=" + encoder(query)
                + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                + "&media=music&entity=musicArtist&limit=" + limit;
        String json = appelApi(SEARCH_URL, params);
        List<Artiste> resultats = new ArrayList<>();
        if (json == null) {
            return resultats;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            Artiste artiste = creerOuMettreAJourArtiste(map);
            if (artiste != null) {
                resultats.add(artiste);
            }
        }
        return resultats;
    }

    private List<Morceau> extraireMorceaux(String json) {
        List<Morceau> resultats = new ArrayList<>();
        if (json == null) {
            return resultats;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            if (!estEntreeMorceau(map)) {
                continue;
            }
            Morceau morceau = creerOuMettreAJourMorceau(map);
            if (morceau != null && !morceau.getStreamUrl().isEmpty()) {
                resultats.add(morceau);
            }
        }
        return resultats;
    }

    private Morceau rechercherMorceauDepuisFlux(Map<String, String> map) {
        String trackId = valeurOuDefaut(map.get("id"), "");
        if (!trackId.isEmpty()) {
            Morceau morceau = rechercherMorceauParId(trackId);
            if (morceau != null) {
                return morceau;
            }
        }

        String titre = valeurOuDefaut(map.get("name"), "");
        String artisteNom = valeurOuDefaut(map.get("artistName"), "");
        if (titre.isEmpty() || artisteNom.isEmpty()) {
            return null;
        }

        String params = "term=" + encoder(artisteNom + " " + titre)
                + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT)
                + "&media=music&entity=song&limit=5";
        String json = appelApi(SEARCH_URL, params);
        if (json == null) {
            return null;
        }

        Morceau meilleur = null;
        int meilleurScore = -1;
        for (Map<String, String> candidat : JsonParser.parseResultats(json)) {
            if (!estEntreeMorceau(candidat)) {
                continue;
            }
            Morceau morceau = creerOuMettreAJourMorceau(candidat);
            if (morceau == null) {
                continue;
            }
            int score = calculerScoreCorrespondance(titre, artisteNom,
                    candidat.get("trackName"), candidat.get("artistName"));
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleur = morceau;
            }
        }
        return meilleurScore >= 0 ? meilleur : null;
    }

    private Morceau rechercherMorceauParId(String trackId) {
        String json = appelApi(LOOKUP_URL,
                "id=" + encoder(trackId)
                        + "&country=" + STOREFRONT.toUpperCase(Locale.ROOT));
        if (json == null) {
            return null;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            if (!estEntreeMorceau(map)) {
                continue;
            }
            Morceau morceau = creerOuMettreAJourMorceau(map);
            if (morceau != null && !morceau.getStreamUrl().isEmpty()) {
                return morceau;
            }
        }
        return null;
    }

    private List<Morceau> chargerTendancesDepuisFluxMarketing(int limit) {
        List<Morceau> resultats = new ArrayList<>();
        String url = String.format(RSS_TOP_SONGS_URL, STOREFRONT, limit);
        String json = appelUrl(url);
        if (json == null) {
            return resultats;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            if (resultats.size() >= limit) {
                break;
            }
            Morceau morceau = rechercherMorceauDepuisFlux(map);
            ajouterMorceauSiUnique(resultats, morceau, limit);
        }
        return resultats;
    }

    private List<Morceau> chargerTendancesDepuisFluxLegacy(int limit) {
        List<Morceau> resultats = new ArrayList<>();
        String url = String.format(LEGACY_RSS_TOP_SONGS_URL, STOREFRONT, limit);
        String json = appelUrl(url);
        if (json == null) {
            return resultats;
        }

        for (String trackId : extraireTrackIdsFluxLegacy(json)) {
            if (resultats.size() >= limit) {
                break;
            }
            Morceau morceau = rechercherMorceauParId(trackId);
            ajouterMorceauSiUnique(resultats, morceau, limit);
        }
        return resultats;
    }

    private List<String> extraireTrackIdsFluxLegacy(String json) {
        List<String> ids = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return ids;
        }

        Pattern pattern = Pattern.compile(
                "\"id\"\\s*:\\s*\\{.*?\"im:id\"\\s*:\\s*\"(\\d+)\"",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String id = matcher.group(1);
            if (!ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private void ajouterMorceauSiUnique(List<Morceau> resultats, Morceau morceau, int limit) {
        if (morceau == null || resultats.size() >= limit) {
            return;
        }
        for (Morceau existant : resultats) {
            if (existant.getId() == morceau.getId()) {
                return;
            }
            if (existant.getSource() == morceau.getSource()
                    && !existant.getSourceId().isEmpty()
                    && existant.getSourceId().equals(morceau.getSourceId())) {
                return;
            }
        }
        resultats.add(morceau);
    }

    private int calculerScoreCorrespondance(String titreAttendu, String artisteAttendu,
                                            String titreTrouve, String artisteTrouve) {
        String titreA = normaliser(titreAttendu);
        String artisteA = normaliser(artisteAttendu);
        String titreB = normaliser(titreTrouve);
        String artisteB = normaliser(artisteTrouve);

        int score = 0;
        if (titreA.equals(titreB)) {
            score += 4;
        } else if (!titreA.isEmpty() && titreB.contains(titreA)) {
            score += 2;
        }

        if (artisteA.equals(artisteB)) {
            score += 4;
        } else if (!artisteA.isEmpty() && artisteB.contains(artisteA)) {
            score += 2;
        }
        return score;
    }

    private Morceau creerOuMettreAJourMorceau(Map<String, String> map) {
        String previewUrl = valeurOuDefaut(map.get("previewUrl"), "");
        if (previewUrl.isEmpty()) {
            return null;
        }

        Artiste artiste = construireArtisteDepuisTrack(map);
        if (artiste == null) {
            return null;
        }

        String trackSourceId = normaliserSourceId(map.get("trackId"), "track", map.get("trackName"));
        if (trackSourceId.isEmpty()) {
            return null;
        }

        String titre = valeurOuDefaut(map.get("trackName"), "Inconnu");
        int duree = parseMillisToSeconds(map.get("trackTimeMillis"));
        if (duree <= 0) {
            duree = 30;
        }
        final int dureeFinale = duree;

        Morceau morceau = remoteRegistry.getOrCreateMorceau(
                Source.APPLE_ITUNES,
                trackSourceId,
                id -> new Morceau(id, titre, dureeFinale, artiste)
        );
        morceau.setTitre(titre);
        morceau.setDuree(dureeFinale);
        morceau.setStreamUrl(previewUrl);
        morceau.setSource(Source.APPLE_ITUNES);
        morceau.setSourceId(trackSourceId);

        String albumSourceId = normaliserSourceId(map.get("collectionId"), "album", map.get("collectionName"));
        if (!albumSourceId.isEmpty()) {
            Album album = remoteRegistry.getOrCreateAlbum(
                    Source.APPLE_ITUNES,
                    albumSourceId,
                    id -> new Album(id, valeurOuDefaut(map.get("collectionName"), "Album inconnu"),
                            parseDate(map.get("releaseDate")), Genre.AUTRE, artiste)
            );
            album.setTitre(valeurOuDefaut(map.get("collectionName"), album.getTitre()));
            album.setDateSortie(parseDate(map.get("releaseDate")));
            album.setImage(valeurOuDefaut(map.get("artworkUrl100"), ""));
            album.setArtiste(artiste);
            album.setSource(Source.APPLE_ITUNES);
            album.setSourceId(albumSourceId);
            morceau.setAlbum(album);
        }

        return morceau;
    }

    private Album creerOuMettreAJourAlbum(Map<String, String> map) {
        String albumSourceId = normaliserSourceId(map.get("collectionId"), "album", map.get("collectionName"));
        if (albumSourceId.isEmpty()) {
            return null;
        }

        Artiste artiste = construireArtisteDepuisAlbum(map);
        if (artiste == null) {
            return null;
        }

        String titre = valeurOuDefaut(map.get("collectionName"), "Inconnu");
        LocalDate dateSortie = parseDate(map.get("releaseDate"));
        Album album = remoteRegistry.getOrCreateAlbum(
                Source.APPLE_ITUNES,
                albumSourceId,
                id -> new Album(id, titre, dateSortie, Genre.AUTRE, artiste)
        );
        album.setTitre(titre);
        album.setDateSortie(dateSortie);
        album.setImage(valeurOuDefaut(map.get("artworkUrl100"), ""));
        album.setArtiste(artiste);
        album.setSource(Source.APPLE_ITUNES);
        album.setSourceId(albumSourceId);
        return album;
    }

    private Artiste creerOuMettreAJourArtiste(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("artistId"), "artist", map.get("artistName"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("artistName"), "Inconnu");
        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.APPLE_ITUNES,
                artistSourceId,
                id -> new Artiste(id, nom, "", map.getOrDefault("primaryGenreName", ""), LocalDate.now())
        );
        artiste.setNom(nom);
        artiste.setPaysOrigine(valeurOuDefaut(map.get("primaryGenreName"), artiste.getPaysOrigine()));
        artiste.setSource(Source.APPLE_ITUNES);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private Artiste construireArtisteDepuisTrack(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("artistId"), "artist", map.get("artistName"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("artistName"), "Inconnu");
        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.APPLE_ITUNES,
                artistSourceId,
                id -> new Artiste(id, nom, "", "", LocalDate.now())
        );
        artiste.setNom(nom);
        artiste.setSource(Source.APPLE_ITUNES);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private Artiste construireArtisteDepuisAlbum(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("artistId"), "artist", map.get("artistName"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("artistName"), "Inconnu");
        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.APPLE_ITUNES,
                artistSourceId,
                id -> new Artiste(id, nom, "", "", LocalDate.now())
        );
        artiste.setNom(nom);
        artiste.setSource(Source.APPLE_ITUNES);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private boolean estEntreeMorceau(Map<String, String> map) {
        String wrapperType = valeurOuDefaut(map.get("wrapperType"), "");
        String kind = valeurOuDefaut(map.get("kind"), "");
        return "track".equalsIgnoreCase(wrapperType) || "song".equalsIgnoreCase(kind);
    }

    private String appelApi(String baseUrl, String params) {
        return appelUrl(baseUrl + "?" + params);
    }

    private String appelUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8");
            conn.setRequestProperty("Cache-Control", "no-cache");

            int responseCode = conn.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                String location = conn.getHeaderField("Location");
                if (location != null && !location.isEmpty() && !location.equals(urlStr)) {
                    return appelUrl(location);
                }
            }

            if (responseCode != 200) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    sb.append(ligne);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String encoder(String texte) {
        try {
            return URLEncoder.encode(texte, "UTF-8");
        } catch (Exception e) {
            return texte == null ? "" : texte;
        }
    }

    private int parseMillisToSeconds(String valeur) {
        try {
            return Math.max(0, Integer.parseInt(valeur == null ? "0" : valeur.trim()) / 1000);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDate(String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(valeur.substring(0, 10));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String valeurOuDefaut(String valeur, String defaut) {
        return (valeur == null || valeur.isEmpty()) ? defaut : valeur;
    }

    private String normaliserSourceId(String valeur, String prefixe, String fallback) {
        if (valeur != null && !valeur.trim().isEmpty()) {
            return valeur.trim();
        }
        if (fallback == null || fallback.trim().isEmpty()) {
            return "";
        }
        return prefixe + "-" + Math.abs(fallback.trim().toLowerCase().hashCode());
    }

    private String normaliser(String texte) {
        if (texte == null) {
            return "";
        }
        return texte.toLowerCase(Locale.ROOT)
                .replace("&", "and")
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
