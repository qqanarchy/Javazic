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
    private boolean positif;
    private String commentaire;
    private LocalDateTime dateAvis;

    public Avis(int id, Utilisateur auteur, Morceau morceau, boolean positif, String commentaire) {
        this.id = id;
        this.auteur = auteur;
        this.morceau = morceau;
        this.positif = positif;
        this.commentaire = commentaire == null ? "" : commentaire;
        this.dateAvis = LocalDateTime.now();
    }

    public int getId() { return id; }
    public Utilisateur getAuteur() { return auteur; }
    public Morceau getMorceau() { return morceau; }
    public boolean isPositif() { return positif; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }

    public void setPositif(boolean positif) { this.positif = positif; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire == null ? "" : commentaire; }

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
        String sentiment = positif ? "Like" : "Dislike";
        return auteur.getNom() + " - " + sentiment + " - " + dateAvis.format(fmt)
                + (commentaire.isEmpty() ? "" : " : " + commentaire);
    }
}
