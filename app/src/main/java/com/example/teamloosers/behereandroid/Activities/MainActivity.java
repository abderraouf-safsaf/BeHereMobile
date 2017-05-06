package com.example.teamloosers.behereandroid.Activities;

import com.example.teamloosers.behereandroid.MainFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Utils;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView enseignantImageView;
    private TextView enseignantNomPrenomTextView, enseignantEmailTextView;
    private ModulesPageAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkEnseignantExistence();

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        enseignantNomPrenomTextView = (TextView) header.findViewById(R.id.enseignantNomPrenomTextView);
        enseignantEmailTextView = (TextView) header.findViewById(R.id.enseignantEmailTextView);
        enseignantImageView = (ImageView) header.findViewById(R.id.enseignantImageView);
    }

    private void checkEnseignantExistence() {
        if (Utils.auth.getCurrentUser() == null)    {

            startLoginActivity();
            finish();
        }
    }

    @Override
    protected void onStart() {

        super.onStart();

        enseignantNomPrenomTextView.setText(String.format("%s %s", Utils.enseignant.getNom(), Utils
        .enseignant.getPrenom()));
        enseignantEmailTextView.setText(Utils.enseignant.getEmail());
        Picasso.with(this).load(Utils.auth.getCurrentUser().getPhotoUrl()).into(enseignantImageView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.root, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.parametresItem) {
            // Handle the camera action
        } else if (id == R.id.seDeconnecterItem) {

            if (Utils.auth.getCurrentUser() != null)    {

                Utils.auth.signOut();
                startLoginActivity();
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
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
