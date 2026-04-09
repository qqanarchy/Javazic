package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class RechercheService {

    private final DataStore dataStore;

    public RechercheService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Artiste> rechercherArtistes(String motCle) {
        String mc = motCle.toLowerCase();
        return dataStore.getTousArtistes().stream()
                .filter(a -> a.getNom().toLowerCase().contains(mc)
                        || a.getPaysOrigine().toLowerCase().contains(mc))
                .collect(Collectors.toList());
    }

    public List<Album> rechercherAlbums(String motCle) {
        String mc = motCle.toLowerCase();
        return dataStore.getTousAlbums().stream()
                .filter(a -> a.getTitre().toLowerCase().contains(mc)
                        || a.getArtiste().getNom().toLowerCase().contains(mc)
                        || a.getGenre().getLibelle().toLowerCase().contains(mc))
                .collect(Collectors.toList());
    }

    public List<Morceau> rechercherMorceaux(String motCle) {
        String mc = motCle.toLowerCase();
        return dataStore.getTousMorceaux().stream()
                .filter(m -> m.getTitre().toLowerCase().contains(mc)
                        || m.getArtistes().stream()
                                .anyMatch(a -> a.getNom().toLowerCase().contains(mc)))
                .collect(Collectors.toList());
    }

    public List<Playlist> rechercherPlaylists(String motCle) {
        String mc = motCle.toLowerCase();
        return dataStore.getPlaylistsPubliques().stream()
                .filter(p -> p.getNom().toLowerCase().contains(mc)
                        || p.getDescription().toLowerCase().contains(mc))
                .collect(Collectors.toList());
    }

    public List<Album> filtrerParGenre(Genre genre) {
        return dataStore.getAlbumsParGenre(genre);
    }
}
