package com.javazic.view.gui;

import com.javazic.model.Playlist;
import com.javazic.model.Utilisateur;
import com.javazic.service.PlaylistService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.Consumer;

/**
 * Page listant les playlists de l'utilisateur avec creation.
 */
public class PlaylistsPage extends VBox {

    private final PlaylistService playlistService;
    private final Utilisateur utilisateur;
    private final Consumer<Playlist> onPlaylistClick;
    private final Runnable onRefreshSidebar;
    private final FlowPane playlistGrid;

    public PlaylistsPage(PlaylistService playlistService,
                         Utilisateur utilisateur,
                         Consumer<Playlist> onPlaylistClick,
                         Runnable onRefreshSidebar) {
        this.playlistService = playlistService;
        this.utilisateur = utilisateur;
        this.onPlaylistClick = onPlaylistClick;
        this.onRefreshSidebar = onRefreshSidebar;

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("Mes Playlists");
        titre.getStyleClass().add("page-title");
        HBox.setHgrow(titre, Priority.ALWAYS);

        Button btnCreer = new Button("+ Nouvelle playlist");
        btnCreer.getStyleClass().add("btn-primary");
        btnCreer.setOnAction(e -> dialogCreerPlaylist());

        header.getChildren().addAll(titre, btnCreer);

        // Playlists publiques
        Label lblPubliques = new Label("Playlists publiques");
        lblPubliques.getStyleClass().add("section-title");

        FlowPane publiquesGrid = new FlowPane(12, 12);
        publiquesGrid.setPadding(new Insets(4, 0, 8, 0));

        List<Playlist> publiques = playlistService.getPlaylistsPubliques();
        for (Playlist p : publiques) {
            publiquesGrid.getChildren().add(creerCartePlaylist(p));
        }

        // Mes playlists
        playlistGrid = new FlowPane(12, 12);
        playlistGrid.setPadding(new Insets(4, 0, 8, 0));

        rafraichirPlaylists();

        getChildren().addAll(header, playlistGrid);

        if (!publiques.isEmpty()) {
            getChildren().addAll(lblPubliques, publiquesGrid);
        }
    }

    private void rafraichirPlaylists() {
        playlistGrid.getChildren().clear();
        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateur.getId());

        if (playlists.isEmpty()) {
            Label vide = new Label("Vous n'avez aucune playlist. Creez-en une !");
            vide.getStyleClass().add("text-secondary");
            vide.setStyle("-fx-font-size: 16px;");
            playlistGrid.getChildren().add(vide);
        } else {
            for (Playlist p : playlists) {
                playlistGrid.getChildren().add(creerCartePlaylist(p));
            }
        }
    }

    private VBox creerCartePlaylist(Playlist playlist) {
        VBox card = new VBox(8);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(180);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Art placeholder
        StackPane art = new StackPane();
        art.setPrefSize(150, 150);
        art.setMaxSize(150, 150);

        Rectangle bg = new Rectangle(150, 150);
        bg.setFill(Color.web(playlist.isEstPublique() ? "#1a3a2a" : "#2a2a3a"));
        bg.setArcWidth(8);
        bg.setArcHeight(8);

        Label icon = new Label("\u266B");
        icon.setTextFill(Color.web(playlist.isEstPublique() ? "#1DB954" : "#7B68EE"));
        icon.setFont(Font.font(40));

        art.getChildren().addAll(bg, icon);

        Label nom = new Label(playlist.getNom());
        nom.getStyleClass().add("card-title");

        String desc = playlist.getNombreMorceaux() + " morceaux - "
                + playlist.getDureeTotaleFormatee();
        Label lblDesc = new Label(desc);
        lblDesc.getStyleClass().add("card-subtitle");

        Label visibilite = new Label(playlist.isEstPublique() ? "Publique" : "Privee");
        visibilite.getStyleClass().add("card-subtitle");
        visibilite.setStyle("-fx-text-fill: " + (playlist.isEstPublique() ? "#1DB954" : "#B3B3B3") + ";");

        card.getChildren().addAll(art, nom, lblDesc, visibilite);
        card.setOnMouseClicked(e -> onPlaylistClick.accept(playlist));

        return card;
    }

    private void dialogCreerPlaylist() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle playlist");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #282828;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nomField = new TextField();
        nomField.setPromptText("Nom de la playlist");

        TextField descField = new TextField();
        descField.setPromptText("Description (optionnel)");

        CheckBox cbPublique = new CheckBox("Playlist publique");
        cbPublique.setTextFill(Color.WHITE);

        VBox content = new VBox(12, nomField, descField, cbPublique);
        content.setPadding(new Insets(16));
        pane.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new String[]{nomField.getText(), descField.getText(),
                        cbPublique.isSelected() ? "true" : "false"};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String nom = result[0].trim();
            if (nom.isEmpty()) return;

            Playlist p = playlistService.creerPlaylist(nom, result[1].trim(), utilisateur);
            if ("true".equals(result[2])) {
                playlistService.changerVisibilite(p.getId(), true);
            }
            rafraichirPlaylists();
            onRefreshSidebar.run();
        });
    }
}
