package com.javazic.service;

import com.javazic.model.Morceau;

import java.util.ArrayList;
import java.util.List;

/**
 * Conserve la derniere liste de morceaux affiches pour relecture et ajout en playlist.
 */
public class ResultContextService {

    private List<Morceau> derniersMorceaux = new ArrayList<>();

    public synchronized void memoriserMorceaux(List<Morceau> morceaux) {
        this.derniersMorceaux = morceaux == null ? new ArrayList<>() : new ArrayList<>(morceaux);
    }

    public synchronized List<Morceau> getDerniersMorceaux() {
        return new ArrayList<>(derniersMorceaux);
    }

    public synchronized Morceau trouverDansDerniersMorceaux(int id) {
        for (Morceau morceau : derniersMorceaux) {
            if (morceau.getId() == id) {
                return morceau;
            }
        }
        return null;
    }
}
