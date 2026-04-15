package com.javazic.view.gui;

import com.javazic.model.Album;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;
import com.javazic.service.AvisService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Page detail d'un album pour la GUI.
 */
public class AlbumDetailPage extends VBox {

    public AlbumDetailPage(Album album,
                           List<Morceau> morceaux,
                           HomePage.LectureHandler onPlay,
                           Runnable onArtistClick,
                           java.util.function.Consumer<Morceau> onAddToPlaylist,
                           Utilisateur courant,
                           AvisService avisService,
                           boolean peutAjouter,
                           boolean peutNoter) {
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #121212;");

        VBox header = creerHeader(album, morceaux, onArtistClick);
        VBox body = new VBox(16);
        body.setPadding(new Insets(24));
        body.getChildren().add(creerSectionMorceaux(
                morceaux,
                onPlay,
                onArtistClick,
                onAddToPlaylist,
                courant,
                avisService,
                peutAjouter,
                peutNoter));

        getChildren().addAll(header, body);
    }

    private VBox creerHeader(Album album, List<Morceau> morceaux, Runnable onArtistClick) {
        VBox header = new VBox(8);
        header.setPadding(new Insets(32, 24, 24, 24));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #1b2f3b, #121212);");

        HBox content = new HBox(20);
        content.setAlignment(Pos.BOTTOM_LEFT);

        String artworkUrl = ArtworkHelper.resolveArtworkUrl(album);
        if (artworkUrl.isBlank() && morceaux != null && !morceaux.isEmpty()) {
            artworkUrl = ArtworkHelper.resolveArtworkUrl(morceaux.get(0));
        }

        StackPane art = ArtworkHelper.createSquareArtwork(180, artworkUrl, "\uD83D\uDCBF", "#282828");

        VBox info = new VBox(6);
        info.setAlignment(Pos.BOTTOM_LEFT);

        Label type = new Label("ALBUM");
        type.getStyleClass().add("text-secondary");
        type.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Label titre = new Label(album.getTitre());
        titre.getStyleClass().add("page-title");
        titre.setStyle("-fx-font-size: 40px;");

        Label artiste = new Label(album.getArtiste() == null ? "Inconnu" : album.getArtiste().getNom());
        artiste.getStyleClass().add(onArtistClick == null ? "text-secondary" : "track-artist-link");
        artiste.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        if (onArtistClick != null) {
            artiste.setCursor(Cursor.HAND);
            artiste.setOnMouseClicked(e -> onArtistClick.run());
        }

        List<String> infos = new ArrayList<>();
        if (album.getGenre() != null && album.getGenre().getLibelle() != null
                && !album.getGenre().getLibelle().isBlank()) {
            infos.add(album.getGenre().getLibelle());
        }
        if (album.getDateSortie() != null) {
            infos.add(String.valueOf(album.getDateSortie().getYear()));
        }
        infos.add((morceaux == null ? 0 : morceaux.size()) + " morceaux");

        Label meta = new Label(String.join(" - ", infos));
        meta.getStyleClass().add("text-secondary");

        info.getChildren().addAll(type, titre, artiste, meta);
        content.getChildren().addAll(art, info);
        header.getChildren().add(content);
        return header;
    }

    private VBox creerSectionMorceaux(List<Morceau> morceaux,
                                      HomePage.LectureHandler onPlay,
                                      Runnable onArtistClick,
                                      java.util.function.Consumer<Morceau> onAddToPlaylist,
                                      Utilisateur courant,
                                      AvisService avisService,
                                      boolean peutAjouter,
                                      boolean peutNoter) {
        VBox section = new VBox(4);
        Label titre = new Label("Morceaux");
        titre.getStyleClass().add("section-title");
        section.getChildren().addAll(titre, TrackListComponents.creerHeader(peutAjouter, peutNoter));

        if (morceaux == null || morceaux.isEmpty()) {
            Label vide = new Label("Aucun morceau disponible.");
            vide.getStyleClass().add("text-secondary");
            section.getChildren().add(vide);
            return section;
        }

        List<Morceau> lecture = new ArrayList<>(morceaux);
        for (int i = 0; i < lecture.size(); i++) {
            Morceau morceau = lecture.get(i);
            int index = i;
            HBox row = TrackListComponents.creerLigne(
                    morceau,
                    i + 1,
                    peutAjouter,
                    peutNoter,
                    avisService,
                    courant,
                    () -> onPlay.lancer(lecture, index),
                    onArtistClick,
                    () -> {
                        if (onAddToPlaylist != null) {
                            onAddToPlaylist.accept(morceau);
                        }
                    });
            HBox.setHgrow(row, Priority.ALWAYS);
            section.getChildren().add(row);
        }
        return section;
    }
}
