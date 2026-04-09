package com.javazic.service;

import com.javazic.dao.DataStore;
import com.javazic.model.Administrateur;
import com.javazic.model.TypeUtilisateur;
import com.javazic.model.Utilisateur;

import java.util.List;

public class UtilisateurService {

    private final DataStore dataStore;

    public UtilisateurService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Utilisateur inscrire(String nom, String email, String motDePasse, TypeUtilisateur type) {
        if (dataStore.trouverUtilisateurParEmail(email) != null) {
            return null; // email déjà utilisé
        }
        int id = dataStore.prochainIdUtilisateur();
        Utilisateur utilisateur;
        if (type == TypeUtilisateur.ADMIN) {
            utilisateur = new Administrateur(id, nom, email, motDePasse, 1);
        } else {
            utilisateur = new Utilisateur(id, nom, email, motDePasse, type);
        }
        dataStore.ajouterUtilisateur(utilisateur);
        return utilisateur;
    }

    public Utilisateur connexion(String email, String motDePasse) {
        Utilisateur utilisateur = dataStore.trouverUtilisateurParEmail(email);
        if (utilisateur != null && utilisateur.verifierMotDePasse(motDePasse)) {
            return utilisateur;
        }
        return null;
    }

    public boolean modifierProfil(int userId, String nouveauNom, String nouvelEmail) {
        Utilisateur u = dataStore.getUtilisateur(userId);
        if (u == null) return false;

        if (nouvelEmail != null && !nouvelEmail.equals(u.getEmail())) {
            Utilisateur existant = dataStore.trouverUtilisateurParEmail(nouvelEmail);
            if (existant != null) return false;
            u.setEmail(nouvelEmail);
        }
        if (nouveauNom != null && !nouveauNom.isEmpty()) {
            u.setNom(nouveauNom);
        }
        return true;
    }

    public boolean changerMotDePasse(int userId, String ancienMdp, String nouveauMdp) {
        Utilisateur u = dataStore.getUtilisateur(userId);
        if (u == null || !u.verifierMotDePasse(ancienMdp)) return false;
        u.setMotDePasse(nouveauMdp);
        return true;
    }

    public List<Utilisateur> getTousUtilisateurs() {
        return dataStore.getTousUtilisateurs();
    }

    public Utilisateur getUtilisateur(int id) {
        return dataStore.getUtilisateur(id);
    }

    public void supprimerUtilisateur(int id) {
        dataStore.supprimerUtilisateur(id);
    }

    public boolean suspendreUtilisateur(int id) {
        Utilisateur u = dataStore.getUtilisateur(id);
        if (u == null) return false;
        u.setSuspendu(!u.isSuspendu());
        return true;
    }
}
