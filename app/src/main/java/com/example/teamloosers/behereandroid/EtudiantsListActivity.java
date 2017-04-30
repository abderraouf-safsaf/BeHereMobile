package com.example.teamloosers.behereandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class EtudiantsListActivity extends AppCompatActivity {

    private RecyclerView etudiantsListRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiants_list);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseRefercence = database.getReference();

        etudiantsListRecyclerView = (RecyclerView) findViewById(R.id.etudiantsListRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsListRecyclerView.setLayoutManager(linearLayoutManager);

        etudiantsListRecyclerView.setHasFixedSize(true);

        DatabaseReference myRef =  database.getReference("/Cycles/Cyclepreparatoireintegree_d35f9666-8af2-4007-8f2f-0550878f6cd1/Informatique_db6f8a0e-db55-4ed5-9112-9fb74858a5e7/Premiereannee_58087455-b575-4cce-8f0c-9252471a36ca/SectionA_cd1aee5d-17f4-4ccb-9cec-100adf08effe/Groupe2_75ac381c-2fe6-4ac5-aaa5-4f325396b2f0/");
            FirebaseRecyclerAdapter<Etudiant, EtudiantViewHolder> adapter = new FirebaseRecyclerAdapter<Etudiant, EtudiantViewHolder>(
                    Etudiant.class, R.layout.etudiant_item_view, EtudiantViewHolder.class, myRef
            ) {
                @Override
                protected void populateViewHolder(EtudiantViewHolder viewHolder, Etudiant model, int position) {

                    if (model != null)
                        System.out.println("Etudiant = " + model.getPrenom());
                }

                @Override
                protected void onCancelled(DatabaseError error) {
                    super.onCancelled(error);
                }

                @Override
                protected Etudiant parseSnapshot(DataSnapshot snapshot) {
                    if (snapshot.hasChildren())
                        return super.parseSnapshot(snapshot);
                    else return null;
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
