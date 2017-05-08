package com.example.teamloosers.behereandroid.Activities;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.EtudiantsFragment;
import com.example.teamloosers.behereandroid.SeancesFragment;
import com.example.teamloosers.behereandroid.Structures.Groupe;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Structures.Seance;

public class StructureActivity <T> extends AppCompatActivity {

    private Module module;
    private Groupe groupe;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);

        module = (Module) getIntent().getExtras().getSerializable("module");
        groupe = (Groupe) getIntent().getExtras().getSerializable("groupe");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0)
                return SeancesFragment.newInstance(module, groupe, Seance.TD);
            else if (position == 1)
                return EtudiantsFragment.newInstance(module, groupe);
            else
                return null;
        }

        @Override
        public int getCount() {

            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.seance_tab_title);
                case 1:
                    return getString(R.string.etudiants_tab_title);
            }
            return null;
        }
    }
}
