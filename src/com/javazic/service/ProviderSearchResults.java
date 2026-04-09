package com.javazic.service;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultats agreges d'une recherche provider.
 */
public class ProviderSearchResults {

    private final List<Artiste> artistes;
    private final List<Album> albums;
    private final List<Morceau> morceaux;

    public ProviderSearchResults(List<Artiste> artistes, List<Album> albums, List<Morceau> morceaux) {
        this.artistes = artistes == null ? new ArrayList<>() : new ArrayList<>(artistes);
        this.albums = albums == null ? new ArrayList<>() : new ArrayList<>(albums);
        this.morceaux = morceaux == null ? new ArrayList<>() : new ArrayList<>(morceaux);
    }

    public static ProviderSearchResults vide() {
        return new ProviderSearchResults(List.of(), List.of(), List.of());
    }

    public List<Artiste> getArtistes() {
        return Collections.unmodifiableList(artistes);
    }

    public List<Album> getAlbums() {
        return Collections.unmodifiableList(albums);
    }

    public List<Morceau> getMorceaux() {
        return Collections.unmodifiableList(morceaux);
    }

    public boolean estVide() {
        return artistes.isEmpty() && albums.isEmpty() && morceaux.isEmpty();
    }
}
