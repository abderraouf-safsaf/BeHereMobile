package com.example.teamloosers.behereandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.example.teamloosers.behereandroid.Activities.EtudiantsPresencesActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by teamloosers on 04/05/17.
 */

public class Authentication extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    public void test()  {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null)  {

            // already signed in
        } else  {


            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                            .build(), RC_SIGN_IN);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)  {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK)   {
                // start the activity
                finish();
            }
            else {
                
            }
        }
    }
}
