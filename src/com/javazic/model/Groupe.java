package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Groupe implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nom;
    private LocalDate dateFormation;
    private List<Artiste> membres;

    public Groupe(int id, String nom, LocalDate dateFormation) {
        this.id = id;
        this.nom = nom;
        this.dateFormation = dateFormation;
        this.membres = new ArrayList<>();
    }

    public void ajouterMembre(Artiste artiste) {
        if (!membres.contains(artiste)) {
            membres.add(artiste);
        }
    }

    public void retirerMembre(Artiste artiste) {
        membres.remove(artiste);
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public LocalDate getDateFormation() { return dateFormation; }
    public List<Artiste> getMembres() { return Collections.unmodifiableList(membres); }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setDateFormation(LocalDate dateFormation) { this.dateFormation = dateFormation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Groupe) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom + " (" + membres.size() + " membres)";
    }
}
