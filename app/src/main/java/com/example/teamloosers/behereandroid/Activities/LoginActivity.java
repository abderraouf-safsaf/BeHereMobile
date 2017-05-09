package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bumptech.glide.util.Util;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private LinearLayout mainLinearLayout;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                if (Utils.auth.getCurrentUser() != null)   {

                    startMainActivity();
                    finish();
                }
                else    {

                    Utils.auth.signOut();
                    Utils.showSnackBar(mainLinearLayout, getResources()
                            .getString(R.string.email_nexiste_pas_message));
                }
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (Utils.auth.getCurrentUser() != null)    {

            loadEnseignantFromUser(Utils.auth.getCurrentUser());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {

                loadEnseignantFromUser(Utils.auth.getCurrentUser());
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button

                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {

                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {

                    return;
                }
            }
        }
    }
    public void startMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }
    public void startLogin(View view) {

        if (Utils.auth.getCurrentUser() != null) {

            loadEnseignantFromUser(Utils.auth.getCurrentUser());
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .setTheme(R.style.BeHere)
                            .build(),
                    RC_SIGN_IN);
        }

    }
    public void loadEnseignantFromUser(final FirebaseUser enseignantUser)   {

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

                    Utils.enseignant = snapshot.getValue(Enseignant.class);
                }
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
