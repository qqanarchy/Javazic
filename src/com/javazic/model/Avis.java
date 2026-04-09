package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Avis implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Utilisateur auteur;
    private Morceau morceau;
    private int note; // 1 a 5
    private String commentaire;
    private LocalDateTime dateAvis;

    public Avis(int id, Utilisateur auteur, Morceau morceau, int note, String commentaire) {
        this.id = id;
        this.auteur = auteur;
        this.morceau = morceau;
        this.note = Math.max(1, Math.min(5, note));
        this.commentaire = commentaire;
        this.dateAvis = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public Utilisateur getAuteur() { return auteur; }
    public Morceau getMorceau() { return morceau; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }

    // Setters
    public void setNote(int note) { this.note = Math.max(1, Math.min(5, note)); }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Avis) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return auteur.getNom() + " - " + note + "/5 - " + dateAvis.format(fmt)
                + (commentaire.isEmpty() ? "" : " : " + commentaire);
    }
}
