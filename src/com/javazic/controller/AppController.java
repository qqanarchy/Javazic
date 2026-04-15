package com.javazic.controller;

import com.javazic.model.Avis;
import com.javazic.model.Morceau;
import com.javazic.model.TypeUtilisateur;
import com.javazic.model.Utilisateur;
import com.javazic.service.AvisService;
import com.javazic.service.CatalogueService;
import com.javazic.service.LecteurAudio;
import com.javazic.service.MediaResolverService;
import com.javazic.service.ResultContextService;
import com.javazic.service.StatistiquesService;
import com.javazic.service.UtilisateurService;
import com.javazic.view.ConsoleView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controleur principal de l'application.
 */
public class AppController {

    private final ConsoleView vue;
    private final UtilisateurService utilisateurService;
    private final CatalogueController catalogueController;
    private final PlaylistController playlistController;
    private final StatistiquesService statistiquesService;
    private final AvisService avisService;
    private final CatalogueService catalogueService;
    private final MediaResolverService mediaResolverService;
    private final ResultContextService resultContextService;
    private final LecteurAudio lecteurAudio;

    private Utilisateur utilisateurConnecte;

    public AppController(ConsoleView vue,
                         UtilisateurService utilisateurService,
                         CatalogueController catalogueController,
                         PlaylistController playlistController,
                         StatistiquesService statistiquesService,
                         AvisService avisService,
                         CatalogueService catalogueService,
                         MediaResolverService mediaResolverService,
                         ResultContextService resultContextService,
                         LecteurAudio lecteurAudio) {
        this.vue = vue;
        this.utilisateurService = utilisateurService;
        this.catalogueController = catalogueController;
        this.playlistController = playlistController;
        this.statistiquesService = statistiquesService;
        this.avisService = avisService;
        this.catalogueService = catalogueService;
        this.mediaResolverService = mediaResolverService;
        this.resultContextService = resultContextService;
        this.lecteurAudio = lecteurAudio;
    }

    public void demarrer() {
        vue.afficherBanniere();

        boolean quitter = false;
        while (!quitter) {
            int choix = vue.afficherMenuAuthentification();
            switch (choix) {
                case 1 -> {
                    if (seConnecter(TypeUtilisateur.ADMIN)) {
                        menuPrincipal();
                    }
                }
                case 2 -> {
                    if (seConnecter(null)) {
                        menuPrincipal();
                    }
                }
                case 3 -> creerCompte();
                case 4 -> menuVisiteur();
                case 0 -> {
                    vue.afficherMessage("Au revoir !");
                    quitter = true;
                }
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private boolean seConnecter(TypeUtilisateur typeAttendu) {
        vue.afficherTitre("Connexion");
        String email = vue.lireTexte("Email");
        String mdp = vue.lireTexte("Mot de passe");

        utilisateurConnecte = utilisateurService.connexion(email, mdp);
        if (utilisateurConnecte != null) {
            if (utilisateurConnecte.isSuspendu()) {
                vue.afficherErreur("Ce compte est suspendu. Contactez un administrateur.");
                utilisateurConnecte = null;
                return false;
            }
            if (typeAttendu == TypeUtilisateur.ADMIN && !utilisateurConnecte.estAdmin()) {
                vue.afficherErreur("Ce compte n'est pas un compte administrateur.");
                utilisateurConnecte = null;
                return false;
            }
            vue.afficherSucces("Bienvenue, " + utilisateurConnecte.getNom() + " !");
            return true;
        }
        vue.afficherErreur("Email ou mot de passe incorrect.");
        return false;
    }

    private void creerCompte() {
        vue.afficherTitre("Inscription");
        String nom = vue.lireTexte("Nom");
        String email = vue.lireTexte("Email");
        String mdp = vue.lireTexte("Mot de passe");

        if (nom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            vue.afficherErreur("Tous les champs sont obligatoires.");
            return;
        }

        Utilisateur u = utilisateurService.inscrire(nom, email, mdp, TypeUtilisateur.ABONNE);
        if (u != null) {
            vue.afficherSucces("Compte cree ! Vous pouvez maintenant vous connecter.");
        } else {
            vue.afficherErreur("Cet email est deja utilise.");
        }
    }

    private void menuVisiteur() {
        int ecoutesRestantes = 5;
        boolean continuer = true;
        while (continuer) {
            vue.afficherMessage("(Ecoutes restantes : " + ecoutesRestantes + "/5)");
            int choix = vue.afficherMenuVisiteur();
            switch (choix) {
                case 1 -> catalogueController.parcourirCatalogue();
                case 2 -> catalogueController.rechercher();
                case 3 -> playlistController.voirPlaylistsPubliques();
                case 4 -> afficherStatistiquesEvoluees();
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }

            if (continuer && (choix == 1 || choix == 2)) {
                int idMorceau = vue.lireEntier("Ecouter un morceau ? (ID du morceau, 0 = non)");
                if (idMorceau != 0) {
                    if (ecoutesRestantes <= 0) {
                        vue.afficherLimiteEcoutes();
                    } else {
                        Morceau morceau = trouverMorceau(idMorceau);
                        if (morceau != null) {
                            lancerLecture(morceau);
                            ecoutesRestantes--;
                        } else {
                            vue.afficherErreur("Morceau introuvable.");
                        }
                    }
                }
            }
        }
        vue.afficherMessage("Fin du mode visiteur.");
    }

    private void menuPrincipal() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuPrincipal(utilisateurConnecte);
            switch (choix) {
                case 1 -> {
                    catalogueController.parcourirCatalogue();
                    proposerEcoute();
                }
                case 2 -> {
                    catalogueController.rechercher();
                    proposerEcoute();
                }
                case 3 -> playlistController.gererMesPlaylists(utilisateurConnecte);
                case 4 -> playlistController.voirPlaylistsPubliques();
                case 5 -> voirHistorique();
                case 6 -> gererAvis();
                case 7 -> gererProfil();
                case 8 -> {
                    if (utilisateurConnecte.estAdmin()) {
                        catalogueController.administrerCatalogue();
                    } else {
                        vue.afficherErreur("Choix invalide.");
                    }
                }
                case 9 -> {
                    if (utilisateurConnecte.estAdmin()) {
                        gererUtilisateurs();
                    } else {
                        vue.afficherErreur("Choix invalide.");
                    }
                }
                case 10 -> {
                    if (utilisateurConnecte.estAdmin()) {
                        afficherStatistiques();
                    } else {
                        vue.afficherErreur("Choix invalide.");
                    }
                }
                case 0 -> {
                    vue.afficherMessage("Deconnexion...");
                    utilisateurConnecte = null;
                    continuer = false;
                }
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void proposerEcoute() {
        int idMorceau = vue.lireEntier("Ecouter un morceau ? (ID du morceau, 0 = non)");
        if (idMorceau != 0) {
            Morceau morceau = trouverMorceau(idMorceau);
            if (morceau != null) {
                lancerLecture(morceau);
            } else {
                vue.afficherErreur("Morceau introuvable.");
            }
        }
    }

    private Morceau trouverMorceau(int id) {
        return mediaResolverService.resoudreMorceau(id);
    }

    private void lancerLecture(Morceau morceauSelectionne) {
        List<Morceau> fileLecture = construireFileLecture(morceauSelectionne.getId(), morceauSelectionne);
        int indexDepart = trouverIndex(fileLecture, morceauSelectionne.getId());
        if (indexDepart < 0) {
            indexDepart = 0;
        }

        lecteurAudio.play(fileLecture, indexDepart);
        vue.afficherCommandesLecture();

        Morceau dernierMorceauObserve = null;
        while (lecteurAudio.isPlaying()) {
            Morceau courant = lecteurAudio.getCurrentTrack();
            if (courant != null && courant != dernierMorceauObserve) {
                System.out.println();
                vue.afficherDebutLecture(courant);
                enregistrerEcoute(courant);
                dernierMorceauObserve = courant;
            }

            String commande = vue.lireCommandeDisponible();
            if (commande != null && !commande.isEmpty()) {
                traiterCommandeLecture(commande);
            }

            dormir(100);
        }

        vue.afficherSucces("Lecture terminee.");
    }

    private List<Morceau> construireFileLecture(int idSelectionne, Morceau fallback) {
        List<Morceau> derniereListe = resultContextService.getDerniersMorceaux();
        if (!derniereListe.isEmpty() && trouverIndex(derniereListe, idSelectionne) >= 0) {
            return derniereListe;
        }
        List<Morceau> unique = new ArrayList<>();
        unique.add(fallback);
        return unique;
    }

    private int trouverIndex(List<Morceau> morceaux, int id) {
        for (int i = 0; i < morceaux.size(); i++) {
            if (morceaux.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private void enregistrerEcoute(Morceau morceau) {
        morceau.incrementerEcoute();
        if (utilisateurConnecte != null) {
            utilisateurConnecte.ajouterEcoute(morceau);
        }
    }

    private void traiterCommandeLecture(String commande) {
        switch (commande) {
            case "s" -> {
                if (lecteurAudio.togglePause()) {
                    System.out.println();
                    vue.afficherMessage(lecteurAudio.isPaused() ? "Lecture en pause." : "Lecture reprise.");
                }
            }
            case "d" -> {
                if (!lecteurAudio.next()) {
                    System.out.println();
                    vue.afficherMessage("Vous etes deja sur le dernier morceau de la liste.");
                }
            }
            case "q" -> {
                if (!lecteurAudio.previous()) {
                    System.out.println();
                    vue.afficherMessage("Vous etes deja sur le premier morceau de la liste.");
                }
            }
            case "0" -> lecteurAudio.stop();
            default -> {
                System.out.println();
                vue.afficherErreur("Commande inconnue. Utilisez s, d, q ou 0.");
            }
        }
    }

    private void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void voirHistorique() {
        vue.afficherHistorique(utilisateurConnecte.getHistoriqueEcoutes());
        vue.attendreTouche();
    }

    private void gererAvis() {
        vue.afficherTitre("Noter un morceau");
        int morceauId = vue.lireEntier("ID du morceau a noter");
        Morceau morceau = trouverMorceau(morceauId);
        if (morceau == null) {
            vue.afficherErreur("Morceau introuvable.");
            return;
        }

        vue.afficherMessage("Morceau : " + morceau.getTitre());

        List<Avis> avisList = avisService.getAvisParMorceau(morceauId);
        int likes = avisService.getNombreLikes(morceauId);
        int dislikes = avisService.getNombreDislikes(morceauId);
        vue.afficherAvisMorceau(avisList, likes, dislikes);
        vue.sautLigne();

        Avis existant = avisService.getAvisUtilisateur(utilisateurConnecte.getId(), morceauId);
        if (existant != null) {
            vue.afficherMessage("Vous avez deja donne un avis : "
                    + (existant.isPositif() ? "Like" : "Dislike") + ".");
            vue.afficherMessage("1. Like");
            vue.afficherMessage("2. Dislike");
            vue.afficherMessage("3. Supprimer mon avis");
            vue.afficherMessage("0. Annuler");
            int choix = vue.lireChoix();

            if (choix == 1 || choix == 2) {
                boolean positif = choix == 1;
                String commentaire = "";
                if (existant.isPositif() != positif) {
                    commentaire = vue.lireTexte("Commentaire (vide pour garder l'existant)");
                }
                AvisService.ResultatToggleAvis resultat =
                        avisService.basculerAvis(utilisateurConnecte, morceau, positif, "", commentaire);
                switch (resultat) {
                    case SUPPRIME -> vue.afficherSucces("Avis retire.");
                    case MODIFIE -> vue.afficherSucces("Avis modifie.");
                    default -> vue.afficherErreur("Impossible de mettre a jour l'avis.");
                }
            } else if (choix == 3) {
                if (avisService.supprimerAvis(existant.getId(), utilisateurConnecte.getId())) {
                    vue.afficherSucces("Avis supprime.");
                } else {
                    vue.afficherErreur("Impossible de supprimer l'avis.");
                }
            }
        } else {
            vue.afficherMessage("1. Like");
            vue.afficherMessage("2. Dislike");
            vue.afficherMessage("3. Annuler");
            int choix = vue.lireChoix();
            if (choix == 3 || choix == 0) {
                return;
            }
            if (choix != 1 && choix != 2) {
                vue.afficherErreur("Choix invalide.");
                return;
            }
            boolean positif = choix == 1;
            String commentaire = vue.lireTexte("Commentaire (optionnel)");
            AvisService.ResultatToggleAvis resultat =
                    avisService.basculerAvis(utilisateurConnecte, morceau, positif, commentaire, commentaire);
            if (resultat == AvisService.ResultatToggleAvis.AJOUTE) {
                vue.afficherSucces("Avis enregistre !");
            } else if (resultat == AvisService.ResultatToggleAvis.SUPPRIME) {
                vue.afficherSucces("Avis retire.");
            } else if (resultat == AvisService.ResultatToggleAvis.MODIFIE) {
                vue.afficherSucces("Avis modifie.");
            } else {
                vue.afficherErreur("Impossible d'enregistrer l'avis.");
            }
        }
    }

    private void afficherStatistiques() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuStatistiques();
            switch (choix) {
                case 1 -> {
                    vue.afficherStatistiquesGenerales(
                            statistiquesService.getNombreUtilisateurs(),
                            statistiquesService.getNombreMorceaux(),
                            statistiquesService.getNombreAlbums(),
                            statistiquesService.getNombreArtistes(),
                            statistiquesService.getNombreGroupes(),
                            statistiquesService.getNombreTotalEcoutes()
                    );
                    vue.attendreTouche();
                }
                case 2 -> {
                    vue.afficherTopMorceaux(statistiquesService.getTopMorceaux(10));
                    vue.attendreTouche();
                }
                case 3 -> {
                    vue.afficherTopArtistes(statistiquesService.getTopArtistes(10));
                    vue.attendreTouche();
                }
                case 4 -> {
                    vue.afficherTopAlbums(statistiquesService.getTopAlbums(10));
                    vue.attendreTouche();
                }
                case 5 -> {
                    List<Map.Entry<Morceau, Integer>> topLikes = new ArrayList<>();
                    for (Morceau morceau : statistiquesService.getMorceauxLesPlusAimes(10)) {
                        topLikes.add(new AbstractMap.SimpleEntry<>(
                                morceau,
                                avisService.getNombreLikes(morceau.getId())));
                    }
                    vue.afficherTopMorceauxLikes(topLikes);
                    vue.attendreTouche();
                }
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void afficherStatistiquesEvoluees() {
        vue.afficherTopMorceaux(statistiquesService.getTopMorceaux(10));
        vue.attendreTouche();
    }

    private void gererProfil() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuProfil(utilisateurConnecte);
            switch (choix) {
                case 1 -> {
                    String nom = vue.lireTexte("Nouveau nom");
                    if (utilisateurService.modifierProfil(utilisateurConnecte.getId(), nom, null)) {
                        vue.afficherSucces("Nom modifie.");
                    } else {
                        vue.afficherErreur("Modification impossible.");
                    }
                }
                case 2 -> {
                    String email = vue.lireTexte("Nouvel email");
                    if (utilisateurService.modifierProfil(utilisateurConnecte.getId(), null, email)) {
                        vue.afficherSucces("Email modifie.");
                    } else {
                        vue.afficherErreur("Cet email est deja utilise.");
                    }
                }
                case 3 -> {
                    String ancien = vue.lireTexte("Ancien mot de passe");
                    String nouveau = vue.lireTexte("Nouveau mot de passe");
                    if (utilisateurService.changerMotDePasse(utilisateurConnecte.getId(), ancien, nouveau)) {
                        vue.afficherSucces("Mot de passe modifie.");
                    } else {
                        vue.afficherErreur("Ancien mot de passe incorrect.");
                    }
                }
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }

    private void gererUtilisateurs() {
        boolean continuer = true;
        while (continuer) {
            int choix = vue.afficherMenuGestionUtilisateurs();
            switch (choix) {
                case 1 -> {
                    List<Utilisateur> users = utilisateurService.getTousUtilisateurs();
                    vue.afficherListeUtilisateurs(users);
                    vue.attendreTouche();
                }
                case 2 -> {
                    List<Utilisateur> users = utilisateurService.getTousUtilisateurs();
                    vue.afficherListeUtilisateurs(users);
                    int id = vue.lireEntier("ID de l'utilisateur a supprimer");
                    if (id == utilisateurConnecte.getId()) {
                        vue.afficherErreur("Vous ne pouvez pas supprimer votre propre compte.");
                    } else if (utilisateurService.getUtilisateur(id) != null) {
                        if (vue.confirmer("Confirmer la suppression ?")) {
                            utilisateurService.supprimerUtilisateur(id);
                            vue.afficherSucces("Utilisateur supprime.");
                        }
                    } else {
                        vue.afficherErreur("Utilisateur introuvable.");
                    }
                }
                case 3 -> {
                    List<Utilisateur> users = utilisateurService.getTousUtilisateurs();
                    vue.afficherListeUtilisateurs(users);
                    int id = vue.lireEntier("ID de l'utilisateur a suspendre/reactiver");
                    if (id == utilisateurConnecte.getId()) {
                        vue.afficherErreur("Vous ne pouvez pas suspendre votre propre compte.");
                    } else {
                        Utilisateur u = utilisateurService.getUtilisateur(id);
                        if (u != null) {
                            utilisateurService.suspendreUtilisateur(id);
                            String statut = u.isSuspendu() ? "suspendu" : "reactve";
                            vue.afficherSucces("Utilisateur " + statut + ".");
                        } else {
                            vue.afficherErreur("Utilisateur introuvable.");
                        }
                    }
                }
                case 0 -> continuer = false;
                default -> vue.afficherErreur("Choix invalide.");
            }
        }
    }
}
