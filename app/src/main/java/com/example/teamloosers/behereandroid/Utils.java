package com.example.teamloosers.behereandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;

import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Structures.Identifiable;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Ref;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by redjohn on 16/04/17.
 */

public class Utils {

    public static final Boolean BACKABLE = true, NOT_BACKABLE = false;

    public static FirebaseDatabase database = null;

    public static Enseignant enseignant;
    static {

        enseignant = new Enseignant("Badsi", "Hichem", Personne.HOMME);
        enseignant.setId("4a04efee-0aa4-4756-bbee-78602a3ee9dc");

        database = FirebaseDatabase.getInstance();

        // Enable disk persistence (Offline)
        database.setPersistenceEnabled(true);

        // TODO: go online
        //database.goOffline();
    }
    public final static String  CYCLES = "/Cycles", SPECIALITE_PROMOS = "/Specialite_Promos",
            FILLIERE_SPECIALITES = "/Filliere_Specialites",
            MODULE_ENSEIGNANTS = "/Module_Enseignants",
            ENSEIGNANT_MODULE = "/Enseignant_Modules",
            PROMO_MODULES = "/Promo_Modules",
            GROUPES = "/Groupes", SECTIONS = "/Sections",
            PHOTO_URL_ATTRIBUT = "imageBase64";

    public static String firebasePath(String ... noeuds)   {

        String path = "";
        for (int i = 0; i < noeuds.length; i++) {

            path += String.format("%s/", noeuds[i]);
        }
        return path;
    }
    public static <P extends Identifiable, C extends Identifiable> ArrayList<C>
    getChildrenFromDataSnapshot(DataSnapshot dataSnapshot, Class<P> parentClass, Class<C> childClass) {

        P parentObject = null; C childObject = null;

        ArrayList<DataSnapshot> dataList = getListFromIterable(dataSnapshot.getChildren());
        try {

            if (!Modifier.isAbstract(parentClass.getModifiers()))
                parentObject = parentClass.newInstance();
            childObject = childClass.newInstance();
        } catch (Exception e)   {   }
        if (!Modifier.isAbstract(parentClass.getModifiers()))
            deleteAttributsFromArrayList(dataList, parentObject.getMap());

        ArrayList<C> childrenList = new ArrayList<>();

        C obj = null;
        for (DataSnapshot dataSnap: dataList)   {

            HashMap<String, Object> attributs = getOnlyAttributs(dataSnap,
                    childObject.getMap());
            try {
                obj = (C) childClass.newInstance();
            } catch (Exception e)   {   }

            obj.setAttributs(attributs);
            childrenList.add(obj);
        }
        return childrenList;
    }
    private static <E> ArrayList<E> getListFromIterable(Iterable<E> iter) {

        ArrayList<E> list = new ArrayList<E>();
        for (E item : iter) {

            list.add(item);
        }
        return list;
    }
    private static void deleteAttributsFromArrayList(ArrayList<DataSnapshot> dataList,
                                                     Map<String, Object> attributsMap) {

        Iterator<DataSnapshot> snapshotIterator= dataList.iterator();

        while (snapshotIterator.hasNext())  {

            DataSnapshot element = snapshotIterator.next();
            if (attributsMap.containsKey(element.getKey()))
                snapshotIterator.remove();
        }
    }
    private static HashMap<String, Object> getOnlyAttributs(DataSnapshot snapshot,
                                                            Map<String, Object> atrributsMap)    {

        HashMap<String, Object> attributsHashMap = new HashMap<>();


        for (DataSnapshot attribut: snapshot.getChildren())    {

            if(atrributsMap.containsKey(attribut.getKey()))
                attributsHashMap.put(attribut.getKey(), attribut.getValue());
        }
        return attributsHashMap;
    }
    public static String generateId()   {

        return UUID.randomUUID().toString();
    }
    public static String generateId(Ref obj)   {

        String designation = obj.getDesignation();
        designation = designation.replaceAll("\\s+","");

        String id = String.format("%s_%s", designation, UUID.randomUUID().toString());
        return id;
    }

    public static Bitmap decodeToImage(String imageString) {

        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }
}
