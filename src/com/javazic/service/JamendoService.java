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
import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service d'acces a l'API Jamendo v3.0.
 */
public class JamendoService {

    private static final String BASE_URL = "https://api.jamendo.com/v3.0/";
    private static final String CLIENT_ID = "876aaf7e";
    private static final int DEFAULT_LIMIT = 20;
    private static final int GLOBAL_ARTISTS_LIMIT = 3;
    private static final int GLOBAL_ALBUMS_LIMIT = 3;
    private static final int GLOBAL_TRACKS_LIMIT = 4;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private final RemoteMediaRegistry remoteRegistry;

    public JamendoService(RemoteMediaRegistry remoteRegistry) {
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
        return rechercherTracksDepuisEndpoint("tracks",
                "limit=" + limit + "&order=popularity_week&include=musicinfo");
    }

    public List<Morceau> getMorceauxAlbum(int albumId) {
        Album album = remoteRegistry.getAlbum(albumId);
        if (album == null || album.getSource() != Source.JAMENDO || album.getSourceId().isEmpty()) {
            return new ArrayList<>();
        }

        List<Morceau> morceaux = rechercherTracksDepuisEndpoint("tracks",
                "album_id=" + encoder(album.getSourceId()) + "&limit=50&include=musicinfo");
        for (Morceau morceau : morceaux) {
            morceau.setAlbum(album);
        }
        return morceaux;
    }

    public List<Morceau> getMorceauxArtiste(int artisteId) {
        Artiste artiste = remoteRegistry.getArtiste(artisteId);
        if (artiste == null || artiste.getSource() != Source.JAMENDO || artiste.getSourceId().isEmpty()) {
            return new ArrayList<>();
        }

        return rechercherTracksDepuisEndpoint("tracks",
                "artist_id=" + encoder(artiste.getSourceId()) + "&limit=20&include=musicinfo");
    }

    public Morceau getMorceauJamendo(int id) {
        Morceau morceau = remoteRegistry.getMorceau(id);
        return morceau != null && morceau.getSource() == Source.JAMENDO ? morceau : null;
    }

    public Album getAlbumJamendo(int id) {
        Album album = remoteRegistry.getAlbum(id);
        return album != null && album.getSource() == Source.JAMENDO ? album : null;
    }

    public Artiste getArtisteJamendo(int id) {
        Artiste artiste = remoteRegistry.getArtiste(id);
        return artiste != null && artiste.getSource() == Source.JAMENDO ? artiste : null;
    }

    private List<Morceau> rechercherMorceaux(String query, int limit) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        return rechercherTracksDepuisEndpoint("tracks",
                "search=" + encoder(query) + "&limit=" + limit + "&include=musicinfo");
    }

    private List<Album> rechercherAlbums(String query, int limit) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        List<Album> resultats = new ArrayList<>();
        String json = appelApi("albums", "namesearch=" + encoder(query) + "&limit=" + limit);
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

        List<Artiste> resultats = new ArrayList<>();
        String json = appelApi("artists", "namesearch=" + encoder(query) + "&limit=" + limit);
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

    private List<Morceau> rechercherTracksDepuisEndpoint(String endpoint, String params) {
        List<Morceau> resultats = new ArrayList<>();
        String json = appelApi(endpoint, params);
        if (json == null) {
            return resultats;
        }

        for (Map<String, String> map : JsonParser.parseResultats(json)) {
            Morceau morceau = creerOuMettreAJourMorceau(map);
            if (morceau != null) {
                resultats.add(morceau);
            }
        }
        return resultats;
    }

    private Morceau creerOuMettreAJourMorceau(Map<String, String> map) {
        String trackSourceId = normaliserSourceId(map.get("id"), "track", map.get("name"));
        if (trackSourceId.isEmpty()) {
            return null;
        }

        Artiste artiste = construireArtisteDepuisTrack(map);
        if (artiste == null) {
            return null;
        }

        String titre = valeurOuDefaut(map.get("name"), "Inconnu");
        int duree = parseInt(map.get("duration"));
        String streamUrl = valeurOuDefaut(map.get("audio"), "");
        String imageUrl = valeurOuDefaut(map.get("image"), "");

        Morceau morceau = remoteRegistry.getOrCreateMorceau(
                Source.JAMENDO,
                trackSourceId,
                id -> new Morceau(id, titre, duree, artiste)
        );
        morceau.setTitre(titre);
        morceau.setDuree(duree);
        morceau.setStreamUrl(streamUrl);
        morceau.setSource(Source.JAMENDO);
        morceau.setSourceId(trackSourceId);

        String albumSourceId = normaliserSourceId(map.get("album_id"), "album", map.get("album_name"));
        if (!albumSourceId.isEmpty()) {
            Album album = remoteRegistry.getOrCreateAlbum(
                    Source.JAMENDO,
                    albumSourceId,
                    id -> new Album(id, valeurOuDefaut(map.get("album_name"), "Album inconnu"),
                            LocalDate.now(), Genre.AUTRE, artiste)
            );
            album.setTitre(valeurOuDefaut(map.get("album_name"), album.getTitre()));
            album.setArtiste(artiste);
            album.setImage(valeurOuDefaut(map.get("album_image"), imageUrl));
            album.setSource(Source.JAMENDO);
            album.setSourceId(albumSourceId);
            morceau.setAlbum(album);
        }

        return morceau;
    }

    private Album creerOuMettreAJourAlbum(Map<String, String> map) {
        String albumSourceId = normaliserSourceId(map.get("id"), "album", map.get("name"));
        if (albumSourceId.isEmpty()) {
            return null;
        }

        Artiste artiste = construireArtisteDepuisAlbum(map);
        if (artiste == null) {
            return null;
        }

        LocalDate dateSortie = parseDate(map.get("releasedate"));
        String nom = valeurOuDefaut(map.get("name"), "Inconnu");
        String imageUrl = valeurOuDefaut(map.get("image"), "");

        Album album = remoteRegistry.getOrCreateAlbum(
                Source.JAMENDO,
                albumSourceId,
                id -> new Album(id, nom, dateSortie, Genre.AUTRE, artiste)
        );
        album.setTitre(nom);
        album.setDateSortie(dateSortie);
        album.setArtiste(artiste);
        album.setImage(imageUrl);
        album.setSource(Source.JAMENDO);
        album.setSourceId(albumSourceId);
        return album;
    }

    private Artiste creerOuMettreAJourArtiste(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("id"), "artist", map.get("name"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("name"), "Inconnu");
        String website = valeurOuDefaut(map.get("website"), "");
        String imageUrl = valeurOuDefaut(map.get("image"), "");
        LocalDate dateDebut = parseDate(map.get("joindate"));

        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.JAMENDO,
                artistSourceId,
                id -> new Artiste(id, nom, website, "", dateDebut)
        );
        artiste.setNom(nom);
        artiste.setBiographie(website);
        artiste.setImage(imageUrl);
        artiste.setDateDebut(dateDebut);
        artiste.setSource(Source.JAMENDO);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private Artiste construireArtisteDepuisTrack(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("artist_id"), "artist", map.get("artist_name"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("artist_name"), "Inconnu");
        String imageUrl = valeurOuDefaut(map.get("image"), "");
        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.JAMENDO,
                artistSourceId,
                id -> new Artiste(id, nom, "", "", LocalDate.now())
        );
        artiste.setNom(nom);
        artiste.setImage(imageUrl);
        artiste.setSource(Source.JAMENDO);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private Artiste construireArtisteDepuisAlbum(Map<String, String> map) {
        String artistSourceId = normaliserSourceId(map.get("artist_id"), "artist", map.get("artist_name"));
        if (artistSourceId.isEmpty()) {
            return null;
        }

        String nom = valeurOuDefaut(map.get("artist_name"), "Inconnu");
        Artiste artiste = remoteRegistry.getOrCreateArtiste(
                Source.JAMENDO,
                artistSourceId,
                id -> new Artiste(id, nom, "", "", LocalDate.now())
        );
        artiste.setNom(nom);
        artiste.setSource(Source.JAMENDO);
        artiste.setSourceId(artistSourceId);
        return artiste;
    }

    private String appelApi(String endpoint, String params) {
        try {
            String urlStr = BASE_URL + endpoint + "?client_id=" + CLIENT_ID + "&format=json&" + params;
            HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            if (conn.getResponseCode() != 200) {
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

    private int parseInt(String valeur) {
        try {
            return Integer.parseInt(valeur == null ? "0" : valeur.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate parseDate(String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            return LocalDate.now();
        }
        try {
            String normalisee = valeur.length() >= 10 ? valeur.substring(0, 10) : valeur;
            return LocalDate.parse(normalisee);
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
}
