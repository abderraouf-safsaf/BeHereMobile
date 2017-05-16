package com.example.teamloosers.behereandroid.Structures;

import java.util.ArrayList;

/**
 * Created by redjohn on 08/03/17.
 */
public class Etudiant extends Personne {

    public static final int SCORE_PLUS = 1, SCORE_MOIN = -1;
    public String idGroupe, idSection, idPromo, idSpecialite, idFilliere, idCycle, imageBase64;
    public int nbAbsences = 0;

    private ArrayList<Absence> absencesList = new ArrayList<>();

    public Etudiant()   {   }
    public Etudiant(String nom, String prenom, String sexe)  {

        super(nom, prenom, sexe);

    }

    public String getImageBase64() { return imageBase64; }

    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(String idGroupe) {
        this.idGroupe = idGroupe;
    }

    public String getIdSection() {
        return idSection;
    }

    public void setIdSection(String idSection) {
        this.idSection = idSection;
    }

    public String getIdPromo() {
        return idPromo;
    }

    public void setIdPromo(String idPromo) {
        this.idPromo = idPromo;
    }

    public String getIdSpecialite() {
        return idSpecialite;
    }

    public void setIdSpecialite(String idSpecialite) {
        this.idSpecialite = idSpecialite;
    }

    public String getIdFilliere() {
        return idFilliere;
    }

    public void setIdFilliere(String idFilliere) {
        this.idFilliere = idFilliere;
    }

    public String getIdCycle() {
        return idCycle;
    }

    public void setIdCycle(String idCycle) {
        this.idCycle = idCycle;
    }

    public int getNbAbsences() {
        return nbAbsences;
    }

    public void setNbAbsences(int nbAbsences) {
        this.nbAbsences = nbAbsences;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Etudiant))
            return false;
        else    {

            return this.toString().equals(obj.toString());
        }
    }

    @Override
    public int hashCode() {

        return getNom().hashCode();
    }
}
