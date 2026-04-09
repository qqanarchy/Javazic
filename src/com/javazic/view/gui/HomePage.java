package com.javazic.view.gui;

import com.javazic.model.Morceau;
import com.javazic.service.AppleItunesService;
import com.javazic.service.CatalogueService;
import com.javazic.service.JamendoService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Page d'accueil avec tendances Jamendo, Apple et catalogue demo.
 */
public class HomePage extends VBox {

    @FunctionalInterface
    public interface LectureHandler {
        void lancer(List<Morceau> morceaux, int index);
    }

    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final CatalogueService catalogueService;
    private final LectureHandler onPlay;
    private final Consumer<Morceau> onAddToPlaylist;
    private final boolean peutAjouter;

    public HomePage(JamendoService jamendoService,
                    AppleItunesService appleItunesService,
                    CatalogueService catalogueService,
                    LectureHandler onPlay,
                    Consumer<Morceau> onAddToPlaylist,
                    boolean peutAjouter) {
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.catalogueService = catalogueService;
        this.onPlay = onPlay;
        this.onAddToPlaylist = onAddToPlaylist;
        this.peutAjouter = peutAjouter;

        setSpacing(8);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        Label titre = new Label("Bienvenue sur Javazic");
        titre.getStyleClass().add("page-title");
        getChildren().add(titre);

        List<Morceau> demo = catalogueService.getTousMorceaux();
        if (!demo.isEmpty()) {
            getChildren().add(creerSection("Catalogue Demo", demo));
        }

        VBox sectionJamendo = creerSectionAsync("Tendances Jamendo");
        getChildren().add(sectionJamendo);
        chargerAsync(() -> jamendoService.getTitresTendance(10), sectionJamendo, "Jamendo");

        VBox sectionApple = creerSectionAsync("Tendances Apple");
        getChildren().add(sectionApple);
        chargerAsync(() -> appleItunesService.getTitresTendance(10), sectionApple, "Apple");
    }

    private VBox creerSection(String titre, List<Morceau> morceaux) {
        VBox section = new VBox(4);
        Label lblTitre = new Label(titre);
        lblTitre.getStyleClass().add("section-title");
        section.getChildren().add(lblTitre);

        FlowPane cards = creerGrilleMorceaux(morceaux);
        section.getChildren().add(cards);
        return section;
    }

    private VBox creerSectionAsync(String titre) {
        VBox section = new VBox(8);
        Label lblTitre = new Label(titre);
        lblTitre.getStyleClass().add("section-title");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(30, 30);
        spinner.setStyle("-fx-progress-color: #1DB954;");

        Label chargement = new Label("Chargement...");
        chargement.getStyleClass().add("text-secondary");
        HBox loading = new HBox(8, spinner, chargement);
        loading.setAlignment(Pos.CENTER_LEFT);

        section.getChildren().addAll(lblTitre, loading);
        return section;
    }

    private void chargerAsync(Callable<List<Morceau>> loader, VBox section, String provider) {
        Thread worker = new Thread(() -> {
            try {
                List<Morceau> morceaux = loader.call();
                Platform.runLater(() -> {
                    if (section.getChildren().size() > 1) {
                        section.getChildren().remove(1);
                    }
                    if (morceaux == null || morceaux.isEmpty()) {
                        Label vide = new Label("Aucune tendance disponible pour " + provider + ".");
                        vide.getStyleClass().add("text-secondary");
                        section.getChildren().add(vide);
                    } else {
                        section.getChildren().add(creerGrilleMorceaux(morceaux));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (section.getChildren().size() > 1) {
                        section.getChildren().remove(1);
                    }
                    Label erreur = new Label("Erreur de chargement " + provider + ".");
                    erreur.getStyleClass().add("text-error");
                    section.getChildren().add(erreur);
                });
            }
        }, "javazic-home-" + provider.toLowerCase());
        worker.setDaemon(true);
        worker.start();
    }

    private FlowPane creerGrilleMorceaux(List<Morceau> morceaux) {
        FlowPane flow = new FlowPane(12, 12);
        flow.setPadding(new Insets(4, 0, 8, 0));

        for (int i = 0; i < morceaux.size(); i++) {
            Morceau morceau = morceaux.get(i);
            int index = i;
            VBox card = creerCarteMorceau(morceau);
            card.setOnMouseClicked(e -> onPlay.lancer(morceaux, index));
            flow.getChildren().add(card);
        }
        return flow;
    }

    private VBox creerCarteMorceau(Morceau morceau) {
        VBox card = new VBox(6);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(160);
        card.setMaxWidth(160);

        StackPane art = ArtworkHelper.createSquareArtwork(
                130,
                ArtworkHelper.resolveArtworkUrl(morceau),
                "\u266B",
                "#333333");

        if (morceau.getSource().estDistant()) {
            Label tag = new Label(morceau.getSource().getTag());
            tag.setTextFill(javafx.scene.paint.Color.web("#1DB954"));
            tag.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; "
                    + "-fx-background-color: rgba(29,185,84,0.18); "
                    + "-fx-padding: 1 4 1 4; -fx-background-radius: 2;");
            StackPane.setAlignment(tag, Pos.TOP_RIGHT);
            StackPane.setMargin(tag, new Insets(4));
            art.getChildren().add(tag);
        }

        Label playIcon = new Label("\u25B6");
        playIcon.setTextFill(javafx.scene.paint.Color.WHITE);
        playIcon.setStyle("-fx-background-color: #1DB954; -fx-background-radius: 50; "
                + "-fx-padding: 8 12 8 14; -fx-opacity: 0;");
        StackPane.setAlignment(playIcon, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(playIcon, new Insets(0, 8, 8, 0));
        art.getChildren().add(playIcon);

        if (peutAjouter && onAddToPlaylist != null) {
            Button btnAjouter = new Button("+");
            btnAjouter.getStyleClass().add("track-add-btn");
            btnAjouter.setOnAction(e -> onAddToPlaylist.accept(morceau));
            btnAjouter.setOnMouseClicked(e -> e.consume());
            StackPane.setAlignment(btnAjouter, Pos.TOP_LEFT);
            StackPane.setMargin(btnAjouter, new Insets(6, 0, 0, 6));
            art.getChildren().add(btnAjouter);
        }

        art.setOnMouseEntered(e -> playIcon.setStyle(playIcon.getStyle().replace("-fx-opacity: 0", "-fx-opacity: 1")));
        art.setOnMouseExited(e -> playIcon.setStyle(playIcon.getStyle().replace("-fx-opacity: 1", "-fx-opacity: 0")));

        Label lblTitre = new Label(tronquer(morceau.getTitre(), 22));
        lblTitre.getStyleClass().add("card-title");

        String artiste = morceau.getArtistes().isEmpty() ? "Inconnu" : morceau.getArtistes().get(0).getNom();
        Label lblArtiste = new Label(tronquer(artiste, 22));
        lblArtiste.getStyleClass().add("card-subtitle");

        Label lblDuree = new Label(morceau.getDureeFormatee());
        lblDuree.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(art, lblTitre, lblArtiste, lblDuree);
        return card;
    }

    private String tronquer(String texte, int max) {
        if (texte == null) {
            return "";
        }
        return texte.length() > max ? texte.substring(0, max - 1) + "\u2026" : texte;
    }
}
