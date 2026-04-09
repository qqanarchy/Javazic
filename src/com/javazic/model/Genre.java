package com.javazic.model;

public enum Genre {
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSIQUE("Classique"),
    HIPHOP("Hip-Hop"),
    ELECTRO("Electro"),
    R_B("R&B"),
    REGGAE("Reggae"),
    METAL("Metal"),
    FOLK("Folk"),
    BLUES("Blues"),
    COUNTRY("Country"),
    LATINO("Latino"),
    AUTRE("Autre");

    private final String libelle;

    Genre(String libelle) {
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
