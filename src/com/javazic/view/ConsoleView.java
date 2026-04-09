package com.javazic.view;

import com.javazic.model.*;
import com.javazic.util.FormatUtil;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Gere tout l'affichage console et la saisie utilisateur.
 * Remplacable par une interface graphique sans modifier les controllers.
 */
public class ConsoleView {

    private static final int CLEAR_LINES = 50;
    private static final int HELPER_MAX_LINES = 20;

    private final Scanner scanner;
    private List<Morceau> derniersMorceauxUtilisables;

    public ConsoleView() {
        this.scanner = new Scanner(System.in, "UTF-8");
        this.derniersMorceauxUtilisables = List.of();
    }

    // ======================== UTILITAIRES ========================

    public void effacerEcran() {
        int lignesVides = CLEAR_LINES;
        if (aideIdsDisponible()) {
            lignesVides -= HELPER_MAX_LINES;
        }

        for (int i = 0; i < lignesVides; i++) {
            System.out.println();
        }

        afficherAideDerniersIds();
    }

    public void afficherMessage(String message) {
        System.out.println(message);
    }

    public void afficherErreur(String message) {
        System.out.println("[ERREUR] " + message);
    }

    public void afficherSucces(String message) {
        System.out.println("[OK] " + message);
    }

    public void afficherSeparateur() {
        System.out.println("─".repeat(50));
    }

    public void afficherTitre(String titre) {
        System.out.println();
        System.out.println("═".repeat(50));
        System.out.println(FormatUtil.centrer(titre, 50));
        System.out.println("═".repeat(50));
    }

    public void sautLigne() {
        System.out.println();
    }

    public int lireChoix() {
        System.out.print("Votre choix > ");
        String ligne = scanner.nextLine().trim();
        try {
            return Integer.parseInt(ligne);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String lireTexte(String prompt) {
        System.out.print(prompt + " > ");
        return scanner.nextLine().trim();
    }

    public String lireCommandeDisponible() {
        try {
            if (System.in.available() > 0) {
                return scanner.nextLine().trim().toLowerCase();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public int lireEntier(String prompt) {
        System.out.print(prompt + " > ");
        String ligne = scanner.nextLine().trim();
        try {
            return Integer.parseInt(ligne);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean confirmer(String question) {
        System.out.print(question + " (o/n) > ");
        String reponse = scanner.nextLine().trim().toLowerCase();
        return reponse.equals("o") || reponse.equals("oui");
    }

    public void attendreTouche() {
        System.out.print("Appuyez sur Entree pour continuer...");
        scanner.nextLine();
    }

    // ======================== BANNIERE ========================

    public void afficherBanniere() {
        System.out.println();
        System.out.println("     ██╗ █████╗ ██╗   ██╗ █████╗ ███████╗██╗ ██████╗");
        System.out.println("     ██║██╔══██╗██║   ██║██╔══██╗╚══███╔╝██║██╔════╝");
        System.out.println("     ██║███████║██║   ██║███████║  ███╔╝ ██║██║     ");
        System.out.println("██   ██║██╔══██║╚██╗ ██╔╝██╔══██║ ███╔╝  ██║██║     ");
        System.out.println("╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║███████╗██║╚██████╗");
        System.out.println(" ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝╚══════╝╚═╝ ╚═════╝");
        System.out.println("          Votre catalogue musical en Java");
        System.out.println();
    }

    // ======================== MENUS ========================

    public int afficherMenuAuthentification() {
        effacerEcran();
        afficherBanniere();
        afficherSeparateur();
        afficherMessage("1. Se connecter en tant qu'administrateur");
        afficherMessage("2. Se connecter en tant qu'abonne");
        afficherMessage("3. Creer un compte");
        afficherMessage("4. Continuer en tant que visiteur");
        afficherMessage("0. Quitter");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuPrincipal(Utilisateur utilisateur) {
        effacerEcran();
        afficherTitre("Menu Principal");
        afficherMessage("Connecte en tant que : " + utilisateur.getNom()
                + " [" + utilisateur.getType() + "]");
        afficherSeparateur();
        afficherMessage("1. Parcourir le catalogue");
        afficherMessage("2. Rechercher");
        afficherMessage("3. Mes playlists");
        afficherMessage("4. Playlists publiques");
        afficherMessage("5. Mon historique d'ecoute");
        afficherMessage("6. Noter un morceau");
        afficherMessage("7. Mon profil");
        if (utilisateur.estAdmin()) {
            afficherSeparateur();
            afficherMessage("8. [Admin] Gestion du catalogue");
            afficherMessage("9. [Admin] Gestion des utilisateurs");
            afficherMessage("10. [Admin] Statistiques");
        }
        afficherSeparateur();
        afficherMessage("0. Se deconnecter");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuVisiteur() {
        effacerEcran();
        afficherTitre("Menu Visiteur");
        afficherMessage("1. Parcourir le catalogue");
        afficherMessage("2. Rechercher");
        afficherMessage("3. Playlists publiques");
        afficherMessage("4. Statistiques des morceaux");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuCatalogue() {
        effacerEcran();
        afficherTitre("Catalogue");
        afficherMessage("1. Demo");
        afficherMessage("2. Jamendo");
        afficherMessage("3. Apple");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuCatalogueDemo() {
        effacerEcran();
        afficherTitre("Catalogue Demo");
        afficherMessage("1. Artistes");
        afficherMessage("2. Albums");
        afficherMessage("3. Tous les morceaux");
        afficherMessage("4. Groupes");
        afficherMessage("5. Filtrer par genre");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuAdminCatalogue() {
        effacerEcran();
        afficherTitre("Gestion du Catalogue");
        afficherMessage("1. Ajouter un artiste");
        afficherMessage("2. Ajouter un album");
        afficherMessage("3. Ajouter un morceau");
        afficherMessage("4. Supprimer un morceau");
        afficherMessage("5. Supprimer un album");
        afficherMessage("6. Supprimer un artiste");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuGestionUtilisateurs() {
        effacerEcran();
        afficherTitre("Gestion des Utilisateurs");
        afficherMessage("1. Lister les utilisateurs");
        afficherMessage("2. Supprimer un utilisateur");
        afficherMessage("3. Suspendre / Reactiver un utilisateur");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuPlaylists() {
        effacerEcran();
        afficherTitre("Mes Playlists");
        afficherMessage("1. Voir mes playlists");
        afficherMessage("2. Creer une playlist");
        afficherMessage("3. Gerer une playlist");
        afficherMessage("4. Supprimer une playlist");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuGestionPlaylist(Playlist playlist) {
        effacerEcran();
        afficherTitre("Playlist : " + playlist.getNom());
        afficherDetailPlaylist(playlist);
        afficherSeparateur();
        afficherMessage("1. Ajouter un morceau");
        afficherMessage("2. Retirer un morceau");
        afficherMessage("3. Renommer");
        afficherMessage("4. Changer la visibilite");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuRecherche() {
        effacerEcran();
        afficherTitre("Recherche");
        afficherMessage("1. Demo");
        afficherMessage("2. Apple");
        afficherMessage("3. Jamendo");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuRechercheDemo() {
        effacerEcran();
        afficherTitre("Recherche Demo");
        afficherMessage("1. Rechercher un artiste");
        afficherMessage("2. Rechercher un album");
        afficherMessage("3. Rechercher un morceau");
        afficherMessage("4. Recherche globale");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuRechercheProvider(String provider) {
        effacerEcran();
        afficherTitre("Recherche " + provider);
        afficherMessage("1. Rechercher un artiste");
        afficherMessage("2. Rechercher un album");
        afficherMessage("3. Rechercher un morceau");
        afficherMessage("4. Recherche globale");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuAjoutPlaylist() {
        effacerEcran();
        afficherTitre("Ajouter a la playlist");
        afficherMessage("1. Ajouter depuis les derniers resultats");
        afficherMessage("2. Ajouter via une source");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuSelectionSource() {
        effacerEcran();
        afficherTitre("Choisir une source");
        afficherMessage("1. Demo");
        afficherMessage("2. Jamendo");
        afficherMessage("3. Apple");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuAjoutPlaylistDemo() {
        effacerEcran();
        afficherTitre("Ajouter depuis Demo");
        afficherMessage("1. Tous les morceaux");
        afficherMessage("2. Rechercher un morceau");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuAjoutPlaylistProvider(String provider) {
        effacerEcran();
        afficherTitre("Ajouter depuis " + provider);
        afficherMessage("1. Tendances");
        afficherMessage("2. Recherche globale");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuProfil(Utilisateur utilisateur) {
        effacerEcran();
        afficherTitre("Mon Profil");
        afficherMessage("Nom    : " + utilisateur.getNom());
        afficherMessage("Email  : " + utilisateur.getEmail());
        afficherMessage("Type   : " + utilisateur.getType());
        afficherMessage("Inscrit: " + utilisateur.getDateInscription());
        afficherSeparateur();
        afficherMessage("1. Modifier le nom");
        afficherMessage("2. Modifier l'email");
        afficherMessage("3. Changer le mot de passe");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    public int afficherMenuStatistiques() {
        effacerEcran();
        afficherTitre("Statistiques");
        afficherMessage("1. Statistiques generales");
        afficherMessage("2. Top morceaux les plus ecoutes");
        afficherMessage("3. Top artistes les plus ecoutes");
        afficherMessage("4. Top albums les plus ecoutes");
        afficherSeparateur();
        afficherMessage("0. Retour");
        afficherSeparateur();
        return lireChoix();
    }

    // ======================== AFFICHAGE LISTES ========================

    public void afficherListeArtistes(List<Artiste> artistes) {
        if (artistes.isEmpty()) {
            afficherMessage("Aucun artiste trouve.");
            return;
        }
        afficherMessage(String.format("%-4s %-8s %-12s %-25s %-15s %s",
                "", "ID", "ID source", "Nom", "Pays", "Debut"));
        afficherSeparateur();
        for (Artiste a : artistes) {
            afficherMessage(String.format("%-4s %-8d %-12s %-25s %-15s %s",
                    a.getSource().getTag(),
                    a.getId(),
                    FormatUtil.tronquer(afficherSourceId(a.getSourceId()), 12),
                    FormatUtil.tronquer(a.getNom(), 25),
                    FormatUtil.tronquer(a.getPaysOrigine(), 15),
                    a.getDateDebut().getYear()));
        }
    }

    public void afficherDetailArtiste(Artiste artiste, List<Album> albums, List<Morceau> morceaux) {
        memoriserMorceauxUtilisables(morceaux);
        afficherTitre("Artiste : " + artiste.getNom());
        afficherMessage("Pays      : " + artiste.getPaysOrigine());
        afficherMessage("Debut     : " + artiste.getDateDebut());
        afficherMessage("Biographie: " + artiste.getBiographie());
        sautLigne();
        if (!albums.isEmpty()) {
            afficherMessage("Albums (" + albums.size() + ") :");
            for (Album a : albums) {
                afficherMessage("  " + a.getId() + ". " + a.getTitre()
                        + " (" + a.getGenre() + ", " + a.getDateSortie().getYear() + ")");
            }
        }
        sautLigne();
        if (!morceaux.isEmpty()) {
            afficherMessage("Morceaux (" + morceaux.size() + ") :");
            for (Morceau m : morceaux) {
                String albumTitre = m.getAlbum() != null ? m.getAlbum().getTitre() : "Single";
                afficherMessage("  " + m.getId() + ". " + m.getTitre()
                        + " [" + m.getDureeFormatee() + "] - " + albumTitre);
            }
        }
    }

    public void afficherListeAlbums(List<Album> albums) {
        if (albums.isEmpty()) {
            afficherMessage("Aucun album trouve.");
            return;
        }
        afficherMessage(String.format("%-4s %-8s %-12s %-25s %-20s %-12s %s",
                "", "ID", "ID source", "Titre", "Artiste", "Genre", "Annee"));
        afficherSeparateur();
        for (Album a : albums) {
            afficherMessage(String.format("%-4s %-8d %-12s %-25s %-20s %-12s %d",
                    a.getSource().getTag(),
                    a.getId(),
                    FormatUtil.tronquer(afficherSourceId(a.getSourceId()), 12),
                    FormatUtil.tronquer(a.getTitre(), 25),
                    FormatUtil.tronquer(a.getArtiste().getNom(), 20),
                    a.getGenre(),
                    a.getDateSortie().getYear()));
        }
    }

    public void afficherDetailAlbum(Album album) {
        memoriserMorceauxUtilisables(album.getMorceaux());
        afficherTitre("Album : " + album.getTitre());
        afficherMessage("Artiste : " + album.getArtiste().getNom());
        afficherMessage("Genre   : " + album.getGenre());
        afficherMessage("Sortie  : " + album.getDateSortie());
        afficherMessage("Duree   : " + FormatUtil.formaterDuree(album.calculerDureeTotale()));
        sautLigne();
        List<Morceau> morceaux = album.getMorceaux();
        if (!morceaux.isEmpty()) {
            afficherMessage("Morceaux (" + morceaux.size() + ") :");
            int i = 1;
            for (Morceau m : morceaux) {
                afficherMessage(String.format("  %d. [ID:%d] %-35s %s",
                        i++, m.getId(), m.getTitre(), m.getDureeFormatee()));
            }
        }
    }

    public void afficherListeMorceaux(List<Morceau> morceaux) {
        memoriserMorceauxUtilisables(morceaux);
        if (morceaux.isEmpty()) {
            afficherMessage("Aucun morceau trouve.");
            return;
        }
        afficherMessage(String.format("%-4s %-8s %-12s %-28s %-20s %s",
                "", "ID", "ID source", "Titre", "Artiste", "Duree"));
        afficherSeparateur();
        for (Morceau m : morceaux) {
            String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
            afficherMessage(String.format("%-4s %-8d %-12s %-28s %-20s %s",
                    m.getSource().getTag(),
                    m.getId(),
                    FormatUtil.tronquer(afficherSourceId(m.getSourceId()), 12),
                    FormatUtil.tronquer(m.getTitre(), 28),
                    FormatUtil.tronquer(artiste, 20),
                    m.getDureeFormatee()));
        }
    }

    public void afficherListeGroupes(List<Groupe> groupes) {
        if (groupes.isEmpty()) {
            afficherMessage("Aucun groupe trouve.");
            return;
        }
        for (Groupe g : groupes) {
            afficherMessage(g.getId() + ". " + g.getNom()
                    + " (forme en " + g.getDateFormation().getYear() + ", "
                    + g.getMembres().size() + " membres)");
            for (Artiste a : g.getMembres()) {
                afficherMessage("     - " + a.getNom());
            }
        }
    }

    public void afficherListePlaylists(List<Playlist> playlists) {
        if (playlists.isEmpty()) {
            afficherMessage("Aucune playlist trouvee.");
            return;
        }
        for (Playlist p : playlists) {
            String visibilite = p.isEstPublique() ? "publique" : "privee";
            afficherMessage(p.getId() + ". " + p.getNom()
                    + " (" + p.getNombreMorceaux() + " morceaux, "
                    + p.getDureeTotaleFormatee() + ", " + visibilite + ")");
        }
    }

    public void afficherDetailPlaylist(Playlist playlist) {
        memoriserMorceauxUtilisables(playlist.getMorceaux());
        afficherMessage("Description : " + playlist.getDescription());
        afficherMessage("Creee le    : " + playlist.getDateCreation());
        afficherMessage("Visibilite  : " + (playlist.isEstPublique() ? "Publique" : "Privee"));
        afficherMessage("Duree totale: " + playlist.getDureeTotaleFormatee());
        sautLigne();
        List<Morceau> morceaux = playlist.getMorceaux();
        if (morceaux.isEmpty()) {
            afficherMessage("  (playlist vide)");
        } else {
            afficherMessage(String.format("  %-4s %-8s %-12s %-26s %-16s %s",
                    "", "ID", "ID source", "Titre", "Artiste", "Duree"));
            afficherSeparateur();
            for (Morceau m : morceaux) {
                String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
                afficherMessage(String.format("  %-4s %-8d %-12s %-26s %-16s %s",
                        m.getSource().getTag(),
                        m.getId(),
                        FormatUtil.tronquer(afficherSourceId(m.getSourceId()), 12),
                        FormatUtil.tronquer(m.getTitre(), 26),
                        FormatUtil.tronquer(artiste, 16),
                        m.getDureeFormatee()));
            }
        }
    }

    public void afficherListeUtilisateurs(List<Utilisateur> utilisateurs) {
        if (utilisateurs.isEmpty()) {
            afficherMessage("Aucun utilisateur.");
            return;
        }
        afficherMessage(String.format("%-5s %-20s %-25s %-15s %s", "ID", "Nom", "Email", "Type", "Statut"));
        afficherSeparateur();
        for (Utilisateur u : utilisateurs) {
            String statut = u.isSuspendu() ? "SUSPENDU" : "Actif";
            afficherMessage(String.format("%-5d %-20s %-25s %-15s %s",
                    u.getId(), u.getNom(), u.getEmail(), u.getType(), statut));
        }
    }

    // ======================== ECOUTE ========================

    public void afficherEcoute(Morceau morceau) {
        String artiste = morceau.getArtistes().isEmpty() ? "Inconnu" : morceau.getArtistes().get(0).getNom();
        afficherMessage("Lecture en cours : " + morceau.getTitre() + " - " + artiste);
        afficherMessage("[" + morceau.getDureeFormatee() + "]");

        // Simulation de lecture avec barre de progression
        int dureeSimulee = Math.min(morceau.getDuree(), 5); // max 5 secondes de simulation
        int etapes = 20;
        for (int i = 0; i <= etapes; i++) {
            StringBuilder barre = new StringBuilder("[");
            for (int j = 0; j < etapes; j++) {
                barre.append(j < i ? "=" : " ");
            }
            barre.append("] ").append(i * 100 / etapes).append("%");
            System.out.print("\r" + barre);
            if (i < etapes) {
                try {
                    Thread.sleep((long) dureeSimulee * 1000 / etapes);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.out.println();
        afficherSucces("Lecture terminee.");
    }

    public void afficherLimiteEcoutes() {
        afficherErreur("Limite d'ecoutes atteinte (5 par session). Creez un compte pour ecouter plus !");
    }

    // ======================== HISTORIQUE ========================

    public void afficherHistorique(List<HistoriqueEcoute> historique) {
        afficherTitre("Historique d'ecoute");
        if (historique.isEmpty()) {
            afficherMessage("Aucune ecoute enregistree.");
        } else {
            // Afficher les plus recentes en premier
            for (int i = historique.size() - 1; i >= 0; i--) {
                afficherMessage("  " + historique.get(i));
            }
        }
    }

    // ======================== AVIS ========================

    public void afficherAvisMorceau(List<Avis> avisList, double noteMoyenne) {
        if (avisList.isEmpty()) {
            afficherMessage("Aucun avis pour ce morceau.");
        } else {
            afficherMessage("Note moyenne : " + String.format("%.1f", noteMoyenne) + "/5"
                    + " (" + avisList.size() + " avis)");
            afficherSeparateur();
            for (Avis a : avisList) {
                String etoiles = "*".repeat(a.getNote()) + " ".repeat(5 - a.getNote());
                afficherMessage("  [" + etoiles + "] " + a.getAuteur().getNom()
                        + (a.getCommentaire().isEmpty() ? "" : " : " + a.getCommentaire()));
            }
        }
    }

    // ======================== STATISTIQUES ========================

    public void afficherStatistiquesGenerales(int nbUtilisateurs, int nbMorceaux, int nbAlbums,
                                               int nbArtistes, int nbGroupes, int nbEcoutes) {
        afficherTitre("Statistiques Generales");
        afficherMessage("Utilisateurs : " + nbUtilisateurs);
        afficherMessage("Artistes     : " + nbArtistes);
        afficherMessage("Albums       : " + nbAlbums);
        afficherMessage("Morceaux     : " + nbMorceaux);
        afficherMessage("Groupes      : " + nbGroupes);
        afficherMessage("Total ecoutes: " + nbEcoutes);
    }

    public void afficherTopMorceaux(List<Morceau> morceaux) {
        afficherTitre("Top Morceaux");
        if (morceaux.isEmpty()) {
            afficherMessage("Aucune donnee.");
            return;
        }
        int rang = 1;
        for (Morceau m : morceaux) {
            String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
            afficherMessage(String.format("  %d. %-30s %-20s %d ecoutes",
                    rang++, m.getTitre(), artiste, m.getNombreEcoute()));
        }
    }

    public void afficherTopArtistes(List<Map.Entry<Artiste, Integer>> artistes) {
        afficherTitre("Top Artistes");
        if (artistes.isEmpty()) {
            afficherMessage("Aucune donnee.");
            return;
        }
        int rang = 1;
        for (Map.Entry<Artiste, Integer> entry : artistes) {
            afficherMessage(String.format("  %d. %-30s %d ecoutes",
                    rang++, entry.getKey().getNom(), entry.getValue()));
        }
    }

    public void afficherTopAlbums(List<Map.Entry<Album, Integer>> albums) {
        afficherTitre("Top Albums");
        if (albums.isEmpty()) {
            afficherMessage("Aucune donnee.");
            return;
        }
        int rang = 1;
        for (Map.Entry<Album, Integer> entry : albums) {
            afficherMessage(String.format("  %d. %-30s %d ecoutes",
                    rang++, entry.getKey().getTitre(), entry.getValue()));
        }
    }

    // ======================== JAMENDO ========================

    public void afficherDebutLecture(Morceau morceau) {
        String artiste = morceau.getArtistes().isEmpty() ? "Inconnu" : morceau.getArtistes().get(0).getNom();
        String source = morceau.getSource().getLibelle();
        afficherMessage("Lecture [" + source + "] : " + morceau.getTitre() + " - " + artiste);
        afficherMessage("[" + morceau.getDureeFormatee() + "]");
    }

    public void afficherCommandesLecture() {
        afficherMessage("Commandes lecture : s = pause/reprise, d = suivant, q = precedent, 0 = stop");
        afficherMessage("Tapez une commande puis appuyez sur Entree.");
    }

    // ======================== GENRES ========================

    public Genre choisirGenre() {
        afficherMessage("Genres disponibles :");
        Genre[] genres = Genre.values();
        for (int i = 0; i < genres.length; i++) {
            afficherMessage((i + 1) + ". " + genres[i].getLibelle());
        }
        int choix = lireEntier("Choisir un genre");
        if (choix >= 1 && choix <= genres.length) {
            return genres[choix - 1];
        }
        return null;
    }

    public void afficherGenresDisponibles() {
        Genre[] genres = Genre.values();
        for (int i = 0; i < genres.length; i++) {
            afficherMessage((i + 1) + ". " + genres[i].getLibelle());
        }
    }

    private String afficherSourceId(String sourceId) {
        return sourceId == null || sourceId.isEmpty() ? "-" : sourceId;
    }

    private void memoriserMorceauxUtilisables(List<Morceau> morceaux) {
        if (morceaux == null || morceaux.isEmpty()) {
            return;
        }
        int borne = Math.min(morceaux.size(), HELPER_MAX_LINES - 1);
        this.derniersMorceauxUtilisables = List.copyOf(morceaux.subList(0, borne));
    }

    private boolean aideIdsDisponible() {
        return derniersMorceauxUtilisables != null && !derniersMorceauxUtilisables.isEmpty();
    }

    private void afficherAideDerniersIds() {
        if (!aideIdsDisponible()) {
            return;
        }

        afficherMessage("IDs utiles recents :");
        for (Morceau morceau : derniersMorceauxUtilisables) {
            String artiste = morceau.getArtistes().isEmpty() ? "?" : morceau.getArtistes().get(0).getNom();
            afficherMessage(String.format("%-4s %-8d %-18s %s",
                    morceau.getSource().getTag(),
                    morceau.getId(),
                    FormatUtil.tronquer(artiste, 18),
                    FormatUtil.tronquer(morceau.getTitre(), 24)));
        }
        sautLigne();
    }
}
