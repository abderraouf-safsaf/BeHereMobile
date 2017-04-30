package com.example.teamloosers.behereandroid.Structures;

import java.io.Serializable;

/**
 * Created by redjohn on 11/03/17.
 */
public abstract class Ref implements Serializable {

    private String id, designation;

    public Ref()   {   }
    public Ref(String designation) {

        this.designation = designation;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public String toString()    {

        return this.designation;
    }
}
