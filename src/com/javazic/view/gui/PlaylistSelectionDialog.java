package com.javazic.view.gui;

import com.javazic.model.Playlist;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;

/**
 * Dialog de selection de playlist reutilisable.
 */
public final class PlaylistSelectionDialog {

    private PlaylistSelectionDialog() {}

    public static Optional<Playlist> choisirPlaylist(List<Playlist> playlists, String titre) {
        if (playlists == null || playlists.isEmpty()) {
            return Optional.empty();
        }

        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #282828; -fx-min-width: 420;");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Label label = new Label("Choisir une playlist");
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ComboBox<Playlist> comboBox = new ComboBox<>(FXCollections.observableArrayList(playlists));
        comboBox.getStyleClass().add("combo-box");
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.getSelectionModel().selectFirst();
        comboBox.setCellFactory(list -> new PlaylistCell());
        comboBox.setButtonCell(new PlaylistCell());

        VBox content = new VBox(12, label, comboBox);
        content.setPadding(new Insets(16));
        pane.setContent(content);

        dialog.setResultConverter(button -> button == ButtonType.OK ? comboBox.getValue() : null);
        return dialog.showAndWait();
    }

    private static class PlaylistCell extends ListCell<Playlist> {
        @Override
        protected void updateItem(Playlist item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getNom() + " (" + item.getNombreMorceaux() + " morceaux)");
            }
        }
    }
}
