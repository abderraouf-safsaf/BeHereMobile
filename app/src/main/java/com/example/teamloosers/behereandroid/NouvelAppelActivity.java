package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NouvelAppelActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;
    private Seance seance;
    private ArrayList<Etudiant> etudiantsList = new ArrayList<>();
    private HashMap<Etudiant, Boolean> etudiantPresenceHashMap = new HashMap<>();

    private AdapterViewFlipper etudiantsAppelAdapterViewFlipper;
    private ImageButton absentImageButton, presentImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouvel_appel);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");

        int annee = getIntent().getExtras().getInt("annee");
        int mois = getIntent().getExtras().getInt("mois");
        int jour = getIntent().getExtras().getInt("jour");


        seance = new Seance();
        seance.setId(Utils.generateId());
        seance.setDate(String.format("%d/%d/%d", jour, mois, annee));
        seance.setIdEnseignant(Utils.enseignant.getId());
        seance.setIdGroupe(groupe.getId());
        seance.setIdModule(module.getId());
        seance.setTypeSeance(Seance.TD);
        seance.ajouterSeance(Utils.database);


        etudiantsAppelAdapterViewFlipper = (AdapterViewFlipper) findViewById(R.id.etudiantsAppelAdapterViewFlipper);

        absentImageButton = (ImageButton) findViewById(R.id.absentImageButton);
        presentImageButton = (ImageButton) findViewById(R.id.presentImageButton);
    }

    @Override
    protected void onStart() {

        super.onStart();

        absentImageButton.setOnClickListener(new PresenceButtonListener());
        presentImageButton.setOnClickListener(new PresenceButtonListener());

        //loadEtudiants();
        loadEtudiantsToViewFlipper();
    }

    private void loadEtudiants() {

        String pathToGroupe = Utils.firebasePath(Utils.CYCLES, groupe.getIdCycle(), groupe.getIdFilliere(), groupe.getIdPromo(),
                groupe.getIdSection(), groupe.getId());
        DatabaseReference myRef =  Utils.database.getReference(pathToGroupe);

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        loadingProgressDialog.show();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren())    {

                    if (snapshot.hasChildren()) {

                        Etudiant etudiant = snapshot.getValue(Etudiant.class);
                        etudiantsList.add(etudiant);
                    }
                }
                loadEtudiantsToViewFlipper();
                loadingProgressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void finAppel() {

        Intent etudiantsPresenceIntent = new Intent(this, EtudiantsPresencesActivity.class);
        etudiantsPresenceIntent.putExtra("etudiantsPresencesHashMap", etudiantPresenceHashMap);
        etudiantsPresenceIntent.putExtra("seance", seance);

        startActivity(etudiantsPresenceIntent);
    }
    private void loadEtudiantsToViewFlipper() {

        EtudiantsAppelAdapter etudiantsArrayAdapter = new EtudiantsAppelAdapter(this, etudiantsList);
        etudiantsAppelAdapterViewFlipper.setAdapter(etudiantsArrayAdapter);
    }

    public class EtudiantsAppelAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Etudiant> etudiantsList;
        private LayoutInflater inflater;

        public EtudiantsAppelAdapter(Context context, ArrayList<Etudiant> etudiantsList) {

            this.context = context;
            this.etudiantsList = etudiantsList;

            inflater = (LayoutInflater.from(this.context));
        }

        @Override
        public int getCount() {
            return etudiantsList.size();
        }

        @Override
        public Object getItem(int position) {
            return etudiantsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.view_holder_consultation_etudiant, null);

            ImageView etudiantImageView = (ImageView) convertView.findViewById(R.id.etudiantImageView);
            TextView nomTextView = (TextView) convertView.findViewById(R.id.nomTextView);
            TextView prenomTextView = (TextView) convertView.findViewById(R.id.prenomTextView);
            TextView emailTextView = (TextView) convertView.findViewById(R.id.emailTextView);

            Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.com_facebook_profile_picture_blank_portrait);
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, defaultImage);
            dr.setCornerRadius(200);
            etudiantImageView.setImageDrawable(dr);

            Etudiant etudiant = (Etudiant) getItem(position);

            nomTextView.setText(etudiant.getNom());
            prenomTextView.setText(etudiant.getPrenom());
            emailTextView.setText(etudiant.getEmail());

            return convertView;
        }
    }

    class PresenceButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Etudiant displayedEtudiant = (Etudiant) etudiantsAppelAdapterViewFlipper.getItemAtPosition(
                    etudiantsAppelAdapterViewFlipper.getDisplayedChild());

            ImageButton clickedButton = (ImageButton) v;
            if (clickedButton == presentImageButton)    {

                etudiantPresenceHashMap.put(displayedEtudiant, Absence.PRESENT);
            }
            else if (clickedButton == absentImageButton)
                etudiantPresenceHashMap.put(displayedEtudiant, Absence.ABSENT);

            if (etudiantsAppelAdapterViewFlipper.getDisplayedChild() == etudiantsAppelAdapterViewFlipper.
                    getChildCount())
                finAppel();
            else
                etudiantsAppelAdapterViewFlipper.showNext();
        }
    }
}
