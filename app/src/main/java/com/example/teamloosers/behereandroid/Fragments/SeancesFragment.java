package com.example.teamloosers.behereandroid.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Activities.AppelListActivity;
import com.example.teamloosers.behereandroid.Activities.SeanceAbsencesActivity;
import com.example.teamloosers.behereandroid.Activities.StructureActivity;
import com.example.teamloosers.behereandroid.Utils.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Structures.Structurable;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by teamloosers on 06/05/17.
 */

public class SeancesFragment<T extends Structurable> extends Fragment {

    private Module module;
    private T structure;
    private String typeSeance;

    private ProgressDialog loadingProgressDialog;
    private RecyclerView seancesRecyclerView;

    public SeancesFragment() {    }

    public static <T extends Structurable> SeancesFragment newInstance(Module module, T structure, String typeSeance) {

        SeancesFragment fragment = new SeancesFragment();
        Bundle args = new Bundle();
        args.putSerializable("module", module);
        args.putSerializable("structure", structure);
        args.putSerializable("typeSeance", typeSeance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_seances, container, false);

        module = (Module) getArguments().getSerializable("module");
        structure = (T) getArguments().getSerializable("structure");
        typeSeance = getArguments().getString("typeSeance");

        seancesRecyclerView = (RecyclerView) rootView.findViewById(R.id.seancesRecyclerView);

        SlideInUpAnimator animator = new SlideInUpAnimator();
        animator.setAddDuration(200);
        seancesRecyclerView.setItemAnimator(animator);

        LinearLayoutManager seancesLinearLayoutManager = new LinearLayoutManager(getContext());
        seancesRecyclerView.setLayoutManager(seancesLinearLayoutManager);

        Utils.setRecyclerViewDecoration(seancesRecyclerView);

        return rootView;
    }
    @Override
    public void onStart() {

        super.onStart();

        loadSeances();
    }
    private void loadSeances() {

        loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_seances_loading_message));

        String pathToStructure = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(),
                module.getId(), typeSeance, structure.getId());
        Query myRef = Utils.database.getReference(pathToStructure).orderByChild("date").
                startAt(0);
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        final FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder> seancesAdapter = new FirebaseRecyclerAdapterViewer<Seance, SeanceViewHolder>(
                Seance.class, R.layout.view_holder_seance, SeanceViewHolder.class, myRef
        ) {
            @Override
            protected void populateView(SeanceViewHolder viewHolder, Seance seance, int position) {

                String dateSeance = String.format("%s - %s:%s", seance.getDate(),
                        seance.getHeureDebut(), seance.getMinuteDebut());
                viewHolder.dateSeanceTextView.setText(dateSeance);
                setNbAbsenceTextView(viewHolder.seanceNbAbsencesTextView, seance);
            }
            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }

            @Override
            protected void onCancelled(DatabaseError error) {

                super.onCancelled(error);
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        };
        seancesAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getContext(), SeanceAbsencesActivity.class);

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

        seanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long seanceNbAbsences = dataSnapshot.getChildrenCount();
                displayNbAbsencesInTextView(seanceNbAbsencesTextView, seanceNbAbsences);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    private void displayNbAbsencesInTextView(TextView seanceNbAbsencesTextView, long seanceNbAbsences) {

        int textColor = Color.BLACK;

        if (getContext() != null)   {

            switch (Integer.valueOf(String.format("%d", seanceNbAbsences))) {

                case 0:
                    textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);
                    break;
                case 1:
                    textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);
                    break;
                case 2:
                    textColor = ContextCompat.getColor(getContext(), R.color.deux_absences);
                    break;
                default: textColor = ContextCompat.getColor(getContext(), R.color.plus_deux_absences);
            }
        }
        seanceNbAbsencesTextView.setText(String.format("%d", seanceNbAbsences));
        seanceNbAbsencesTextView.setTextColor(textColor);
    }

    public static class SeanceViewHolder extends ItemViewHolder {

        TextView dateSeanceTextView, seanceNbAbsencesTextView;
        public SeanceViewHolder(View itemView) {

            super(itemView);

            dateSeanceTextView = (TextView) itemView.findViewById(R.id.dateSeanceTextView);
            seanceNbAbsencesTextView = (TextView) itemView.findViewById(R.id.seanceNbAbsencesTextView);
        }
    }
}