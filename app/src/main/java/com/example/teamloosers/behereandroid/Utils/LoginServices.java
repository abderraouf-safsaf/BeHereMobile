package com.example.teamloosers.behereandroid.Utils;

import android.app.Activity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by teamloosers on 12/05/17.
 */

public class LoginServices {

    /*
        Check if enseignant is logged in
     */
    public static Boolean isEnseignantLoggedIn() {

        return (Utils.enseignant != null);
    }

    /*
        Get logged in user from FirebaseAuth API
     */
    public static FirebaseUser getCurrentUser()    {

        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /*
        Sign out
     */
    public static void signOut(Activity activity)   {

        AuthUI.getInstance().signOut(activity);
        Utils.enseignant = null;
    }
}
