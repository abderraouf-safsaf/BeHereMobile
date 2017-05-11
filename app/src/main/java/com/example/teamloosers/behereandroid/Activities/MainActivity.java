package com.example.teamloosers.behereandroid.Activities;

import com.example.teamloosers.behereandroid.Fragments.MainFragment;
import com.example.teamloosers.behereandroid.R;
import com.example.teamloosers.behereandroid.Structures.Enseignant;
import com.example.teamloosers.behereandroid.Structures.Module;
import com.example.teamloosers.behereandroid.Utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    private ArrayList<Module> modulesList = new ArrayList<>();

    private ImageView enseignantImageView;
    private TextView enseignantNomPrenomTextView, enseignantEmailTextView;
    private ModulesPageAdapter mPageAdapter;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkEnseignantExistence();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(String.format("M.%s %s", Utils.enseignant.getNom(), Utils.enseignant.getPrenom()));
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.container);

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

        loadEnseignantModules();
    }

    private void checkEnseignantExistence() {
        if (Utils.enseignant == null)    {

            startLoginActivity();
            finish();
        }
    }
    @Override
    protected void onStart() {

        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null)   {

            enseignantNomPrenomTextView.setText(String.format("%s %s", Utils.enseignant.getNom(), Utils
                    .enseignant.getPrenom()));
            enseignantEmailTextView.setText(Utils.enseignant.getEmail());
            Picasso.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(enseignantImageView);
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.parametresItem) {
            // Handle the camera action
        } else if (id == R.id.seDeconnecterItem) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null)    {

                AuthUI.getInstance().signOut(this);
                Utils.enseignant = null;
                startLoginActivity();
                finish();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadEnseignantModules()    {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.chargement_modules_loading_message));

        String pathToEnseignant = Utils.firebasePath(Utils.ENSEIGNANT_MODULE, Utils.enseignant.getId());

        Query query = Utils.database.getReference(pathToEnseignant).orderByChild("semestre").startAt(0);

        query.keepSynced(true); // Keeping data fresh

        progressDialog.show();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                    Module module = snapshot.getValue(Module.class);
                    modulesList.add(module);
                }

                mPageAdapter = new ModulesPageAdapter(getSupportFragmentManager(), modulesList, MainActivity.this);
                mViewPager.setAdapter(mPageAdapter);

                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    private void startLoginActivity() {

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return true;
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
