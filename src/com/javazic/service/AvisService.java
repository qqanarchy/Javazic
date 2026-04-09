package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Avis;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;

import java.util.List;

public class AvisService {

    private final DataStore dataStore;

    public AvisService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Avis ajouterAvis(Utilisateur auteur, Morceau morceau, int note, String commentaire) {
        // Verifier si l'utilisateur a deja laisse un avis sur ce morceau
        Avis existant = dataStore.getAvisParUtilisateurEtMorceau(auteur.getId(), morceau.getId());
        if (existant != null) {
            return null; // deja note
        }
        int id = dataStore.prochainIdAvis();
        Avis avis = new Avis(id, auteur, morceau, note, commentaire);
        dataStore.ajouterAvis(avis);
        return avis;
    }

    public boolean modifierAvis(int avisId, int utilisateurId, int nouvelleNote, String nouveauCommentaire) {
        Avis avis = dataStore.getAvis(avisId);
        if (avis == null || avis.getAuteur().getId() != utilisateurId) return false;
        avis.setNote(nouvelleNote);
        avis.setCommentaire(nouveauCommentaire);
        return true;
    }

    public boolean supprimerAvis(int avisId, int utilisateurId) {
        Avis avis = dataStore.getAvis(avisId);
        if (avis == null || avis.getAuteur().getId() != utilisateurId) return false;
        dataStore.supprimerAvis(avisId);
        return true;
    }

    public List<Avis> getAvisParMorceau(int morceauId) {
        return dataStore.getAvisParMorceau(morceauId);
    }

    public double getNoteMoyenne(int morceauId) {
        List<Avis> liste = dataStore.getAvisParMorceau(morceauId);
        if (liste.isEmpty()) return 0;
        return liste.stream().mapToInt(Avis::getNote).average().orElse(0);
    }

    public Avis getAvisUtilisateur(int utilisateurId, int morceauId) {
        return dataStore.getAvisParUtilisateurEtMorceau(utilisateurId, morceauId);
    }
}
