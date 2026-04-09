package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Morceau;
import com.javazic.model.Playlist;
import com.javazic.model.Utilisateur;

import java.util.List;

public class PlaylistService {

    private final DataStore dataStore;
    private final MediaResolverService mediaResolverService;

    public PlaylistService(DataStore dataStore, MediaResolverService mediaResolverService) {
        this.dataStore = dataStore;
        this.mediaResolverService = mediaResolverService;
    }

    public Playlist creerPlaylist(String nom, String description, Utilisateur proprietaire) {
        int id = dataStore.prochainIdPlaylist();
        Playlist playlist = new Playlist(id, nom, description, proprietaire);
        dataStore.ajouterPlaylist(playlist);
        return playlist;
    }

    public boolean ajouterMorceauAPlaylist(int playlistId, int morceauId) {
        return ajouterMorceauAPlaylist(playlistId, mediaResolverService.resoudreMorceau(morceauId));
    }

    public boolean ajouterMorceauAPlaylist(int playlistId, Morceau morceau) {
        Playlist playlist = dataStore.getPlaylist(playlistId);
        if (playlist == null || morceau == null) {
            return false;
        }
        return playlist.ajouterMorceau(morceau);
    }

    public boolean retirerMorceauDePlaylist(int playlistId, int morceauId) {
        Playlist playlist = dataStore.getPlaylist(playlistId);
        if (playlist == null) {
            return false;
        }

        Morceau morceau = playlist.trouverMorceauParId(morceauId);
        if (morceau == null) {
            morceau = mediaResolverService.resoudreMorceau(morceauId);
        }
        if (morceau == null) {
            return false;
        }
        return playlist.retirerMorceau(morceau);
    }

    public Playlist getPlaylist(int id) {
        return dataStore.getPlaylist(id);
    }

    public List<Playlist> getPlaylistsUtilisateur(int utilisateurId) {
        return dataStore.getPlaylistsParUtilisateur(utilisateurId);
    }

    public List<Playlist> getPlaylistsPubliques() {
        return dataStore.getPlaylistsPubliques();
    }

    public boolean supprimerPlaylist(int playlistId, int utilisateurId) {
        Playlist playlist = dataStore.getPlaylist(playlistId);
        if (playlist == null) return false;
        if (playlist.getProprietaire().getId() != utilisateurId) return false;
        dataStore.supprimerPlaylist(playlistId);
        return true;
    }

    public boolean renommerPlaylist(int playlistId, String nouveauNom) {
        Playlist playlist = dataStore.getPlaylist(playlistId);
        if (playlist == null) return false;
        playlist.setNom(nouveauNom);
        return true;
    }

    public boolean changerVisibilite(int playlistId, boolean publique) {
        Playlist playlist = dataStore.getPlaylist(playlistId);
        if (playlist == null) return false;
        playlist.setEstPublique(publique);
        return true;
    }
}
