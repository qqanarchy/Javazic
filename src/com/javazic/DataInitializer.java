package com.javazic;

import com.javazic.model.*;
import com.javazic.service.CatalogueService;
import com.javazic.service.UtilisateurService;
import com.javazic.service.PlaylistService;

import java.time.LocalDate;

/**
 * Initialise le DataStore avec des données d'exemple.
 */
public class DataInitializer {

    public static void initialiser(CatalogueService catalogue,
                                   UtilisateurService utilisateurs,
                                   PlaylistService playlists) {

        // === Utilisateurs ===
        Utilisateur admin = utilisateurs.inscrire("Admin", "admin@javazic.com", "admin", TypeUtilisateur.ADMIN);
        Utilisateur dave = utilisateurs.inscrire("Dave", "dave@gmail.com", "dave123", TypeUtilisateur.PREMIUM);
        Utilisateur tibo = utilisateurs.inscrire("Tibo", "tibo@gmail.com", "tibo123", TypeUtilisateur.STANDARD);
        Utilisateur eloi = utilisateurs.inscrire("Eloi", "eloi@gmail.com", "eloi123", TypeUtilisateur.STANDARD);

        // === Artistes ===
        Artiste daftPunk = catalogue.creerArtiste("Daft Punk", "Duo electronique francais",
                "France", LocalDate.of(1993, 1, 1));
        Artiste stromae = catalogue.creerArtiste("Stromae", "Auteur-compositeur belge",
                "Belgique", LocalDate.of(2009, 1, 1));
        Artiste edith = catalogue.creerArtiste("Edith Piaf", "Chanteuse francaise legendaire",
                "France", LocalDate.of(1935, 1, 1));
        Artiste miles = catalogue.creerArtiste("Miles Davis", "Trompettiste et compositeur americain",
                "USA", LocalDate.of(1944, 1, 1));
        Artiste angele = catalogue.creerArtiste("Angele", "Auteure-compositrice belge",
                "Belgique", LocalDate.of(2017, 1, 1));

        // === Groupe ===
        Groupe dpGroupe = catalogue.creerGroupe("Daft Punk (Groupe)", LocalDate.of(1993, 1, 1));
        catalogue.ajouterMembreAuGroupe(dpGroupe.getId(), daftPunk);

        // === Albums ===
        Album discovery = catalogue.creerAlbum("Discovery", LocalDate.of(2001, 3, 12),
                Genre.ELECTRO, daftPunk);
        Album racineCarree = catalogue.creerAlbum("Racine carree", LocalDate.of(2013, 8, 16),
                Genre.HIPHOP, stromae);
        Album multitude = catalogue.creerAlbum("Multitude", LocalDate.of(2022, 3, 4),
                Genre.POP, stromae);
        Album kindOfBlue = catalogue.creerAlbum("Kind of Blue", LocalDate.of(1959, 8, 17),
                Genre.JAZZ, miles);
        Album bpiaf = catalogue.creerAlbum("Best of Edith Piaf", LocalDate.of(1960, 1, 1),
                Genre.CLASSIQUE, edith);
        Album nonante = catalogue.creerAlbum("Nonante-Cinq", LocalDate.of(2021, 12, 3),
                Genre.POP, angele);

        // === Morceaux ===
        // Discovery
        catalogue.creerMorceau("One More Time", 320, daftPunk, discovery);
        catalogue.creerMorceau("Aerodynamic", 228, daftPunk, discovery);
        catalogue.creerMorceau("Digital Love", 301, daftPunk, discovery);
        catalogue.creerMorceau("Harder, Better, Faster, Stronger", 224, daftPunk, discovery);
        catalogue.creerMorceau("Something About Us", 232, daftPunk, discovery);

        // Racine carree
        catalogue.creerMorceau("Papaoutai", 234, stromae, racineCarree);
        catalogue.creerMorceau("Formidable", 234, stromae, racineCarree);
        catalogue.creerMorceau("Tous les memes", 203, stromae, racineCarree);
        catalogue.creerMorceau("Quand c'est ?", 210, stromae, racineCarree);
        catalogue.creerMorceau("Ave Cesaria", 248, stromae, racineCarree);

        // Multitude
        catalogue.creerMorceau("Sante", 188, stromae, multitude);
        catalogue.creerMorceau("L'enfer", 222, stromae, multitude);
        catalogue.creerMorceau("Mon amour", 161, stromae, multitude);

        // Kind of Blue
        catalogue.creerMorceau("So What", 562, miles, kindOfBlue);
        catalogue.creerMorceau("Blue in Green", 327, miles, kindOfBlue);
        catalogue.creerMorceau("All Blues", 690, miles, kindOfBlue);

        // Best of Edith Piaf
        catalogue.creerMorceau("La Vie en rose", 197, edith, bpiaf);
        catalogue.creerMorceau("Non, je ne regrette rien", 134, edith, bpiaf);
        catalogue.creerMorceau("Hymne a l'amour", 193, edith, bpiaf);

        // Nonante-Cinq
        catalogue.creerMorceau("Bruxelles je t'aime", 189, angele, nonante);
        catalogue.creerMorceau("Libre", 180, angele, nonante);
        catalogue.creerMorceau("Demon", 207, angele, nonante);

        // === Playlists ===
        if (dave != null) {
            Playlist favs = playlists.creerPlaylist("Mes favoris", "Mes morceaux preferes", dave);
            playlists.ajouterMorceauAPlaylist(favs.getId(), 1);  // One More Time
            playlists.ajouterMorceauAPlaylist(favs.getId(), 6);  // Papaoutai
            playlists.ajouterMorceauAPlaylist(favs.getId(), 17); // La Vie en rose
            playlists.ajouterMorceauAPlaylist(favs.getId(), 20); // Bruxelles je t'aime
            playlists.changerVisibilite(favs.getId(), true);

            Playlist chill = playlists.creerPlaylist("Chill", "Pour se detendre", dave);
            playlists.ajouterMorceauAPlaylist(chill.getId(), 5);  // Something About Us
            playlists.ajouterMorceauAPlaylist(chill.getId(), 15); // Blue in Green
            playlists.ajouterMorceauAPlaylist(chill.getId(), 12); // L'enfer
        }
    }
}
