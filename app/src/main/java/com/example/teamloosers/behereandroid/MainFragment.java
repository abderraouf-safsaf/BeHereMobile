package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Activities.ListEtudiantsActivity;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Section;
import com.google.firebase.database.Query;

/**
 * Created by teamloosers on 01/05/17.
 */

public class MainFragment extends Fragment {

    private Module module;

    private RecyclerView groupesRecyclerView, sectionsRecyclerView;
    private TextView moduleDesignationTextView;

    public MainFragment() {  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        module = (Module) getArguments().getSerializable("module");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        sectionsRecyclerView = (RecyclerView) rootView.findViewById(R.id.sectionsRecyclerView);
        groupesRecyclerView = (RecyclerView) rootView.findViewById(R.id.groupesRecyclerView);
        moduleDesignationTextView = (TextView) rootView.findViewById(R.id.moduleDesignationTextView);

        sectionsRecyclerView.setHasFixedSize(true);
        groupesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager sectionsLinearLayoutManager = new LinearLayoutManager(getContext());
        sectionsRecyclerView.setLayoutManager(sectionsLinearLayoutManager);

        LinearLayoutManager groupesLinearLayoutManager = new LinearLayoutManager(getContext());
        groupesRecyclerView.setLayoutManager(groupesLinearLayoutManager);

        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();

        moduleDesignationTextView.setText(module.getDesignation());
        loadSections();
        loadGroupes();
    }
    private void loadSections() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_sections_loading_message));

        String sectionsPath = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(), module.getId(), Utils.SECTIONS);

        Query sectionQuery = Utils.database.getReference(sectionsPath);

        loadingProgressDialog.show();

        FirebaseRecyclerAdapterViewer<Section, StructureViewHolder> adapter = new FirebaseRecyclerAdapterViewer<Section, StructureViewHolder>(Section.class,
                R.layout.view_holder_structure, StructureViewHolder.class, sectionQuery) {
            @Override
            protected void populateView(StructureViewHolder viewHolder, final Section section, int position) {

                Button structureButton = viewHolder.structureButton;
                viewHolder.structureButton.setText(section.getDesignation());
                structureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getContext(), ListEtudiantsActivity.class);
                        intent.putExtra("module", module);
                        intent.putExtra("section", section);

                        startActivity(intent);
                    }
                });
            }
            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }
        };
        sectionsRecyclerView.setAdapter(adapter);
    }
    private void loadGroupes()  {


        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_groupes_loading_message));

        String groupesPath = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(), module.getId(), Utils.GROUPES);
        Query groupesQuery = Utils.database.getReference(groupesPath);
        groupesQuery.keepSynced(true); // Keeping data fresh
        loadingProgressDialog.show();
        FirebaseRecyclerAdapterViewer<Groupe, StructureViewHolder> adapter = new FirebaseRecyclerAdapterViewer<Groupe, StructureViewHolder>(
                Groupe.class, R.layout.view_holder_structure, StructureViewHolder.class, groupesQuery
        ) {
            @Override
            protected void populateView(StructureViewHolder viewHolder, final Groupe groupe, int position) {

                Button structureButton = viewHolder.structureButton;
                viewHolder.structureButton.setText(groupe.getDesignation());
                structureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getContext(), ListEtudiantsActivity.class);
                        intent.putExtra("module", module);
                        intent.putExtra("groupe", groupe);

                        startActivity(intent);
                    }
                });
            }

            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }
        };
        groupesRecyclerView.setAdapter(adapter);
    }

    public static class StructureViewHolder extends ItemViewHolder {

        Button structureButton;

        public StructureViewHolder(View itemView) {

            super(itemView);

            structureButton = (Button) itemView.findViewById(R.id.structureButton);
        }
    }
    public static MainFragment newInstance(Module module) {

        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putSerializable("module", module);
        fragment.setArguments(args);
        return fragment;
    }
}