package com.javazic;

import com.javazic.dao.DataStore;
import com.javazic.service.*;
import com.javazic.view.gui.LoginPage;
import com.javazic.view.gui.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainGUI extends Application {

    private DataStore dataStore;

    @Override
    public void start(Stage primaryStage) {
        // 1. Donnees
        dataStore = DataStore.charger();
        boolean donneesChargees = (dataStore != null);
        if (!donneesChargees) {
            dataStore = new DataStore();
        }

        // 2. Services
        CatalogueService catalogueService = new CatalogueService(dataStore);
        UtilisateurService utilisateurService = new UtilisateurService(dataStore);
        RechercheService rechercheService = new RechercheService(dataStore);
        StatistiquesService statistiquesService = new StatistiquesService(dataStore);
        AvisService avisService = new AvisService(dataStore);
        RemoteMediaRegistry remoteMediaRegistry = new RemoteMediaRegistry();
        MediaResolverService mediaResolverService = new MediaResolverService(dataStore, remoteMediaRegistry);
        ResultContextService resultContextService = new ResultContextService();
        PlaylistService playlistService = new PlaylistService(dataStore, mediaResolverService);
        JamendoService jamendoService = new JamendoService(remoteMediaRegistry);
        AppleItunesService appleItunesService = new AppleItunesService(remoteMediaRegistry);

        if (donneesChargees) {
            mediaResolverService.rehydraterRegistreDistant();
        }

        // 3. Lecteur audio (mode graphique = pas de sortie console)
        LecteurAudio lecteurAudio = new LecteurAudio();
        lecteurAudio.setAfficherConsole(false);

        // 4. Donnees d'exemple
        if (!donneesChargees) {
            DataInitializer.initialiser(catalogueService, utilisateurService, playlistService);
        }

        // 5. Construction de l'interface
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #121212;");

        MainView mainView = new MainView(
                catalogueService, utilisateurService, rechercheService, statistiquesService,
                avisService, playlistService, jamendoService, appleItunesService,
                mediaResolverService, resultContextService, lecteurAudio);

        LoginPage loginPage = new LoginPage(utilisateurService, utilisateur -> {
            mainView.setUtilisateurConnecte(utilisateur);
            root.getChildren().setAll(mainView);
        }, () -> {
            mainView.setModeVisiteur(true);
            root.getChildren().setAll(mainView);
        });

        root.getChildren().add(loginPage);

        Scene scene = new Scene(root, 1200, 750);
        String css = getClass().getResource("/com/javazic/view/gui/javazic.css") != null
                ? getClass().getResource("/com/javazic/view/gui/javazic.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("Javazic");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // 6. Sauvegarde a la fermeture
        primaryStage.setOnCloseRequest(e -> {
            lecteurAudio.stop();
            dataStore.sauvegarder();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
