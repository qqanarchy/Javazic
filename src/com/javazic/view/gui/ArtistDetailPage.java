package com.javazic.view.gui;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;
import com.javazic.service.AvisService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Page detail d'un artiste pour la GUI.
 */
public class ArtistDetailPage extends VBox {

    public ArtistDetailPage(Artiste artiste,
                            List<Album> albums,
                            List<Morceau> morceaux,
                            HomePage.LectureHandler onPlay,
                            Consumer<Album> onAlbumClick,
                            Consumer<Morceau> onAddToPlaylist,
                            Utilisateur courant,
                            AvisService avisService,
                            boolean peutAjouter,
                            boolean peutNoter) {
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #121212;");

        VBox header = creerHeader(artiste, albums, morceaux);
        VBox body = new VBox(16);
        body.setPadding(new Insets(24));

        if (albums != null && !albums.isEmpty()) {
            body.getChildren().add(creerSectionAlbums(albums, onAlbumClick));
        }
        body.getChildren().add(creerSectionMorceaux(
                morceaux,
                onPlay,
                onAddToPlaylist,
                courant,
                avisService,
                peutAjouter,
                peutNoter));

        getChildren().addAll(header, body);
    }

    private VBox creerHeader(Artiste artiste, List<Album> albums, List<Morceau> morceaux) {
        VBox header = new VBox(8);
        header.setPadding(new Insets(32, 24, 24, 24));
        header.setStyle("-fx-background-color: linear-gradient(to bottom, #153326, #121212);");

        HBox content = new HBox(20);
        content.setAlignment(Pos.BOTTOM_LEFT);

        String artworkUrl = ArtworkHelper.resolveArtworkUrl(artiste);
        if (artworkUrl.isBlank() && albums != null && !albums.isEmpty()) {
            artworkUrl = ArtworkHelper.resolveArtworkUrl(albums.get(0));
        }
        if (artworkUrl.isBlank() && morceaux != null && !morceaux.isEmpty()) {
            artworkUrl = ArtworkHelper.resolveArtworkUrl(morceaux.get(0));
        }

        StackPane art = ArtworkHelper.createSquareArtwork(180, artworkUrl, "\uD83C\uDFA4", "#282828");

        VBox info = new VBox(6);
        info.setAlignment(Pos.BOTTOM_LEFT);

        Label type = new Label("ARTISTE");
        type.getStyleClass().add("text-secondary");
        type.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Label nom = new Label(artiste.getNom());
        nom.getStyleClass().add("page-title");
        nom.setStyle("-fx-font-size: 40px;");

        List<String> infos = new ArrayList<>();
        if (artiste.getPaysOrigine() != null && !artiste.getPaysOrigine().isBlank()) {
            infos.add(artiste.getPaysOrigine());
        }
        if (artiste.getDateDebut() != null) {
            infos.add(String.valueOf(artiste.getDateDebut().getYear()));
        }

        Label meta = new Label(String.join(" - ", infos));
        meta.getStyleClass().add("text-secondary");

        Label stats = new Label((morceaux == null ? 0 : morceaux.size()) + " morceaux");
        stats.getStyleClass().add("text-secondary");

        info.getChildren().addAll(type, nom);
        if (!infos.isEmpty()) {
            info.getChildren().add(meta);
        }
        info.getChildren().add(stats);

        content.getChildren().addAll(art, info);
        header.getChildren().add(content);
        return header;
    }

    private VBox creerSectionAlbums(List<Album> albums, Consumer<Album> onAlbumClick) {
        VBox section = new VBox(8);
        Label titre = new Label("Albums");
        titre.getStyleClass().add("section-title");

        FlowPane flow = new FlowPane(12, 12);
        flow.setPadding(new Insets(4, 0, 8, 0));

        for (Album album : albums) {
            VBox card = creerCarteAlbum(album);
            if (onAlbumClick != null) {
                card.setOnMouseClicked(e -> onAlbumClick.accept(album));
            }
            flow.getChildren().add(card);
        }

        section.getChildren().addAll(titre, flow);
        return section;
    }

    private VBox creerSectionMorceaux(List<Morceau> morceaux,
                                      HomePage.LectureHandler onPlay,
                                      Consumer<Morceau> onAddToPlaylist,
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
                    null,
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

    private VBox creerCarteAlbum(Album album) {
        VBox card = new VBox(6);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(170);
        card.setMaxWidth(170);

        StackPane art = ArtworkHelper.createSquareArtwork(130,
                ArtworkHelper.resolveArtworkUrl(album), "\uD83D\uDCBF", "#282828");

        Label titre = new Label(tronquer(album.getTitre(), 22));
        titre.getStyleClass().add("card-title");

        String sousTitre = album.getDateSortie() == null ? ""
                : String.valueOf(album.getDateSortie().getYear());
        Label meta = new Label(sousTitre);
        meta.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(art, titre, meta);
        return card;
    }

    private String tronquer(String texte, int max) {
        if (texte == null) {
            return "";
        }
        return texte.length() > max ? texte.substring(0, max - 1) + "\u2026" : texte;
    }
}
