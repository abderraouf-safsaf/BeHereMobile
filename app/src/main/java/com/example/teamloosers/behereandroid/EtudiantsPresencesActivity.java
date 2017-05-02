package com.example.teamloosers.behereandroid;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Seance;

import java.util.ArrayList;
import java.util.HashMap;

public class EtudiantsPresencesActivity extends AppCompatActivity {

    private HashMap<Etudiant, Boolean> etudiantsPresenecesHashMap;
    private Seance seance;

    private RecyclerView etudiantsPresencesRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiants_presences);

        etudiantsPresenecesHashMap = (HashMap<Etudiant, Boolean>) getIntent().getExtras().getSerializable(
                "etudiantsPresencesHashMap");
        seance = (Seance) getIntent().getExtras().getSerializable("seance");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String toolbarTitle = String.format("Seance: %s", seance.getDate());
        toolbar.setTitle(toolbarTitle);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EtudiantsPresencesActivity.this);
                alertDialog.setMessage(R.string.valider_appel_message);
                alertDialog.setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ajouterAbsencesDb();
                        Toast.makeText(EtudiantsPresencesActivity.this, R.string.valider_appel_toast, Toast.LENGTH_SHORT).show();
                        finish();
                        }
                });
                alertDialog.setNegativeButton(R.string.non, null);
                alertDialog.show();
            }
        });




        etudiantsPresencesRecyclerView = (RecyclerView) findViewById(R.id.etudiantsPresencesRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsPresencesRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {

        super.onStart();

        EtudiantPresenceRecyclerViewAdapter etudiantsPresencesAdapter = new EtudiantPresenceRecyclerViewAdapter(
                etudiantsPresenecesHashMap);
        etudiantsPresencesRecyclerView.setAdapter(etudiantsPresencesAdapter);

    }

    private void ajouterAbsencesDb() {

        for (Etudiant etudiant: etudiantsPresenecesHashMap.keySet())  {

            Boolean isPresent = etudiantsPresenecesHashMap.get(etudiant);
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

    public class EtudiantPresenceRecyclerViewAdapter extends RecyclerView.Adapter<EtudiantPresenceRecyclerViewAdapter.ViewHolder> {

        private final HashMap<Etudiant, Boolean> etudiantsPresenceHashMap;
        private ArrayList<Etudiant> etudiantsList;

        public EtudiantPresenceRecyclerViewAdapter(HashMap<Etudiant, Boolean> etudiantsPresenceHashMap) {

            this.etudiantsPresenceHashMap = etudiantsPresenceHashMap;

            this.etudiantsList = new ArrayList<>(etudiantsPresenceHashMap.keySet());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_etudiant_presence, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            Etudiant etudiant = etudiantsList.get(position);
            holder.etudiant = etudiant;
            holder.isPresent = etudiantsPresenceHashMap.get(etudiant);
            holder.setViews();
        }

        @Override
        public int getItemCount() {
            return etudiantsPresenceHashMap.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public View mView;
            public TextView etudiantNomPrenom;
            public Switch absentSwitch;
            public Etudiant etudiant;
            public Boolean isPresent;

            public ViewHolder(View view) {

                super(view);
                mView = view;
                etudiantNomPrenom = (TextView) view.findViewById(R.id.etudiantTextView);
                absentSwitch = (Switch) view.findViewById(R.id.absentSwitch);
            }

            public void setViews()  {

                etudiantNomPrenom.setText(String.format("%s %s", etudiant.getNom(), etudiant.getPrenom()));
                absentSwitch.setChecked(isPresent);
            }
        }
    }
}
