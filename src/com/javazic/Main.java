package com.javazic;

import com.javazic.controller.AppController;
import com.javazic.controller.CatalogueController;
import com.javazic.controller.PlaylistController;
import com.javazic.dao.DataStore;
import com.javazic.service.*;
import com.javazic.view.ConsoleView;

public class Main {

    public static void main(String[] args) {

        // 1. Couche donnees - charger les donnees sauvegardees si elles existent
        DataStore dataStore = DataStore.charger();
        boolean donneesChargees = (dataStore != null);

        if (!donneesChargees) {
            dataStore = new DataStore();
        }

        // 2. Couche services
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

        // 3. Lecteur audio (initialise JavaFX si disponible)
        try {
            LecteurAudio.initialiserJavaFX();
        } catch (Exception e) {
            System.out.println("[INFO] JavaFX non disponible, lecture simulee uniquement.");
        }
        LecteurAudio lecteurAudio = new LecteurAudio();

        // 4. Couche vue
        ConsoleView vue = new ConsoleView();

        // 5. Couche controleurs
        CatalogueController catalogueCtrl = new CatalogueController(
                catalogueService, rechercheService, jamendoService, appleItunesService,
                resultContextService, vue);
        PlaylistController playlistCtrl = new PlaylistController(
                playlistService, catalogueService, rechercheService, jamendoService,
                appleItunesService, mediaResolverService, resultContextService, vue);
        AppController appCtrl = new AppController(
                vue, utilisateurService, catalogueCtrl, playlistCtrl,
                statistiquesService, avisService, catalogueService,
                mediaResolverService, resultContextService, lecteurAudio);

        // 6. Donnees d'exemple (seulement si pas de sauvegarde existante)
        if (!donneesChargees) {
            DataInitializer.initialiser(catalogueService, utilisateurService, playlistService);
        }

        // 7. Hook de sauvegarde automatique a la fermeture
        final DataStore ds = dataStore;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ds.sauvegarder();
            System.out.println("[OK] Donnees sauvegardees.");
        }));

        // 8. Lancement
        if (donneesChargees) {
            System.out.println("[OK] Donnees chargees depuis la sauvegarde.");
        }
        appCtrl.demarrer();
    }
}
