package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Avis;
import com.javazic.model.Morceau;
import com.javazic.model.Utilisateur;

import java.util.List;

public class AvisService {

    public enum ResultatToggleAvis {
        AJOUTE,
        MODIFIE,
        SUPPRIME,
        ECHEC
    }

    private final DataStore dataStore;

    public AvisService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Avis ajouterAvis(Utilisateur auteur, Morceau morceau, boolean positif, String commentaire) {
        Avis existant = dataStore.getAvisParUtilisateurEtMorceau(auteur.getId(), morceau.getId());
        if (existant != null) {
            return null;
        }
        int id = dataStore.prochainIdAvis();
        Avis avis = new Avis(id, auteur, morceau, positif, commentaire);
        dataStore.ajouterAvis(avis);
        return avis;
    }

    public ResultatToggleAvis basculerAvis(Utilisateur auteur,
                                           Morceau morceau,
                                           boolean positif,
                                           String commentaireCreation,
                                           String commentaireModification) {
        Avis existant = dataStore.getAvisParUtilisateurEtMorceau(auteur.getId(), morceau.getId());
        if (existant == null) {
            Avis avis = ajouterAvis(auteur, morceau, positif, commentaireCreation);
            return avis != null ? ResultatToggleAvis.AJOUTE : ResultatToggleAvis.ECHEC;
        }

        if (existant.isPositif() == positif) {
            return supprimerAvis(existant.getId(), auteur.getId())
                    ? ResultatToggleAvis.SUPPRIME
                    : ResultatToggleAvis.ECHEC;
        }

        String commentaire = commentaireModification;
        if (commentaire == null || commentaire.isBlank()) {
            commentaire = existant.getCommentaire();
        }
        return modifierAvis(existant.getId(), auteur.getId(), positif, commentaire)
                ? ResultatToggleAvis.MODIFIE
                : ResultatToggleAvis.ECHEC;
    }

    public boolean modifierAvis(int avisId, int utilisateurId, boolean positif, String nouveauCommentaire) {
        Avis avis = dataStore.getAvis(avisId);
        if (avis == null || avis.getAuteur().getId() != utilisateurId) return false;
        avis.setPositif(positif);
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

    public int getNombreLikes(int morceauId) {
        return (int) dataStore.getAvisParMorceau(morceauId).stream()
                .filter(Avis::isPositif)
                .count();
    }

    public int getNombreDislikes(int morceauId) {
        return (int) dataStore.getAvisParMorceau(morceauId).stream()
                .filter(avis -> !avis.isPositif())
                .count();
    }

    public double getRatioLikes(int morceauId) {
        int likes = getNombreLikes(morceauId);
        int dislikes = getNombreDislikes(morceauId);
        int total = likes + dislikes;
        if (total == 0) {
            return 0;
        }
        return (double) likes / total;
    }

    public Avis getAvisUtilisateur(int utilisateurId, int morceauId) {
        return dataStore.getAvisParUtilisateurEtMorceau(utilisateurId, morceauId);
    }
}
