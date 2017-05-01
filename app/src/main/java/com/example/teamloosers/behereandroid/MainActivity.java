package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.example.teamloosers.behereandroid.Structures.Section;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    private Module module;

    private RecyclerView groupesRecyclerView, sectionsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        module = new Module("Architecture 1");
        module.setId("Architecture1_50824b38-b894-44b9-af7f-a6e1971e884b");
        module.setIdCycle("Cyclepreparatoireintegree_d35f9666-8af2-4007-8f2f-0550878f6cd1");
        module.setIdFilliere("Informatique_db6f8a0e-db55-4ed5-9112-9fb74858a5e7");
        module.setIdFilliere("Premiereannee_58087455-b575-4cce-8f0c-9252471a36ca");

        sectionsRecyclerView = (RecyclerView) findViewById(R.id.sectionsRecyclerView);
        groupesRecyclerView = (RecyclerView) findViewById(R.id.groupesRecyclerView);

        sectionsRecyclerView.setHasFixedSize(true);
        groupesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager sectionsLinearLayoutManager = new LinearLayoutManager(this);
        sectionsRecyclerView.setLayoutManager(sectionsLinearLayoutManager);

        LinearLayoutManager groupesLinearLayoutManager = new LinearLayoutManager(this);
        groupesRecyclerView.setLayoutManager(groupesLinearLayoutManager);
    }

    @Override
    protected void onStart() {

        super.onStart();

        loadSections();
        loadGroupes();
    }

    private void loadSections() {

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
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

                        Intent intent = new Intent(MainActivity.this, ListEtudiantsActivity.class);
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

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_groupes_loading_message));

        String groupesPath = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId(), module.getId(), Utils.GROUPES);
        Query groupesQuery = Utils.database.getReference(groupesPath);

        loadingProgressDialog.show();;
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

                        Intent intent = new Intent(MainActivity.this, ListEtudiantsActivity.class);
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
}
