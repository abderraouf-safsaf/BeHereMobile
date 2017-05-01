package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Personne;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ConsultationEtudiantActivity extends AppCompatActivity {

    private Module module;
    private ArrayList<Etudiant> etudiantsList;
    private int currentEtudiantPosition;

    private AdapterViewFlipper etudiantsAdapterViewFlipper;
    private RecyclerView etudiantsAbsencesRecyclerView;
    private float lastX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation_etudiant);

        this.module = (Module) getIntent().getExtras().getSerializable("module");
        this.etudiantsList = (ArrayList<Etudiant>) getIntent().getExtras().getSerializable("etudiantsList");
        this.currentEtudiantPosition = getIntent().getExtras().getInt("currentEtudiantPosition");


        etudiantsAdapterViewFlipper = (AdapterViewFlipper) findViewById(R.id.etudiantsAdapterViewFlipper);

        EtudiantsConsultationAdapter etudiantsArrayAdapter = new EtudiantsConsultationAdapter(this,
                etudiantsList);
        etudiantsAdapterViewFlipper.setAdapter(etudiantsArrayAdapter);
        etudiantsAdapterViewFlipper.setDisplayedChild(currentEtudiantPosition);
}

    @Override
    protected void onStart() {

        super.onStart();

        etudiantsAbsencesRecyclerView = (RecyclerView) findViewById(R.id.etudiantsAbsencesRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        etudiantsAbsencesRecyclerView.setLayoutManager(linearLayoutManager);

        etudiantsAbsencesRecyclerView.setHasFixedSize(true);
        //loadAbsecnces();
    }

    private void loadAbsecnces() {

        Etudiant currentEtudiant = etudiantsList.get(etudiantsAdapterViewFlipper.getDisplayedChild());

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, currentEtudiant.getIdCycle(), currentEtudiant.getIdFilliere(),
                currentEtudiant.getIdPromo(), currentEtudiant.getIdSection(), currentEtudiant.getIdGroupe(), currentEtudiant.getId(), module.getId());

        DatabaseReference etudiantRef = Utils.database.getReference(pathToEtudiant);

        final ProgressDialog loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiant_absence_message));

        loadingProgressDialog.show();
        FirebaseRecyclerAdapterViewer<Absence, AbsenceViewHolder> absencesListAdapter = new FirebaseRecyclerAdapterViewer<Absence, AbsenceViewHolder>(
                Absence.class, R.layout.view_holder_absence, AbsenceViewHolder.class, etudiantRef) {
            @Override
            protected void populateView(AbsenceViewHolder viewHolder, final Absence absence, int position) {

                viewHolder.absenceDateTextView.setText(absence.getDate());
                viewHolder.supprimerAbsenceImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConsultationEtudiantActivity.this);
                        alertDialog.setMessage(R.string.confirmer_suppression_absence_message);
                        alertDialog.setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                absence.supprimerDb(Utils.database);
                                Toast.makeText(ConsultationEtudiantActivity.this, R.string.absence_supprimee_message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.setNegativeButton(R.string.non, null);
                        alertDialog.show();
                    }
                });
            }
            @Override
            protected void onDataChanged() {

                super.onDataChanged();

                loadingProgressDialog.dismiss();
            }
        };

        etudiantsAbsencesRecyclerView.setAdapter(absencesListAdapter);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = event.getX();

                if (lastX < currentX) {

                    if (etudiantsAdapterViewFlipper.getDisplayedChild() == 0)
                        break;

                    etudiantsAdapterViewFlipper.showNext();
                }
                if (lastX > currentX) {

                    if (etudiantsAdapterViewFlipper.getDisplayedChild() == 1)
                        break;
                    etudiantsAdapterViewFlipper.showPrevious();
                }
                break;
        }
        return false;
    }
    public static class AbsenceViewHolder extends ItemViewHolder   {

        TextView absenceDateTextView;
        ImageButton supprimerAbsenceImageButton;

        public AbsenceViewHolder(View itemView) {

            super(itemView);

            absenceDateTextView = (TextView) itemView.findViewById(R.id.abseneDateTextView);
            supprimerAbsenceImageButton = (ImageButton) itemView.findViewById(R.id.supprimerAbsenceImageButton);
        }
    }
    public class EtudiantsConsultationAdapter extends BaseAdapter   {

        private Context context;
        private ArrayList<Etudiant> etudiantsList;
        private LayoutInflater inflater;

        public EtudiantsConsultationAdapter(Context context, ArrayList<Etudiant> etudiantsList) {

            this.context = context;
            this.etudiantsList = etudiantsList;

            inflater = (LayoutInflater.from(this.context));
        }

        @Override
        public int getCount() {
            return etudiantsList.size();
        }

        @Override
        public Object getItem(int position) {
            return etudiantsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.view_holder_consultation_etudiant, null);

            ImageView etudiantImageView = (ImageView) convertView.findViewById(R.id.etudiantImageView);
            TextView nomTextView = (TextView) convertView.findViewById(R.id.nomTextView);
            TextView prenomTextView = (TextView) convertView.findViewById(R.id.prenomTextView);
            TextView emailTextView = (TextView) convertView.findViewById(R.id.emailTextView);

            Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.com_facebook_profile_picture_blank_portrait);
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, defaultImage);
            dr.setCornerRadius(200);
            etudiantImageView.setImageDrawable(dr);

            Etudiant etudiant = (Etudiant) getItem(position);

            nomTextView.setText(etudiant.getNom());
            prenomTextView.setText(etudiant.getPrenom());
            emailTextView.setText(etudiant.getEmail());

            return convertView;
        }
    }
}
