package com.example.teamloosers.behereandroid.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Utils.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class ListEtudiantsActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;

    private Toolbar toolbar;
    private RecyclerView etudiantsListRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_etudiants);

        module = (Module) getIntent().getExtras().getSerializable("module");
        groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String toolbarTitle = String.format("M. %s %s", Utils.enseignant.getNom(), Utils.enseignant.getPrenom());
        String toolbarSubTitle = String.format("%s: %s", module.getDesignation(), groupe.getDesignation());
        toolbar.setTitle(toolbarTitle);
        toolbar.setSubtitle(toolbarSubTitle);

        setSupportActionBar(toolbar);

        etudiantsListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsListRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsListRecyclerView.setLayoutManager(linearLayoutManager);

        SlideInRightAnimator animator = new SlideInRightAnimator();
        animator.setAddDuration(300);
        etudiantsListRecyclerView.setItemAnimator(animator);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(etudiantsListRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        etudiantsListRecyclerView.addItemDecoration(dividerItemDecoration);


        FloatingActionButton seancesFloatButton = (FloatingActionButton) findViewById(R.id.seancesFloatButton);
        seancesFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent seancesIntent = new Intent(ListEtudiantsActivity.this, SeancesActivity.class);

                seancesIntent.putExtra("module", module);
                seancesIntent.putExtra("groupe", groupe);
                seancesIntent.putExtra("typeSeance", Seance.TD);

                startActivity(seancesIntent);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Intent appelListIntent = new Intent(ListEtudiantsActivity.this, AppelListActivity.class);
                appelListIntent.putExtra("module", module);
                appelListIntent.putExtra("groupe", groupe);

                startActivity(appelListIntent);
            }
        });
    }
    @Override
    protected void onStart() {

        super.onStart();



        loadEtudiants();
    }

    private void loadEtudiants() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        String pathToGroupe = Utils.firebasePath(Utils.CYCLES, groupe.getIdCycle(), groupe.getIdFilliere(), groupe.getIdPromo(),
                groupe.getIdSection(), groupe.getId());
        Query myRef = Utils.database.getReference(pathToGroupe).orderByChild("idCycle").
                equalTo(groupe.getIdCycle());
        myRef.keepSynced(true); // Keeping data fresh
        loadingProgressDialog.show();
        final FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder> etudiantsListAdapater = new FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder>(
                Etudiant.class, R.layout.view_holder_etudiant, EtudiantViewHolder.class, myRef
        ) {
            @Override
            protected void populateView(EtudiantViewHolder viewHolder, Etudiant etudiant, int position) {

                String nomEtPrenom = String.format("%s %s", etudiant.getNom(), etudiant.getPrenom());
                viewHolder.etudiantNomPrenomTextView.setText(nomEtPrenom);

                setNbAbsenceTextView(viewHolder.etudiantNbAbsencesTextView, etudiant);
            }

            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }

        };
        etudiantsListAdapater.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(ListEtudiantsActivity.this, ConsultationEtudiantsActivity.class);
                intent.putExtra("etudiantsList", etudiantsListAdapater.getItems());
                intent.putExtra("currentEtudiantPosition", position);
                intent.putExtra("module", module);

                startActivity(intent);
            }
        });
        etudiantsListRecyclerView.setAdapter(etudiantsListAdapater);
    }

    private void setNbAbsenceTextView(final TextView etudiantNbAbsencesTextView, Etudiant etudiant) {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(),
                etudiant.getId(), module.getId());

        Query etudiantRef = Utils.database.getReference(pathToEtudiant).orderByChild("idModule")
                .equalTo(module.getId());
        etudiantRef.keepSynced(true); // Keeping data fresh

        etudiantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long etudiantNbAbsences = dataSnapshot.getChildrenCount();
                displayNbAbsencesInTextView(etudiantNbAbsencesTextView, etudiantNbAbsences);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }

    private void displayNbAbsencesInTextView(TextView etudiantNbAbsencesTextView, long etudiantNbAbsences) {

        int textColor;
        switch (Integer.valueOf(String.format("%d", etudiantNbAbsences))) {

            case 0:
                textColor = ContextCompat.getColor(this, R.color.textSecondary);
                break;
            case 1:
                textColor = ContextCompat.getColor(this, R.color.textSecondary);
                break;
            case 2:
                textColor = ContextCompat.getColor(this, R.color.deux_absences);
                break;
            default: textColor = ContextCompat.getColor(this, R.color.plus_deux_absences);
        }
        etudiantNbAbsencesTextView.setText(String.format("%d", etudiantNbAbsences));
        etudiantNbAbsencesTextView.setTextColor(textColor);
    }

    public static class EtudiantViewHolder extends ItemViewHolder {

        TextView etudiantNomPrenomTextView, etudiantNbAbsencesTextView;

        public EtudiantViewHolder(View itemView) {

            super(itemView);

            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
            etudiantNbAbsencesTextView = (TextView) itemView.findViewById(R.id.etudiantNbAbsences);
        }

    }
}
