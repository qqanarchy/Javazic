package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoriqueEcoute implements Serializable {
    private static final long serialVersionUID = 1L;

    private Morceau morceau;
    private LocalDateTime dateEcoute;

    public HistoriqueEcoute(Morceau morceau, LocalDateTime dateEcoute) {
        this.morceau = morceau;
        this.dateEcoute = dateEcoute;
    }

    public Morceau getMorceau() { return morceau; }
    public LocalDateTime getDateEcoute() { return dateEcoute; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String artiste = morceau.getArtistes().isEmpty() ? "Inconnu" : morceau.getArtistes().get(0).getNom();
        return dateEcoute.format(fmt) + " - " + morceau.getTitre() + " (" + artiste + ")";
    }
}
