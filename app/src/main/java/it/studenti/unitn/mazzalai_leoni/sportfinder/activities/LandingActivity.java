package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.authentication.AuthActivity;

public class LandingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CHANNEL_ID = "default_channel_id";
    private static String TAG = "LandingActivity";

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        findViewById(R.id.cardMappa).setOnClickListener(this);
        findViewById(R.id.cardAggiungiLuogo).setOnClickListener(this);
        findViewById(R.id.cardAggiungiSport).setOnClickListener(this);
        findViewById(R.id.cardAuth).setOnClickListener(this);
        findViewById(R.id.cardSportRequests).setOnClickListener(this);
        findViewById(R.id.cardReports).setOnClickListener(this);
        findViewById(R.id.cardMyLocations).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        loadUI();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.cardMappa:
                intent = new Intent(this, MapsActivity.class);
                break;
            case R.id.cardAggiungiLuogo:
                intent = new Intent(this, AddLocationActivity.class);
                break;
            case R.id.cardAggiungiSport:
                intent = new Intent(this, AddSportActivity.class);
                break;
            case R.id.cardAuth:
                auth();
                break;
            case R.id.cardSportRequests:
                intent = new Intent(this, SportRequestActivity.class);
                break;
            case R.id.cardReports:
                intent = new Intent(this, ReportActivity.class);
                break;
            case R.id.cardMyLocations:
                intent = new Intent(this, MyLocationsActivity.class);
                break;
            default:
                return;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    public void loadUI() {
        updateUIAuth();
//default everything is invisible
        findViewById(R.id.cardMyLocations).setVisibility(View.GONE);
        findViewById(R.id.cardReports).setVisibility(View.GONE);
        findViewById(R.id.cardSportRequests).setVisibility(View.GONE);

        if (mAuth.getCurrentUser() == null)
            return;
        //logged users have MyLocation section
        findViewById(R.id.cardMyLocations).setVisibility(View.VISIBLE);

        //admins have reports and sport requests
        List<String> admins = Arrays.asList(getResources().getStringArray(R.array.admins));
        if (admins.contains(mAuth.getCurrentUser().getEmail())) {
            findViewById(R.id.cardReports).setVisibility(View.VISIBLE);
            findViewById(R.id.cardSportRequests).setVisibility(View.VISIBLE);
        }
    }

    public void updateUIAuth() {
        Boolean logged = (mAuth.getCurrentUser() == null);

        findViewById(R.id.iconaAuth).setRotation(logged ? 0 : 180);
        ((TextView) findViewById(R.id.titoloAuth)).setText(logged ? "Login" : "Logout");
        ((TextView) findViewById(R.id.descrizioneAuth)).setText(logged ? R.string.login_description : R.string.logout_description);
    }

    public void auth() {
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(LandingActivity.this)
                    .setTitle("Logout")
                    .setMessage(getString(R.string.ask_logout))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), R.string.disconnected, Toast.LENGTH_SHORT).show();
                        loadUI();
                    }).setNegativeButton(R.string.no, null).show();
        }
    }
}
