package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.Fragments.DatePickerFragment;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Section;
import com.example.teamloosers.behereandroid.Utils.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Structures.Structurable;
import com.example.teamloosers.behereandroid.Utils.SpotlightSequence;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.database.ChangeEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class AppelListActivity <T extends Structurable> extends AppCompatActivity
        implements DatePickerFragment.OnDateSelectedListener, View.OnClickListener {

    public static final int APPEL_UN_PAR_UN_RC = 1;

    private Module module;
    private T structure;
    private Seance seance;

    private int annee, mois, jour, heureDebut, minuteDebut;

    private SectionEtudiantsRecyclerAdapter etudiantsRecyclerAdapter;

    private ProgressDialog loadingProgressDialog;
    private Toolbar toolbar;
    private RecyclerView etudiantAppelListRecyclerView;
    private TextView dateTextView, heureTextView;
    private Button modifierDateButton, modifierHeureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Utils.makeActivityFullScreen(this);

        setContentView(R.layout.activity_appel_list);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.structure = (T) getIntent().getExtras().getSerializable("structure");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        etudiantAppelListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsAppelListRecyclerView);
        modifierDateButton = (Button) findViewById(R.id.modifierDateButton);
        modifierHeureButton = (Button) findViewById(R.id.modifierHeureButton);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        heureTextView = (TextView) findViewById(R.id.heureTextView);

        SpotlightSequence spotlightSequence = new SpotlightSequence(this);
        spotlightSequence.addSpotlight(toolbar, R.string.valider_appel_spotlight_title,
                R.string.valider_appel_spotlight_subtitle, "");
        spotlightSequence.addSpotlight(modifierDateButton, R.string.date_spotlight_title,
                R.string.date_spotlight_subtitle, "datespotlight");
        spotlightSequence.startSequence();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeDateAndTime();

        updateDateSeanceTextView();
        updateHeureSeanceTextView();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantAppelListRecyclerView.setLayoutManager(linearLayoutManager);
        Utils.setRecyclerViewDecoration(etudiantAppelListRecyclerView);

        SlideInRightAnimator animator = new SlideInRightAnimator();
        animator.setAddDuration(getResources().getInteger(R.integer.animation_duration));
        etudiantAppelListRecyclerView.setItemAnimator(animator);

        modifierDateButton.setOnClickListener(this);
        modifierHeureButton.setOnClickListener(this);
    }
    @Override
    protected void onStart() {

        super.onStart();

        Utils.setActionBarTitle(this, getString(R.string.faire_appel_toolbar_title));
        Utils.setActionBarSubtitle(this,String.format("%s: %s", module.getDesignation(),
                structure.getDesignation()));

        if (structure instanceof Groupe)
            loadEtudiantGroupe();
        else if (structure instanceof Section)
            loadEtudiantSection();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_appel_list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.validerAppelMenuItem)   {

            instancierNouvelleSeance(jour, mois, annee, heureDebut, minuteDebut);

            ajouterAbsencesDb();
            Toast.makeText(AppelListActivity.this, R.string.valider_appel_toast, Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        Get the current student in an ArrayList from the RecyclerView
     */
    private ArrayList<Etudiant> getEtudiantsList() {

        ArrayList<Etudiant> etudiantsList = new ArrayList();

        for (int i = 0; i < etudiantAppelListRecyclerView.getChildCount(); i++) {

            EtudiantPresenceViewHolder etudiantPresenceViewHolder = (EtudiantPresenceViewHolder) etudiantAppelListRecyclerView
                    .findViewHolderForLayoutPosition(i);

            Etudiant etudiant = etudiantPresenceViewHolder.etudiant;
            etudiantsList.add(etudiant);
        }

        return etudiantsList;
    }

    /*
        Load all section students from database to etudiantsAppelListRecyclerView
     */
    private void loadEtudiantSection() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        final ArrayList<Etudiant> etudiantsList = new ArrayList<>();

        String pathToStructure = Utils.firebasePath(Utils.CYCLES, structure.getIdCycle(), structure.getIdFilliere(), structure.getIdPromo(),
                structure.getIdSection());

        Query groupeQuery = Utils.database.getReference(pathToStructure).orderByChild("id")
                .startAt("");
        groupeQuery.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        groupeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) { // Groupes

                    if (snapshot.hasChildren()) { // It's Groupe

                        for (DataSnapshot snapshot2: snapshot.getChildren())    {

                            if (snapshot2.hasChildren()) { // It's Etudiant

                                Etudiant etudiant = snapshot2.getValue(Etudiant.class);
                                etudiantsList.add(etudiant);
                            }
                        }
                    }
                }
                etudiantsRecyclerAdapter = new SectionEtudiantsRecyclerAdapter(etudiantsList);
                etudiantAppelListRecyclerView.setAdapter(etudiantsRecyclerAdapter);
                loadingProgressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(AppelListActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    /*
        Load all group students from database to etudiantsAppelListRecyclerView
     */
    private void loadEtudiantGroupe() {

        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string
                .chargement_etudiants_loading_message));

        String pathToGroupe = Utils.firebasePath(Utils.CYCLES, structure.getIdCycle(), structure.getIdFilliere(), structure.getIdPromo(),
                structure.getIdSection(), structure.getId());
        Query myRef =  Utils.database.getReference(pathToGroupe).orderByChild("nom").startAt("");
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        FirebaseRecyclerAdapterViewer<Etudiant, EtudiantPresenceViewHolder> etudiantAppelListAdapter =
                new FirebaseRecyclerAdapterViewer<Etudiant, EtudiantPresenceViewHolder>(
                Etudiant.class, R.layout.view_holder_etudiant_appel_presence, EtudiantPresenceViewHolder.class,
                myRef) {

                    @Override
            protected void populateView(EtudiantPresenceViewHolder viewHolder, final Etudiant etudiant, int position) {

                viewHolder.etudiant = etudiant;

                int etudiantImageHeight = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_height);
                int etudiantImageWidth = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_width);
                Bitmap image = Utils.decode64BaseImageToBmp(etudiant.getImageBase64());
                Bitmap imageResized = Bitmap.createScaledBitmap(image, etudiantImageWidth, etudiantImageHeight, true);

                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, imageResized);
                dr.setCornerRadius(10);
                viewHolder.etudiantSmallImageView.setImageDrawable(dr);

                viewHolder.etudiantNomPrenomTextView.setText(String.format("%s %s", etudiant.getNom(),
                        etudiant.getPrenom()));

                setNbAbsenceTextView(viewHolder.etudiantNbAbsencesTextView, etudiant);
                viewHolder.presenceSwitch.setChecked(true);

                loadEtudiantScore(etudiant, viewHolder.etudiantScoreTextView);

                viewHolder.likeImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        addToEtudiantScore(etudiant, Etudiant.SCORE_PLUS);
                    }
                });
                viewHolder.dislikeImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        addToEtudiantScore(etudiant, Etudiant.SCORE_MOIN);
                    }
                });

            }
            @Override
            protected void onChildChanged(ChangeEventListener.EventType type, int index, int oldIndex) {

                super.onChildChanged(type, index, oldIndex);

                loadingProgressDialog.dismiss();
            }
            @Override
            protected void onCancelled(DatabaseError error) {

                super.onCancelled(error);
                Utils.showSnackBar(AppelListActivity.this, Utils.DATABASE_ERR_MESSAGE);
                loadingProgressDialog.cancel();
            }
        };

        etudiantAppelListRecyclerView.setAdapter(etudiantAppelListAdapter);
    }

    /*
        Initialize date and time fields to current date
     */
    private void initializeDateAndTime() {

        Calendar calendar = Calendar.getInstance();
        annee = calendar.get(Calendar.YEAR);
        mois = calendar.get(Calendar.MONTH) + 1;
        jour = calendar.get(Calendar.DAY_OF_MONTH);

        heureDebut = calendar.get(Calendar.HOUR_OF_DAY);
        minuteDebut = calendar.get(Calendar.MINUTE);
    }

    /*
        Update date textview from date fields
     */
    private void updateDateSeanceTextView() {

        dateTextView.setText(String.format("%s/%s/%s", jour, mois, annee));
    }
    /*
        Update time textview from date fields
     */
    private void updateHeureSeanceTextView()    {

        heureTextView.setText(String.format("%s:%s", heureDebut, minuteDebut));
    }

    /*
        Get student score from database to textview
     */
    private void loadEtudiantScore(Etudiant etudiant, final TextView etudiantScoreTextView) {

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
                displayScoreOnTextView(etudiantScoreTextView, score);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(AppelListActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    /*
        Displaying student score in textview according to specific colors
     */
    private void displayScoreOnTextView(TextView etudiantNbAbsencesTextView, long etudiantScore) {

        int textColor;

        if (etudiantScore > 0)
            textColor = ContextCompat.getColor(this, R.color.score_positif);
        else if (etudiantScore < 0)
            textColor = ContextCompat.getColor(this, R.color.score_negatif);
        else
            textColor = ContextCompat.getColor(this, R.color.textSecondary);

        String prefix = (etudiantScore > 0)? "+": "";
        etudiantNbAbsencesTextView.setText(String.format("%s%d",
                prefix, etudiantScore));

        etudiantNbAbsencesTextView.setTextColor(textColor);
    }

    /*
        Add a "toAdd" value to student score in database
     */
    private void addToEtudiantScore(Etudiant etudiant, final int toAdd) {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId(),
                Utils.SCORE);

        Query scoreRef = Utils.database.getReference(pathToEtudiantScore);
        scoreRef.keepSynced(true); // Keeping data fresh

        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Long score = (Long) dataSnapshot.getValue();
                Long newScore = (score == null)? toAdd: score + toAdd;
                dataSnapshot.getRef().setValue(newScore);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(AppelListActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    /*
        Load student nb absences from database to text view
     */
    private void setNbAbsenceTextView(final TextView etudiantNbAbsencesTextView, Etudiant etudiant) {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(),
                etudiant.getId(), module.getId());

        String typeSeance = (structure instanceof Groupe)? Seance.TD: Seance.COURS;
        Query etudiantRef = Utils.database.getReference(pathToEtudiant).orderByChild("typeSeance")
                .equalTo(typeSeance);
        etudiantRef.keepSynced(true); // Keeping data fresh

        etudiantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long etudiantNbAbsences = dataSnapshot.getChildrenCount();
                displayNbAbsencesInTextView(etudiantNbAbsencesTextView, etudiantNbAbsences);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                Utils.showSnackBar(AppelListActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    /*
        Display student nb absences in textview according to specific colors
     */
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
    private void instancierNouvelleSeance(int jour, int mois, int annee, int heureDebut,
                                          int minuteDebut) {

        seance = new Seance(jour, mois, annee, heureDebut, minuteDebut);
        seance.setId(Utils.generateId());
        seance.setIdEnseignant(Utils.enseignant.getId());
        seance.setIdGroupe(structure.getId());
        seance.setIdSection(structure.getIdSection());
        seance.setIdModule(module.getId());
        if (structure instanceof Groupe)
            seance.setTypeSeance(Seance.TD);
        else if (structure instanceof Section)
            seance.setTypeSeance(Seance.COURS);
        seance.ajouterSeance(Utils.database);
    }

    /*
        Add all absences to database
     */
    private void ajouterAbsencesDb() {

        for (int i = 0; i < etudiantAppelListRecyclerView.getChildCount(); i++)  {

            EtudiantPresenceViewHolder etudiantPresenceViewHolder = (EtudiantPresenceViewHolder) etudiantAppelListRecyclerView
                    .findViewHolderForLayoutPosition(i);

            Etudiant etudiant = etudiantPresenceViewHolder.etudiant;
            Boolean isPresent = etudiantPresenceViewHolder.presenceSwitch.isChecked();

            if (!isPresent) {

                Absence absence = newAbsence(etudiant);
                absence.ajouterDb();
            }
        }
    }

    /*
        Instanciate a new absence object
     */
    private Absence newAbsence(Etudiant etudiant) {

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
        absence.setJustifier(false);

        return absence;
    }

    /*
        A specific methode implemented to treat date changes from DateDialogPicker
     */
    @Override
    public void onDateSelected(int day, int month, int year) {

        this.jour = day;
        this.mois = month + 1;
        this.annee = year;

        updateDateSeanceTextView();
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v == modifierDateButton)    {

            DatePickerFragment changerDateDialog = new DatePickerFragment();
            changerDateDialog.show(getSupportFragmentManager(), "datePickerDialog");
        }
        else if (v == modifierHeureButton)   {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    heureDebut = hourOfDay;
                    minuteDebut = minute;

                    updateHeureSeanceTextView();
                }
            }, hour, minute, false);

            timePickerDialog.show();
        }
    }
    public class SectionEtudiantsRecyclerAdapter extends RecyclerView.Adapter<EtudiantPresenceViewHolder> {

        private ArrayList<Etudiant> etudiantsList;

        private AdapterView.OnItemClickListener listener;

        public SectionEtudiantsRecyclerAdapter(ArrayList<Etudiant> etudiantsList) {

            this.etudiantsList = etudiantsList;
        }

        @Override
        public EtudiantPresenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_etudiant_appel_presence, parent, false);
            return new EtudiantPresenceViewHolder(view);
        }
        @Override
        public void onBindViewHolder(final EtudiantPresenceViewHolder holder, int position) {

            final Etudiant etudiant = etudiantsList.get(position);

            holder.etudiant = etudiant;
            int etudiantImageHeight = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_height);
            int etudiantImageWidth = getResources().getDimensionPixelSize(R.dimen.etudiant_small_image_width);
            Bitmap image = Utils.decode64BaseImageToBmp(etudiant.getImageBase64());
            Bitmap imageResized = Bitmap.createScaledBitmap(image, etudiantImageWidth, etudiantImageHeight, true);

            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, imageResized);
            dr.setCornerRadius(10);
            holder.etudiantSmallImageView.setImageDrawable(dr);

            holder.etudiantNomPrenomTextView.setText(String.format("%s %s", etudiant.getNom(),
                    etudiant.getPrenom()));

            setNbAbsenceTextView(holder.etudiantNbAbsencesTextView, etudiant);
            holder.presenceSwitch.setChecked(true);

            loadEtudiantScore(etudiant, holder.etudiantScoreTextView);

            holder.likeImageButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    addToEtudiantScore(etudiant, Etudiant.SCORE_PLUS);
                }
            });
            holder.dislikeImageButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    addToEtudiantScore(etudiant, Etudiant.SCORE_MOIN);
                }
            });
        }
        @Override
        public int getItemCount() {

            return etudiantsList.size();
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            this.listener = listener;
        }
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
