package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Utils.LoginServices;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private static final int SIGN_IN_REQUEST_CODE = 123;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Utils.makeActivityFullScreen(this);

        setContentView(R.layout.activity_login);
    }
    @Override
    protected void onStart() {

        super.onStart();

        if (LoginServices.getCurrentUser() != null)    {

            checkIfEnseignantAndStart();
        }
        else if (LoginServices.isEnseignantLoggedIn()) {

            startMainActivity();
            finish();
        }
        else    {

            LoginServices.signOut(this);
        }
    }
    public void login(View view) {

        if (LoginServices.getCurrentUser() != null)   {

            checkIfEnseignantAndStart();
        }
        else    {

            startLoginUI();
        }
    }
    private void startLoginUI() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                        .setTheme(R.style.BeHere)
                        .setIsSmartLockEnabled(false)
                        .setAllowNewEmailAccounts(true)
                        .setLogo(R.drawable.ic_launcher)
                        .build(),
                SIGN_IN_REQUEST_CODE);
    }
    public void checkIfEnseignantAndStart()   {

        progressDialog = new ProgressDialog(this);
        progressDialog.setOnDismissListener(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.chargement_enseignants_message));

        String pathToEnseignants = Utils.firebasePath(Utils.ENSEIGNANT_MODULE);
        Query query = Utils.database.getReference(pathToEnseignants).orderByChild("email")
                .equalTo(LoginServices.getCurrentUser().getEmail());
        query.keepSynced(true); // Keeping data fresh

        progressDialog.show();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                    Enseignant enseignant = snapshot.getValue(Enseignant.class);
                    Utils.enseignant = enseignant;
                }
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(LoginActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }
    public void startMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {

                checkIfEnseignantAndStart();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Utils.showSnackBar(LoginActivity.this, getString(R.string.erreur_back_button_pressed_message));
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {

                    Utils.showSnackBar(LoginActivity.this, getString(R.string.no_network_message));
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {

                    Utils.showSnackBar(LoginActivity.this, getString(R.string.unknown_error));
                    return;
                }
            }
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {

        if (LoginServices.isEnseignantLoggedIn())   {

            startMainActivity();
            finish();
        }
        else    {

            LoginServices.signOut(this);
            Utils.showSnackBar(LoginActivity.this, getResources()
                    .getString(R.string.email_nexiste_pas_message));
        }
    }
}
