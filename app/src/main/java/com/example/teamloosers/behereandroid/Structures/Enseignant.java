package com.example.teamloosers.behereandroid.Structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by redjohn on 08/03/17.
 */
public class Enseignant extends Personne implements Serializable{

    public static final String PR = "Pr", MCA = "MCA", MCB = "MCB", MAA = "MAA",
            MAB = "MAB";

    private String grade;

    public Enseignant() {

        super();
    }
    public Enseignant(String nom, String prenom, String sexe)    {

        super(nom, prenom, sexe);
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }



}
