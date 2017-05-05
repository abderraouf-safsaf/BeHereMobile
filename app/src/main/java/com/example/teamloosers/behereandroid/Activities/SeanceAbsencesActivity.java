package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
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
import com.firebase.ui.FirebaseUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class SeanceAbsencesActivity extends AppCompatActivity {

    private Module module;
    private Groupe groupe;
    private Seance seance;

    private RecyclerView seanceAbsencesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seance_absences);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");
        this.seance = (Seance) getIntent().getExtras().getSerializable("seance");

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

        seanceAbsencesRecyclerView = (RecyclerView) findViewById(R.id.seanceAbsencesRecyclerView);
        LinearLayoutManager seancesLinearLayoutManager = new LinearLayoutManager(this);

        seanceAbsencesRecyclerView.setLayoutManager(seancesLinearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(seanceAbsencesRecyclerView.getContext(),
                seancesLinearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        seanceAbsencesRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onStart() {

        super.onStart();

        loadSeanceAbsences();
    }

    private void loadSeanceAbsences() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_absences_loading_message));

        String structureId = seance.getTypeSeance().equals(Seance.COURS)?
                seance.getIdSection(): seance.getIdGroupe();
        String pathToSeance = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(),
                module.getId(), seance.getTypeSeance(), structureId, seance.getId());
        Query myQuery = Utils.database.getReference(pathToSeance).orderByChild("typeSeance").
                equalTo(seance.getTypeSeance());

        myQuery.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        FirebaseRecyclerAdapterViewer<Absence, SeanceAbsenceViewHolder> seanceAbsencesAdapter = new FirebaseRecyclerAdapterViewer<Absence, SeanceAbsenceViewHolder>(
                Absence.class, R.layout.view_holder_seance_absence, SeanceAbsenceViewHolder.class, myQuery
        ) {
            @Override
            protected void populateView(SeanceAbsenceViewHolder viewHolder, Absence absence, int position) {

                setViewHolderWithAbsenceInformation(viewHolder, absence);
            }

            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }
        };

        seanceAbsencesRecyclerView.setAdapter(seanceAbsencesAdapter);
    }

    private void setViewHolderWithAbsenceInformation(final SeanceAbsenceViewHolder viewHolder, final Absence absence) {


        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, absence.getIdCycle(), absence.getIdFilliere(),
                absence.getIdPromo(), absence.getIdSection(), absence.getIdGroupe(), absence.getIdEtudiant());

        DatabaseReference myRef = Utils.database.getReference(pathToEtudiant);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Etudiant etudiant = dataSnapshot.getValue(Etudiant.class);
                String nomPrenomEtudiant = String.format("%s %s", etudiant.getNom(), etudiant.getPrenom());
                viewHolder.nomPrenomEtudiantTextView.setText(nomPrenomEtudiant);
                viewHolder.supprimerAbsenceImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SeanceAbsencesActivity.this);
                        alertDialog.setMessage(R.string.confirmer_suppression_absence_message);
                        alertDialog.setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                absence.supprimerDb(Utils.database);
                                Toast.makeText(SeanceAbsencesActivity.this, R.string.absence_supprimee_message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.setNegativeButton(R.string.non, null);
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class SeanceAbsenceViewHolder extends ItemViewHolder {

        TextView nomPrenomEtudiantTextView;
        ImageButton supprimerAbsenceImageButton;

        public SeanceAbsenceViewHolder(View itemView) {

            super(itemView);

            nomPrenomEtudiantTextView = (TextView) itemView.findViewById(R.id.nomPrenomEtudiantTextView);
            supprimerAbsenceImageButton = (ImageButton) itemView.findViewById(R.id.supprimerAbsenceImageButton);

        }
    }
}
