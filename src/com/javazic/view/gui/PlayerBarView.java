package com.javazic.view.gui;

import com.javazic.model.Morceau;
import com.javazic.service.LecteurAudio;
import com.javazic.util.FormatUtil;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Barre de lecture en bas de l'ecran, style Spotify.
 */
public class PlayerBarView extends HBox {

    private final LecteurAudio lecteurAudio;
    private final Consumer<Morceau> onTrackClick;

    private final Label lblTitre;
    private final Label lblArtiste;
    private final Label lblSourceTag;
    private final Button btnPrev;
    private final Button btnPlayPause;
    private final Button btnNext;
    private final Button btnStop;
    private final StackPane progressTrack;
    private final Region progressFill;
    private final Label lblElapsed;
    private final Label lblTotal;
    private final StackPane albumArt;

    private Morceau dernierMorceau;
    private final AnimationTimer timer;
    private double progressionCourante;

    public PlayerBarView(LecteurAudio lecteurAudio, Consumer<Morceau> onTrackClick) {
        this.lecteurAudio = lecteurAudio;
        this.onTrackClick = onTrackClick;

        getStyleClass().add("player-bar");
        setAlignment(Pos.CENTER);
        setSpacing(0);

        // ===== Left: track info =====
        albumArt = ArtworkHelper.createSquareArtwork(48, "", "\u266B", "#333333");

        lblTitre = new Label("Aucune lecture");
        lblTitre.getStyleClass().add("player-track-title");
        lblTitre.setMaxWidth(180);

        lblArtiste = new Label("");
        lblArtiste.getStyleClass().add("player-track-artist");
        lblArtiste.setMaxWidth(180);

        lblSourceTag = new Label("");
        lblSourceTag.getStyleClass().add("track-source-tag");
        lblSourceTag.setVisible(false);

        VBox trackInfo = new VBox(2, lblTitre, lblArtiste);
        trackInfo.setAlignment(Pos.CENTER_LEFT);

        HBox leftSection = new HBox(10, albumArt, trackInfo, lblSourceTag);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        leftSection.setPrefWidth(320);
        leftSection.setMinWidth(200);

        // ===== Center: controls + progress =====
        btnPrev = creerBoutonTransport("\u23EE");
        btnPlayPause = creerBoutonPlay();
        btnNext = creerBoutonTransport("\u23ED");
        btnStop = creerBoutonTransport("\u23F9");

        btnPrev.setOnAction(e -> lecteurAudio.previous());
        btnPlayPause.setOnAction(e -> lecteurAudio.togglePause());
        btnNext.setOnAction(e -> lecteurAudio.next());
        btnStop.setOnAction(e -> lecteurAudio.stop());

        HBox controls = new HBox(12, btnPrev, btnPlayPause, btnNext, btnStop);
        controls.setAlignment(Pos.CENTER);

        lblElapsed = new Label("0:00");
        lblElapsed.getStyleClass().add("player-time");
        lblElapsed.setMinWidth(72);
        lblElapsed.setPrefWidth(72);
        lblElapsed.setMaxWidth(72);
        lblElapsed.setAlignment(Pos.CENTER_RIGHT);

        lblTotal = new Label("0:00");
        lblTotal.getStyleClass().add("player-time");
        lblTotal.setMinWidth(72);
        lblTotal.setPrefWidth(72);
        lblTotal.setMaxWidth(72);
        lblTotal.setAlignment(Pos.CENTER_LEFT);

        progressFill = new Region();
        progressFill.getStyleClass().add("player-progress-fill");
        progressFill.setMinHeight(4);
        progressFill.setPrefHeight(4);
        progressFill.setMaxHeight(4);
        progressFill.setMinWidth(0);
        progressFill.setPrefWidth(0);
        progressFill.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(progressFill, Pos.CENTER_LEFT);

        Region progressBackground = new Region();
        progressBackground.getStyleClass().add("player-progress-track");
        progressBackground.setMinHeight(4);
        progressBackground.setPrefHeight(4);
        progressBackground.setMaxHeight(4);
        progressBackground.setMinWidth(0);
        progressBackground.setMaxWidth(Double.MAX_VALUE);

        progressTrack = new StackPane(progressBackground, progressFill);
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.setPrefWidth(400);
        progressTrack.setMaxWidth(Double.MAX_VALUE);
        progressTrack.widthProperty().addListener((obs, oldVal, newVal) -> mettreAJourProgressionVisuelle());
        HBox.setHgrow(progressTrack, Priority.ALWAYS);

        HBox progressRow = new HBox(8, lblElapsed, progressTrack, lblTotal);
        progressRow.setAlignment(Pos.CENTER);
        progressRow.setMaxWidth(560);

        VBox centerSection = new VBox(4, controls, progressRow);
        centerSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerSection, Priority.ALWAYS);

        // ===== Right: spacer =====
        Region rightSpacer = new Region();
        rightSpacer.setPrefWidth(220);
        rightSpacer.setMinWidth(100);

        getChildren().addAll(leftSection, centerSection, rightSpacer);

        // ===== Animation timer for polling player state =====
        timer = new AnimationTimer() {
            private long dernierUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - dernierUpdate < 200_000_000L) return; // 200ms
                dernierUpdate = now;
                mettreAJour();
            }
        };
        timer.start();
    }

    private void mettreAJour() {
        Morceau courant = lecteurAudio.getCurrentTrack();

        if (courant != null && courant != dernierMorceau) {
            String artiste = courant.getArtistes().isEmpty() ? "Inconnu" : courant.getArtistes().get(0).getNom();
            lblTitre.setText(courant.getTitre());
            lblArtiste.setText(artiste);
            ArtworkHelper.fillSquareArtwork(albumArt, 48, ArtworkHelper.resolveArtworkUrl(courant),
                    "\u266B", "#333333");

            if (courant.getSource().estDistant()) {
                lblSourceTag.setText(courant.getSource().getTag());
                lblSourceTag.setVisible(true);
            } else {
                lblSourceTag.setVisible(false);
            }

            dernierMorceau = courant;
        }

        if (lecteurAudio.isPlaying()) {
            int elapsed = lecteurAudio.getSecondesEcoulees();
            int total = lecteurAudio.getDureeTotaleCourante();

            lblElapsed.setText(FormatUtil.formaterDureeCompacte(elapsed));
            lblTotal.setText(FormatUtil.formaterDureeCompacte(total));

            double progress = total > 0 ? (double) elapsed / total : 0;
            progressionCourante = progress;
            mettreAJourProgressionVisuelle();

            btnPlayPause.setText(lecteurAudio.isPaused() ? "\u25B6" : "\u23F8");
        } else {
            if (dernierMorceau != null) {
                progressionCourante = 0;
                mettreAJourProgressionVisuelle();
                btnPlayPause.setText("\u25B6");
                lblElapsed.setText("0:00");
                lblTotal.setText(FormatUtil.formaterDureeCompacte(
                        Math.max(0, dernierMorceau.getDuree())));
            }
        }
    }

    private Button creerBoutonTransport(String symbole) {
        Button btn = new Button(symbole);
        btn.getStyleClass().add("player-btn");
        btn.setMinSize(32, 32);
        return btn;
    }

    private Button creerBoutonPlay() {
        Button btn = new Button("\u25B6");
        btn.getStyleClass().add("player-btn-play");
        return btn;
    }

    private void mettreAJourProgressionVisuelle() {
        double largeur = progressTrack.getWidth();
        if (largeur <= 0) {
            largeur = progressTrack.getPrefWidth();
        }
        double progressionBornee = Math.max(0, Math.min(1, progressionCourante));
        progressFill.setPrefWidth(largeur * progressionBornee);
    }
}
