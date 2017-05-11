package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private LinearLayout mainLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
    }
    @Override
    protected void onStart() {

        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null)    {

            checkIfEnseignantAndStart(FirebaseAuth.getInstance().getCurrentUser());
        }
        else if (isEnseignantLoggedIn()) {

            startMainActivity();
            finish();
        }
        else    {

            signOut();
        }
    }
    public void login(View view) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null)   {

            checkIfEnseignantAndStart(FirebaseAuth.getInstance().getCurrentUser());
        }
        else    {

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .setTheme(R.style.BeHere)
                            .setIsSmartLockEnabled(false)
                            .setAllowNewEmailAccounts(true)
                            .setLogo(R.drawable.ic_launcher)
                            .build(),
                    RC_SIGN_IN);
        }
    }
    public void checkIfEnseignantAndStart(final FirebaseUser enseignantUser)   {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                if (isEnseignantLoggedIn())   {

                    startMainActivity();
                    finish();
                }
                else    {

                    signOut();
                    Utils.showSnackBar(mainLinearLayout, getResources()
                            .getString(R.string.email_nexiste_pas_message));
                }
            }
        });

        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.chargement_enseignants_message));

        String pathToEnseignants = Utils.firebasePath(Utils.ENSEIGNANT_MODULE);
        Query query = Utils.database.getReference(pathToEnseignants).orderByChild("email")
                .equalTo(enseignantUser.getEmail());

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

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {

                checkIfEnseignantAndStart(FirebaseAuth.getInstance().getCurrentUser());
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button

                    Utils.showSnackBar(mainLinearLayout, getString(R.string.erreur_back_button_pressed_message));
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {

                    Utils.showSnackBar(mainLinearLayout, getString(R.string.no_network_message));
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {

                    Utils.showSnackBar(mainLinearLayout, getString(R.string.unknown_error));
                    return;
                }
            }
        }
    }
    public Boolean isEnseignantLoggedIn() {

        return (Utils.enseignant != null);

    }
    public void signOut()   {

        AuthUI.getInstance().signOut(this);
        Utils.enseignant = null;
    }
}
