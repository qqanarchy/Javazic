package com.javazic.model;

public enum TypeUtilisateur {
    ABONNE("Abonne"),
    ADMIN("Administrateur");

    private final String libelle;

    TypeUtilisateur(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
