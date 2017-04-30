package com.example.teamloosers.behereandroid.Structures;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by redjohn on 08/03/17.
 */
public class Section extends Ref {

    private String idPromo, idSpecialite, idFilliere, idCycle;
    private int nbEtudiants;
    public Section()    {

        super();
    }
    public Section(String designation)  {

        super(designation);
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

    public int getNbEtudiants() {
        return nbEtudiants;
    }

    public void setNbEtudiants(int nbEtudiants) {
        this.nbEtudiants = nbEtudiants;
    }

    public Map<String, Object> getMap(){

        Map<String, Object> mapdata = new HashMap<String, Object>();

        mapdata.put("id", this.getId());
        mapdata.put("designation", this.getDesignation());
        mapdata.put("idCycle", this.getIdCycle());
        mapdata.put("idFilliere", this.getIdFilliere());
        mapdata.put("idSpecialite", this.getIdSpecialite());
        mapdata.put("idPromo", this.getIdPromo());
        mapdata.put("nbEtudiants", this.getNbEtudiants());

        return mapdata;
    }
    public void setAttributs(HashMap<String, Object> attributs)  {

        this.setId((String) attributs.get("id"));
        this.setDesignation((String) attributs.get("designation"));
        this.setIdCycle((String) attributs.get("idCycle"));
        this.setIdFilliere((String) attributs.get("idFilliere"));
        this.setIdSpecialite((String) attributs.get("idSpecialite"));
        this.setIdPromo((String) attributs.get("idPromo"));
        this.setNbEtudiants((Integer) attributs.get("nbEtudiants"));
    }
}
