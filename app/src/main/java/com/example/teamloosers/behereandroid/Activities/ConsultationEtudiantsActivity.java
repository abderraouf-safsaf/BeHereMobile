package com.example.teamloosers.behereandroid.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.teamloosers.behereandroid.ConsultationEtudiantFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Personne;

import java.util.ArrayList;

public class ConsultationEtudiantsActivity extends AppCompatActivity {

    private Module module;
    private ArrayList<Etudiant> etudiantsList;

    private EtudiantPageAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation_etudiants);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etudiantsList = (ArrayList<Etudiant>) getIntent().getExtras().getSerializable("etudiantsList");
        module = (Module) getIntent().getExtras().getSerializable("module");

        mSectionsPagerAdapter = new EtudiantPageAdapter(getSupportFragmentManager(), etudiantsList, module);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Etudiant etudiant = etudiantsList.get(mViewPager.getCurrentItem());

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{etudiant.getEmail()});

                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.email_intent_title)));
            }
        });

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
