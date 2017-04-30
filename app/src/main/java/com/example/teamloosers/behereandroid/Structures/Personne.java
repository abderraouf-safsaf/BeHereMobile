package com.example.teamloosers.behereandroid.Structures;

import android.graphics.Color;

import java.io.Serializable;

/**
 * Created by redjohn on 08/03/17.
 */
public abstract class Personne implements Serializable {

    public static final String HOMME = "H", FEMME = "H";
    public static final int HOMME_COLOR = Color.parseColor("#004D40"), FEMME_COLLOR = Color.parseColor("#b71c1c");

    private String id, nom, prenom, email;
    private String sexe;

    public Personne()   {

    }
    public Personne(String nom, String prenom, String sexe)  {

        this.nom = nom;
        this.prenom = prenom;
        this.sexe = sexe;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.nom, this.prenom);
    }
}
