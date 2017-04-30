package com.example.teamloosers.behereandroid.Structures;

import com.example.teamloosers.behereandroid.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by redjohn on 08/03/17.
 */
public class Absence extends Ref {

    public static final Boolean PRESENT = true, ABSENT = false;
    private String idSeance, idGroupe, idSection, idPromo, idSpecialite,
                    idFilliere, idCycle, idModule, idEnseignant, idEtudiant, typeSeance, date;

    public Absence()    {    }

    public String getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(String idSeance) {
        this.idSeance = idSeance;
    }

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

    public String getIdModule() {
        return idModule;
    }

    public void setIdModule(String idModule) {
        this.idModule = idModule;
    }

    public String getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(String idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public String getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(String idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTypeSeance() {
        return typeSeance;
    }

    public void setTypeSeance(String typeSeance) {
        this.typeSeance = typeSeance;
    }

    public Map<String, Object> getMap() {

        Map<String, Object> mapdata = new HashMap<String, Object>();

        mapdata.put("id", this.getId());
        mapdata.put("idCycle", this.getIdCycle());
        mapdata.put("idFilliere", this.getIdFilliere());
        mapdata.put("idSpecialite", this.getIdSpecialite());
        mapdata.put("idPromo", this.getIdPromo());
        mapdata.put("idSection", this.getIdSection());
        mapdata.put("idGroupe", this.getIdGroupe());
        mapdata.put("idEnseignant", this.getIdEnseignant());
        mapdata.put("idModule", this.getIdModule());
        mapdata.put("idSeance", this.getIdSeance());
        mapdata.put("idEtudiant", this.getIdEtudiant());
        mapdata.put("typeSeance", this.getTypeSeance());
        mapdata.put("date", this.getDate());
        return mapdata;
    }

    public void ajouterDb(FirebaseDatabase database)    {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, this.getIdCycle(),
                this.getIdFilliere(), this.getIdPromo(), this.getIdSection(),
                this.getIdGroupe(), this.getIdEtudiant(), this.getIdModule(), this.getId());
        DatabaseReference etudiantRef = database.getReference(pathToEtudiant);
        etudiantRef.setValue(this.getMap());

        String idStructure = (this.getTypeSeance().equals(Utils.SECTIONS))? this.getIdSection(): this.getIdGroupe();
        String pathToEnseignant_Module = Utils.firebasePath(Utils.ENSEIGNANT_MODULE,
                this.getIdEnseignant(), this.getIdModule(), this.getTypeSeance(), idStructure, this.getIdSeance(),
                this.getId());
        DatabaseReference strucutreRef = database.getReference(pathToEnseignant_Module);

        strucutreRef.updateChildren(this.getMap());
    }

    @Override
    public String toString() {

        return this.getDate();
    }

    public void supprimerDb(FirebaseDatabase database) {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, this.getIdCycle(),
                this.getIdFilliere(), this.getIdPromo(), this.getIdSection(),
                this.getIdGroupe(), this.getIdEtudiant(), this.getIdModule(), this.getId());
        DatabaseReference etudiantRef = database.getReference(pathToEtudiant);
        etudiantRef.removeValue();

        String idStructure = (this.getTypeSeance().equals(Utils.SECTIONS))? this.getIdSection(): this.getIdGroupe();
        String pathToEnseignant_Module = Utils.firebasePath(Utils.ENSEIGNANT_MODULE,
                this.getIdEnseignant(), this.getIdModule(), this.getTypeSeance(), idStructure, this.getIdSeance(),
                this.getId());
        DatabaseReference strucutreRef = database.getReference(pathToEnseignant_Module);
        strucutreRef.removeValue();
    }
}
