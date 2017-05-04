package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import org.w3c.dom.Text;

public class SeancesActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;
    private String typeSeance;

    private RecyclerView seancesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seances);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");
        this.typeSeance = getIntent().getExtras().getString("typeSeance");

        seancesRecyclerView = (RecyclerView) findViewById(R.id.seancesRecyclerView);
        LinearLayoutManager seancesLinearLayoutManager = new LinearLayoutManager(this);
        seancesRecyclerView.setLayoutManager(seancesLinearLayoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment nouvelSeanceDialog = new DatePickerFragment();

                Bundle args = new Bundle();
                args.putSerializable("module", module);
                args.putSerializable("groupe", groupe);
                nouvelSeanceDialog.setArguments(args);

                nouvelSeanceDialog.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();

        loadSeances();
    }

    private void loadSeances() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_seances_loading_message));

        String pathToGroupe = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(),
                module.getId(), typeSeance, groupe.getId());
        Query myRef = Utils.database.getReference(pathToGroupe).orderByChild("idModule").
                equalTo(module.getId());
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        final FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder> seancesAdapter = new FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder>(
                Seance.class, R.layout.view_holder_seance, SeanceViewHolder.class, myRef
        ) {
            @Override
            protected void populateView(SeanceViewHolder viewHolder, Seance seance, int position) {

                String dateSeance = seance.getDate();

                //TODO: mettre la date de la seance au lieu que l'id
                String idSeance = seance.getId();
                viewHolder.dateSeanceTextView.setText(idSeance);
            }

            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }
        };
        seancesAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(SeancesActivity.this, SeanceAbsencesActivity.class);

                intent.putExtra("module", module);
                intent.putExtra("groupe", groupe);
                intent.putExtra("seance", seancesAdapter.getItem(position));

                startActivity(intent);
            }
        });
        seancesRecyclerView.setAdapter(seancesAdapter);
    }
    public static class SeanceViewHolder extends ItemViewHolder{

        TextView dateSeanceTextView;

        public SeanceViewHolder(View itemView) {

            super(itemView);

            dateSeanceTextView = (TextView) itemView.findViewById(R.id.dateSeanceTextView);
        }
    }
}
