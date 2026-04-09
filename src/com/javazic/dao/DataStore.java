package com.javazic.dao;

import com.javazic.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Stockage centralise en memoire avec sauvegarde/chargement par serialisation.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FICHIER_SAUVEGARDE = "javazic_data.ser";

    private final Map<Integer, Utilisateur> utilisateurs = new LinkedHashMap<>();
    private final Map<Integer, Artiste> artistes = new LinkedHashMap<>();
    private final Map<Integer, Album> albums = new LinkedHashMap<>();
    private final Map<Integer, Morceau> morceaux = new LinkedHashMap<>();
    private final Map<Integer, Groupe> groupes = new LinkedHashMap<>();
    private final Map<Integer, Playlist> playlists = new LinkedHashMap<>();
    private final Map<Integer, Avis> avis = new LinkedHashMap<>();

    private final AtomicInteger seqUtilisateur = new AtomicInteger(0);
    private final AtomicInteger seqArtiste = new AtomicInteger(0);
    private final AtomicInteger seqAlbum = new AtomicInteger(0);
    private final AtomicInteger seqMorceau = new AtomicInteger(0);
    private final AtomicInteger seqGroupe = new AtomicInteger(0);
    private final AtomicInteger seqPlaylist = new AtomicInteger(0);
    private final AtomicInteger seqAvis = new AtomicInteger(0);

    // === Generation d'ID ===
    public int prochainIdUtilisateur() { return seqUtilisateur.incrementAndGet(); }
    public int prochainIdArtiste() { return seqArtiste.incrementAndGet(); }
    public int prochainIdAlbum() { return seqAlbum.incrementAndGet(); }
    public int prochainIdMorceau() { return seqMorceau.incrementAndGet(); }
    public int prochainIdGroupe() { return seqGroupe.incrementAndGet(); }
    public int prochainIdPlaylist() { return seqPlaylist.incrementAndGet(); }
    public int prochainIdAvis() { return seqAvis.incrementAndGet(); }

    // === Utilisateurs ===
    public void ajouterUtilisateur(Utilisateur u) { utilisateurs.put(u.getId(), u); }
    public Utilisateur getUtilisateur(int id) { return utilisateurs.get(id); }
    public void supprimerUtilisateur(int id) { utilisateurs.remove(id); }
    public List<Utilisateur> getTousUtilisateurs() { return new ArrayList<>(utilisateurs.values()); }

    public Utilisateur trouverUtilisateurParEmail(String email) {
        return utilisateurs.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    // === Artistes ===
    public void ajouterArtiste(Artiste a) { artistes.put(a.getId(), a); }
    public Artiste getArtiste(int id) { return artistes.get(id); }
    public void supprimerArtiste(int id) { artistes.remove(id); }
    public List<Artiste> getTousArtistes() { return new ArrayList<>(artistes.values()); }

    // === Albums ===
    public void ajouterAlbum(Album a) { albums.put(a.getId(), a); }
    public Album getAlbum(int id) { return albums.get(id); }
    public void supprimerAlbum(int id) { albums.remove(id); }
    public List<Album> getTousAlbums() { return new ArrayList<>(albums.values()); }

    public List<Album> getAlbumsParArtiste(int artisteId) {
        return albums.values().stream()
                .filter(a -> a.getArtiste().getId() == artisteId)
                .collect(Collectors.toList());
    }

    public List<Album> getAlbumsParGenre(Genre genre) {
        return albums.values().stream()
                .filter(a -> a.getGenre() == genre)
                .collect(Collectors.toList());
    }

    // === Morceaux ===
    public void ajouterMorceau(Morceau m) { morceaux.put(m.getId(), m); }
    public Morceau getMorceau(int id) { return morceaux.get(id); }
    public void supprimerMorceau(int id) { morceaux.remove(id); }
    public List<Morceau> getTousMorceaux() { return new ArrayList<>(morceaux.values()); }

    public List<Morceau> getMorceauxParAlbum(int albumId) {
        return morceaux.values().stream()
                .filter(m -> m.getAlbum() != null && m.getAlbum().getId() == albumId)
                .collect(Collectors.toList());
    }

    public List<Morceau> getMorceauxParArtiste(int artisteId) {
        return morceaux.values().stream()
                .filter(m -> m.getArtistes().stream().anyMatch(a -> a.getId() == artisteId))
                .collect(Collectors.toList());
    }

    // === Groupes ===
    public void ajouterGroupe(Groupe g) { groupes.put(g.getId(), g); }
    public Groupe getGroupe(int id) { return groupes.get(id); }
    public void supprimerGroupe(int id) { groupes.remove(id); }
    public List<Groupe> getTousGroupes() { return new ArrayList<>(groupes.values()); }

    // === Playlists ===
    public void ajouterPlaylist(Playlist p) { playlists.put(p.getId(), p); }
    public Playlist getPlaylist(int id) { return playlists.get(id); }
    public void supprimerPlaylist(int id) { playlists.remove(id); }
    public List<Playlist> getToutesPlaylists() { return new ArrayList<>(playlists.values()); }

    public List<Playlist> getPlaylistsParUtilisateur(int utilisateurId) {
        return playlists.values().stream()
                .filter(p -> p.getProprietaire().getId() == utilisateurId)
                .collect(Collectors.toList());
    }

    public List<Playlist> getPlaylistsPubliques() {
        return playlists.values().stream()
                .filter(Playlist::isEstPublique)
                .collect(Collectors.toList());
    }

    // === Avis ===
    public void ajouterAvis(Avis a) { avis.put(a.getId(), a); }
    public Avis getAvis(int id) { return avis.get(id); }
    public void supprimerAvis(int id) { avis.remove(id); }

    public List<Avis> getAvisParMorceau(int morceauId) {
        return avis.values().stream()
                .filter(a -> a.getMorceau().getId() == morceauId)
                .collect(Collectors.toList());
    }

    public Avis getAvisParUtilisateurEtMorceau(int utilisateurId, int morceauId) {
        return avis.values().stream()
                .filter(a -> a.getAuteur().getId() == utilisateurId
                        && a.getMorceau().getId() == morceauId)
                .findFirst().orElse(null);
    }

    public List<Avis> getTousAvis() { return new ArrayList<>(avis.values()); }

    // === Statistiques ===
    public int getNombreUtilisateurs() { return utilisateurs.size(); }
    public int getNombreMorceaux() { return morceaux.size(); }
    public int getNombreAlbums() { return albums.size(); }
    public int getNombreGroupes() { return groupes.size(); }
    public int getNombreArtistes() { return artistes.size(); }

    public int getNombreTotalEcoutes() {
        return morceaux.values().stream().mapToInt(Morceau::getNombreEcoute).sum();
    }

    // === Persistance ===
    public void sauvegarder() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FICHIER_SAUVEGARDE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("[ERREUR] Impossible de sauvegarder les donnees : " + e.getMessage());
        }
    }

    public static DataStore charger() {
        File fichier = new File(FICHIER_SAUVEGARDE);
        if (!fichier.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fichier))) {
            return (DataStore) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERREUR] Impossible de charger les donnees : " + e.getMessage());
            return null;
        }
    }
}
