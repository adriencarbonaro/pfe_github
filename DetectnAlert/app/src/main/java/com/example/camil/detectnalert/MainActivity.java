package com.example.camil.detectnalert;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.security.cert.Extension;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    /**
     * Header information.
     *
     * mStatusTextView username
     * mDetailTextView profession
     *
     * navigationView  the variable getting the header layout view.
     * hview           the variable getting the header view.
     */
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private NavigationView navigationView;
    private View           hView;

    private Button mNewAlertButton;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]


    /* Creation de la page */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
        Intent SignOutIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(SignOutIntent);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID)
    {
        super.setContentView(layoutResID);
        onCreateDrawer() ;
    }

    protected void onCreateDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button logTokenButton = findViewById(R.id.logTokenButton);

        if(logTokenButton != null) {
            logTokenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get token
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            Log.w("tag", token);
                        }
                    });
                }
            });
        }

        mNewAlertButton = (Button) findViewById(R.id.newAlertButton);

        if (mNewAlertButton != null) {
            mNewAlertButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent NewAlertIntent = new Intent(MainActivity.this, AlertActivity.class);
                    startActivity(NewAlertIntent);
                }
            });
        }
        else
        {
            Log.d("tag", "button null");
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Get the header view
            navigationView  = (NavigationView) findViewById(R.id.nav_view);
            hView           = navigationView.getHeaderView(0);

            // Getting the information inside the header
            mStatusTextView = (TextView) hView.findViewById(R.id.email);
            mDetailTextView = (TextView) hView.findViewById(R.id.user);

            // Set information
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

        /**
         * Gestion des items de la nav bar
         * Pour ajouter des items, ajouter des boucles if avec l'id de l'item (nav_xxx)
         */
        if (id == R.id.nav_historique_alertes)
        {
            Intent TestActivityIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(TestActivityIntent);
        }
        else if (id == R.id.nav_account)
        {
            Intent TestActivityIntent = new Intent(MainActivity.this, MyAccountActivity.class);
            startActivity(TestActivityIntent);
        }
        else if (id == R.id.nav_logout)
        {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
