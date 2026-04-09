package com.javazic.view.gui;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;
import com.javazic.service.AppleItunesService;
import com.javazic.service.JamendoService;
import com.javazic.service.ProviderSearchResults;
import com.javazic.service.RechercheService;
import com.javazic.service.ResultContextService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Page de recherche multi-provider avec navigation artiste/album et ajout playlist.
 */
public class SearchPage extends VBox {

    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final RechercheService rechercheService;
    private final ResultContextService resultContextService;
    private final HomePage.LectureHandler onPlay;
    private final Consumer<Artiste> onArtistClick;
    private final Consumer<Album> onAlbumClick;
    private final Consumer<Morceau> onAddToPlaylist;
    private final boolean peutAjouter;

    private final TextField searchField;
    private final VBox resultatsContainer;
    private String providerActif = "Tout";

    public SearchPage(JamendoService jamendoService,
                      AppleItunesService appleItunesService,
                      RechercheService rechercheService,
                      ResultContextService resultContextService,
                      HomePage.LectureHandler onPlay,
                      Consumer<Artiste> onArtistClick,
                      Consumer<Album> onAlbumClick,
                      Consumer<Morceau> onAddToPlaylist,
                      boolean peutAjouter) {
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.rechercheService = rechercheService;
        this.resultContextService = resultContextService;
        this.onPlay = onPlay;
        this.onArtistClick = onArtistClick;
        this.onAlbumClick = onAlbumClick;
        this.onAddToPlaylist = onAddToPlaylist;
        this.peutAjouter = peutAjouter;

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #121212;");

        Label titre = new Label("Rechercher");
        titre.getStyleClass().add("page-title");

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Artistes, morceaux ou albums...");
        searchField.setMaxWidth(500);
        searchField.setOnAction(e -> lancerRecherche(searchField.getText()));

        HBox tabs = creerTabsProvider();

        resultatsContainer = new VBox(12);

        getChildren().addAll(titre, searchField, tabs, resultatsContainer);
    }

    public void lancerRecherche(String requete) {
        if (requete == null || requete.trim().isEmpty()) {
            return;
        }
        String motCle = requete.trim();
        searchField.setText(motCle);

        resultatsContainer.getChildren().clear();

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(40, 40);
        spinner.setStyle("-fx-progress-color: #1DB954;");
        Label loadingText = new Label("Recherche en cours...");
        loadingText.getStyleClass().add("text-secondary");
        HBox loadingBox = new HBox(8, spinner, loadingText);
        loadingBox.setAlignment(Pos.CENTER_LEFT);
        resultatsContainer.getChildren().add(loadingBox);

        Thread worker = new Thread(() -> {
            List<Morceau> tousMorceaux = new ArrayList<>();
            List<Artiste> tousArtistes = new ArrayList<>();
            List<Album> tousAlbums = new ArrayList<>();

            try {
                if (providerActif.equals("Tout") || providerActif.equals("Demo")) {
                    tousArtistes.addAll(rechercheService.rechercherArtistes(motCle));
                    tousAlbums.addAll(rechercheService.rechercherAlbums(motCle));
                    tousMorceaux.addAll(rechercheService.rechercherMorceaux(motCle));
                }

                if (providerActif.equals("Tout") || providerActif.equals("Jamendo")) {
                    try {
                        ProviderSearchResults res = jamendoService.rechercherGlobal(motCle);
                        tousArtistes.addAll(res.getArtistes());
                        tousAlbums.addAll(res.getAlbums());
                        tousMorceaux.addAll(res.getMorceaux());
                    } catch (Exception ignored) {
                    }
                }

                if (providerActif.equals("Tout") || providerActif.equals("Apple")) {
                    try {
                        ProviderSearchResults res = appleItunesService.rechercherGlobal(motCle);
                        tousArtistes.addAll(res.getArtistes());
                        tousAlbums.addAll(res.getAlbums());
                        tousMorceaux.addAll(res.getMorceaux());
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }

            resultContextService.memoriserMorceaux(tousMorceaux);

            Platform.runLater(() -> afficherResultats(motCle, tousArtistes, tousAlbums, tousMorceaux));
        }, "javazic-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void afficherResultats(String motCle,
                                   List<Artiste> artistes,
                                   List<Album> albums,
                                   List<Morceau> morceaux) {
        resultatsContainer.getChildren().clear();

        if (artistes.isEmpty() && albums.isEmpty() && morceaux.isEmpty()) {
            Label vide = new Label("Aucun resultat pour \"" + motCle + "\".");
            vide.getStyleClass().add("text-secondary");
            vide.setStyle("-fx-font-size: 16px; -fx-padding: 24 0 0 0;");
            resultatsContainer.getChildren().add(vide);
            return;
        }

        if (!artistes.isEmpty()) {
            resultatsContainer.getChildren().add(creerSectionArtistes(artistes));
        }

        if (!albums.isEmpty()) {
            resultatsContainer.getChildren().add(creerSectionAlbums(albums));
        }

        if (!morceaux.isEmpty()) {
            resultatsContainer.getChildren().add(creerSectionMorceaux(morceaux));
        }
    }

    private HBox creerTabsProvider() {
        HBox tabs = new HBox(8);
        tabs.setAlignment(Pos.CENTER_LEFT);

        String[] providers = {"Tout", "Demo", "Jamendo", "Apple"};
        for (String provider : providers) {
            Button btn = new Button(provider);
            btn.getStyleClass().add("provider-tab");
            if (provider.equals(providerActif)) {
                btn.getStyleClass().add("provider-tab-active");
            }
            btn.setOnAction(e -> {
                providerActif = provider;
                tabs.getChildren().forEach(node -> node.getStyleClass().remove("provider-tab-active"));
                btn.getStyleClass().add("provider-tab-active");
                if (!searchField.getText().isEmpty()) {
                    lancerRecherche(searchField.getText());
                }
            });
            tabs.getChildren().add(btn);
        }
        return tabs;
    }

    private VBox creerSectionArtistes(List<Artiste> artistes) {
        VBox section = new VBox(4);
        Label titre = new Label("Artistes (" + artistes.size() + ")");
        titre.getStyleClass().add("section-title");
        section.getChildren().add(titre);

        FlowPane flow = new FlowPane(12, 12);
        flow.setPadding(new Insets(4, 0, 8, 0));

        int limit = Math.min(artistes.size(), 6);
        for (int i = 0; i < limit; i++) {
            flow.getChildren().add(creerCarteArtiste(artistes.get(i)));
        }
        section.getChildren().add(flow);
        return section;
    }

    private VBox creerSectionAlbums(List<Album> albums) {
        VBox section = new VBox(4);
        Label titre = new Label("Albums (" + albums.size() + ")");
        titre.getStyleClass().add("section-title");
        section.getChildren().add(titre);

        FlowPane flow = new FlowPane(12, 12);
        flow.setPadding(new Insets(4, 0, 8, 0));

        int limit = Math.min(albums.size(), 6);
        for (int i = 0; i < limit; i++) {
            flow.getChildren().add(creerCarteAlbum(albums.get(i)));
        }
        section.getChildren().add(flow);
        return section;
    }

    private VBox creerSectionMorceaux(List<Morceau> morceaux) {
        VBox section = new VBox(4);
        Label titre = new Label("Morceaux (" + morceaux.size() + ")");
        titre.getStyleClass().add("section-title");
        section.getChildren().addAll(titre, TrackListComponents.creerHeader(peutAjouter));

        for (int i = 0; i < morceaux.size(); i++) {
            Morceau morceau = morceaux.get(i);
            int index = i;
            Artiste artistePrincipal = morceau.getArtistes().isEmpty() ? null : morceau.getArtistes().get(0);
            HBox row = TrackListComponents.creerLigne(
                    morceau,
                    i + 1,
                    peutAjouter,
                    () -> onPlay.lancer(morceaux, index),
                    artistePrincipal == null ? null : () -> onArtistClick.accept(artistePrincipal),
                    () -> {
                        if (onAddToPlaylist != null) {
                            onAddToPlaylist.accept(morceau);
                        }
                    });
            section.getChildren().add(row);
        }
        return section;
    }

    private VBox creerCarteArtiste(Artiste artiste) {
        VBox card = new VBox(6);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onArtistClick.accept(artiste));

        String artworkUrl = ArtworkHelper.resolveArtworkUrl(artiste);
        StackPane art = ArtworkHelper.createSquareArtwork(110, artworkUrl, "\uD83C\uDFA4", "#282828");

        if (artiste.getSource().estDistant()) {
            Label tag = new Label(artiste.getSource().getTag());
            tag.setTextFill(Color.web("#1DB954"));
            tag.setFont(Font.font(9));
            tag.setStyle("-fx-font-weight: bold;");
            StackPane.setAlignment(tag, Pos.TOP_RIGHT);
            StackPane.setMargin(tag, new Insets(4));
            art.getChildren().add(tag);
        }

        Label nom = new Label(tronquer(artiste.getNom(), 20));
        nom.getStyleClass().add("card-title");

        String metaTexte = artiste.getPaysOrigine();
        if (metaTexte == null || metaTexte.isBlank()) {
            metaTexte = artiste.getDateDebut() == null ? "" : String.valueOf(artiste.getDateDebut().getYear());
        }
        Label meta = new Label(tronquer(metaTexte, 20));
        meta.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(art, nom, meta);
        return card;
    }

    private VBox creerCarteAlbum(Album album) {
        VBox card = new VBox(6);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onAlbumClick.accept(album));

        StackPane art = ArtworkHelper.createSquareArtwork(
                110,
                ArtworkHelper.resolveArtworkUrl(album),
                "\uD83D\uDCBF",
                "#282828");

        if (album.getSource().estDistant()) {
            Label tag = new Label(album.getSource().getTag());
            tag.setTextFill(Color.web("#1DB954"));
            tag.setFont(Font.font(9));
            tag.setStyle("-fx-font-weight: bold;");
            StackPane.setAlignment(tag, Pos.TOP_RIGHT);
            StackPane.setMargin(tag, new Insets(4));
            art.getChildren().add(tag);
        }

        Label titre = new Label(tronquer(album.getTitre(), 20));
        titre.getStyleClass().add("card-title");

        String artisteNom = album.getArtiste() == null ? "Inconnu" : album.getArtiste().getNom();
        Label meta = new Label(tronquer(artisteNom, 20));
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
