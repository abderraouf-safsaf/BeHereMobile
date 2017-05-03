package com.example.teamloosers.behereandroid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

/**
 * Created by teamloosers on 01/05/17.
 */
public class ConsultationEtudiantFragment extends Fragment {

    private Module module;
    private Etudiant etudiant;

    private ImageView etudiantImageView;
    private TextView nomTextView, prenomTextView, emailTextView;
    private RecyclerView etudiantsAbsencesRecyclerView;

    public ConsultationEtudiantFragment() {  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        module = (Module) getArguments().getSerializable("module");
        etudiant = (Etudiant) getArguments().getSerializable("etudiant");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_consultation_etudiants, container, false);

        etudiantImageView = (ImageView) rootView.findViewById(R.id.etudiantImageView);
        nomTextView = (TextView) rootView.findViewById(R.id.nomTextView);
        prenomTextView = (TextView) rootView.findViewById(R.id.prenomTextView);
        emailTextView = (TextView) rootView.findViewById(R.id.emailTextView);
        etudiantsAbsencesRecyclerView = (RecyclerView) rootView.findViewById(R.id.etudiantsAbsencesRecyclerView);

        etudiantsAbsencesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager absenceLinearLayoutManager = new LinearLayoutManager(getContext());
        etudiantsAbsencesRecyclerView.setLayoutManager(absenceLinearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(etudiantsAbsencesRecyclerView.getContext(),
                absenceLinearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recyclerview_divider));
        etudiantsAbsencesRecyclerView.addItemDecoration(dividerItemDecoration);
        return rootView;
    }
    @Override
    public void onStart() {

        super.onStart();

        loadEtudinatInformations();
        loadAbsecnces();
    }
    private void loadEtudinatInformations() {

        int etudiantImageHeight = getResources().getDimensionPixelSize(R.dimen.etudiantImageHeight);
        int etudiantImageWidth = getResources().getDimensionPixelSize(R.dimen.etudiantImageWidth);
        Bitmap image = Utils.decodeToImage(etudiant.getImageBase64());
        Bitmap imageResized = Bitmap.createScaledBitmap(image, etudiantImageWidth, etudiantImageHeight, true);

        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, imageResized);
        dr.setCornerRadius(200);
        etudiantImageView.setImageDrawable(dr);

        nomTextView.setText(etudiant.getNom());
        prenomTextView.setText(etudiant.getPrenom());
        emailTextView.setText(etudiant.getEmail());
    }
    private void loadAbsecnces() {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId());

        Query etudiantRef = Utils.database.getReference(pathToEtudiant);
        etudiantRef.keepSynced(true); // Keeping data fresh
        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiant_absence_message));

        loadingProgressDialog.show();
        FirebaseRecyclerAdapter<Absence, AbsenceViewHolder> absencesListAdapter = new FirebaseRecyclerAdapter<Absence, AbsenceViewHolder>(
                Absence.class, R.layout.view_holder_absence, AbsenceViewHolder.class, etudiantRef) {
            @Override
            protected void populateViewHolder(AbsenceViewHolder viewHolder, final Absence absence, int position) {

                viewHolder.absenceDateTextView.setText(absence.getDate());
                viewHolder.supprimerAbsenceImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setMessage(R.string.confirmer_suppression_absence_message);
                        alertDialog.setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                absence.supprimerDb(Utils.database);
                                Toast.makeText(getContext(), R.string.absence_supprimee_message, Toast.LENGTH_SHORT).show();
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
    public static class AbsenceViewHolder extends ItemViewHolder {

        TextView absenceDateTextView;
        ImageButton supprimerAbsenceImageButton;

        public AbsenceViewHolder(View itemView) {

            super(itemView);

            absenceDateTextView = (TextView) itemView.findViewById(R.id.abseneDateTextView);
            supprimerAbsenceImageButton = (ImageButton) itemView.findViewById(R.id.supprimerAbsenceImageButton);
        }
    }
    public static ConsultationEtudiantFragment newInstance(Etudiant etudiant, Module module) {

        ConsultationEtudiantFragment fragment = new ConsultationEtudiantFragment();
        Bundle args = new Bundle();
        args.putSerializable("etudiant", etudiant);
        args.putSerializable("module", module);
        fragment.setArguments(args);
        return fragment;
    }
}