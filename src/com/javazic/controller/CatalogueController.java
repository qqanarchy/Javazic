package com.javazic.controller;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Genre;
import com.javazic.model.Groupe;
import com.javazic.model.Morceau;
import com.javazic.service.AppleItunesService;
import com.javazic.service.CatalogueService;
import com.javazic.service.JamendoService;
import com.javazic.service.RechercheService;
import com.javazic.service.ResultContextService;
import com.javazic.view.ConsoleView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

public class CatalogueController {

    private static final int PROVIDER_LIMIT = 10;

    private final CatalogueService catalogueService;
    private final RechercheService rechercheService;
    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final ResultContextService resultContextService;
    private final ConsoleView vue;

    public CatalogueController(CatalogueService catalogueService,
                               RechercheService rechercheService,
                               JamendoService jamendoService,
                               AppleItunesService appleItunesService,
                               ResultContextService resultContextService,
                               ConsoleView vue) {
        this.catalogueService = catalogueService;
        this.rechercheService = rechercheService;
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.resultContextService = resultContextService;
        this.vue = vue;
    }

    public void parcourirCatalogue() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuCatalogue();
            switch (choix) {
                case 1 -> parcourirCatalogueDemo();
                case 2 -> afficherTendancesJamendo();
                case 3 -> afficherTendancesApple();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    public void rechercher() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuRecherche();
            switch (choix) {
                case 1 -> rechercherDemo();
                case 2 -> rechercherProviderApple();
                case 3 -> rechercherProviderJamendo();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    public void administrerCatalogue() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuAdminCatalogue();
            switch (choix) {
                case 1 -> ajouterArtiste();
                case 2 -> ajouterAlbum();
                case 3 -> ajouterMorceau();
                case 4 -> supprimerMorceau();
                case 5 -> supprimerAlbum();
                case 6 -> supprimerArtiste();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void parcourirCatalogueDemo() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuCatalogueDemo();
            switch (choix) {
                case 1 -> voirArtistes();
                case 2 -> voirAlbums();
                case 3 -> voirMorceaux();
                case 4 -> voirGroupes();
                case 5 -> filtrerParGenre();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void rechercherDemo() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuRechercheDemo();
            switch (choix) {
                case 1 -> {
                    String mc = vue.lireTexte("Rechercher un artiste");
                    vue.afficherListeArtistes(rechercheService.rechercherArtistes(mc));
                    vue.attendreTouche();
                }
                case 2 -> {
                    String mc = vue.lireTexte("Rechercher un album");
                    vue.afficherListeAlbums(rechercheService.rechercherAlbums(mc));
                    vue.attendreTouche();
                }
                case 3 -> {
                    String mc = vue.lireTexte("Rechercher un morceau");
                    afficherEtMemoriserMorceaux(rechercheService.rechercherMorceaux(mc));
                    vue.attendreTouche();
                }
                case 4 -> rechercheGlobaleDemo();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void rechercheGlobaleDemo() {
        String mc = vue.lireTexte("Recherche globale demo");
        memoriserMorceaux(new ArrayList<>());

        List<Artiste> artistes = rechercheService.rechercherArtistes(mc);
        List<Album> albums = rechercheService.rechercherAlbums(mc);
        List<Morceau> morceaux = rechercheService.rechercherMorceaux(mc);

        vue.afficherTitre("Resultats demo pour \"" + mc + "\"");

        if (!artistes.isEmpty()) {
            vue.afficherMessage("--- Artistes Demo (" + artistes.size() + ") ---");
            vue.afficherListeArtistes(artistes);
            vue.sautLigne();
        }
        if (!albums.isEmpty()) {
            vue.afficherMessage("--- Albums Demo (" + albums.size() + ") ---");
            vue.afficherListeAlbums(albums);
            vue.sautLigne();
        }
        if (!morceaux.isEmpty()) {
            vue.afficherMessage("--- Morceaux Demo (" + morceaux.size() + ") ---");
            afficherEtMemoriserMorceaux(morceaux);
            vue.sautLigne();
        }
        if (artistes.isEmpty() && albums.isEmpty() && morceaux.isEmpty()) {
            vue.afficherMessage("Aucun resultat demo.");
        }
        vue.attendreTouche();
    }

    private void afficherTendancesJamendo() {
        vue.afficherTitre("Tendances Jamendo");
        List<Morceau> morceaux = jamendoService.getTitresTendance(PROVIDER_LIMIT);
        if (morceaux.isEmpty()) {
            memoriserMorceaux(new ArrayList<>());
            vue.afficherMessage("Tendances indisponibles pour Jamendo.");
        } else {
            afficherEtMemoriserMorceaux(morceaux);
        }
        vue.attendreTouche();
    }

    private void afficherTendancesApple() {
        vue.afficherTitre("Tendances Apple");
        List<Morceau> morceaux = appleItunesService.getTitresTendance(PROVIDER_LIMIT);
        if (morceaux.isEmpty()) {
            memoriserMorceaux(new ArrayList<>());
            vue.afficherMessage("Tendances indisponibles pour Apple.");
        } else {
            afficherEtMemoriserMorceaux(morceaux);
        }
        vue.attendreTouche();
    }

    private void rechercherProviderApple() {
        rechercherProviderAvecMenu("Apple",
                appleItunesService::rechercherArtistes,
                appleItunesService::rechercherAlbums,
                appleItunesService::rechercherMorceaux,
                appleItunesService::getMorceauxArtiste,
                appleItunesService::getMorceauxAlbum);
    }

    private void rechercherProviderJamendo() {
        rechercherProviderAvecMenu("Jamendo",
                jamendoService::rechercherArtistes,
                jamendoService::rechercherAlbums,
                jamendoService::rechercherMorceaux,
                jamendoService::getMorceauxArtiste,
                jamendoService::getMorceauxAlbum);
    }

    private void rechercherProviderAvecMenu(String provider,
                                            Function<String, List<Artiste>> rechercheArtistes,
                                            Function<String, List<Album>> rechercheAlbums,
                                            Function<String, List<Morceau>> rechercheMorceaux,
                                            IntFunction<List<Morceau>> morceauxParArtiste,
                                            IntFunction<List<Morceau>> morceauxParAlbum) {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuRechercheProvider(provider);
            switch (choix) {
                case 1 -> rechercherArtisteProvider(provider, rechercheArtistes, morceauxParArtiste);
                case 2 -> rechercherAlbumProvider(provider, rechercheAlbums, morceauxParAlbum);
                case 3 -> rechercherMorceauProvider(provider, rechercheMorceaux);
                case 4 -> rechercheGlobaleProvider(provider, rechercheArtistes, rechercheAlbums,
                        rechercheMorceaux, morceauxParArtiste, morceauxParAlbum);
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void rechercherArtisteProvider(String provider,
                                           Function<String, List<Artiste>> rechercheArtistes,
                                           IntFunction<List<Morceau>> morceauxParArtiste) {
        String motCle = vue.lireTexte("Rechercher un artiste " + provider);
        List<Artiste> artistes = rechercheArtistes.apply(motCle);
        vue.afficherTitre("Artistes " + provider);
        vue.afficherListeArtistes(artistes);
        vue.sautLigne();

        if (artistes.isEmpty()) {
            memoriserMorceaux(new ArrayList<>());
            vue.attendreTouche();
            return;
        }

        int artisteId = vue.lireEntier("ID de l'artiste pour voir ses morceaux (0 = retour)");
        if (artisteId != 0) {
            afficherListeMorceauxProvider("Morceaux " + provider, morceauxParArtiste.apply(artisteId));
        }
        vue.attendreTouche();
    }

    private void rechercherAlbumProvider(String provider,
                                         Function<String, List<Album>> rechercheAlbums,
                                         IntFunction<List<Morceau>> morceauxParAlbum) {
        String motCle = vue.lireTexte("Rechercher un album " + provider);
        List<Album> albums = rechercheAlbums.apply(motCle);
        vue.afficherTitre("Albums " + provider);
        vue.afficherListeAlbums(albums);
        vue.sautLigne();

        if (albums.isEmpty()) {
            memoriserMorceaux(new ArrayList<>());
            vue.attendreTouche();
            return;
        }

        int albumId = vue.lireEntier("ID de l'album pour voir ses morceaux (0 = retour)");
        if (albumId != 0) {
            afficherListeMorceauxProvider("Morceaux " + provider, morceauxParAlbum.apply(albumId));
        }
        vue.attendreTouche();
    }

    private void rechercherMorceauProvider(String provider,
                                           Function<String, List<Morceau>> rechercheMorceaux) {
        String motCle = vue.lireTexte("Rechercher un morceau " + provider);
        afficherListeMorceauxProvider("Morceaux " + provider, limiterMorceaux(rechercheMorceaux.apply(motCle)));
        vue.attendreTouche();
    }

    private void rechercheGlobaleProvider(String provider,
                                          Function<String, List<Artiste>> rechercheArtistes,
                                          Function<String, List<Album>> rechercheAlbums,
                                          Function<String, List<Morceau>> rechercheMorceaux,
                                          IntFunction<List<Morceau>> morceauxParArtiste,
                                          IntFunction<List<Morceau>> morceauxParAlbum) {
        String motCle = vue.lireTexte("Recherche globale " + provider);
        List<Morceau> morceaux = new ArrayList<>();

        ajouterMorceauxUniques(morceaux, rechercheMorceaux.apply(motCle));
        for (Album album : limiterAlbums(rechercheAlbums.apply(motCle))) {
            ajouterMorceauxUniques(morceaux, morceauxParAlbum.apply(album.getId()));
        }
        for (Artiste artiste : limiterArtistes(rechercheArtistes.apply(motCle))) {
            ajouterMorceauxUniques(morceaux, morceauxParArtiste.apply(artiste.getId()));
        }

        afficherListeMorceauxProvider("Recherche globale " + provider, morceaux);
        vue.attendreTouche();
    }

    private void afficherListeMorceauxProvider(String titre, List<Morceau> morceaux) {
        vue.afficherTitre(titre);
        if (morceaux.isEmpty()) {
            memoriserMorceaux(new ArrayList<>());
            vue.afficherMessage("Aucun morceau trouve.");
            return;
        }
        afficherEtMemoriserMorceaux(limiterMorceaux(morceaux));
    }

    private List<Artiste> limiterArtistes(List<Artiste> artistes) {
        return limiterListe(artistes, 3);
    }

    private List<Album> limiterAlbums(List<Album> albums) {
        return limiterListe(albums, 3);
    }

    private List<Morceau> limiterMorceaux(List<Morceau> morceaux) {
        return limiterListe(morceaux, PROVIDER_LIMIT);
    }

    private <T> List<T> limiterListe(List<T> elements, int limite) {
        if (elements == null || elements.isEmpty()) {
            return new ArrayList<>();
        }
        int borne = Math.min(limite, elements.size());
        return new ArrayList<>(elements.subList(0, borne));
    }

    private void ajouterMorceauxUniques(List<Morceau> cible, List<Morceau> candidats) {
        if (candidats == null || candidats.isEmpty()) {
            return;
        }

        for (Morceau morceau : candidats) {
            if (cible.size() >= PROVIDER_LIMIT) {
                return;
            }
            if (!contientMorceauEquivalent(cible, morceau)) {
                cible.add(morceau);
            }
        }
    }

    private boolean contientMorceauEquivalent(List<Morceau> morceaux, Morceau candidat) {
        for (Morceau morceau : morceaux) {
            if (morceau.getId() == candidat.getId()) {
                return true;
            }
            if (morceau.estDistant() && candidat.estDistant()
                    && morceau.getSource() == candidat.getSource()
                    && !morceau.getSourceId().isEmpty()
                    && morceau.getSourceId().equals(candidat.getSourceId())) {
                return true;
            }
        }
        return false;
    }

    private void voirArtistes() {
        List<Artiste> artistes = catalogueService.getTousArtistes();
        vue.afficherListeArtistes(artistes);
        vue.sautLigne();

        int id = vue.lireEntier("Entrez l'ID d'un artiste pour voir les details (0 = retour)");
        if (id > 0) {
            Artiste artiste = catalogueService.getArtiste(id);
            if (artiste != null) {
                List<Album> albums = catalogueService.getAlbumsParArtiste(id);
                List<Morceau> morceaux = catalogueService.getMorceauxParArtiste(id);
                memoriserMorceaux(morceaux);
                vue.afficherDetailArtiste(artiste, albums, morceaux);
                vue.sautLigne();

                int albumId = vue.lireEntier("Entrez l'ID d'un album pour le detail (0 = retour)");
                if (albumId > 0) {
                    Album album = catalogueService.getAlbum(albumId);
                    if (album != null) {
                        memoriserMorceaux(album.getMorceaux());
                        vue.afficherDetailAlbum(album);
                    } else {
                        vue.afficherErreur("Album introuvable.");
                    }
                }
            } else {
                vue.afficherErreur("Artiste introuvable.");
            }
        }
    }

    private void voirAlbums() {
        List<Album> albums = catalogueService.getTousAlbums();
        vue.afficherListeAlbums(albums);
        vue.sautLigne();

        int id = vue.lireEntier("Entrez l'ID d'un album pour voir les details (0 = retour)");
        if (id > 0) {
            Album album = catalogueService.getAlbum(id);
            if (album != null) {
                memoriserMorceaux(album.getMorceaux());
                vue.afficherDetailAlbum(album);
            } else {
                vue.afficherErreur("Album introuvable.");
            }
        }
    }

    private void voirMorceaux() {
        afficherEtMemoriserMorceaux(catalogueService.getTousMorceaux());
        vue.attendreTouche();
    }

    private void voirGroupes() {
        List<Groupe> groupes = catalogueService.getTousGroupes();
        vue.afficherListeGroupes(groupes);
        vue.attendreTouche();
    }

    private void filtrerParGenre() {
        Genre genre = vue.choisirGenre();
        if (genre != null) {
            List<Album> albums = rechercheService.filtrerParGenre(genre);
            vue.afficherMessage("Albums du genre " + genre.getLibelle() + " :");
            vue.afficherListeAlbums(albums);
        } else {
            vue.afficherErreur("Genre invalide.");
        }
        vue.attendreTouche();
    }

    private void ajouterArtiste() {
        vue.afficherTitre("Ajouter un artiste");
        String nom = vue.lireTexte("Nom");
        String bio = vue.lireTexte("Biographie");
        String pays = vue.lireTexte("Pays d'origine");
        int annee = vue.lireEntier("Annee de debut");

        if (nom.isEmpty()) {
            vue.afficherErreur("Le nom est obligatoire.");
            return;
        }
        LocalDate dateDebut = LocalDate.of(annee > 0 ? annee : 2000, 1, 1);
        Artiste artiste = catalogueService.creerArtiste(nom, bio, pays, dateDebut);
        vue.afficherSucces("Artiste cree : " + artiste);
    }

    private void ajouterAlbum() {
        vue.afficherTitre("Ajouter un album");
        List<Artiste> artistes = catalogueService.getTousArtistes();
        vue.afficherListeArtistes(artistes);
        vue.sautLigne();

        int artisteId = vue.lireEntier("ID de l'artiste");
        Artiste artiste = catalogueService.getArtiste(artisteId);
        if (artiste == null) {
            vue.afficherErreur("Artiste introuvable.");
            return;
        }

        String titre = vue.lireTexte("Titre de l'album");
        int annee = vue.lireEntier("Annee de sortie");
        Genre genre = vue.choisirGenre();

        if (titre.isEmpty() || genre == null) {
            vue.afficherErreur("Titre et genre sont obligatoires.");
            return;
        }
        LocalDate dateSortie = LocalDate.of(annee > 0 ? annee : 2000, 1, 1);
        Album album = catalogueService.creerAlbum(titre, dateSortie, genre, artiste);
        vue.afficherSucces("Album cree : " + album);
    }

    private void ajouterMorceau() {
        vue.afficherTitre("Ajouter un morceau");
        List<Album> albums = catalogueService.getTousAlbums();
        vue.afficherListeAlbums(albums);
        vue.sautLigne();

        int albumId = vue.lireEntier("ID de l'album (0 = pas d'album)");
        Album album = albumId > 0 ? catalogueService.getAlbum(albumId) : null;

        Artiste artiste;
        if (album != null) {
            artiste = album.getArtiste();
            vue.afficherMessage("Artiste : " + artiste.getNom());
        } else {
            List<Artiste> artistes = catalogueService.getTousArtistes();
            vue.afficherListeArtistes(artistes);
            int artisteId = vue.lireEntier("ID de l'artiste");
            artiste = catalogueService.getArtiste(artisteId);
            if (artiste == null) {
                vue.afficherErreur("Artiste introuvable.");
                return;
            }
        }

        String titre = vue.lireTexte("Titre du morceau");
        int duree = vue.lireEntier("Duree (en secondes)");

        if (titre.isEmpty() || duree <= 0) {
            vue.afficherErreur("Titre et duree sont obligatoires.");
            return;
        }
        Morceau morceau = catalogueService.creerMorceau(titre, duree, artiste, album);
        vue.afficherSucces("Morceau cree : " + morceau);
    }

    private void supprimerMorceau() {
        List<Morceau> morceaux = catalogueService.getTousMorceaux();
        afficherEtMemoriserMorceaux(morceaux);
        int id = vue.lireEntier("ID du morceau a supprimer");
        if (catalogueService.getMorceau(id) != null) {
            if (vue.confirmer("Confirmer la suppression ?")) {
                catalogueService.supprimerMorceau(id);
                vue.afficherSucces("Morceau supprime.");
            }
        } else {
            vue.afficherErreur("Morceau introuvable.");
        }
    }

    private void supprimerAlbum() {
        List<Album> albums = catalogueService.getTousAlbums();
        vue.afficherListeAlbums(albums);
        int id = vue.lireEntier("ID de l'album a supprimer");
        if (catalogueService.getAlbum(id) != null) {
            if (vue.confirmer("Confirmer la suppression ?")) {
                catalogueService.supprimerAlbum(id);
                vue.afficherSucces("Album supprime.");
            }
        } else {
            vue.afficherErreur("Album introuvable.");
        }
    }

    private void supprimerArtiste() {
        List<Artiste> artistes = catalogueService.getTousArtistes();
        vue.afficherListeArtistes(artistes);
        int id = vue.lireEntier("ID de l'artiste a supprimer");
        if (catalogueService.getArtiste(id) != null) {
            if (vue.confirmer("Confirmer la suppression ?")) {
                catalogueService.supprimerArtiste(id);
                vue.afficherSucces("Artiste supprime.");
            }
        } else {
            vue.afficherErreur("Artiste introuvable.");
        }
    }

    private void afficherEtMemoriserMorceaux(List<Morceau> morceaux) {
        memoriserMorceaux(morceaux);
        vue.afficherListeMorceaux(morceaux);
    }

    private void memoriserMorceaux(List<Morceau> morceaux) {
        resultContextService.memoriserMorceaux(morceaux);
    }
}
