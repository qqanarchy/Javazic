package com.javazic.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nom;
    private String email;
    private String motDePasse;
    private LocalDate dateInscription;
    private TypeUtilisateur type;
    private boolean suspendu;
    private List<HistoriqueEcoute> historiqueEcoutes;

    public Utilisateur(int id, String nom, String email, String motDePasse, TypeUtilisateur type) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = LocalDate.now();
        this.type = type;
        this.suspendu = false;
        this.historiqueEcoutes = new ArrayList<>();
    }

    public boolean verifierMotDePasse(String motDePasse) {
        return this.motDePasse.equals(motDePasse);
    }

    public boolean estAdmin() {
        return this.type == TypeUtilisateur.ADMIN;
    }

    public void ajouterEcoute(Morceau morceau) {
        historiqueEcoutes.add(new HistoriqueEcoute(morceau, LocalDateTime.now()));
    }

    public List<HistoriqueEcoute> getHistoriqueEcoutes() {
        return Collections.unmodifiableList(historiqueEcoutes);
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public LocalDate getDateInscription() { return dateInscription; }
    public TypeUtilisateur getType() { return type; }
    public boolean isSuspendu() { return suspendu; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setType(TypeUtilisateur type) { this.type = type; }
    public void setDateInscription(LocalDate date) { this.dateInscription = date; }
    public void setSuspendu(boolean suspendu) { this.suspendu = suspendu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Utilisateur) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom + " (" + type + ")";
    }
}
