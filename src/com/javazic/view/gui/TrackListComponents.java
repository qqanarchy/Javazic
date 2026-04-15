package com.javazic.view.gui;

import com.javazic.model.Avis;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;
import com.javazic.service.AvisService;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Elements communs pour les listes de morceaux GUI.
 */
public final class TrackListComponents {

    private TrackListComponents() {}

    public static HBox creerHeader(boolean avecAction, boolean avecNotation) {
        return creerHeader(avecAction, avecNotation, false);
    }

    public static HBox creerHeader(boolean avecAction, boolean avecNotation, boolean avecSuppression) {
        HBox header = new HBox(12);
        header.getStyleClass().add("column-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label num = new Label("#");
        num.setMinWidth(36);
        num.setPrefWidth(36);
        num.getStyleClass().add("text-secondary");

        Label titreCol = new Label("TITRE");
        titreCol.getStyleClass().add("text-secondary");
        titreCol.setMinWidth(320);
        HBox.setHgrow(titreCol, Priority.ALWAYS);

        Label artisteCol = new Label("ARTISTE");
        artisteCol.getStyleClass().add("text-secondary");
        artisteCol.setMinWidth(190);
        artisteCol.setPrefWidth(190);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dureeCol = new Label("DUREE");
        dureeCol.getStyleClass().add("text-secondary");
        dureeCol.setMinWidth(64);
        dureeCol.setPrefWidth(64);
        dureeCol.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(num, titreCol, artisteCol, spacer, dureeCol);

        if (avecNotation) {
            header.getChildren().addAll(creerColAction(), creerColAction());
        }
        if (avecAction) {
            header.getChildren().add(creerColAction());
        }
        if (avecSuppression) {
            header.getChildren().add(creerColAction());
        }
        return header;
    }

    public static HBox creerLigne(Morceau morceau,
                                  int numero,
                                  boolean peutAjouter,
                                  boolean peutNoter,
                                  AvisService avisService,
                                  Utilisateur courant,
                                  Runnable onPlay,
                                  Runnable onArtistClick,
                                  Runnable onAjouter) {
        HBox row = new HBox(12);
        row.getStyleClass().add("track-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        if (onPlay != null) {
            row.setOnMouseClicked(e -> onPlay.run());
        }

        Label numLabel = new Label(String.valueOf(numero));
        numLabel.getStyleClass().add("track-index");
        numLabel.setMinWidth(36);
        numLabel.setPrefWidth(36);

        HBox titreBox = new HBox(8);
        titreBox.setAlignment(Pos.CENTER_LEFT);
        titreBox.setMinWidth(320);
        HBox.setHgrow(titreBox, Priority.ALWAYS);

        if (morceau.getSource().estDistant()) {
            Label tag = new Label(morceau.getSource().getTag());
            tag.getStyleClass().add("track-source-tag");
            titreBox.getChildren().add(tag);
        }

        Label lblTitre = new Label(morceau.getTitre());
        lblTitre.getStyleClass().add("track-title");
        titreBox.getChildren().add(lblTitre);

        String artisteNom = morceau.getArtistes().isEmpty() ? "Inconnu" : morceau.getArtistes().get(0).getNom();
        Label lblArtiste = new Label(artisteNom);
        lblArtiste.getStyleClass().add(onArtistClick != null ? "track-artist-link" : "track-artist");
        lblArtiste.setMinWidth(190);
        lblArtiste.setPrefWidth(190);
        if (onArtistClick != null) {
            lblArtiste.setCursor(Cursor.HAND);
            lblArtiste.setOnMouseClicked(e -> {
                e.consume();
                onArtistClick.run();
            });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblDuree = new Label(morceau.getDureeFormatee());
        lblDuree.getStyleClass().add("track-duration");
        lblDuree.setMinWidth(64);
        lblDuree.setPrefWidth(64);
        lblDuree.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(numLabel, titreBox, lblArtiste, spacer, lblDuree);

        if (peutNoter && avisService != null && courant != null) {
            Button btnLike = creerBoutonReaction("\uD83D\uDC4D");
            Button btnDislike = creerBoutonReaction("\uD83D\uDC4E");

            Avis avisCourant = avisService.getAvisUtilisateur(courant.getId(), morceau.getId());
            appliquerEtatReaction(btnLike, btnDislike, avisCourant);

            btnLike.setOnAction(e -> {
                e.consume();
                if (avisService.basculerAvis(courant, morceau, true, "", "")
                        != AvisService.ResultatToggleAvis.ECHEC) {
                    appliquerEtatReaction(btnLike, btnDislike, avisService.getAvisUtilisateur(courant.getId(), morceau.getId()));
                }
            });

            btnDislike.setOnAction(e -> {
                e.consume();
                if (avisService.basculerAvis(courant, morceau, false, "", "")
                        != AvisService.ResultatToggleAvis.ECHEC) {
                    appliquerEtatReaction(btnLike, btnDislike, avisService.getAvisUtilisateur(courant.getId(), morceau.getId()));
                }
            });

            row.getChildren().addAll(btnLike, btnDislike);
        }

        if (peutAjouter && onAjouter != null) {
            Button btnAjouter = new Button("+");
            btnAjouter.getStyleClass().add("track-add-btn");
            btnAjouter.setOnAction(e -> {
                e.consume();
                onAjouter.run();
            });
            row.getChildren().add(btnAjouter);
        }

        return row;
    }

    private static Region creerColAction() {
        Region actionCol = new Region();
        actionCol.setMinWidth(30);
        actionCol.setPrefWidth(30);
        return actionCol;
    }

    private static Button creerBoutonReaction(String label) {
        Button btn = new Button(label);
        btn.getStyleClass().add("track-rate-btn");
        return btn;
    }

    private static void appliquerEtatReaction(Button btnLike, Button btnDislike, Avis avis) {
        btnLike.getStyleClass().removeAll("track-rate-btn-like", "track-rate-btn-dislike");
        btnDislike.getStyleClass().removeAll("track-rate-btn-like", "track-rate-btn-dislike");

        if (avis == null) {
            return;
        }
        if (avis.isPositif()) {
            btnLike.getStyleClass().add("track-rate-btn-like");
        } else {
            btnDislike.getStyleClass().add("track-rate-btn-dislike");
        }
    }
}
