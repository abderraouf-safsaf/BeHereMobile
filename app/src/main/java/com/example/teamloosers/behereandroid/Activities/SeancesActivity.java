package com.example.teamloosers.behereandroid.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.example.teamloosers.behereandroid.Fragments.DatePickerFragment;
import com.example.teamloosers.behereandroid.Structures.Structurable;
import com.example.teamloosers.behereandroid.Utils.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class SeancesActivity<T extends Structurable> extends AppCompatActivity {

    private Module module;
    private T structure;
    private String typeSeance;

    private RecyclerView seancesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seances);


        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.structure = (T) getIntent().getExtras().getSerializable("structure");
        this.typeSeance = getIntent().getExtras().getString("typeSeance");

        seancesRecyclerView = (RecyclerView) findViewById(R.id.seancesRecyclerView);

        LinearLayoutManager seancesLinearLayoutManager = new LinearLayoutManager(this);
        seancesRecyclerView.setLayoutManager(seancesLinearLayoutManager);

        SlideInRightAnimator animator = new SlideInRightAnimator();
        animator.setAddDuration(300);
        seancesRecyclerView.setItemAnimator(animator);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(seancesRecyclerView.getContext(),
                seancesLinearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.recyclerview_divider));
        seancesRecyclerView.addItemDecoration(dividerItemDecoration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment nouvelSeanceDialog = new DatePickerFragment();

                Bundle args = new Bundle();
                args.putSerializable("module", module);
                args.putSerializable("structure", structure);
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

        String pathToStructure = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(),
                module.getId(), typeSeance, structure.getId());
        Query myRef = Utils.database.getReference(pathToStructure).orderByChild("idModule").
                equalTo(module.getId());
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        final FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder> seancesAdapter = new FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder>(
                Seance.class, R.layout.view_holder_seance, SeanceViewHolder.class, myRef
        ) {
            @Override
            protected void populateView(SeanceViewHolder viewHolder, Seance seance, int position) {

                String dateSeance = seance.getDate();

                viewHolder.dateSeanceTextView.setText(dateSeance);
                setNbAbsenceTextView(viewHolder.seanceNbAbsencesTextView, seance);
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
                intent.putExtra("structure", structure);
                intent.putExtra("seance", seancesAdapter.getItem(position));

                startActivity(intent);
            }
        });
        seancesRecyclerView.setAdapter(seancesAdapter);
    }
    private void setNbAbsenceTextView(final TextView seanceNbAbsencesTextView, Seance seance) {

        String pathToSeance = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(),
                module.getId(),  seance.getTypeSeance(), structure.getId(), seance.getId());

        Query seanceRef = Utils.database.getReference(pathToSeance).orderByChild("idModule")
                .equalTo(module.getId());
        seanceRef.keepSynced(true); // Keeping data fresh

        System.out.println("Path = " + seanceRef.getRef());
        seanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long seanceNbAbsences = dataSnapshot.getChildrenCount();
                displayNbAbsencesInTextView(seanceNbAbsencesTextView, seanceNbAbsences);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    private void displayNbAbsencesInTextView(TextView seanceNbAbsencesTextView, long seanceNbAbsences) {

        int textColor;
        switch (Integer.valueOf(String.format("%d", seanceNbAbsences))) {

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
        seanceNbAbsencesTextView.setText(String.format("%d", seanceNbAbsences));
        seanceNbAbsencesTextView.setTextColor(textColor);
    }
    public static class SeanceViewHolder extends ItemViewHolder{

        TextView dateSeanceTextView, seanceNbAbsencesTextView;

        public SeanceViewHolder(View itemView) {

            super(itemView);

            dateSeanceTextView = (TextView) itemView.findViewById(R.id.dateSeanceTextView);
            seanceNbAbsencesTextView = (TextView) itemView.findViewById(R.id.seanceNbAbsencesTextView);
        }
    }
}
