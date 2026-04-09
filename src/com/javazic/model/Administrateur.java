package com.javazic.model;

import java.io.Serializable;

public class Administrateur extends Utilisateur {
    private static final long serialVersionUID = 1L;
    private int niveauAcces;

    public Administrateur(int id, String nom, String email, String motDePasse, int niveauAcces) {
        super(id, nom, email, motDePasse, TypeUtilisateur.ADMIN);
        this.niveauAcces = niveauAcces;
    }

    // Getters
    public int getNiveauAcces() { return niveauAcces; }

    // Setters
    public void setNiveauAcces(int niveauAcces) { this.niveauAcces = niveauAcces; }

    @Override
    public String toString() {
        return getNom() + " (Admin, niveau " + niveauAcces + ")";
    }
}
