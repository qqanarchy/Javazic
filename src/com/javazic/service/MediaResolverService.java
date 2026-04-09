package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Avis;
import com.javazic.model.HistoriqueEcoute;
import com.javazic.model.Morceau;
import com.javazic.model.Playlist;
import com.javazic.model.Utilisateur;

/**
 * Resolution centralisee des morceaux et rehydratation du registre distant.
 */
public class MediaResolverService {

    private final DataStore dataStore;
    private final RemoteMediaRegistry remoteRegistry;

    public MediaResolverService(DataStore dataStore, RemoteMediaRegistry remoteRegistry) {
        this.dataStore = dataStore;
        this.remoteRegistry = remoteRegistry;
    }

    public Morceau resoudreMorceau(int id) {
        if (id < 0) {
            return remoteRegistry.getMorceau(id);
        }
        return dataStore.getMorceau(id);
    }

    public void rehydraterRegistreDistant() {
        remoteRegistry.vider();

        for (Playlist playlist : dataStore.getToutesPlaylists()) {
            for (Morceau morceau : playlist.getMorceaux()) {
                remoteRegistry.registerExistingMorceau(morceau);
            }
        }

        for (Utilisateur utilisateur : dataStore.getTousUtilisateurs()) {
            for (HistoriqueEcoute ecoute : utilisateur.getHistoriqueEcoutes()) {
                remoteRegistry.registerExistingMorceau(ecoute.getMorceau());
            }
        }

        for (Avis avis : dataStore.getTousAvis()) {
            remoteRegistry.registerExistingMorceau(avis.getMorceau());
        }
    }
}
