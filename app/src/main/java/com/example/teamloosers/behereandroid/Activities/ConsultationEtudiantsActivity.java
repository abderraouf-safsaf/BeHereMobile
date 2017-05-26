package com.example.teamloosers.behereandroid.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.teamloosers.behereandroid.Fragments.ConsultationEtudiantFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ConsultationEtudiantsActivity extends AppCompatActivity {

    private Module module;
    private ArrayList<Etudiant> etudiantsList;
    private int currentEtudiantPosition;

    private EtudiantPageAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_consultation_etudiants);

        etudiantsList = (ArrayList<Etudiant>) getIntent().getExtras().getSerializable("etudiantsList");
        module = (Module) getIntent().getExtras().getSerializable("module");
        currentEtudiantPosition = getIntent().getExtras().getInt("currentEtudiantPosition");

        mSectionsPagerAdapter = new EtudiantPageAdapter(getSupportFragmentManager(), etudiantsList, module);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(currentEtudiantPosition);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LayoutInflater li = LayoutInflater.from(ConsultationEtudiantsActivity.this);
                View promptsView = li.inflate(R.layout.dialog_etudiant_remarque_input, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConsultationEtudiantsActivity.this);
                alertDialogBuilder.setView(promptsView);

                final EditText remarqueEditText = (EditText) promptsView.findViewById(R.id.etudiantRemarqueEditText);

                alertDialogBuilder.setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        updateEtudiantRemarque(remarqueEditText.getText().toString());
                    }
                }).setNegativeButton(getString(R.string.annuler), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();

                loadEtudiantRemarqueToEditText(remarqueEditText);
            }
        });
    }
    private void loadEtudiantRemarqueToEditText(final EditText remarqueEditText)  {

        Etudiant etudiant = etudiantsList.get(mViewPager.getCurrentItem());

        String pathToEtudiantRemarque = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(),
                etudiant.getId(), module.getId(), Utils.REMARQUE);

        Query remarqueRef = Utils.database.getReference(pathToEtudiantRemarque);
        remarqueRef.keepSynced(true); // Keeping data fresh

        remarqueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String remarque = dataSnapshot.getValue(String.class);
                if (remarque != null)
                    remarqueEditText.setText(remarque);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Utils.showSnackBar(ConsultationEtudiantsActivity.this, Utils.DATABASE_ERR_MESSAGE);
            }
        });
    }
    private void updateEtudiantRemarque(String remarque)    {

        Etudiant etudiant = etudiantsList.get(mViewPager.getCurrentItem());

        String pathToEtudiantRemarque = Utils.firebasePath(Utils.CYCLES, etudiant.getIdCycle(), etudiant.getIdFilliere(),
                etudiant.getIdPromo(), etudiant.getIdSection(), etudiant.getIdGroupe(),
                etudiant.getId(), module.getId(), Utils.REMARQUE);

        DatabaseReference remarqueRef = Utils.database.getReference(pathToEtudiantRemarque);
        remarqueRef.setValue(remarque);
    }
    public class EtudiantPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Etudiant> etudiantsList;
        private Module module;

        public EtudiantPageAdapter(FragmentManager fm, ArrayList<Etudiant> etudiantsList, Module module) {

            super(fm);
            this.etudiantsList = etudiantsList;
            this.module = module;
        }

        @Override
        public Fragment getItem(int position) {

            return ConsultationEtudiantFragment.newInstance(etudiantsList.get(position), module);
        }

        @Override
        public int getCount() {

            return etudiantsList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return etudiantsList.get(position).toString();
        }
    }
}
