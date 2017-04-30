package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListEtudiantsActivity extends AppCompatActivity {

    private RecyclerView etudiantsListRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        etudiantsListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsListRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsListRecyclerView.setLayoutManager(linearLayoutManager);

        etudiantsListRecyclerView.setHasFixedSize(true);

        loadEtudiants();
    }

    private void loadEtudiants() {
        
        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        loadingProgressDialog.show();
        DatabaseReference myRef =  Utils.database.getReference("/Cycles/Cyclepreparatoireintegree_d35f9666-8af2-4007-8f2f-0550878f6cd1/Informatique_db6f8a0e-db55-4ed5-9112-9fb74858a5e7/Premiereannee_58087455-b575-4cce-8f0c-9252471a36ca/SectionA_cd1aee5d-17f4-4ccb-9cec-100adf08effe/Groupe2_75ac381c-2fe6-4ac5-aaa5-4f325396b2f0/");
        FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder> adapter = new FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder>(
                Etudiant.class, R.layout.etudiant_item_view, EtudiantViewHolder.class, myRef
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
        etudiantsListRecyclerView.setAdapter(adapter);
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder  {

        TextView etudiantNomPrenomTextView;
        public EtudiantViewHolder(View itemView) {

            super(itemView);

            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
        }
    }
}
