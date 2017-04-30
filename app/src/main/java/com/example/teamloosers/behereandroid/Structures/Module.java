package com.example.teamloosers.behereandroid.Structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by redjohn on 08/03/17.
 */
public class Module extends Ref implements Serializable {

    private String idPromo, idSpecialite, idFilliere, idCycle;
    private int coeff, credit, semestre;
    private double VHcours, VHtd, VHtp;

    public Module() {

        super();
    }
    public Module(String designation)   {

        super(designation);
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

    public int getCoeff() {
        return coeff;
    }

    public void setCoeff(int coeff) {
        this.coeff = coeff;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public double getVHcours() {
        return VHcours;
    }

    public void setVHcours(double VHcours) {
        this.VHcours = VHcours;
    }

    public double getVHtd() {
        return VHtd;
    }

    public void setVHtd(double VHtd) {
        this.VHtd = VHtd;
    }

    public double getVHtp() {
        return VHtp;
    }

    public void setVHtp(double VHtp) {
        this.VHtp = VHtp;
    }

    public String getIdPromo() {
        return idPromo;
    }

    public void setIdPromo(String idPromo) {
        this.idPromo = idPromo;
    }

    public Map<String, Object> getMap() {

        Map<String, Object> mapData = new HashMap<String, Object>();

        mapData.put("id", this.getId());
        mapData.put("designation", this.getDesignation());
        mapData.put("idCycle", this.getIdCycle());
        mapData.put("idFilliere", this.getIdFilliere());
        mapData.put("idSpecialite", this.getIdSpecialite());
        mapData.put("idPromo", this.getIdPromo());
        mapData.put("coeff", this.getCoeff());
        mapData.put("credit", this.getCredit());
        mapData.put("semestre", this.getSemestre());
        mapData.put("VHcours", this.getVHcours());
        mapData.put("VHtd", this.getVHtd());
        mapData.put("VHtp", this.getVHtp());


        return mapData;
    }

    public void setAttributs(HashMap<String, Object> attributs) {

        this.setId((String) attributs.get("id"));
        this.setDesignation((String) attributs.get("designation"));
        this.setIdCycle((String) attributs.get("idCycle"));
        this.setIdFilliere((String) attributs.get("idFilliere"));
        this.setIdSpecialite((String) attributs.get("idSpecialite"));
        this.setIdPromo((String) attributs.get("idPromo"));
        this.setCoeff(Integer.valueOf(String.valueOf( attributs.get("coeff"))));
        this.setCredit(Integer.valueOf(String.valueOf( attributs.get("credit"))));
        this.setSemestre(Integer.valueOf(String.valueOf( attributs.get("semestre"))));
        this.setVHcours(Double.valueOf(String.valueOf( attributs.get("VHcours"))));
        this.setVHtd(Double.valueOf(String.valueOf( attributs.get("VHtd"))));
        this.setVHtp(Double.valueOf(String.valueOf(attributs.get("VHtp"))));
    }

}
