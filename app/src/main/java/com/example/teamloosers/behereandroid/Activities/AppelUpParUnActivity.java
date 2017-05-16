package com.example.teamloosers.behereandroid.Activities;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.teamloosers.behereandroid.Fragments.AppelUnParUnFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Absence;
import com.example.teamloosers.behereandroid.Structures.Etudiant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;
import com.example.teamloosers.behereandroid.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppelUpParUnActivity extends AppCompatActivity {

    private Module module;
    private HashMap<Etudiant, Boolean> etudiantsPresenceMap;
    private ArrayList<Etudiant> etudiantsList;
    private Seance seance;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appel_up_par_un);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        module = (Module) getIntent().getExtras().getSerializable("module");
        seance = (Seance) getIntent().getExtras().getSerializable("seance");
        etudiantsPresenceMap = (HashMap<Etudiant, Boolean>)
                getIntent().getExtras().getSerializable("etudiantsPresenceMap");

        etudiantsList = new ArrayList<>(etudiantsPresenceMap.keySet());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
    public void ajouterAbsencesDb() {

        for (Map.Entry<Etudiant, Boolean> entry: etudiantsPresenceMap.entrySet())  {

            Etudiant etudiant = entry.getKey();
            Boolean isPresent = entry.getValue();

            if (!isPresent) {

                Absence absence = newAbsence(etudiant);
                absence.ajouterDb();
            }
        }
    }

    private Absence newAbsence(Etudiant etudiant) {

        Absence absence = new Absence();
        absence.setId(Utils.generateId());
        absence.setIdCycle(etudiant.getIdCycle());
        absence.setIdFilliere(etudiant.getIdFilliere());
        absence.setIdPromo(etudiant.getIdPromo());
        absence.setIdSection(etudiant.getIdSection());
        absence.setIdGroupe(etudiant.getIdGroupe());
        absence.setIdEtudiant(etudiant.getId());
        absence.setIdEnseignant(seance.getIdEnseignant());
        absence.setIdModule(seance.getIdModule());
        absence.setIdSeance(seance.getId());
        absence.setTypeSeance(seance.getTypeSeance());
        absence.setDate(seance.getDate());
        absence.setJustifier(false);

        return absence;
    }
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return AppelUnParUnFragment.newInstance(etudiantsList.get(position));
        }
        @Override
        public int getCount() {

            return etudiantsList.size();
        }
    }

    public Module getModule() {
        return module;
    }

    public HashMap<Etudiant, Boolean> getEtudiantsPresenceMap() {
        return etudiantsPresenceMap;
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    public SectionsPagerAdapter getmSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }
}
