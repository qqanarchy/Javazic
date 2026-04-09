package com.javazic.view.gui;

import com.javazic.model.*;
import com.javazic.service.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Map;

/**
 * Page profil utilisateur + panneau admin (stats, gestion utilisateurs, catalogue).
 */
public class ProfilePage extends VBox {

    private final Utilisateur utilisateur;
    private final UtilisateurService utilisateurService;
    private final StatistiquesService statistiquesService;
    private final AvisService avisService;
    private final CatalogueService catalogueService;

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

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        getChildren().add(creerSectionProfil());
        getChildren().add(creerSectionHistorique());

        if (utilisateur.estAdmin()) {
            getChildren().add(creerSectionAdmin());
        }
    }

    // ========== Profil ==========

    private VBox creerSectionProfil() {
        VBox section = new VBox(16);

        HBox headerRow = new HBox(20);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        Circle cercle = new Circle(40);
        cercle.setFill(Color.web("#1DB954"));
        Label initiale = new Label(utilisateur.getNom().substring(0, 1).toUpperCase());
        initiale.setTextFill(Color.WHITE);
        initiale.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
        avatar.getChildren().addAll(cercle, initiale);

        VBox infoUser = new VBox(4);
        Label nom = new Label(utilisateur.getNom());
        nom.getStyleClass().add("page-title");

        Label details = new Label(utilisateur.getEmail() + "  |  " + utilisateur.getType()
                + "  |  Inscrit le " + utilisateur.getDateInscription());
        details.getStyleClass().add("text-secondary");

        infoUser.getChildren().addAll(nom, details);
        headerRow.getChildren().addAll(avatar, infoUser);

        // Edit fields
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

            if (okNom) utilisateurService.modifierProfil(utilisateur.getId(), newNom, null);
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

        // Password change
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

    // ========== Historique ==========

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

    // ========== Admin ==========

    private VBox creerSectionAdmin() {
        VBox section = new VBox(16);

        Label titre = new Label("Administration");
        titre.getStyleClass().add("section-title");
        titre.setStyle("-fx-text-fill: #F0A500; -fx-font-size: 22px;");

        // Stats
        VBox stats = creerStats();

        // User management
        VBox gestionUsers = creerGestionUtilisateurs();

        section.getChildren().addAll(titre, stats, gestionUsers);
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

        // Top morceaux
        VBox topBox = new VBox(4);
        Label lblTop = new Label("Top 5 morceaux");
        lblTop.getStyleClass().add("text-primary");
        lblTop.setStyle("-fx-font-weight: bold;");
        topBox.getChildren().add(lblTop);

        List<Morceau> top = statistiquesService.getTopMorceaux(5);
        int rang = 1;
        for (Morceau m : top) {
            String artiste = m.getArtistes().isEmpty() ? "?" : m.getArtistes().get(0).getNom();
            Label l = new Label(rang++ + ". " + m.getTitre() + " - " + artiste
                    + " (" + m.getNombreEcoute() + " ecoutes)");
            l.getStyleClass().add("text-secondary");
            topBox.getChildren().add(l);
        }

        box.getChildren().addAll(titre, grid, topBox);
        return box;
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
}
