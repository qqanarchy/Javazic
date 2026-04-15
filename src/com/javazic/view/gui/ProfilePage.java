package com.javazic.view.gui;

import com.javazic.model.HistoriqueEcoute;
import com.javazic.model.Genre;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;
import com.javazic.service.AvisService;
import com.javazic.service.CatalogueService;
import com.javazic.service.StatistiquesService;
import com.javazic.service.UtilisateurService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Page profil utilisateur ou visiteur.
 */
public class ProfilePage extends VBox {

    private final Utilisateur utilisateur;
    private final UtilisateurService utilisateurService;
    private final StatistiquesService statistiquesService;
    private final AvisService avisService;
    private final CatalogueService catalogueService;
    private final boolean modeVisiteur;
    private final int ecoutesRestantes;
    private final int ecoutesMax;

    public ProfilePage(Utilisateur utilisateur,
                       UtilisateurService utilisateurService,
                       StatistiquesService statistiquesService,
                       AvisService avisService,
                       CatalogueService catalogueService) {
        this.utilisateur = utilisateur;
        this.utilisateurService = utilisateurService;
        this.statistiquesService = statistiquesService;
        this.avisService = avisService;
        this.catalogueService = catalogueService;
        this.modeVisiteur = false;
        this.ecoutesRestantes = 0;
        this.ecoutesMax = 0;

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        getChildren().add(creerSectionProfilAbonne());
        getChildren().add(creerSectionHistorique());

        if (utilisateur.estAdmin()) {
            getChildren().add(creerSectionAdmin());
        }
    }

    public ProfilePage(int ecoutesRestantes, int ecoutesMax) {
        this.utilisateur = null;
        this.utilisateurService = null;
        this.statistiquesService = null;
        this.avisService = null;
        this.catalogueService = null;
        this.modeVisiteur = true;
        this.ecoutesRestantes = ecoutesRestantes;
        this.ecoutesMax = ecoutesMax;

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        getChildren().add(creerSectionProfilVisiteur());
    }

    private VBox creerSectionProfilVisiteur() {
        VBox section = new VBox(18);

        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = creerAvatar("V", "#535353");

        VBox info = new VBox(4);
        Label nom = new Label("Visiteur");
        nom.getStyleClass().add("page-title");

        Label details = new Label("Mode invite");
        details.getStyleClass().add("text-secondary");
        info.getChildren().addAll(nom, details);

        headerRow.getChildren().addAll(avatar, info);

        VBox ecoutesBox = new VBox(8);
        Label titre = new Label("Ecoutes disponibles");
        titre.getStyleClass().add("text-primary");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label compteur = new Label(ecoutesRestantes + " / " + ecoutesMax);
        compteur.getStyleClass().add("text-secondary");

        double progression = ecoutesMax <= 0 ? 0 : (double) ecoutesRestantes / ecoutesMax;
        ProgressBar bar = new ProgressBar(Math.max(0, Math.min(1, progression)));
        bar.setPrefWidth(340);
        bar.setStyle("-fx-accent: #1DB954;");

        Label aide = new Label("Creez un compte pour debloquer l'ecoute sans limite et les playlists.");
        aide.getStyleClass().add("text-secondary");

        ecoutesBox.getChildren().addAll(titre, compteur, bar, aide);
        section.getChildren().addAll(headerRow, ecoutesBox);
        return section;
    }

    private VBox creerSectionProfilAbonne() {
        VBox section = new VBox(16);

        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = creerAvatar(utilisateur.getNom().substring(0, 1).toUpperCase(), "#1DB954");

        VBox infoUser = new VBox(4);
        Label nom = new Label(utilisateur.getNom());
        nom.getStyleClass().add("page-title");

        Label details = new Label(utilisateur.getEmail() + "  |  " + utilisateur.getType()
                + "  |  Inscrit le " + utilisateur.getDateInscription());
        details.getStyleClass().add("text-secondary");

        infoUser.getChildren().addAll(nom, details);
        headerRow.getChildren().addAll(avatar, infoUser);

        HBox editRow = new HBox(12);
        editRow.setAlignment(Pos.CENTER_LEFT);

        TextField nomField = new TextField(utilisateur.getNom());
        nomField.setPromptText("Nom");
        nomField.setPrefWidth(200);

        TextField emailField = new TextField(utilisateur.getEmail());
        emailField.setPromptText("Email");
        emailField.setPrefWidth(200);

        Button btnSauver = new Button("Enregistrer");
        btnSauver.getStyleClass().add("btn-primary");
        btnSauver.setStyle("-fx-padding: 8 20 8 20;");

        Label lblResultat = new Label();
        lblResultat.setVisible(false);

        btnSauver.setOnAction(e -> {
            String newNom = nomField.getText().trim();
            String newEmail = emailField.getText().trim();
            boolean okNom = !newNom.isEmpty() && !newNom.equals(utilisateur.getNom());
            boolean okEmail = !newEmail.isEmpty() && !newEmail.equals(utilisateur.getEmail());

            if (okNom) {
                utilisateurService.modifierProfil(utilisateur.getId(), newNom, null);
            }
            if (okEmail) {
                boolean res = utilisateurService.modifierProfil(utilisateur.getId(), null, newEmail);
                if (!res) {
                    lblResultat.setText("Email deja utilise.");
                    lblResultat.setTextFill(Color.web("#F15E6C"));
                    lblResultat.setVisible(true);
                    return;
                }
            }
            lblResultat.setText("Profil mis a jour.");
            lblResultat.setTextFill(Color.web("#1DB954"));
            lblResultat.setVisible(true);
            nom.setText(utilisateur.getNom());
        });

        editRow.getChildren().addAll(nomField, emailField, btnSauver, lblResultat);

        HBox mdpRow = new HBox(12);
        mdpRow.setAlignment(Pos.CENTER_LEFT);

        PasswordField ancienMdp = new PasswordField();
        ancienMdp.setPromptText("Ancien mot de passe");
        ancienMdp.setPrefWidth(200);

        PasswordField nouveauMdp = new PasswordField();
        nouveauMdp.setPromptText("Nouveau mot de passe");
        nouveauMdp.setPrefWidth(200);

        Button btnMdp = new Button("Changer");
        btnMdp.getStyleClass().add("btn-secondary");
        btnMdp.setStyle("-fx-padding: 8 20 8 20;");

        Label lblMdp = new Label();
        lblMdp.setVisible(false);

        btnMdp.setOnAction(e -> {
            boolean ok = utilisateurService.changerMotDePasse(
                    utilisateur.getId(), ancienMdp.getText(), nouveauMdp.getText());
            lblMdp.setText(ok ? "Mot de passe modifie." : "Ancien mot de passe incorrect.");
            lblMdp.setTextFill(ok ? Color.web("#1DB954") : Color.web("#F15E6C"));
            lblMdp.setVisible(true);
            if (ok) {
                ancienMdp.clear();
                nouveauMdp.clear();
            }
        });

        mdpRow.getChildren().addAll(ancienMdp, nouveauMdp, btnMdp, lblMdp);

        section.getChildren().addAll(headerRow, editRow, mdpRow);
        return section;
    }

    private VBox creerSectionHistorique() {
        VBox section = new VBox(8);

        Label titre = new Label("Historique d'ecoute");
        titre.getStyleClass().add("section-title");
        section.getChildren().add(titre);

        List<HistoriqueEcoute> historique = utilisateur.getHistoriqueEcoutes();
        if (historique.isEmpty()) {
            Label vide = new Label("Aucune ecoute enregistree.");
            vide.getStyleClass().add("text-secondary");
            section.getChildren().add(vide);
        } else {
            int limit = Math.min(historique.size(), 20);
            for (int i = historique.size() - 1; i >= historique.size() - limit; i--) {
                HistoriqueEcoute h = historique.get(i);
                Label ligne = new Label(h.toString());
                ligne.getStyleClass().add("text-secondary");
                ligne.setStyle("-fx-padding: 2 0 2 8;");
                section.getChildren().add(ligne);
            }
        }
        return section;
    }

    private VBox creerSectionAdmin() {
        VBox section = new VBox(16);

        Label titre = new Label("Administration");
        titre.getStyleClass().add("section-title");
        titre.setStyle("-fx-text-fill: #F0A500; -fx-font-size: 22px;");

        VBox stats = creerStats();
        VBox gestionDemo = creerGestionMorceauxDemo();
        VBox gestionUsers = creerGestionUtilisateurs();

        section.getChildren().addAll(titre, stats, gestionDemo, gestionUsers);
        return section;
    }

    private VBox creerStats() {
        VBox box = new VBox(8);
        Label titre = new Label("Statistiques generales");
        titre.getStyleClass().add("text-primary");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);

        ajouterStat(grid, 0, "Utilisateurs", statistiquesService.getNombreUtilisateurs());
        ajouterStat(grid, 1, "Artistes", statistiquesService.getNombreArtistes());
        ajouterStat(grid, 2, "Albums", statistiquesService.getNombreAlbums());
        ajouterStat(grid, 3, "Morceaux", statistiquesService.getNombreMorceaux());
        ajouterStat(grid, 4, "Ecoutes totales", statistiquesService.getNombreTotalEcoutes());

        VBox topBox = new VBox(4);
        Label lblTop = new Label("Top 5 morceaux");
        lblTop.getStyleClass().add("text-primary");
        lblTop.setStyle("-fx-font-weight: bold;");
        topBox.getChildren().add(lblTop);

        List<Morceau> top = statistiquesService.getTopMorceaux(5);
        int rang = 1;
        for (Morceau m : top) {
            String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
            Label ligne = new Label(rang++ + ". " + m.getTitre() + " - " + artiste
                    + " (" + m.getNombreEcoute() + " ecoutes)");
            ligne.getStyleClass().add("text-secondary");
            topBox.getChildren().add(ligne);
        }

        VBox likesBox = new VBox(4);
        Label lblLikes = new Label("Top 5 morceaux likes");
        lblLikes.getStyleClass().add("text-primary");
        lblLikes.setStyle("-fx-font-weight: bold;");
        likesBox.getChildren().add(lblLikes);

        List<Morceau> topLikes = statistiquesService.getMorceauxLesPlusAimes(5);
        if (topLikes.isEmpty()) {
            Label vide = new Label("Aucun avis enregistre.");
            vide.getStyleClass().add("text-secondary");
            likesBox.getChildren().add(vide);
        } else {
            int rangLikes = 1;
            for (Morceau m : topLikes) {
                String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
                int likes = avisService.getNombreLikes(m.getId());
                Label ligne = new Label(rangLikes++ + ". " + m.getTitre() + " - " + artiste
                        + " (" + likes + " likes)");
                ligne.getStyleClass().add("text-secondary");
                likesBox.getChildren().add(ligne);
            }
        }

        box.getChildren().addAll(titre, grid, topBox, likesBox);
        return box;
    }

    private VBox creerGestionMorceauxDemo() {
        VBox box = new VBox(10);
        Label titre = new Label("Gestion des morceaux Demo");
        titre.getStyleClass().add("text-primary");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnAjouter = new Button("+ Creer un morceau");
        btnAjouter.getStyleClass().add("btn-primary");
        btnAjouter.setStyle("-fx-padding: 8 16 8 16;");

        VBox listeBox = new VBox(6);

        Runnable[] refreshRef = new Runnable[1];
        refreshRef[0] = () -> {
            listeBox.getChildren().clear();
            List<Morceau> morceaux = catalogueService.getMorceauxDemo();
            if (morceaux.isEmpty()) {
                Label vide = new Label("Aucun morceau Demo disponible.");
                vide.getStyleClass().add("text-secondary");
                listeBox.getChildren().add(vide);
                return;
            }

            for (Morceau morceau : morceaux) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 8, 6, 8));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 4;");

                String artiste = morceau.getArtistes().isEmpty() ? "?" : morceau.getArtistes().get(0).getNom();
                Label info = new Label(morceau.getId() + ". " + morceau.getTitre()
                        + " - " + artiste + " (" + morceau.getDureeFormatee()
                        + ", " + morceau.getGenre() + ")");
                info.getStyleClass().add("text-primary");
                HBox.setHgrow(info, Priority.ALWAYS);

                Button btnConsulter = new Button("Consulter");
                btnConsulter.getStyleClass().add("btn-secondary");
                btnConsulter.setOnAction(e -> afficherDetailMorceauDemo(morceau));

                Button btnSupprimer = new Button("Supprimer");
                btnSupprimer.getStyleClass().add("btn-secondary");
                btnSupprimer.setStyle("-fx-text-fill: #F15E6C; -fx-border-color: #F15E6C;");
                btnSupprimer.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Supprimer le morceau");
                    alert.setHeaderText(null);
                    alert.setContentText("Supprimer \"" + morceau.getTitre() + "\" ?");
                    alert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            catalogueService.supprimerMorceau(morceau.getId());
                            refreshRef[0].run();
                        }
                    });
                });

                row.getChildren().addAll(info, btnConsulter, btnSupprimer);
                listeBox.getChildren().add(row);
            }
        };

        btnAjouter.setOnAction(e -> {
            dialogCreerMorceauDemo();
            refreshRef[0].run();
        });

        refreshRef[0].run();
        box.getChildren().addAll(titre, btnAjouter, listeBox);
        return box;
    }

    private void dialogCreerMorceauDemo() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Creer un morceau Demo");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #282828;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");

        TextField dureeField = new TextField();
        dureeField.setPromptText("Duree en secondes");

        ComboBox<Artiste> artisteBox = new ComboBox<>();
        artisteBox.getItems().setAll(catalogueService.getTousArtistes());
        artisteBox.setPromptText("Artiste");

        ComboBox<Genre> genreBox = new ComboBox<>();
        genreBox.getItems().setAll(Genre.values());
        genreBox.setValue(Genre.AUTRE);

        VBox content = new VBox(10,
                new Label("Titre"), titreField,
                new Label("Artiste"), artisteBox,
                new Label("Duree"), dureeField,
                new Label("Categorie"), genreBox);
        content.setPadding(new Insets(16));
        pane.setContent(content);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) {
                return;
            }

            String titre = titreField.getText().trim();
            String dureeTexte = dureeField.getText().trim();
            Artiste artiste = artisteBox.getValue();
            Genre genre = genreBox.getValue() == null ? Genre.AUTRE : genreBox.getValue();

            if (titre.isEmpty() || dureeTexte.isEmpty() || artiste == null) {
                Alert erreur = new Alert(Alert.AlertType.ERROR, "Titre, artiste et duree sont obligatoires.");
                erreur.showAndWait();
                return;
            }

            int duree;
            try {
                duree = Integer.parseInt(dureeTexte);
            } catch (NumberFormatException ex) {
                Alert erreur = new Alert(Alert.AlertType.ERROR, "La duree doit etre un nombre entier.");
                erreur.showAndWait();
                return;
            }

            if (duree <= 0) {
                Alert erreur = new Alert(Alert.AlertType.ERROR, "La duree doit etre positive.");
                erreur.showAndWait();
                return;
            }

            catalogueService.creerMorceau(titre, duree, artiste, null, genre);
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Morceau Demo cree.");
            ok.showAndWait();
        });
    }

    private void afficherDetailMorceauDemo(Morceau morceau) {
        String artiste = morceau.getArtistes().isEmpty() ? "?" : morceau.getArtistes().get(0).getNom();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail du morceau");
        alert.setHeaderText(morceau.getTitre());
        alert.setContentText("Artiste : " + artiste
                + "\nDuree : " + morceau.getDureeFormatee()
                + "\nCategorie : " + morceau.getGenre()
                + "\nAjout : " + morceau.getDateAjout());
        alert.showAndWait();
    }

    private void ajouterStat(GridPane grid, int row, String label, int valeur) {
        Label lblLabel = new Label(label + " :");
        lblLabel.getStyleClass().add("text-secondary");
        Label lblValeur = new Label(String.valueOf(valeur));
        lblValeur.getStyleClass().add("text-primary");
        lblValeur.setStyle("-fx-font-weight: bold;");
        grid.add(lblLabel, 0, row);
        grid.add(lblValeur, 1, row);
    }

    private VBox creerGestionUtilisateurs() {
        VBox box = new VBox(8);
        Label titre = new Label("Gestion des utilisateurs");
        titre.getStyleClass().add("text-primary");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox listeBox = new VBox(4);

        Runnable refresh = () -> {
            listeBox.getChildren().clear();
            List<Utilisateur> users = utilisateurService.getTousUtilisateurs();
            for (Utilisateur u : users) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 8, 4, 8));
                row.setStyle("-fx-background-radius: 4;");
                row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;"));
                row.setOnMouseExited(e -> row.setStyle("-fx-background-radius: 4;"));

                Label info = new Label(u.getId() + ". " + u.getNom() + " (" + u.getEmail()
                        + ") - " + u.getType());
                info.getStyleClass().add("text-primary");
                HBox.setHgrow(info, Priority.ALWAYS);

                Label statut = new Label(u.isSuspendu() ? "SUSPENDU" : "Actif");
                statut.setTextFill(u.isSuspendu() ? Color.web("#F15E6C") : Color.web("#1DB954"));
                statut.setMinWidth(80);

                if (u.getId() != utilisateur.getId()) {
                    Button btnSuspendre = new Button(u.isSuspendu() ? "Reactiver" : "Suspendre");
                    btnSuspendre.getStyleClass().add("btn-secondary");
                    btnSuspendre.setStyle("-fx-padding: 4 12 4 12; -fx-font-size: 11px;");
                    btnSuspendre.setOnAction(e -> {
                        utilisateurService.suspendreUtilisateur(u.getId());
                        statut.setText(u.isSuspendu() ? "SUSPENDU" : "Actif");
                        statut.setTextFill(u.isSuspendu() ? Color.web("#F15E6C") : Color.web("#1DB954"));
                        btnSuspendre.setText(u.isSuspendu() ? "Reactiver" : "Suspendre");
                    });
                    row.getChildren().addAll(info, statut, btnSuspendre);
                } else {
                    Label vous = new Label("(vous)");
                    vous.getStyleClass().add("text-secondary");
                    row.getChildren().addAll(info, statut, vous);
                }

                listeBox.getChildren().add(row);
            }
        };

        refresh.run();
        box.getChildren().addAll(titre, listeBox);
        return box;
    }

    private StackPane creerAvatar(String texte, String couleurFond) {
        StackPane avatar = new StackPane();
        Circle cercle = new Circle(40);
        cercle.setFill(Color.web(couleurFond));
        Label initiale = new Label(texte);
        initiale.setTextFill(Color.WHITE);
        initiale.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        avatar.getChildren().addAll(cercle, initiale);
        return avatar;
    }
}
