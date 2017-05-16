package com.example.teamloosers.behereandroid.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Identifiable;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Ref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by redjohn on 16/04/17.
 */

public class Utils {

    public static final String DATABASE_ERR_MESSAGE = "Une erreur s'est produit lors de la connexion" +
            "à la base de donnée";
    public static FirebaseDatabase database;

    public static Enseignant enseignant;
    static {

        /*
            Create an instance from database
         */
        database = FirebaseDatabase.getInstance();

        // Enable disk persistence (Offline)
        database.setPersistenceEnabled(true);
    }

    public final static String  CYCLES = "/Cycles", SPECIALITE_PROMOS = "/Specialite_Promos",
            FILLIERE_SPECIALITES = "/Filliere_Specialites",
            MODULE_ENSEIGNANTS = "/Module_Enseignants",
            ENSEIGNANT_MODULE = "/Enseignant_Modules",
            PROMO_MODULES = "/Promo_Modules",
            GROUPES = "/Groupes", SECTIONS = "/Sections",
            PHOTO_URL_ATTRIBUT = "imageBase64",
            SCORE = "/score";

    /*
        Generate a path by concatenate nodes ID
     */
    public static String firebasePath(String ... noeuds)   {

        String path = "";
        for (int i = 0; i < noeuds.length; i++) {

            path += String.format("%s/", noeuds[i]);
        }
        return path;
    }
    /*
        Generate unique ID using built-in class UUID
     */
    public static String generateId()   {

        return UUID.randomUUID().toString();
    }

    /*
        Decode image from 64-base to a BMP image
     */
    public static Bitmap decode64BaseImageToBmp(String imageString) {

        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;
    }

    /*
        Show a snackbar (Toast-like) in activity
     */
    public static void showSnackBar(Activity activity, String message) {

        View rootView = activity.getWindow().getDecorView().getRootView();
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(ContextCompat.getColor(rootView.getContext(), R.color.white));
        snackbar.show();
    }

    /*
        Send notification e-mail to a student
        Sender E-mail adress and password are obtained from Firebase Remote Config service
     */
    public static void envoyerNotification(final Activity activity, final Etudiant etudiant, final String message)   {

        final FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mRemoteConfig.setConfigSettings(remoteConfigSettings);

        long cacheExpiration = 3600;
        if (mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // task successful. Activate the fetched data
                            mRemoteConfig.activateFetched();

                            final String email = mRemoteConfig.getString("email");
                            final String password = mRemoteConfig.getString("password");

                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try  {
                                        Mail m = new Mail(email, password);

                                        String[] toArr = {etudiant.getEmail()};
                                        m.set_to(toArr);
                                        m.set_from(email);
                                        m.set_subject(activity.getString(R.string.email_subject));
                                        m.setBody(message);
                                        try {
                                            if (m.send())
                                                System.out.println("Email envoye");
                                            else System.out.println("Erreur envoie email");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        } else {
                            //task failed
                        }
                    }
                });
    }

    /*
        Make the activity Full screen
     */
    public static void makeActivityFullScreen(Activity activity) {

        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /*
        Make lines between items in RecycerView using R.drawable.recyclerview_divider xml file
     */
    public static void setRecyclerViewDecoration(RecyclerView recyclerView) {

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayout.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    /*
        Setting activity ActionBar title and subtitle
     */
    public static void setActionBarTitle(AppCompatActivity activity, String title) {

        activity.getSupportActionBar().setTitle(title);
    }
    public static void setActionBarSubtitle(AppCompatActivity activity, String title) {

        activity.getSupportActionBar().setSubtitle(title);
    }
}
