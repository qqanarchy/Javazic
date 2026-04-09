package com.javazic.view.gui;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.List;

/**
 * Helper centralisant la resolution et l'affichage des artworks.
 */
public final class ArtworkHelper {

    private ArtworkHelper() {}

    public static String resolveArtworkUrl(Morceau morceau) {
        if (morceau == null) {
            return "";
        }
        Album album = morceau.getAlbum();
        if (album != null && album.getImage() != null && !album.getImage().isBlank()) {
            return album.getImage();
        }

        List<Artiste> artistes = morceau.getArtistes();
        for (Artiste artiste : artistes) {
            String image = resolveArtworkUrl(artiste);
            if (!image.isBlank()) {
                return image;
            }
        }
        return "";
    }

    public static String resolveArtworkUrl(Album album) {
        if (album == null) {
            return "";
        }
        return album.getImage() == null ? "" : album.getImage().trim();
    }

    public static String resolveArtworkUrl(Artiste artiste) {
        if (artiste == null) {
            return "";
        }
        return artiste.getImage() == null ? "" : artiste.getImage().trim();
    }

    public static StackPane createSquareArtwork(double size, String imageUrl,
                                                String placeholderSymbol, String backgroundHex) {
        return createArtwork(size, size, imageUrl, placeholderSymbol, backgroundHex, 8);
    }

    public static void fillSquareArtwork(StackPane container, double size, String imageUrl,
                                         String placeholderSymbol, String backgroundHex) {
        fillArtwork(container, size, size, imageUrl, placeholderSymbol, backgroundHex, 8);
    }

    public static StackPane createArtwork(double width, double height, String imageUrl,
                                          String placeholderSymbol, String backgroundHex, double radius) {
        StackPane container = new StackPane();
        fillArtwork(container, width, height, imageUrl, placeholderSymbol, backgroundHex, radius);
        return container;
    }

    public static void fillArtwork(StackPane container, double width, double height, String imageUrl,
                                   String placeholderSymbol, String backgroundHex, double radius) {
        container.getChildren().clear();
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(width, height);
        container.setMinSize(width, height);
        container.setMaxSize(width, height);

        Rectangle background = new Rectangle(width, height);
        background.setFill(Color.web(backgroundHex));
        background.setArcWidth(radius);
        background.setArcHeight(radius);

        Label placeholder = new Label(placeholderSymbol);
        placeholder.setTextFill(Color.web("#535353"));
        placeholder.setFont(Font.font(Math.max(18, Math.min(width, height) / 3.2)));

        container.getChildren().addAll(background, placeholder);

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        Image image = new Image(imageUrl, width, height, false, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        imageView.setClip(clip);

        container.getChildren().add(imageView);
    }
}
