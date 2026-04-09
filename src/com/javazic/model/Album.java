package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String titre;
    private LocalDate dateSortie;
    private Genre genre;
    private String image;
    private Artiste artiste;
    private List<Morceau> morceaux;
    private Source source = Source.LOCAL;
    private String sourceId = "";

    public Album(int id, String titre, LocalDate dateSortie, Genre genre, Artiste artiste) {
        this.id = id;
        this.titre = titre;
        this.dateSortie = dateSortie;
        this.genre = genre;
        this.artiste = artiste;
        this.image = "";
        this.morceaux = new ArrayList<>();
    }

    public void ajouterMorceau(Morceau morceau) {
        if (!morceaux.contains(morceau)) {
            morceaux.add(morceau);
            morceau.setAlbum(this);
        }
    }

    public int calculerDureeTotale() {
        return morceaux.stream().mapToInt(Morceau::getDuree).sum();
    }

    // Getters
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public LocalDate getDateSortie() { return dateSortie; }
    public Genre getGenre() { return genre; }
    public String getImage() { return image; }
    public Artiste getArtiste() { return artiste; }
    public List<Morceau> getMorceaux() { return Collections.unmodifiableList(morceaux); }
    public Source getSource() {
        if (source == null) {
            source = Source.LOCAL;
        }
        return source;
    }
    public boolean estJamendo() { return getSource() == Source.JAMENDO; }
    public boolean estDistant() { return getSource().estDistant(); }
    public String getSourceId() { return sourceId == null ? "" : sourceId; }

    // Setters
    public void setTitre(String titre) { this.titre = titre; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public void setSource(Source source) { this.source = source == null ? Source.LOCAL : source; }
    public void setImage(String image) { this.image = image; }
    public void setArtiste(Artiste artiste) { this.artiste = artiste; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId == null ? "" : sourceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Album) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return titre + " - " + artiste.getNom() + " (" + genre + ", " + dateSortie.getYear() + ")";
    }
}
