package com.example.teamloosers.behereandroid.Activities;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.util.Util;
import com.example.teamloosers.behereandroid.MainFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ModulesPageAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(String.format("M.%s %s", Utils.enseignant.getNom(), Utils.enseignant.getPrenom()));
        setSupportActionBar(toolbar);

        Module architecture1 = new Module("Architecture 1");
        architecture1.setId("Architecture1_50824b38-b894-44b9-af7f-a6e1971e884b");
        architecture1.setIdCycle("Cyclepreparatoireintegree_d35f9666-8af2-4007-8f2f-0550878f6cd1");
        architecture1.setIdFilliere("Informatique_db6f8a0e-db55-4ed5-9112-9fb74858a5e7");
        architecture1.setIdFilliere("Premiereannee_58087455-b575-4cce-8f0c-9252471a36ca");

        Module systemExploitation1 = new Module("Systeme d'exploitation 1");
        systemExploitation1.setId("Systemed'exploitation1_f82a8c66-2340-4095-8118-2e4f4a2b21fd");
        systemExploitation1.setIdCycle("Cyclepreparatoireintegree_d35f9666-8af2-4007-8f2f-0550878f6cd1");
        systemExploitation1.setIdFilliere("Informatique_db6f8a0e-db55-4ed5-9112-9fb74858a5e7");
        systemExploitation1.setIdPromo("Premiereannee_58087455-b575-4cce-8f0c-9252471a36ca");

        ArrayList<Module> modulesList = new ArrayList<>();

        modulesList.add(architecture1);
        modulesList.add(systemExploitation1);

        mSectionsPagerAdapter = new ModulesPageAdapter(getSupportFragmentManager(), modulesList, this);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    public class ModulesPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Module> modulesList;
        private Context context;

        public ModulesPageAdapter(FragmentManager fm, ArrayList<Module> modulesList, Context context) {

            super(fm);

            this.modulesList = modulesList;
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {

            return MainFragment.newInstance(modulesList.get(position));
        }

        @Override
        public int getCount() {

            return modulesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return modulesList.get(position).getDesignation();
        }
    }
}
