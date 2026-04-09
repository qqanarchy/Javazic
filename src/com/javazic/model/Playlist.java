package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nom;
    private String description;
    private LocalDate dateCreation;
    private boolean estPublique;
    private Utilisateur proprietaire;
    private List<Morceau> morceaux;

    public Playlist(int id, String nom, String description, Utilisateur proprietaire) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateCreation = LocalDate.now();
        this.estPublique = false;
        this.proprietaire = proprietaire;
        this.morceaux = new ArrayList<>();
    }

    public boolean ajouterMorceau(Morceau morceau) {
        if (morceau == null || contientEquivalent(morceau)) {
            return false;
        }
        morceaux.add(morceau);
        return true;
    }

    public boolean retirerMorceau(Morceau morceau) {
        return morceaux.remove(morceau);
    }

    public Morceau trouverMorceauParId(int morceauId) {
        return morceaux.stream()
                .filter(m -> m.getId() == morceauId)
                .findFirst()
                .orElse(null);
    }

    public void changerOrdre(int ancienIndex, int nouvelIndex) {
        if (ancienIndex >= 0 && ancienIndex < morceaux.size()
                && nouvelIndex >= 0 && nouvelIndex < morceaux.size()) {
            Morceau morceau = morceaux.remove(ancienIndex);
            morceaux.add(nouvelIndex, morceau);
        }
    }

    public int calculerDureeTotale() {
        return morceaux.stream().mapToInt(Morceau::getDuree).sum();
    }

    public String getDureeTotaleFormatee() {
        int total = calculerDureeTotale();
        int h = total / 3600;
        int m = (total % 3600) / 60;
        int s = total % 60;
        if (h > 0) {
            return String.format("%dh %02dmin %02ds", h, m, s);
        }
        return String.format("%dmin %02ds", m, s);
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public LocalDate getDateCreation() { return dateCreation; }
    public boolean isEstPublique() { return estPublique; }
    public Utilisateur getProprietaire() { return proprietaire; }
    public List<Morceau> getMorceaux() { return Collections.unmodifiableList(morceaux); }
    public int getNombreMorceaux() { return morceaux.size(); }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setEstPublique(boolean estPublique) { this.estPublique = estPublique; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Playlist) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom + " (" + morceaux.size() + " morceaux, " + getDureeTotaleFormatee() + ")";
    }

    private boolean contientEquivalent(Morceau morceau) {
        return morceaux.stream().anyMatch(existant -> memeMorceauPlaylist(existant, morceau));
    }

    private boolean memeMorceauPlaylist(Morceau a, Morceau b) {
        if (a.getId() == b.getId()) {
            return true;
        }
        if (a.estDistant() && b.estDistant()) {
            return a.getSource() == b.getSource()
                    && !a.getSourceId().isEmpty()
                    && a.getSourceId().equals(b.getSourceId());
        }
        return false;
    }
}
