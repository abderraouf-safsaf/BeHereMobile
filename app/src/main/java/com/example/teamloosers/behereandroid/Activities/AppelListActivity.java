package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils;
import com.firebase.ui.database.ChangeEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AppelListActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;
    private Seance seance;

    private int annee, mois, jour;

    private RecyclerView etudiantAppelListRecyclerView;
    private FloatingActionButton validerAppelFloatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appel_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");

        Calendar calendar = Calendar.getInstance();
        annee = calendar.get(Calendar.YEAR);
        mois = calendar.get(Calendar.MONTH);
        jour = calendar.get(Calendar.DAY_OF_MONTH);


        etudiantAppelListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsAppelListRecyclerView);
        validerAppelFloatButton = (FloatingActionButton) findViewById(R.id.validerAppelFloatButton);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantAppelListRecyclerView.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
            }
        });

        validerAppelFloatButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                instancierNouvelleSeance(jour, mois, annee);

                ajouterAbsencesDb();
                Toast.makeText(AppelListActivity.this, R.string.valider_appel_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        loadEtudiant();
    }

    private void loadEtudiant() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        String pathToGroupe = Utils.firebasePath(Utils.CYCLES, groupe.getIdCycle(), groupe.getIdFilliere(), groupe.getIdPromo(),
                groupe.getIdSection(), groupe.getId());
        Query myRef =  Utils.database.getReference(pathToGroupe).orderByChild("idCycle").equalTo(
                groupe.getIdCycle());
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        FirebaseRecyclerAdapterViewer<Etudiant, EtudiantPresenceViewHolder> etudiantAppelListAdapter = new FirebaseRecyclerAdapterViewer<Etudiant, EtudiantPresenceViewHolder>(
                Etudiant.class, R.layout.view_holder_etudiant_appel_presence, EtudiantPresenceViewHolder.class,
                myRef
        ) {
            @Override
            protected void populateView(EtudiantPresenceViewHolder viewHolder, Etudiant etudiant, int position) {

                viewHolder.etudiant = etudiant;
                viewHolder.etudiantNomPrenomTextView.setText(String.format("%s %s", etudiant.getNom(),
                        etudiant.getPrenom()));
                viewHolder.presenceSwitch.setChecked(true);
            }
            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex) {

                super.onChildChanged(type, index, oldIndex);

                loadingProgressDialog.dismiss();
            }
        };
        etudiantAppelListRecyclerView.setAdapter(etudiantAppelListAdapter);
    }

    private void instancierNouvelleSeance(int jour, int mois, int annee) {

        seance = new Seance(jour, mois, annee);
        seance.setId(Utils.generateId());
        seance.setIdEnseignant(Utils.enseignant.getId());
        seance.setIdGroupe(groupe.getId());
        seance.setIdModule(module.getId());
        seance.setTypeSeance(Seance.TD);
        seance.ajouterSeance(Utils.database);
    }
    private void ajouterAbsencesDb() {

        for (int i = 0; i < etudiantAppelListRecyclerView.getChildCount(); i++)  {

            EtudiantPresenceViewHolder etudiantPresenceViewHolder = (EtudiantPresenceViewHolder) etudiantAppelListRecyclerView
                    .findViewHolderForLayoutPosition(i);

            Etudiant etudiant = etudiantPresenceViewHolder.etudiant;
            Boolean isPresent = etudiantPresenceViewHolder.presenceSwitch.isChecked();

            if (!isPresent) {

                Absence absence = new Absence();
                absence.setId(Utils.generateId());
                absence.setIdCycle(etudiant.getIdCycle());
                absence.setIdFilliere(etudiant.getIdFilliere());
                absence.setIdPromo(etudiant.getIdPromo());
                absence.setIdSection(etudiant.getIdSection());
                absence.setIdGroupe(etudiant.getIdGroupe());
                absence.setIdEtudiant(etudiant.getId());
                absence.setIdEnseignant(seance.getIdEnseignant());
                absence.setIdModule(seance.getIdModule());
                absence.setIdSeance(seance.getId());
                absence.setTypeSeance(seance.getTypeSeance());
                absence.setDate(seance.getDate());
                absence.ajouterDb(Utils.database);
            }
        }
    }
    public static class EtudiantPresenceViewHolder extends ItemViewHolder {

        Etudiant etudiant;
        TextView etudiantNomPrenomTextView;
        Switch presenceSwitch;

        public EtudiantPresenceViewHolder(View itemView) {

            super(itemView);

            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
            presenceSwitch = (Switch) itemView.findViewById(R.id.presenceSwitch);
        }
    }
}
