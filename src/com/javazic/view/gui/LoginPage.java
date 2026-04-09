package com.javazic.view.gui;

import com.javazic.model.TypeUtilisateur;
import com.javazic.model.Utilisateur;
import com.javazic.service.UtilisateurService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Page de connexion / inscription, style Spotify.
 */
public class LoginPage extends StackPane {

    private final UtilisateurService utilisateurService;
    private final Consumer<Utilisateur> onLogin;
    private final Runnable onVisiteur;

    private final VBox cardContent;
    private final Label lblErreur;

    public LoginPage(UtilisateurService utilisateurService,
                     Consumer<Utilisateur> onLogin,
                     Runnable onVisiteur) {
        this.utilisateurService = utilisateurService;
        this.onLogin = onLogin;
        this.onVisiteur = onVisiteur;

        setStyle("-fx-background-color: #121212;");
        setAlignment(Pos.CENTER);

        cardContent = new VBox(16);
        cardContent.getStyleClass().add("login-card");
        cardContent.setMaxWidth(400);
        cardContent.setAlignment(Pos.CENTER);

        lblErreur = new Label();
        lblErreur.getStyleClass().add("text-error");
        lblErreur.setVisible(false);
        lblErreur.setWrapText(true);

        afficherFormulaireConnexion();
        getChildren().add(cardContent);
    }

    private void afficherFormulaireConnexion() {
        cardContent.getChildren().clear();

        Label titre = new Label("JAVAZIC");
        titre.getStyleClass().add("sidebar-logo");
        titre.setStyle("-fx-font-size: 36px; -fx-text-fill: #1DB954;");

        Label sousTitre = new Label("Connectez-vous pour continuer");
        sousTitre.getStyleClass().add("login-subtitle");

        Region sep1 = new Region();
        sep1.getStyleClass().add("login-separator");
        VBox.setMargin(sep1, new Insets(8, 0, 8, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field");

        PasswordField mdpField = new PasswordField();
        mdpField.setPromptText("Mot de passe");
        mdpField.getStyleClass().add("password-field");

        Button btnConnexion = new Button("Se connecter");
        btnConnexion.getStyleClass().add("btn-primary");
        btnConnexion.setMaxWidth(Double.MAX_VALUE);

        Button btnAdmin = new Button("Connexion administrateur");
        btnAdmin.getStyleClass().add("btn-secondary");
        btnAdmin.setMaxWidth(Double.MAX_VALUE);

        Region sep2 = new Region();
        sep2.getStyleClass().add("login-separator");
        VBox.setMargin(sep2, new Insets(4, 0, 4, 0));

        Label lblInscription = new Label("Pas de compte ? Inscrivez-vous");
        lblInscription.getStyleClass().add("login-link");

        Label lblVisiteur = new Label("Continuer en tant que visiteur");
        lblVisiteur.getStyleClass().add("login-link");
        lblVisiteur.setStyle("-fx-text-fill: #B3B3B3;");

        HBox liens = new HBox(20, lblInscription, lblVisiteur);
        liens.setAlignment(Pos.CENTER);

        // Actions
        btnConnexion.setOnAction(e -> tenterConnexion(emailField.getText(), mdpField.getText(), false));
        btnAdmin.setOnAction(e -> tenterConnexion(emailField.getText(), mdpField.getText(), true));
        mdpField.setOnAction(e -> tenterConnexion(emailField.getText(), mdpField.getText(), false));
        lblInscription.setOnMouseClicked(e -> afficherFormulaireInscription());
        lblVisiteur.setOnMouseClicked(e -> onVisiteur.run());

        lblErreur.setVisible(false);

        cardContent.getChildren().addAll(titre, sousTitre, sep1,
                emailField, mdpField, lblErreur,
                btnConnexion, btnAdmin, sep2, liens);
    }

    private void afficherFormulaireInscription() {
        cardContent.getChildren().clear();

        Label titre = new Label("Creer un compte");
        titre.getStyleClass().add("login-title");

        Label sousTitre = new Label("Rejoignez Javazic gratuitement");
        sousTitre.getStyleClass().add("login-subtitle");

        Region sep = new Region();
        sep.getStyleClass().add("login-separator");
        VBox.setMargin(sep, new Insets(8, 0, 8, 0));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField mdpField = new PasswordField();
        mdpField.setPromptText("Mot de passe");

        Button btnInscrire = new Button("S'inscrire");
        btnInscrire.getStyleClass().add("btn-primary");
        btnInscrire.setMaxWidth(Double.MAX_VALUE);

        Label lblRetour = new Label("Deja un compte ? Se connecter");
        lblRetour.getStyleClass().add("login-link");

        btnInscrire.setOnAction(e -> {
            String nom = nomField.getText().trim();
            String email = emailField.getText().trim();
            String mdp = mdpField.getText().trim();

            if (nom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                afficherErreur("Tous les champs sont obligatoires.");
                return;
            }

            Utilisateur u = utilisateurService.inscrire(nom, email, mdp, TypeUtilisateur.STANDARD);
            if (u != null) {
                afficherFormulaireConnexion();
                lblErreur.setTextFill(Color.web("#1DB954"));
                lblErreur.setText("Compte cree ! Connectez-vous.");
                lblErreur.setVisible(true);
            } else {
                afficherErreur("Cet email est deja utilise.");
            }
        });

        lblRetour.setOnMouseClicked(e -> afficherFormulaireConnexion());

        lblErreur.setVisible(false);

        cardContent.getChildren().addAll(titre, sousTitre, sep,
                nomField, emailField, mdpField, lblErreur,
                btnInscrire, lblRetour);
    }

    private void tenterConnexion(String email, String mdp, boolean admin) {
        email = email.trim();
        mdp = mdp.trim();

        if (email.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur u = utilisateurService.connexion(email, mdp);
        if (u == null) {
            afficherErreur("Email ou mot de passe incorrect.");
            return;
        }
        if (u.isSuspendu()) {
            afficherErreur("Ce compte est suspendu.");
            return;
        }
        if (admin && !u.estAdmin()) {
            afficherErreur("Ce compte n'est pas administrateur.");
            return;
        }

        onLogin.accept(u);
    }

    private void afficherErreur(String message) {
        lblErreur.setTextFill(Color.web("#F15E6C"));
        lblErreur.setText(message);
        lblErreur.setVisible(true);
    }
}
