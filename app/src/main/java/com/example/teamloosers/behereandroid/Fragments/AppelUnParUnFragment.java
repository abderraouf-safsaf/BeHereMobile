package com.example.teamloosers.behereandroid.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamloosers.behereandroid.Activities.AppelListActivity;
import com.example.teamloosers.behereandroid.Activities.AppelUpParUnActivity;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by teamloosers on 15/05/17.
 */


public class AppelUnParUnFragment extends Fragment implements View.OnClickListener {

    private Etudiant etudiant;

    private AppelUpParUnActivity parentActivity;
    private ImageView etudiantImageView;
    private TextView nomTextView, prenomTextView, emailTextView, etudiantScoreTextView;
    private ImageButton minusImageButton, plusImageButton,
            presentImageButton, absentImageButton;

    public AppelUnParUnFragment() { }

    public static AppelUnParUnFragment newInstance(Etudiant etudiant) {
        AppelUnParUnFragment fragment = new AppelUnParUnFragment();
        Bundle args = new Bundle();
        args.putSerializable("etudiant", etudiant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_appel_up_par_un, container, false);

        etudiant = (Etudiant) getArguments().getSerializable("etudiant");
        parentActivity = (AppelUpParUnActivity) getActivity();

        etudiantImageView = (ImageView) rootView.findViewById(R.id.etudiantImageView);
        nomTextView = (TextView) rootView.findViewById(R.id.nomTextView);
        prenomTextView = (TextView) rootView.findViewById(R.id.prenomTextView);
        emailTextView = (TextView) rootView.findViewById(R.id.emailTextView);
        etudiantScoreTextView = (TextView) rootView.findViewById(R.id.etudiantScoreTextView);
        minusImageButton = (ImageButton) rootView.findViewById(R.id.minusImageButton);
        plusImageButton = (ImageButton) rootView.findViewById(R.id.plusImageButton);
        absentImageButton = (ImageButton) rootView.findViewById(R.id.absentImageButton);
        presentImageButton = (ImageButton) rootView.findViewById(R.id.presentImageButton);

        minusImageButton.setOnClickListener(this);
        plusImageButton.setOnClickListener(this);
        absentImageButton.setOnClickListener(this);
        presentImageButton.setOnClickListener(this);
        return rootView;
    }
    @Override
    public void onStart() {

        super.onStart();

        loadEtudinatInformations();
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

        loadEtudiantScore(etudiantScoreTextView);
    }
    private void loadEtudiantScore(final TextView etudiantScoreTextView) {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(),
                parentActivity.getModule().getId(), Utils.SCORE);

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

        int textColor;

        if (etudiantScore > 0)
            textColor = ContextCompat.getColor(getContext(), R.color.score_positif);
        else if (etudiantScore < 0)
            textColor = ContextCompat.getColor(getContext(), R.color.score_negatif);
        else
            textColor = ContextCompat.getColor(getContext(), R.color.textSecondary);

        String prefix = (etudiantScore > 0)? "+": "";
        etudiantScoreTextView.setText(String.format("%s%d",
                prefix, etudiantScore));

        etudiantScoreTextView.setTextColor(textColor);
    }
    private void addToEtudiantScore(Etudiant etudiant, final int toAdd) {

        String pathToEtudiantScore = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(), etudiant.getId(),
                parentActivity.getModule().getId(), Utils.SCORE);

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

        if (v == minusImageButton)  {

            addToEtudiantScore(etudiant, Etudiant.SCORE_PLUS);
        }
        else if (v == plusImageButton)  {

            addToEtudiantScore(etudiant, Etudiant.SCORE_MOIN);
        }
        if (v == absentImageButton) {

            parentActivity.getEtudiantsPresenceMap().put(etudiant, Absence.ABSENT);
            showNextEtudiantFragment();
        }
        if (v == presentImageButton)    {

            parentActivity.getEtudiantsPresenceMap().put(etudiant, Absence.PRESENT);
            showNextEtudiantFragment();
        }
    }
    private void showNextEtudiantFragment() {

        ViewPager viewPager = parentActivity.getmViewPager();

        System.out.println("Current item = " + viewPager.getCurrentItem() + " Child count = "
        + parentActivity.getmSectionsPagerAdapter().getCount());
        if (viewPager.getCurrentItem() == parentActivity.getmSectionsPagerAdapter().getCount() - 1)    {

            Intent etudiantsPresencesIntent = new Intent();
            etudiantsPresencesIntent.putExtra("etudiantsPresenceHashMap",
                    parentActivity.getEtudiantsPresenceMap());
            parentActivity.ajouterAbsencesDb();

            Toast.makeText(parentActivity, R.string.valider_appel_toast, Toast.LENGTH_SHORT).show();
            parentActivity.finish();
        }
        else
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }
}

