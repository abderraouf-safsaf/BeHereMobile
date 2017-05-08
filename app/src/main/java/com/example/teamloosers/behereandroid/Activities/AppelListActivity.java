package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.DatePickerFragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AppelListActivity extends AppCompatActivity implements DatePickerFragment.OnDateSelectedListener {

    private Module module;
    private Groupe groupe;
    private Seance seance;

    private int annee, mois, jour;

    private CoordinatorLayout mainLayout;
    private RecyclerView etudiantAppelListRecyclerView;
    private Button validerAppelButton, modifierDateButton;
    private TextView dateSeanceTextView;
    private FloatingActionButton validerAppelFloatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_appel_list);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");

        Calendar calendar = Calendar.getInstance();
        annee = calendar.get(Calendar.YEAR);
        mois = calendar.get(Calendar.MONTH);
        jour = calendar.get(Calendar.DAY_OF_MONTH);

        etudiantAppelListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsAppelListRecyclerView);
        validerAppelButton = (Button) findViewById(R.id.validerAppelButton);
        modifierDateButton = (Button) findViewById(R.id.modifierDateButton);
        dateSeanceTextView = (TextView) findViewById(R.id.dateSeanceTextView);
        mainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);

        updateDateSeanceTextView();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantAppelListRecyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(etudiantAppelListRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        etudiantAppelListRecyclerView.addItemDecoration(dividerItemDecoration);

        modifierDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment changerDateDialog = new DatePickerFragment();
                changerDateDialog.show(getSupportFragmentManager(), "datePickerDialog");
            }
        });

        validerAppelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                instancierNouvelleSeance(jour, mois, annee);

                ajouterAbsencesDb();
                Toast.makeText(AppelListActivity.this, R.string.valider_appel_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDateSeanceTextView() {
        dateSeanceTextView.setText(String.format("%s/%s/%s", jour, mois, annee));
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
            protected void populateView(EtudiantPresenceViewHolder viewHolder, final Etudiant etudiant, int position) {

                viewHolder.etudiant = etudiant;

                int etudiantImageHeight = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_height);
                int etudiantImageWidth = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_width);
                Bitmap image = Utils.decodeToImage(etudiant.getImageBase64());
                Bitmap imageResized = Bitmap.createScaledBitmap(image, etudiantImageWidth, etudiantImageHeight, true);

                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, imageResized);
                dr.setCornerRadius(200);
                viewHolder.etudiantSmallImageView.setImageDrawable(dr);

                viewHolder.etudiantNomPrenomTextView.setText(String.format("%s %s", etudiant.getNom(),
                        etudiant.getPrenom()));

                setNbAbsenceTextView(viewHolder.etudiantNbAbsencesTextView, etudiant);
                viewHolder.presenceSwitch.setChecked(true);

                loadEtudiantScore(etudiant, viewHolder.etudiantScoreTextView);

                viewHolder.likeImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        incrementerScore(etudiant);
                    }
                });
                viewHolder.dislikeImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        decrementerScore(etudiant);
                    }
                });
            }
            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex) {

                super.onChildChanged(type, index, oldIndex);

                loadingProgressDialog.dismiss();
            }
        };
        etudiantAppelListRecyclerView.setAdapter(etudiantAppelListAdapter);
    }
    private void loadEtudiantScore(Etudiant etudiant, final TextView etudiantScoreTextView    ) {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId(),
                Utils.SCORE);

        Query scoreRef = Utils.database.getReference(pathToEtudiantScore);
        scoreRef.keepSynced(true); // Keeping data fresh

        scoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Long score = (Long) dataSnapshot.getValue();
                score = (score == null)? 0: score;
                etudiantScoreTextView.setText(String.valueOf(score));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    private void incrementerScore(Etudiant etudiant) {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId(),
                Utils.SCORE);

        Query scoreRef = Utils.database.getReference(pathToEtudiantScore);
        scoreRef.keepSynced(true); // Keeping data fresh

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Long score = (Long) dataSnapshot.getValue();
                Long newScore = (score == null)? 1: score + 1;
                dataSnapshot.getRef().setValue(newScore);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    private void decrementerScore(Etudiant etudiant)    {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId(),
                Utils.SCORE);

        Query scoreRef = Utils.database.getReference(pathToEtudiantScore);
        scoreRef.keepSynced(true); // Keeping data fresh

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Long score = (Long) dataSnapshot.getValue();
                Long newScore = (score == null)? 1: score - 1;
                dataSnapshot.getRef().setValue(newScore);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
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
    @Override
    public void onDateSelected(int day, int month, int year) {

        this.jour = day;
        this.mois = month;
        this.annee = year;

        updateDateSeanceTextView();
    }

    public static class EtudiantPresenceViewHolder extends ItemViewHolder {

        Etudiant etudiant;
        ImageView etudiantSmallImageView;
        TextView etudiantNomPrenomTextView, etudiantNbAbsencesTextView, etudiantScoreTextView;
        Switch presenceSwitch;
        ImageButton likeImageButton, dislikeImageButton;

        public EtudiantPresenceViewHolder(View itemView) {

            super(itemView);

            etudiantSmallImageView = (ImageView) itemView.findViewById(R.id.etudiantSmallImageView);
            etudiantNomPrenomTextView = (TextView) itemView.findViewById(R.id.etudiantNomPrenomTextView);
            etudiantNbAbsencesTextView = (TextView) itemView.findViewById(R.id.etudiantNbAbsencesTextView);
            etudiantScoreTextView = (TextView) itemView.findViewById(R.id.etudiantScoreTextView);
            presenceSwitch = (Switch) itemView.findViewById(R.id.presenceSwitch);
            likeImageButton = (ImageButton) itemView.findViewById(R.id.likeImageButton);
            dislikeImageButton = (ImageButton) itemView.findViewById(R.id.dislikeImageButton);
        }
    }
}
