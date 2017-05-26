package com.example.teamloosers.behereandroid.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils.ItemViewHolder;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by teamloosers on 01/05/17.
 */
public class ConsultationEtudiantFragment extends Fragment implements View.OnClickListener {

    private Module module;
    private Etudiant etudiant;

    private ImageView etudiantImageView;
    private ImageButton sendMessageImageButton, plusImageButton, minusImageButton;
    private TextView nomTextView, prenomTextView, emailTextView, etudiantScoreTextView;
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
        sendMessageImageButton = (ImageButton) rootView.findViewById(R.id.sendMessageImageButton);
        plusImageButton = (ImageButton) rootView.findViewById(R.id.plusImageButton);
        minusImageButton = (ImageButton) rootView.findViewById(R.id.minusImageButton);
        etudiantScoreTextView = (TextView) rootView.findViewById(R.id.etudiantScoreTextView);
        nomTextView = (TextView) rootView.findViewById(R.id.nomTextView);
        prenomTextView = (TextView) rootView.findViewById(R.id.prenomTextView);
        emailTextView = (TextView) rootView.findViewById(R.id.emailTextView);
        etudiantsAbsencesRecyclerView = (RecyclerView) rootView.findViewById(R.id.etudiantsAbsencesRecyclerView);
        etudiantsAbsencesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager absenceLinearLayoutManager = new LinearLayoutManager(getContext());
        etudiantsAbsencesRecyclerView.setLayoutManager(absenceLinearLayoutManager);

        Utils.setRecyclerViewDecoration(etudiantsAbsencesRecyclerView);

        plusImageButton.setOnClickListener(this);
        minusImageButton.setOnClickListener(this);
        sendMessageImageButton.setOnClickListener(this);

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
        Bitmap image = Utils.decode64BaseImageToBmp(etudiant.getImageBase64());
        //Bitmap imageResized = Bitmap.createScaledBitmap(image, etudiantImageWidth, etudiantImageHeight, true);
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(null, image);
        dr.setCornerRadius(200);

        etudiantImageView.setImageDrawable(dr);

        nomTextView.setText(etudiant.getNom());
        prenomTextView.setText(etudiant.getPrenom());
        emailTextView.setText(etudiant.getEmail());

        loadEtudiantScore(etudiantScoreTextView);
    }
    private void loadEtudiantScore(final TextView etudiantScoreTextView) {

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
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }
    private void displayScoreOnTextView(TextView etudiantScoreTextView, long etudiantScore) {

        int textColor = Color.BLACK;

        if (getContext() != null) {

            if (etudiantScore > 0)
                textColor = ContextCompat.getColor(getContext(), R.color.score_positif);
            else if (etudiantScore < 0)
                textColor = ContextCompat.getColor(getContext(), R.color.score_negatif);
            else
                textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);
        }

        String prefix = (etudiantScore > 0)? "+": "";
        etudiantScoreTextView.setText(String.format("%s%d",
                prefix, etudiantScore));

        etudiantScoreTextView.setTextColor(textColor);
    }
    private void loadAbsecnces() {

        String pathToEtudiant = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(), module.getId());

        Query etudiantRef = Utils.database.getReference(pathToEtudiant).orderByChild("idModule")
                .startAt("");
        etudiantRef.keepSynced(true); // Keeping data fresh

        final ProgressDialog loadingProgressDialog = new ProgressDialog(getContext());
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setMessage(getResources().getString(R.string.chargement_etudiant_absence_message));

        loadingProgressDialog.show();
        FirebaseRecyclerAdapter<Absence, AbsenceViewHolder> absencesListAdapter = new FirebaseRecyclerAdapter<Absence, AbsenceViewHolder>(
                Absence.class, R.layout.view_holder_absence, AbsenceViewHolder.class, etudiantRef) {
            @Override
            protected void populateViewHolder(AbsenceViewHolder viewHolder, final Absence absence, int position) {

                setTypeSeanceTextView(viewHolder.typeSeanceTextView, absence);
                setModuleTextViewFromAbsence(viewHolder.moduleTextView, absence);
                viewHolder.absenceDateTextView.setText(absence.getDate());

                if (!absence.isJustifier()) {

                    viewHolder.consulterJustificationImageButton.setVisibility(View.GONE);

                    viewHolder.supprimerAbsenceImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                            alertDialog.setMessage(R.string.confirmer_suppression_absence_message);
                            alertDialog.setPositiveButton(R.string.oui, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    absence.supprimerDb();
                                    Toast.makeText(getContext(), R.string.absence_supprimee_message, Toast.LENGTH_SHORT).show();
                                }
                            });
                            alertDialog.setNegativeButton(R.string.non, null);
                            alertDialog.show();
                        }
                    });
                }
                else    { // Absence justifiee

                    viewHolder.supprimerAbsenceImageButton.setVisibility(View.GONE);
                    viewHolder.absenceLinearLayout.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.absence_justifier_background_color));
                    viewHolder.consulterJustificationImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // TODO: consulter justification
                        }
                    });
                }
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

        etudiantsAbsencesRecyclerView.setAdapter(absencesListAdapter);
    }
    private void setTypeSeanceTextView(TextView typeSeanceTextView, Absence absence) {

        String typeSeance = (absence.getTypeSeance().equals(Seance.COURS))? "Cours :": "TD :";
        typeSeanceTextView.setText(typeSeance);
    }
    private void setModuleTextViewFromAbsence(final TextView moduleTextView, Absence absence) {

        String pathToModule = Utils.firebasePath(Utils.MODULE_ENSEIGNANTS, absence.getIdModule());

        Query query = Utils.database.getReference(pathToModule);
        query.keepSynced(true); // Keeping data fresh

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Module module = dataSnapshot.getValue(Module.class);
                if (module != null)
                    moduleTextView.setText(module.getDesignation());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }
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
                Utils.showSnackBar(getActivity(), Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }
    @Override
    public void onClick(View v) {

        if (v == plusImageButton)   {

            addToEtudiantScore(etudiant, Etudiant.SCORE_PLUS);
        }
        else if (v == minusImageButton) {

            addToEtudiantScore(etudiant, Etudiant.SCORE_MOIN);
        }
        else if (v == sendMessageImageButton)   {

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{etudiant.getEmail()});

            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.email_intent_title)));
        }
    }

    public static class AbsenceViewHolder extends ItemViewHolder {

        LinearLayout absenceLinearLayout;
        TextView typeSeanceTextView, moduleTextView, absenceDateTextView;
        ImageButton supprimerAbsenceImageButton, consulterJustificationImageButton;

        public AbsenceViewHolder(View itemView) {

            super(itemView);

            typeSeanceTextView = (TextView) itemView.findViewById(R.id.typeSeanceTextView);
            absenceDateTextView = (TextView) itemView.findViewById(R.id.abseneDateTextView);
            moduleTextView = (TextView) itemView.findViewById(R.id.moduleTextView);
            supprimerAbsenceImageButton = (ImageButton) itemView.findViewById(R.id.supprimerAbsenceImageButton);
            consulterJustificationImageButton = (ImageButton) itemView.findViewById(R.id.consulterJustificationImageButton);
            absenceLinearLayout = (LinearLayout) itemView.findViewById(R.id.absenceLinearLayout);
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