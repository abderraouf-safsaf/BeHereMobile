package com.example.teamloosers.behereandroid.Structures;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by redjohn on 08/03/17.
 */
public class Cycle extends Ref {

    private int nbEtudiants;

    public Cycle()  {

        super();
    }
    public Cycle(String designation)    {

        super(designation);
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
        mapdata.put("nbEtudiants", this.getNbEtudiants());

        return mapdata;
    }
    public void setAttributs(HashMap<String, Object> attributs)  {

        this.setId((String) attributs.get("id"));
        this.setDesignation((String) attributs.get("designation"));
        this.setNbEtudiants((Integer) attributs.get("nbEtudiants"));
    }
}
