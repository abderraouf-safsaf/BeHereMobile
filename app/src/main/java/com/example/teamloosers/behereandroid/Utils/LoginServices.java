package com.example.teamloosers.behereandroid.Utils;

import android.app.Activity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by teamloosers on 12/05/17.
 */

public class LoginServices {

    public static Boolean isEnseignantLoggedIn() {

        return (Utils.enseignant != null);
    }
    public static FirebaseUser getCurrentUser()    {

        return FirebaseAuth.getInstance().getCurrentUser();
    }
    public static void signOut(Activity activity)   {

        AuthUI.getInstance().signOut(activity);
        Utils.enseignant = null;
    }
}
