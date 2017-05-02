package com.example.teamloosers.behereandroid;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.google.firebase.database.DatabaseReference;
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

        etudiantsListRecyclerView.setHasFixedSize(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(etudiantsListRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        etudiantsListRecyclerView.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Snackbar.make(view, R.string.nouvel_appel_snackbar_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.nouvel_appel_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                DatePickerFragment nouvelSeanceDialog = new DatePickerFragment();

                                Bundle args = new Bundle();
                                args.putSerializable("module", module);
                                args.putSerializable("groupe", groupe);
                                nouvelSeanceDialog.setArguments(args);

                                nouvelSeanceDialog.show(getSupportFragmentManager(), "datePicker");
                            }
                        }).show();
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

                Intent intent = new Intent(ListEtudiantsActivity.this, ConsultationEtudiantActivity.class);
                intent.putExtra("etudiantsList", etudiantsListAdapater.getItems());
                intent.putExtra("currentEtudiantPosition", position);
                intent.putExtra("module", module);

                startActivity(intent);
            }
        });
        etudiantsListRecyclerView.setAdapter(etudiantsListAdapater);
    }

    public static class EtudiantViewHolder extends ItemViewHolder  {

        TextView etudiantNomPrenomTextView;

        public EtudiantViewHolder(View itemView) {

            super(itemView);

            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
        }

    }
}
