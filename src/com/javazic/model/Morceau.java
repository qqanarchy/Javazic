package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Morceau implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String titre;
    private int duree; // en secondes
    private String cheminFichier;
    private LocalDate dateAjout;
    private int nombreEcoute;
    private Album album;
    private List<Artiste> artistes;
    private String streamUrl = "";
    private Source source = Source.LOCAL;
    private String sourceId = "";

    public Morceau(int id, String titre, int duree, Artiste artistePrincipal) {
        this.id = id;
        this.titre = titre;
        this.duree = duree;
        this.cheminFichier = "";
        this.dateAjout = LocalDate.now();
        this.nombreEcoute = 0;
        this.artistes = new ArrayList<>();
        this.artistes.add(artistePrincipal);
    }

    public void incrementerEcoute() {
        this.nombreEcoute++;
    }

    public void ajouterArtiste(Artiste artiste) {
        if (!artistes.contains(artiste)) {
            artistes.add(artiste);
        }
    }

    public String getDureeFormatee() {
        int min = duree / 60;
        int sec = duree % 60;
        return String.format("%d:%02d", min, sec);
    }

    // Getters
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public int getDuree() { return duree; }
    public String getCheminFichier() { return cheminFichier; }
    public LocalDate getDateAjout() { return dateAjout; }
    public int getNombreEcoute() { return nombreEcoute; }
    public Album getAlbum() { return album; }
    public List<Artiste> getArtistes() { return Collections.unmodifiableList(artistes); }
    public String getStreamUrl() { return streamUrl; }
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
    public void setDuree(int duree) { this.duree = duree; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }
    public void setDateAjout(LocalDate dateAjout) { this.dateAjout = dateAjout; }
    public void setAlbum(Album album) { this.album = album; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public void setSource(Source source) { this.source = source == null ? Source.LOCAL : source; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId == null ? "" : sourceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Morceau) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String artisteNom = artistes.isEmpty() ? "Inconnu" : artistes.get(0).getNom();
        return titre + " - " + artisteNom + " [" + getDureeFormatee() + "]";
    }
}
