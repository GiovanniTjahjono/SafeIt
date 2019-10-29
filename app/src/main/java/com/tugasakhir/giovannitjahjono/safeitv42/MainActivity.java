package com.tugasakhir.giovannitjahjono.safeitv42;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import com.cloudrail.si.CloudRail;
import com.vistrav.ask.Ask;
import com.vistrav.ask.annotations.AskDenied;
import com.vistrav.ask.annotations.AskGranted;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private SharedPreferences sp;
    private SharedPreferences.Editor spe;
    CloudServices cs = new CloudServices();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Ask.on(this).forPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).go();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        CloudServices.getInstance().prepare(this);
        //sp = this.getPreferences(Context.MODE_PRIVATE);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        int service = sp.getInt("service", 0);
        //
        if (service != 0) {
            this.navigationView.getMenu().getItem(service-1).setChecked(true);
            this.navigateToService(service);
        }
        else {

        }
    }

    @Override
    protected void onStop() {
        CloudServices.getInstance().storePersistent();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Files fragment = (Files) getFragmentManager().findFragmentByTag("files");

            if(fragment == null) {
                super.onBackPressed();
                return;
            }
            if(fragment.onBackPressed()) super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        if(sp.getInt("service", 0) == 0){
            TextView usernameDisplay = (TextView) findViewById(R.id.txtUsernameLogin);
            TextView emailDisplay = (TextView) findViewById(R.id.txtEmailLogin);
            try{
                usernameDisplay.setText(sp.getString("username","unknown"));
                emailDisplay.setText(sp.getString("email_user","unknown"));
            }
            catch (Exception e){

            }
        }
        return false;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.dropbox) {
            this.navigateToService(1);
        } if (id == R.id.google_drive) {
            this.navigateToService(2);
        } if (id == R.id.box) {
            this.navigateToService(3);
        } if (id == R.id.onedrive) {
            this.navigateToService(4);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void navigateToService(int service) {
        spe = sp.edit();
        spe.putInt("service", service);
        spe.apply();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment content = Files.newInstance(service);
        fragmentTransaction.replace(R.id.content, content, "files");
        fragmentTransaction.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Files fragment = (Files) getFragmentManager().findFragmentByTag("files");
            if (fragment != null) {
                fragment.search(query);
            }
        } else if(intent.getCategories().contains("android.intent.category.BROWSABLE")) {
            CloudRail.setAuthenticationResponse(intent);
        }
        super.onNewIntent(intent);
    }

}