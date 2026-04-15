package com.javazic.view.gui;

import com.javazic.model.Morceau;
import com.javazic.model.Playlist;
import com.javazic.model.Utilisateur;
import com.javazic.service.AppleItunesService;
import com.javazic.service.AvisService;
import com.javazic.service.JamendoService;
import com.javazic.service.PlaylistService;
import com.javazic.service.RechercheService;
import com.javazic.service.ResultContextService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Page detail d'une playlist avec gestion des morceaux.
 */
public class PlaylistDetailPage extends VBox {

    private final Playlist playlist;
    private final PlaylistService playlistService;
    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final RechercheService rechercheService;
    private final AvisService avisService;
    private final ResultContextService resultContextService;
    private final Utilisateur utilisateur;
    private final HomePage.LectureHandler onPlay;
    private final Runnable onRefreshSidebar;
    private final Runnable onDeleted;

    private final VBox trackListContainer;

    public PlaylistDetailPage(Playlist playlist,
                              PlaylistService playlistService,
                              JamendoService jamendoService,
                              AppleItunesService appleItunesService,
                              RechercheService rechercheService,
                              AvisService avisService,
                              ResultContextService resultContextService,
                              Utilisateur utilisateur,
                              HomePage.LectureHandler onPlay,
                              Runnable onRefreshSidebar,
                              Runnable onDeleted) {
        this.playlist = playlist;
        this.playlistService = playlistService;
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.rechercheService = rechercheService;
        this.avisService = avisService;
        this.resultContextService = resultContextService;
        this.utilisateur = utilisateur;
        this.onPlay = onPlay;
        this.onRefreshSidebar = onRefreshSidebar;
        this.onDeleted = onDeleted;

        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #121212;");

        VBox header = creerHeader();
        trackListContainer = new VBox(0);
        VBox body = new VBox(16);
        body.setPadding(new Insets(24));
        body.getChildren().addAll(creerBarreActions(), trackListContainer);

        rafraichirListeMorceaux();
        getChildren().addAll(header, body);
    }

    private VBox creerHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(32, 24, 24, 24));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #1a3a2a, #121212);");

        HBox headerContent = new HBox(20);
        headerContent.setAlignment(Pos.BOTTOM_LEFT);

        StackPane art = new StackPane();
        art.setPrefSize(180, 180);
        art.setMinSize(180, 180);

        Rectangle bg = new Rectangle(180, 180);
        bg.setFill(Color.web("#282828"));
        bg.setArcWidth(8);
        bg.setArcHeight(8);

        Label icon = new Label("\u266B");
        icon.setTextFill(Color.web("#1DB954"));
        icon.setFont(Font.font(60));

        art.getChildren().addAll(bg, icon);

        VBox info = new VBox(4);
        info.setAlignment(Pos.BOTTOM_LEFT);

        Label typeLabel = new Label("PLAYLIST");
        typeLabel.getStyleClass().add("text-secondary");
        typeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Label nom = new Label(playlist.getNom());
        nom.getStyleClass().add("page-title");
        nom.setStyle("-fx-font-size: 40px;");

        String desc = playlist.getDescription() != null && !playlist.getDescription().isEmpty()
                ? playlist.getDescription() : "";
        Label lblDesc = new Label(desc);
        lblDesc.getStyleClass().add("text-secondary");

        String stats = playlist.getNombreMorceaux() + " morceaux, "
                + playlist.getDureeTotaleFormatee()
                + " - " + (playlist.isEstPublique() ? "Publique" : "Privee");
        Label lblStats = new Label(stats);
        lblStats.getStyleClass().add("text-secondary");

        info.getChildren().addAll(typeLabel, nom);
        if (!desc.isEmpty()) {
            info.getChildren().add(lblDesc);
        }
        info.getChildren().add(lblStats);

        headerContent.getChildren().addAll(art, info);
        header.getChildren().add(headerContent);
        return header;
    }

    private HBox creerBarreActions() {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnPlay = new Button("\u25B6  Lecture");
        btnPlay.getStyleClass().add("btn-primary");
        btnPlay.setStyle("-fx-font-size: 16px; -fx-padding: 12 36 12 36;");
        btnPlay.setOnAction(e -> {
            List<Morceau> morceaux = playlist.getMorceaux();
            if (!morceaux.isEmpty()) {
                onPlay.lancer(new ArrayList<>(morceaux), 0);
            }
        });

        Button btnAjouter = new Button("+ Ajouter");
        btnAjouter.getStyleClass().add("btn-secondary");
        btnAjouter.setOnAction(e -> dialogAjouterMorceau());

        boolean estProprietaire = utilisateur != null
                && playlist.getProprietaire().getId() == utilisateur.getId();

        actions.getChildren().addAll(btnPlay, btnAjouter);

        if (estProprietaire) {
            Button btnVisibilite = new Button(
                    playlist.isEstPublique() ? "Rendre privee" : "Rendre publique");
            btnVisibilite.getStyleClass().add("btn-secondary");
            btnVisibilite.setOnAction(e -> {
                playlistService.changerVisibilite(playlist.getId(), !playlist.isEstPublique());
                btnVisibilite.setText(playlist.isEstPublique() ? "Rendre privee" : "Rendre publique");
                onRefreshSidebar.run();
            });

            Button btnRenommer = new Button("Renommer");
            btnRenommer.getStyleClass().add("btn-secondary");
            btnRenommer.setOnAction(e -> dialogRenommer());

            Button btnSupprimer = new Button("Supprimer la playlist");
            btnSupprimer.getStyleClass().add("btn-secondary");
            btnSupprimer.setStyle("-fx-text-fill: #F15E6C; -fx-border-color: #F15E6C;");
            btnSupprimer.setOnAction(e -> supprimerPlaylist());

            actions.getChildren().addAll(btnVisibilite, btnRenommer, btnSupprimer);
        }

        return actions;
    }

    private void rafraichirListeMorceaux() {
        trackListContainer.getChildren().clear();
        List<Morceau> morceaux = playlist.getMorceaux();

        if (morceaux.isEmpty()) {
            Label vide = new Label("Cette playlist est vide. Ajoutez des morceaux !");
            vide.getStyleClass().add("text-secondary");
            vide.setStyle("-fx-font-size: 16px; -fx-padding: 16 0 0 0;");
            trackListContainer.getChildren().add(vide);
            return;
        }

        boolean estProprietaire = utilisateur != null
                && playlist.getProprietaire().getId() == utilisateur.getId();

        trackListContainer.getChildren().add(TrackListComponents.creerHeader(false, utilisateur != null, estProprietaire));

        for (int i = 0; i < morceaux.size(); i++) {
            Morceau morceau = morceaux.get(i);
            int index = i;
            HBox row = TrackListComponents.creerLigne(
                    morceau,
                    i + 1,
                    false,
                    utilisateur != null,
                    avisService,
                    utilisateur,
                    () -> onPlay.lancer(new ArrayList<>(morceaux), index),
                    null,
                    null);

            if (estProprietaire) {
                Button btnRetirer = new Button("\u2715");
                btnRetirer.getStyleClass().add("player-btn");
                btnRetirer.setStyle("-fx-text-fill: #F15E6C; -fx-font-size: 12px;");
                btnRetirer.setMinWidth(40);
                btnRetirer.setOnAction(e -> {
                    e.consume();
                    playlistService.retirerMorceauDePlaylist(playlist.getId(), morceau.getId());
                    rafraichirListeMorceaux();
                    onRefreshSidebar.run();
                });
                row.getChildren().add(btnRetirer);
            }

            trackListContainer.getChildren().add(row);
        }
    }

    private void supprimerPlaylist() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la playlist");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer \"" + playlist.getNom() + "\" ?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK
                    && utilisateur != null
                    && playlistService.supprimerPlaylist(playlist.getId(), utilisateur.getId())) {
                onRefreshSidebar.run();
                if (onDeleted != null) {
                    onDeleted.run();
                }
            }
        });
    }

    private void dialogAjouterMorceau() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un morceau");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #282828; -fx-min-width: 500;");
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un morceau...");
        searchField.getStyleClass().add("search-field");

        VBox resultats = new VBox(4);
        Label info = new Label("Recherchez dans Demo, Jamendo et Apple iTunes");
        info.getStyleClass().add("text-secondary");
        resultats.getChildren().add(info);

        List<Morceau> derniers = resultContextService.getDerniersMorceaux();
        if (!derniers.isEmpty()) {
            Label lblDerniers = new Label("Derniers resultats (" + derniers.size() + ")");
            lblDerniers.getStyleClass().add("text-secondary");
            lblDerniers.setStyle("-fx-font-weight: bold;");
            resultats.getChildren().add(lblDerniers);
            for (Morceau morceau : derniers) {
                resultats.getChildren().add(creerLigneAjout(morceau));
            }
        }

        searchField.setOnAction(e -> {
            String motCle = searchField.getText().trim();
            if (motCle.isEmpty()) {
                return;
            }

            resultats.getChildren().clear();
            Label loading = new Label("Recherche...");
            loading.getStyleClass().add("text-secondary");
            resultats.getChildren().add(loading);

            Thread worker = new Thread(() -> {
                List<Morceau> morceaux = new ArrayList<>();
                morceaux.addAll(rechercheService.rechercherMorceaux(motCle));
                try {
                    morceaux.addAll(jamendoService.rechercherMorceaux(motCle));
                } catch (Exception ignored) {
                }
                try {
                    morceaux.addAll(appleItunesService.rechercherMorceaux(motCle));
                } catch (Exception ignored) {
                }

                int limit = Math.min(morceaux.size(), 15);
                List<Morceau> limited = morceaux.subList(0, limit);

                Platform.runLater(() -> {
                    resultats.getChildren().clear();
                    if (limited.isEmpty()) {
                        Label vide = new Label("Aucun resultat.");
                        vide.getStyleClass().add("text-secondary");
                        resultats.getChildren().add(vide);
                    } else {
                        for (Morceau morceau : limited) {
                            resultats.getChildren().add(creerLigneAjout(morceau));
                        }
                    }
                });
            }, "javazic-playlist-search");
            worker.setDaemon(true);
            worker.start();
        });

        ScrollPane scroll = new ScrollPane(resultats);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.setStyle("-fx-background-color: transparent;");

        content.getChildren().addAll(searchField, scroll);
        pane.setContent(content);
        dialog.showAndWait();
    }

    private HBox creerLigneAjout(Morceau morceau) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));
        row.setStyle("-fx-background-radius: 4;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 4;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-radius: 4;"));

        HBox infoBox = new HBox(6);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        if (morceau.getSource().estDistant()) {
            Label tag = new Label(morceau.getSource().getTag());
            tag.getStyleClass().add("track-source-tag");
            infoBox.getChildren().add(tag);
        }

        Label titre = new Label(morceau.getTitre());
        titre.getStyleClass().add("text-primary");

        String artiste = morceau.getArtistes().isEmpty() ? "" : " - " + morceau.getArtistes().get(0).getNom();
        Label lblArtiste = new Label(artiste);
        lblArtiste.getStyleClass().add("text-secondary");

        infoBox.getChildren().addAll(titre, lblArtiste);

        Button btnAjouter = new Button("+");
        btnAjouter.getStyleClass().add("btn-primary");
        btnAjouter.setStyle("-fx-padding: 4 12 4 12; -fx-font-size: 12px;");
        btnAjouter.setOnAction(e -> {
            boolean ok = playlistService.ajouterMorceauAPlaylist(playlist.getId(), morceau);
            if (ok) {
                btnAjouter.setText("\u2713");
                btnAjouter.setDisable(true);
                rafraichirListeMorceaux();
                onRefreshSidebar.run();
            } else {
                btnAjouter.setText("Deja");
                btnAjouter.setDisable(true);
            }
        });

        row.getChildren().addAll(infoBox, btnAjouter);
        return row;
    }

    private void dialogRenommer() {
        TextInputDialog dialog = new TextInputDialog(playlist.getNom());
        dialog.setTitle("Renommer la playlist");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau nom :");
        dialog.showAndWait().ifPresent(nom -> {
            if (!nom.trim().isEmpty()) {
                playlistService.renommerPlaylist(playlist.getId(), nom.trim());
                onRefreshSidebar.run();
                getChildren().clear();
                VBox header = creerHeader();
                VBox body = new VBox(16);
                body.setPadding(new Insets(24));
                body.getChildren().addAll(creerBarreActions(), trackListContainer);
                getChildren().addAll(header, body);
            }
        });
    }
}
