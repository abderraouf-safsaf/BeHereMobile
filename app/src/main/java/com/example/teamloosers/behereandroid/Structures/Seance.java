package com.example.teamloosers.behereandroid.Structures;


import android.support.annotation.NonNull;

import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

public class Seance extends Ref implements Comparable {

    public static final String TD = "/Groupes", COURS = "/Sections";
    private String idSection, idGroupe;
    private String idEnseignant;
    private String idModule;
    private String typeSeance;
    private String date;

    public Seance(){    }

    public Seance(int jour, int mois, int annee)   {

        this.date = String.format("%d/%d/%d", jour, mois, annee);
    }

    public String getIdSection() {
        return idSection;
    }

    public void setIdSection(String idSection) {
        this.idSection = idSection;
    }

    public String getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(String idGroupe) {
        this.idGroupe = idGroupe;
    }


    public String getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(String idEnseignant) {
        this.idEnseignant = idEnseignant;
    }


    public String getIdModule() {
        return idModule;
    }

    public void setIdModule(String idModule) {
        this.idModule = idModule;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNbAbsence(Integer nbAbsence) {
        nbAbsence = nbAbsence;
    }

    public String getTypeSeance() {
        return typeSeance;
    }

    public void setTypeSeance(String typeSeance) {
        this.typeSeance = typeSeance;
    }

    public Map<String, Object> getMap(){

        Map<String, Object> mapdata = new LinkedHashMap<String, Object>();

        mapdata.put("id", getId());
        mapdata.put("idSection", getIdSection());
        mapdata.put("idGroupe", getIdGroupe());
        mapdata.put("idEnseignant", getIdEnseignant());
        mapdata.put("idModule", getIdModule());
        mapdata.put("typeSeance", getTypeSeance());
        mapdata.put("date", getDate());

        return mapdata;
    }


    public void ajouterSeance(FirebaseDatabase database) {

        String idStructure = (this.getTypeSeance().equals(Utils.SECTIONS))? this.getIdSection(): this.getIdGroupe();
        String pathToEnseignant_Module = Utils.firebasePath(Utils.ENSEIGNANT_MODULE,
                this.getIdEnseignant(), this.getIdModule(), this.getTypeSeance(), idStructure, this.getId());
        DatabaseReference strucutreRef = database.getReference(pathToEnseignant_Module);

        strucutreRef.updateChildren(this.getMap());
    }

    @Override
    public int compareTo(@NonNull Object o) {

        Seance seance = (Seance) o;
        String currentDate = getDate();
        String date = seance.getDate();

        return currentDate.compareTo(date);
    }
}