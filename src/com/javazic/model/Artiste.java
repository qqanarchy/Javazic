package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artiste implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nom;
    private String biographie;
    private String paysOrigine;
    private LocalDate dateDebut;
    private String image;
    private Source source = Source.LOCAL;
    private String sourceId = "";

    public Artiste(int id, String nom, String biographie, String paysOrigine, LocalDate dateDebut) {
        this.id = id;
        this.nom = nom;
        this.biographie = biographie;
        this.paysOrigine = paysOrigine;
        this.dateDebut = dateDebut;
        this.image = "";
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getBiographie() { return biographie; }
    public String getPaysOrigine() { return paysOrigine; }
    public LocalDate getDateDebut() { return dateDebut; }
    public String getImage() { return image; }
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
    public void setNom(String nom) { this.nom = nom; }
    public void setBiographie(String biographie) { this.biographie = biographie; }
    public void setPaysOrigine(String paysOrigine) { this.paysOrigine = paysOrigine; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setImage(String image) { this.image = image; }
    public void setSource(Source source) { this.source = source == null ? Source.LOCAL : source; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId == null ? "" : sourceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Artiste) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom + " (" + paysOrigine + ")";
    }
}
