package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Avis;
import com.javazic.model.Morceau;

import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesService {

    private final DataStore dataStore;

    public StatistiquesService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // === Statistiques de base (admin) ===

    public int getNombreUtilisateurs() { return dataStore.getNombreUtilisateurs(); }
    public int getNombreMorceaux() { return dataStore.getNombreMorceaux(); }
    public int getNombreAlbums() { return dataStore.getNombreAlbums(); }
    public int getNombreGroupes() { return dataStore.getNombreGroupes(); }
    public int getNombreArtistes() { return dataStore.getNombreArtistes(); }
    public int getNombreTotalEcoutes() { return dataStore.getNombreTotalEcoutes(); }

    // === Statistiques evoluees (fonctionnalite bonus) ===

    public List<Morceau> getTopMorceaux(int limite) {
        return dataStore.getTousMorceaux().stream()
                .sorted(Comparator.comparingInt(Morceau::getNombreEcoute).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Artiste, Integer>> getTopArtistes(int limite) {
        Map<Artiste, Integer> ecoutesParArtiste = new LinkedHashMap<>();
        for (Morceau m : dataStore.getTousMorceaux()) {
            for (Artiste a : m.getArtistes()) {
                ecoutesParArtiste.merge(a, m.getNombreEcoute(), Integer::sum);
            }
        }
        return ecoutesParArtiste.entrySet().stream()
                .sorted(Map.Entry.<Artiste, Integer>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Album, Integer>> getTopAlbums(int limite) {
        Map<Album, Integer> ecoutesParAlbum = new LinkedHashMap<>();
        for (Morceau m : dataStore.getTousMorceaux()) {
            if (m.getAlbum() != null) {
                ecoutesParAlbum.merge(m.getAlbum(), m.getNombreEcoute(), Integer::sum);
            }
        }
        return ecoutesParAlbum.entrySet().stream()
                .sorted(Map.Entry.<Album, Integer>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<Morceau> getMorceauxLesPlusAimes(int limite) {
        return dataStore.getTousMorceaux().stream()
                .sorted(Comparator
                        .comparingInt((Morceau morceau) -> getNombreLikes(morceau.getId()))
                        .reversed()
                        .thenComparing(Morceau::getTitre))
                .limit(limite)
                .collect(Collectors.toList());
    }

    private int getNombreLikes(int morceauId) {
        return (int) dataStore.getAvisParMorceau(morceauId).stream()
                .filter(Avis::isPositif)
                .count();
    }
}
