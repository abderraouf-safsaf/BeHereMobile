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

import com.example.teamloosers.behereandroid.DatePickerFragment;
import com.example.teamloosers.behereandroid.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils;
import com.google.firebase.database.Query;

public class ListEtudiantsActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;
    private RecyclerView etudiantsListRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_etudiants);

        module = (Module) getIntent().getExtras().getSerializable("module");
        groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String toolbarTitle = String.format("M. %s %s", Utils.enseignant.getNom(), Utils.enseignant.getPrenom());
        String toolbarSubTitle = String.format("%s: %s", module.getDesignation(), groupe.getDesignation());
        toolbar.setTitle(toolbarTitle);
        toolbar.setSubtitle(toolbarSubTitle);

        setSupportActionBar(toolbar);

        etudiantsListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsListRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsListRecyclerView.setLayoutManager(linearLayoutManager);

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

                //TODO: changer l'intent
                /*DatePickerFragment nouvelSeanceDialog = new DatePickerFragment();

                Bundle args = new Bundle();
                args.putSerializable("module", module);
                args.putSerializable("groupe", groupe);
                nouvelSeanceDialog.setArguments(args);

                nouvelSeanceDialog.show(getSupportFragmentManager(), "datePicker");*/

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

    public static class EtudiantViewHolder extends ItemViewHolder {

        TextView etudiantNomPrenomTextView;

        public EtudiantViewHolder(View itemView) {

            super(itemView);

            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
        }

    }
}
