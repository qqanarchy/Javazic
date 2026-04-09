package com.javazic.view.gui;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;
import com.javazic.model.Playlist;
import com.javazic.model.Source;
import com.javazic.model.Utilisateur;
import com.javazic.service.AppleItunesService;
import com.javazic.service.AvisService;
import com.javazic.service.CatalogueService;
import com.javazic.service.JamendoService;
import com.javazic.service.LecteurAudio;
import com.javazic.service.MediaResolverService;
import com.javazic.service.PlaylistService;
import com.javazic.service.RechercheService;
import com.javazic.service.ResultContextService;
import com.javazic.service.StatistiquesService;
import com.javazic.service.UtilisateurService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vue principale : sidebar gauche + contenu central + player bar en bas.
 */
public class MainView extends BorderPane {

    private final CatalogueService catalogueService;
    private final UtilisateurService utilisateurService;
    private final RechercheService rechercheService;
    private final StatistiquesService statistiquesService;
    private final AvisService avisService;
    private final PlaylistService playlistService;
    private final JamendoService jamendoService;
    private final AppleItunesService appleItunesService;
    private final MediaResolverService mediaResolverService;
    private final ResultContextService resultContextService;
    private final LecteurAudio lecteurAudio;

    private final StackPane contentArea;
    private final VBox sidebarPlaylists;
    private final PlayerBarView playerBar;

    private Utilisateur utilisateurConnecte;
    private boolean modeVisiteur;
    private int ecoutesVisiteur = 5;

    private Button btnAccueil;
    private Button btnRecherche;
    private Button btnProfil;
    private Button activeBtn;

    public MainView(CatalogueService catalogueService,
                    UtilisateurService utilisateurService,
                    RechercheService rechercheService,
                    StatistiquesService statistiquesService,
                    AvisService avisService,
                    PlaylistService playlistService,
                    JamendoService jamendoService,
                    AppleItunesService appleItunesService,
                    MediaResolverService mediaResolverService,
                    ResultContextService resultContextService,
                    LecteurAudio lecteurAudio) {
        this.catalogueService = catalogueService;
        this.utilisateurService = utilisateurService;
        this.rechercheService = rechercheService;
        this.statistiquesService = statistiquesService;
        this.avisService = avisService;
        this.playlistService = playlistService;
        this.jamendoService = jamendoService;
        this.appleItunesService = appleItunesService;
        this.mediaResolverService = mediaResolverService;
        this.resultContextService = resultContextService;
        this.lecteurAudio = lecteurAudio;

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        ScrollPane scrollContent = new ScrollPane(contentArea);
        scrollContent.setFitToWidth(true);
        scrollContent.setFitToHeight(true);
        scrollContent.setStyle("-fx-background-color: #121212;");

        sidebarPlaylists = new VBox(2);
        VBox sidebar = construireSidebar();

        playerBar = new PlayerBarView(lecteurAudio, this::lancerLectureMorceau);

        setLeft(sidebar);
        setCenter(scrollContent);
        setBottom(playerBar);
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.modeVisiteur = false;
        rafraichirSidebar();
        naviguerAccueil();
    }

    public void setModeVisiteur(boolean visiteur) {
        this.modeVisiteur = visiteur;
        this.utilisateurConnecte = null;
        this.ecoutesVisiteur = 5;
        rafraichirSidebar();
        naviguerAccueil();
    }

    public void naviguerAccueil() {
        setActiveSidebarBtn(btnAccueil);
        HomePage page = new HomePage(
                jamendoService,
                appleItunesService,
                catalogueService,
                this::lancerLectureListe,
                this::ouvrirSelectionPlaylist,
                peutAjouterAuxPlaylists());
        afficherPage(page);
    }

    public void naviguerRecherche() {
        naviguerRecherche(null);
    }

    public void naviguerRecherche(String requeteInitiale) {
        setActiveSidebarBtn(btnRecherche);
        SearchPage page = new SearchPage(
                jamendoService,
                appleItunesService,
                rechercheService,
                resultContextService,
                this::lancerLectureListe,
                this::naviguerArtiste,
                this::naviguerAlbum,
                this::ouvrirSelectionPlaylist,
                peutAjouterAuxPlaylists());
        afficherPage(page);
        if (requeteInitiale != null && !requeteInitiale.isEmpty()) {
            page.lancerRecherche(requeteInitiale);
        }
    }

    public void naviguerPlaylists() {
        setActiveSidebarBtn(null);
        if (utilisateurConnecte == null) {
            return;
        }
        PlaylistsPage page = new PlaylistsPage(
                playlistService,
                utilisateurConnecte,
                this::naviguerPlaylistDetail,
                this::rafraichirSidebar);
        afficherPage(page);
    }

    public void naviguerPlaylistDetail(Playlist playlist) {
        setActiveSidebarBtn(null);
        PlaylistDetailPage page = new PlaylistDetailPage(
                playlist,
                playlistService,
                jamendoService,
                appleItunesService,
                rechercheService,
                resultContextService,
                utilisateurConnecte,
                this::lancerLectureListe,
                this::rafraichirSidebar);
        afficherPage(page);
    }

    public void naviguerArtiste(Artiste artiste) {
        if (artiste == null) {
            return;
        }
        setActiveSidebarBtn(null);
        afficherPage(creerPageChargement("Chargement de l'artiste..."));

        Thread worker = new Thread(() -> {
            List<Morceau> morceaux = chargerMorceauxPourArtiste(artiste);
            List<Album> albums = chargerAlbumsPourArtiste(artiste, morceaux);

            Platform.runLater(() -> afficherPage(new ArtistDetailPage(
                    artiste,
                    albums,
                    morceaux,
                    this::lancerLectureListe,
                    this::naviguerAlbum,
                    this::ouvrirSelectionPlaylist,
                    peutAjouterAuxPlaylists())));
        }, "javazic-artist-detail");
        worker.setDaemon(true);
        worker.start();
    }

    public void naviguerAlbum(Album album) {
        if (album == null) {
            return;
        }
        setActiveSidebarBtn(null);
        afficherPage(creerPageChargement("Chargement de l'album..."));

        Thread worker = new Thread(() -> {
            List<Morceau> morceaux = chargerMorceauxPourAlbum(album);

            Platform.runLater(() -> afficherPage(new AlbumDetailPage(
                    album,
                    morceaux,
                    this::lancerLectureListe,
                    album.getArtiste() == null ? null : () -> naviguerArtiste(album.getArtiste()),
                    this::ouvrirSelectionPlaylist,
                    peutAjouterAuxPlaylists())));
        }, "javazic-album-detail");
        worker.setDaemon(true);
        worker.start();
    }

    public void naviguerProfil() {
        setActiveSidebarBtn(btnProfil);
        if (utilisateurConnecte == null) {
            return;
        }
        ProfilePage page = new ProfilePage(
                utilisateurConnecte,
                utilisateurService,
                statistiquesService,
                avisService,
                catalogueService);
        afficherPage(page);
    }

    public void deconnecter() {
        lecteurAudio.stop();
        utilisateurConnecte = null;
        modeVisiteur = false;
        if (getParent() instanceof StackPane root) {
            LoginPage loginPage = new LoginPage(utilisateurService, utilisateur -> {
                setUtilisateurConnecte(utilisateur);
                root.getChildren().setAll(this);
            }, () -> {
                setModeVisiteur(true);
                root.getChildren().setAll(this);
            });
            root.getChildren().setAll(loginPage);
        }
    }

    private void afficherPage(Node page) {
        contentArea.getChildren().setAll(page);
    }

    public void lancerLectureMorceau(Morceau morceau) {
        if (morceau == null) {
            return;
        }
        if (modeVisiteur && ecoutesVisiteur <= 0) {
            afficherNotification("Limite de 5 ecoutes atteinte. Creez un compte !");
            return;
        }
        lancerLectureListe(List.of(morceau), 0);
    }

    public void lancerLectureListe(List<Morceau> morceaux, int index) {
        if (morceaux == null || morceaux.isEmpty()) {
            return;
        }
        if (modeVisiteur && ecoutesVisiteur <= 0) {
            afficherNotification("Limite de 5 ecoutes atteinte. Creez un compte !");
            return;
        }

        resultContextService.memoriserMorceaux(morceaux);
        lecteurAudio.play(morceaux, index);

        Morceau courant = morceaux.get(Math.max(0, Math.min(index, morceaux.size() - 1)));
        courant.incrementerEcoute();
        if (utilisateurConnecte != null) {
            utilisateurConnecte.ajouterEcoute(courant);
        }
        if (modeVisiteur) {
            ecoutesVisiteur--;
        }
    }

    private void ouvrirSelectionPlaylist(Morceau morceau) {
        if (morceau == null) {
            return;
        }
        if (utilisateurConnecte == null) {
            afficherNotification("Connectez-vous pour ajouter ce morceau a une playlist.");
            return;
        }

        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateurConnecte.getId());
        if (playlists.isEmpty()) {
            afficherNotification("Creez d'abord une playlist.");
            return;
        }

        Optional<Playlist> selection = PlaylistSelectionDialog.choisirPlaylist(
                playlists,
                "Ajouter \"" + morceau.getTitre() + "\"");
        if (selection.isEmpty()) {
            return;
        }

        Playlist playlist = selection.get();
        boolean ajoute = playlistService.ajouterMorceauAPlaylist(playlist.getId(), morceau);
        if (ajoute) {
            afficherNotification("Morceau ajoute a \"" + playlist.getNom() + "\".");
            rafraichirSidebar();
        } else {
            afficherNotification("Ce morceau est deja present dans \"" + playlist.getNom() + "\".");
        }
    }

    private boolean peutAjouterAuxPlaylists() {
        return utilisateurConnecte != null && !modeVisiteur;
    }

    private VBox creerPageChargement(String message) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(48));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(42, 42);
        spinner.setStyle("-fx-progress-color: #1DB954;");

        Label texte = new Label(message);
        texte.getStyleClass().add("text-secondary");
        texte.setStyle("-fx-font-size: 15px;");

        box.getChildren().addAll(spinner, texte);
        return box;
    }

    private List<Morceau> chargerMorceauxPourArtiste(Artiste artiste) {
        if (artiste == null) {
            return new ArrayList<>();
        }
        if (!artiste.estDistant()) {
            return new ArrayList<>(catalogueService.getMorceauxParArtiste(artiste.getId()));
        }
        if (artiste.getSource() == Source.APPLE_ITUNES) {
            return new ArrayList<>(appleItunesService.getMorceauxArtiste(artiste.getId()));
        }
        return new ArrayList<>(jamendoService.getMorceauxArtiste(artiste.getId()));
    }

    private List<Album> chargerAlbumsPourArtiste(Artiste artiste, List<Morceau> morceaux) {
        if (artiste == null) {
            return new ArrayList<>();
        }
        if (!artiste.estDistant()) {
            return new ArrayList<>(catalogueService.getAlbumsParArtiste(artiste.getId()));
        }

        Map<String, Album> albums = new LinkedHashMap<>();
        if (morceaux != null) {
            for (Morceau morceau : morceaux) {
                ajouterAlbum(albums, morceau == null ? null : morceau.getAlbum());
            }
        }

        List<Album> recherches = artiste.getSource() == Source.APPLE_ITUNES
                ? appleItunesService.rechercherAlbums(artiste.getNom())
                : jamendoService.rechercherAlbums(artiste.getNom());

        for (Album album : recherches) {
            if (album == null) {
                continue;
            }
            if (artistesCorrespondent(artiste, album.getArtiste())) {
                ajouterAlbum(albums, album);
            }
        }

        return new ArrayList<>(albums.values());
    }

    private List<Morceau> chargerMorceauxPourAlbum(Album album) {
        if (album == null) {
            return new ArrayList<>();
        }
        if (!album.estDistant()) {
            return new ArrayList<>(catalogueService.getMorceauxParAlbum(album.getId()));
        }
        if (album.getSource() == Source.APPLE_ITUNES) {
            return new ArrayList<>(appleItunesService.getMorceauxAlbum(album.getId()));
        }
        return new ArrayList<>(jamendoService.getMorceauxAlbum(album.getId()));
    }

    private void ajouterAlbum(Map<String, Album> albums, Album album) {
        if (album == null) {
            return;
        }
        String key = album.estDistant()
                ? album.getSource().name() + ":" + album.getSourceId()
                : String.valueOf(album.getId());
        albums.putIfAbsent(key, album);
    }

    private boolean artistesCorrespondent(Artiste a, Artiste b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.estDistant() && b.estDistant()
                && a.getSource() == b.getSource()
                && !a.getSourceId().isEmpty()
                && a.getSourceId().equals(b.getSourceId())) {
            return true;
        }
        return a.getNom() != null && b.getNom() != null
                && a.getNom().equalsIgnoreCase(b.getNom());
    }

    private void afficherNotification(String message) {
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast-text");
        HBox toastBox = new HBox(toast);
        toastBox.getStyleClass().add("toast");
        toastBox.setAlignment(Pos.CENTER);
        toastBox.setMaxHeight(40);
        toastBox.setMaxWidth(460);
        StackPane.setAlignment(toastBox, Pos.TOP_CENTER);
        StackPane.setMargin(toastBox, new Insets(16, 0, 0, 0));
        contentArea.getChildren().add(toastBox);

        Thread timer = new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> contentArea.getChildren().remove(toastBox));
        }, "javazic-toast");
        timer.setDaemon(true);
        timer.start();
    }

    private VBox construireSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(200);

        Label logo = new Label("JAVAZIC");
        logo.getStyleClass().add("sidebar-logo");
        logo.setPadding(new Insets(0, 0, 16, 8));

        btnAccueil = creerSidebarBtn("Accueil", this::naviguerAccueil);
        btnRecherche = creerSidebarBtn("Rechercher", this::naviguerRecherche);
        btnProfil = creerSidebarBtn("Profil", this::naviguerProfil);

        Region separateur = new Region();
        separateur.getStyleClass().add("sidebar-separator");
        VBox.setMargin(separateur, new Insets(8, 0, 8, 0));

        Label labelPlaylists = new Label("MES PLAYLISTS");
        labelPlaylists.getStyleClass().add("sidebar-section");

        Button btnCreerPlaylist = creerSidebarBtn("+ Creer une playlist", this::creerPlaylist);

        VBox playlistSection = new VBox(2, labelPlaylists, btnCreerPlaylist, sidebarPlaylists);

        Region sep2 = new Region();
        sep2.getStyleClass().add("sidebar-separator");
        VBox.setMargin(sep2, new Insets(8, 0, 8, 0));

        Button btnDeconnexion = creerSidebarBtn("Deconnexion", this::deconnecter);
        btnDeconnexion.setStyle("-fx-text-fill: #F15E6C;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                logo,
                btnAccueil,
                btnRecherche,
                btnProfil,
                separateur,
                playlistSection,
                spacer,
                sep2,
                btnDeconnexion);

        return sidebar;
    }

    private Button creerSidebarBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void setActiveSidebarBtn(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("sidebar-btn-active");
        }
        activeBtn = btn;
        if (activeBtn != null && !activeBtn.getStyleClass().contains("sidebar-btn-active")) {
            activeBtn.getStyleClass().add("sidebar-btn-active");
        }
    }

    public void rafraichirSidebar() {
        sidebarPlaylists.getChildren().clear();
        if (utilisateurConnecte == null) {
            return;
        }
        List<Playlist> playlists = playlistService.getPlaylistsUtilisateur(utilisateurConnecte.getId());
        for (Playlist playlist : playlists) {
            Button btn = new Button(playlist.getNom());
            btn.getStyleClass().add("sidebar-playlist-btn");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> naviguerPlaylistDetail(playlist));
            sidebarPlaylists.getChildren().add(btn);
        }
    }

    private void creerPlaylist() {
        if (utilisateurConnecte == null) {
            afficherNotification("Connectez-vous pour creer une playlist.");
            return;
        }
        naviguerPlaylists();
    }
}
