package com.javazic.controller;

import com.javazic.model.Morceau;
import com.javazic.model.Playlist;
import com.javazic.model.Utilisateur;
import com.javazic.service.AppleItunesService;
import com.javazic.service.CatalogueService;
import com.javazic.service.JamendoService;
import com.javazic.service.MediaResolverService;
import com.javazic.service.PlaylistService;
import com.javazic.service.ProviderSearchResults;
import com.javazic.service.RechercheService;
import com.javazic.service.ResultContextService;
import com.javazic.view.ConsoleView;

import java.util.List;

public class PlaylistController {

    private static final int PROVIDER_LIMIT = 10;

    private final PlaylistService playlistService;
    private final CatalogueService catalogueService;
    private final RechercheService rechercheService;
    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final MediaResolverService mediaResolverService;
    private final ResultContextService resultContextService;
    private final ConsoleView vue;

    public PlaylistController(PlaylistService playlistService,
                              CatalogueService catalogueService,
                              RechercheService rechercheService,
                              JamendoService jamendoService,
                              AppleItunesService appleItunesService,
                              MediaResolverService mediaResolverService,
                              ResultContextService resultContextService,
                              ConsoleView vue) {
        this.playlistService = playlistService;
        this.catalogueService = catalogueService;
        this.rechercheService = rechercheService;
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.mediaResolverService = mediaResolverService;
        this.resultContextService = resultContextService;
        this.vue = vue;
    }

    public void gererMesPlaylists(Utilisateur utilisateur) {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuPlaylists();
            switch (choix) {
                case 1 -> voirMesPlaylists(utilisateur);
                case 2 -> creerPlaylist(utilisateur);
                case 3 -> gererUnePlaylist(utilisateur);
                case 4 -> supprimerPlaylist(utilisateur);
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    public void voirPlaylistsPubliques() {
        vue.afficherTitre("Playlists Publiques");
        List<Playlist> playlists = playlistService.getPlaylistsPubliques();
        vue.afficherListePlaylists(playlists);
        vue.sautLigne();

        int id = vue.lireEntier("Entrez l'ID d'une playlist pour le detail (0 = retour)");
        if (id > 0) {
            Playlist playlist = playlistService.getPlaylist(id);
            if (playlist != null && playlist.isEstPublique()) {
                vue.afficherTitre("Playlist : " + playlist.getNom());
                vue.afficherMessage("Par : " + playlist.getProprietaire().getNom());
                vue.afficherDetailPlaylist(playlist);
            } else {
                vue.afficherErreur("Playlist introuvable.");
            }
        }
        vue.attendreTouche();
    }

    private void voirMesPlaylists(Utilisateur utilisateur) {
        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateur.getId());
        vue.afficherTitre("Mes Playlists");
        vue.afficherListePlaylists(playlists);
        vue.sautLigne();

        int id = vue.lireEntier("Entrez l'ID d'une playlist pour le detail (0 = retour)");
        if (id > 0) {
            Playlist playlist = playlistService.getPlaylist(id);
            if (playlist != null && playlist.getProprietaire().getId() == utilisateur.getId()) {
                vue.afficherTitre("Playlist : " + playlist.getNom());
                vue.afficherDetailPlaylist(playlist);
            } else {
                vue.afficherErreur("Playlist introuvable.");
            }
        }
        vue.attendreTouche();
    }

    private void creerPlaylist(Utilisateur utilisateur) {
        vue.afficherTitre("Creer une playlist");
        String nom = vue.lireTexte("Nom de la playlist");
        String desc = vue.lireTexte("Description");

        if (nom.isEmpty()) {
            vue.afficherErreur("Le nom est obligatoire.");
            return;
        }
        Playlist playlist = playlistService.creerPlaylist(nom, desc, utilisateur);
        vue.afficherSucces("Playlist creee : " + playlist.getNom());
    }

    private void gererUnePlaylist(Utilisateur utilisateur) {
        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateur.getId());
        vue.afficherListePlaylists(playlists);
        vue.sautLigne();

        int playlistId = vue.lireEntier("ID de la playlist a gerer");
        Playlist playlist = playlistService.getPlaylist(playlistId);

        if (playlist == null || playlist.getProprietaire().getId() != utilisateur.getId()) {
            vue.afficherErreur("Playlist introuvable ou non autorisee.");
            return;
        }

        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuGestionPlaylist(playlist);
            switch (choix) {
                case 1 -> ajouterMorceauAPlaylist(playlist);
                case 2 -> retirerMorceauDePlaylist(playlist);
                case 3 -> renommerPlaylist(playlist);
                case 4 -> changerVisibilite(playlist);
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void ajouterMorceauAPlaylist(Playlist playlist) {
        int choix = vue.afficherMenuAjoutPlaylist();
        switch (choix) {
            case 1 -> ajouterDepuisDerniersResultats(playlist);
            case 2 -> ajouterDepuisSource(playlist);
            case 0 -> { }
            default -> vue.afficherErreur("Choix invalide.");
        }
    }

    private void ajouterDepuisDerniersResultats(Playlist playlist) {
        List<Morceau> morceaux = resultContextService.getDerniersMorceaux();
        if (morceaux.isEmpty()) {
            vue.afficherErreur("Aucun dernier resultat exploitable.");
            return;
        }

        vue.afficherListeMorceaux(morceaux);
        vue.sautLigne();

        int morceauId = vue.lireEntier("ID du morceau a ajouter");
        Morceau morceau = resultContextService.trouverDansDerniersMorceaux(morceauId);
        enregistrerAjoutPlaylist(playlist, morceau);
    }

    private void ajouterDepuisSource(Playlist playlist) {
        int choix = vue.afficherMenuSelectionSource();
        switch (choix) {
            case 1 -> ajouterDepuisDemo(playlist);
            case 2 -> ajouterDepuisJamendo(playlist);
            case 3 -> ajouterDepuisApple(playlist);
            case 0 -> { }
            default -> vue.afficherErreur("Choix invalide.");
        }
    }

    private void ajouterDepuisDemo(Playlist playlist) {
        int choix = vue.afficherMenuAjoutPlaylistDemo();
        switch (choix) {
            case 1 -> {
                List<Morceau> morceaux = catalogueService.getTousMorceaux();
                if (morceaux.isEmpty()) {
                    vue.afficherErreur("Aucun morceau demo disponible.");
                    return;
                }
                afficherListeEtMemoriser(morceaux);
                int morceauId = vue.lireEntier("ID du morceau demo a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 2 -> {
                String mc = vue.lireTexte("Rechercher un morceau demo");
                List<Morceau> morceaux = rechercheService.rechercherMorceaux(mc);
                if (morceaux.isEmpty()) {
                    vue.afficherErreur("Aucun morceau demo trouve.");
                    return;
                }
                afficherListeEtMemoriser(morceaux);
                int morceauId = vue.lireEntier("ID du morceau demo a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 0 -> { }
            default -> vue.afficherErreur("Choix invalide.");
        }
    }

    private void ajouterDepuisJamendo(Playlist playlist) {
        int choix = vue.afficherMenuAjoutPlaylistProvider("Jamendo");
        switch (choix) {
            case 1 -> {
                List<Morceau> morceaux = jamendoService.getTitresTendance(PROVIDER_LIMIT);
                if (morceaux.isEmpty()) {
                    vue.afficherErreur("Tendances Jamendo indisponibles.");
                    return;
                }
                afficherListeEtMemoriser(morceaux);
                int morceauId = vue.lireEntier("ID du morceau Jamendo a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 2 -> {
                String mc = vue.lireTexte("Recherche globale Jamendo");
                ProviderSearchResults resultats = jamendoService.rechercherGlobal(mc);
                afficherResultatsProvider("Jamendo", resultats);
                if (resultats.getMorceaux().isEmpty()) {
                    vue.afficherErreur("Aucun morceau Jamendo ajoutable dans ces resultats.");
                    return;
                }
                int morceauId = vue.lireEntier("ID du morceau Jamendo a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 0 -> { }
            default -> vue.afficherErreur("Choix invalide.");
        }
    }

    private void ajouterDepuisApple(Playlist playlist) {
        int choix = vue.afficherMenuAjoutPlaylistProvider("Apple");
        switch (choix) {
            case 1 -> {
                List<Morceau> morceaux = appleItunesService.getTitresTendance(PROVIDER_LIMIT);
                if (morceaux.isEmpty()) {
                    vue.afficherErreur("Tendances Apple indisponibles.");
                    return;
                }
                afficherListeEtMemoriser(morceaux);
                int morceauId = vue.lireEntier("ID du morceau Apple a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 2 -> {
                String mc = vue.lireTexte("Recherche globale Apple");
                ProviderSearchResults resultats = appleItunesService.rechercherGlobal(mc);
                afficherResultatsProvider("Apple", resultats);
                if (resultats.getMorceaux().isEmpty()) {
                    vue.afficherErreur("Aucun morceau Apple ajoutable dans ces resultats.");
                    return;
                }
                int morceauId = vue.lireEntier("ID du morceau Apple a ajouter");
                enregistrerAjoutPlaylist(playlist, mediaResolverService.resoudreMorceau(morceauId));
            }
            case 0 -> { }
            default -> vue.afficherErreur("Choix invalide.");
        }
    }

    private void enregistrerAjoutPlaylist(Playlist playlist, Morceau morceau) {
        if (morceau == null) {
            vue.afficherErreur("Morceau introuvable.");
            return;
        }

        if (playlistService.ajouterMorceauAPlaylist(playlist.getId(), morceau)) {
            vue.afficherSucces("Morceau ajoute a la playlist.");
        } else {
            vue.afficherErreur("Impossible d'ajouter le morceau (doublon ou morceau invalide).");
        }
    }

    private void retirerMorceauDePlaylist(Playlist playlist) {
        vue.afficherDetailPlaylist(playlist);
        vue.sautLigne();

        int morceauId = vue.lireEntier("ID du morceau a retirer");
        if (playlistService.retirerMorceauDePlaylist(playlist.getId(), morceauId)) {
            vue.afficherSucces("Morceau retire de la playlist.");
        } else {
            vue.afficherErreur("Impossible de retirer le morceau.");
        }
    }

    private void renommerPlaylist(Playlist playlist) {
        String nouveauNom = vue.lireTexte("Nouveau nom");
        if (!nouveauNom.isEmpty()) {
            playlistService.renommerPlaylist(playlist.getId(), nouveauNom);
            vue.afficherSucces("Playlist renommee : " + nouveauNom);
        }
    }

    private void changerVisibilite(Playlist playlist) {
        boolean publique = vue.confirmer("Rendre la playlist publique ?");
        playlistService.changerVisibilite(playlist.getId(), publique);
        vue.afficherSucces("Visibilite changee : " + (publique ? "publique" : "privee"));
    }

    private void supprimerPlaylist(Utilisateur utilisateur) {
        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateur.getId());
        vue.afficherListePlaylists(playlists);
        int id = vue.lireEntier("ID de la playlist a supprimer");
        if (playlistService.supprimerPlaylist(id, utilisateur.getId())) {
            vue.afficherSucces("Playlist supprimee.");
        } else {
            vue.afficherErreur("Impossible de supprimer la playlist.");
        }
    }

    private void afficherListeEtMemoriser(List<Morceau> morceaux) {
        resultContextService.memoriserMorceaux(morceaux);
        vue.afficherListeMorceaux(morceaux);
        vue.sautLigne();
    }

    private void afficherResultatsProvider(String provider, ProviderSearchResults resultats) {
        resultContextService.memoriserMorceaux(resultats.getMorceaux());
        vue.afficherTitre("Resultats " + provider);

        if (resultats.estVide()) {
            vue.afficherMessage("Aucun resultat " + provider + ".");
            return;
        }

        if (!resultats.getArtistes().isEmpty()) {
            vue.afficherMessage("--- Artistes " + provider + " (" + resultats.getArtistes().size() + ") ---");
            vue.afficherListeArtistes(resultats.getArtistes());
            vue.sautLigne();
        }
        if (!resultats.getAlbums().isEmpty()) {
            vue.afficherMessage("--- Albums " + provider + " (" + resultats.getAlbums().size() + ") ---");
            vue.afficherListeAlbums(resultats.getAlbums());
            vue.sautLigne();
        }
        if (!resultats.getMorceaux().isEmpty()) {
            vue.afficherMessage("--- Morceaux " + provider + " (" + resultats.getMorceaux().size() + ") ---");
            vue.afficherListeMorceaux(resultats.getMorceaux());
            vue.sautLigne();
        }
    }
}
