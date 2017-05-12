package com.example.teamloosers.behereandroid.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Activities.ConsultationEtudiantsActivity;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Structures.Section;
import com.example.teamloosers.behereandroid.Utils.FirebaseRecyclerAdapterViewer;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Structurable;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

/**
 * Created by teamloosers on 06/05/17.
 */

public class EtudiantsFragment <T extends Structurable> extends Fragment {

    private Module module;
    private T structure;
    private FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder> etudiantsListAdapater;
    private SectionEtudiantsRecyclerAdapter etudiantsRecyclerAdapter;
    private RecyclerView etudiantsListRecyclerView;

    public EtudiantsFragment() {
    }

    public static <T extends Structurable> EtudiantsFragment newInstance(Module module, T structure) {

        EtudiantsFragment fragment = new EtudiantsFragment();
        Bundle args = new Bundle();
        args.putSerializable("module", module);
        args.putSerializable("structure", structure);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_etudiants, container, false);

        module = (Module) getArguments().getSerializable("module");
        structure = (T) getArguments().getSerializable("structure");

        etudiantsListRecyclerView = (RecyclerView) rootView.findViewById(R.id.etudiantsListRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        etudiantsListRecyclerView.setLayoutManager(linearLayoutManager);

        SlideInRightAnimator animator = new SlideInRightAnimator();
        animator.setAddDuration(300);
        etudiantsListRecyclerView.setItemAnimator(animator);

        Utils.setRecyclerViewDecoration(etudiantsListRecyclerView);

        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();

        if (structure instanceof Groupe)
            loadGroupeEtudiants();
        else if (structure instanceof Section)
            loadSectionsEtudiant();
    }

    private void loadSectionsEtudiant() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
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
                etudiantsRecyclerAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent intent = new Intent(getContext(), ConsultationEtudiantsActivity.class);
                        intent.putExtra("etudiantsList", etudiantsRecyclerAdapter.getItems());
                        intent.putExtra("currentEtudiantPosition", position);
                        intent.putExtra("module", module);

                        startActivity(intent);
                    }
                });
                etudiantsListRecyclerView.setAdapter(etudiantsRecyclerAdapter);
                loadingProgressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    private void loadGroupeEtudiants() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiants_loading_message));

        String pathToStructure = Utils.firebasePath(Utils.CYCLES, structure.getIdCycle(), structure.getIdFilliere(), structure.getIdPromo(),
                structure.getIdSection(), structure.getId());
        Query myRef = Utils.database.getReference(pathToStructure).orderByChild("nom")
                .startAt("");
        myRef.keepSynced(true); // Keeping data fresh

        loadingProgressDialog.show();
        etudiantsListAdapater = new FirebaseRecyclerAdapterViewer<Etudiant, EtudiantViewHolder>(
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

            @Override
            protected void onCancelled(DatabaseError error) {

                super.onCancelled(error);
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
                loadingProgressDialog.cancel();
            }
        };
        etudiantsListAdapater.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getContext(), ConsultationEtudiantsActivity.class);
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

        String typeSeance = (structure instanceof Groupe)? Seance.TD: Seance.COURS;
        Query etudiantRef = Utils.database.getReference(pathToEtudiant).orderByChild("typeSeance")
                .equalTo(typeSeance);
        etudiantRef.keepSynced(true); // Keeping data fresh

        etudiantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long etudiantNbAbsences = dataSnapshot.getChildrenCount();
                displayNbAbsencesInTextView(etudiantNbAbsencesTextView, etudiantNbAbsences);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }

    private void displayNbAbsencesInTextView(TextView etudiantNbAbsencesTextView, long etudiantNbAbsences) {

        int textColor;
        switch (Integer.valueOf(String.format("%d", etudiantNbAbsences))) {

            case 0:
                textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);
                break;
            case 1:
                textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);
                break;
            case 2:
                textColor = ContextCompat.getColor(getContext(), R.color.deux_absences);
                break;
            default:
                textColor = ContextCompat.getColor(getContext(), R.color.plus_deux_absences);
        }
        etudiantNbAbsencesTextView.setText(String.format("%d", etudiantNbAbsences));
        etudiantNbAbsencesTextView.setTextColor(textColor);
    }

    public class SectionEtudiantsRecyclerAdapter extends RecyclerView.Adapter<EtudiantViewHolder> {

        private ArrayList<Etudiant> etudiantsList;

        private AdapterView.OnItemClickListener listener;

        public SectionEtudiantsRecyclerAdapter(ArrayList<Etudiant> etudiantsList) {

            this.etudiantsList = etudiantsList;
        }

        @Override
        public EtudiantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_holder_etudiant, parent, false);
            return new EtudiantViewHolder(view);
        }
        @Override
        public void onBindViewHolder(final EtudiantViewHolder holder, int position) {

            Etudiant etudiant = etudiantsList.get(position);
            String nomEtPrenom = String.format("%s %s", etudiant.getNom(), etudiant.getPrenom());

            holder.etudiantNomPrenomTextView.setText(nomEtPrenom);
            setNbAbsenceTextView(holder.etudiantNbAbsencesTextView, etudiant);

            if (holder.visible)
                holder.bind(holder.itemView, listener);
        }
        @Override
        public int getItemCount() {

            return etudiantsList.size();
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {

            this.listener = listener;
        }
        public ArrayList<Etudiant> getItems()   {

            return this.etudiantsList;
        }
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