package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogueService {

    private final DataStore dataStore;

    public CatalogueService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // === Artistes ===

    public Artiste creerArtiste(String nom, String biographie, String paysOrigine, LocalDate dateDebut) {
        int id = dataStore.prochainIdArtiste();
        Artiste artiste = new Artiste(id, nom, biographie, paysOrigine, dateDebut);
        dataStore.ajouterArtiste(artiste);
        return artiste;
    }

    public List<Artiste> getTousArtistes() {
        return dataStore.getTousArtistes();
    }

    public Artiste getArtiste(int id) {
        return dataStore.getArtiste(id);
    }

    public void supprimerArtiste(int id) {
        dataStore.supprimerArtiste(id);
    }

    // === Albums ===

    public Album creerAlbum(String titre, LocalDate dateSortie, Genre genre, Artiste artiste) {
        int id = dataStore.prochainIdAlbum();
        Album album = new Album(id, titre, dateSortie, genre, artiste);
        dataStore.ajouterAlbum(album);
        return album;
    }

    public List<Album> getTousAlbums() {
        return dataStore.getTousAlbums();
    }

    public Album getAlbum(int id) {
        return dataStore.getAlbum(id);
    }

    public List<Album> getAlbumsParArtiste(int artisteId) {
        return dataStore.getAlbumsParArtiste(artisteId);
    }

    public List<Album> getAlbumsParGenre(Genre genre) {
        return dataStore.getAlbumsParGenre(genre);
    }

    public void supprimerAlbum(int id) {
        dataStore.supprimerAlbum(id);
    }

    // === Morceaux ===

    public Morceau creerMorceau(String titre, int duree, Artiste artistePrincipal, Album album) {
        return creerMorceau(titre, duree, artistePrincipal, album, Genre.AUTRE);
    }

    public Morceau creerMorceau(String titre, int duree, Artiste artistePrincipal, Album album, Genre genre) {
        int id = dataStore.prochainIdMorceau();
        Morceau morceau = new Morceau(id, titre, duree, artistePrincipal);
        morceau.setGenre(genre);
        if (album != null) {
            album.ajouterMorceau(morceau);
        }
        dataStore.ajouterMorceau(morceau);
        return morceau;
    }

    public List<Morceau> getTousMorceaux() {
        return dataStore.getTousMorceaux();
    }

    public List<Morceau> getMorceauxDemo() {
        return dataStore.getTousMorceaux().stream()
                .filter(m -> !m.estDistant())
                .collect(Collectors.toList());
    }

    public Morceau getMorceau(int id) {
        return dataStore.getMorceau(id);
    }

    public List<Morceau> getMorceauxParAlbum(int albumId) {
        return dataStore.getMorceauxParAlbum(albumId);
    }

    public List<Morceau> getMorceauxParArtiste(int artisteId) {
        return dataStore.getMorceauxParArtiste(artisteId);
    }

    public void supprimerMorceau(int id) {
        dataStore.supprimerMorceau(id);
    }

    // === Groupes ===

    public Groupe creerGroupe(String nom, LocalDate dateFormation) {
        int id = dataStore.prochainIdGroupe();
        Groupe groupe = new Groupe(id, nom, dateFormation);
        dataStore.ajouterGroupe(groupe);
        return groupe;
    }

    public List<Groupe> getTousGroupes() {
        return dataStore.getTousGroupes();
    }

    public Groupe getGroupe(int id) {
        return dataStore.getGroupe(id);
    }

    public void ajouterMembreAuGroupe(int groupeId, Artiste artiste) {
        Groupe groupe = dataStore.getGroupe(groupeId);
        if (groupe != null) {
            groupe.ajouterMembre(artiste);
        }
    }

    public void supprimerGroupe(int id) {
        dataStore.supprimerGroupe(id);
    }
}
