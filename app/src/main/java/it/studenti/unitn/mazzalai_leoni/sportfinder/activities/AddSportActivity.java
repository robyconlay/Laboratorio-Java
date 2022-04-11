package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.authentication.AuthActivity;

public class AddSportActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "AddSportActivity";
    public ArrayList<String> sportsList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sport);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        db.collection("sports")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String sport = document.get("sport").toString();

                            LinearLayout sportsLL = findViewById(R.id.sportsLL);
                            this.sportsList.add(sport);
                            TextView textView = new TextView(getApplicationContext());
                            textView.setText(sport);
                            sportsLL.addView(textView);
                        }
                    } else {
                        Log.d(TAG, "Impossibile caricare gli sport", task.getException());
                    }
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                });


        findViewById(R.id.aggiungiButton).setOnClickListener(this);
    }

    /**
     * if at the start of the activity the user is not signed in, bring them to the AuthActivity
     */
    @Override
    protected void onStart() {
        super.onStart();
//
//        if (mAuth.getCurrentUser() == null) {
//            Intent intent = new Intent(AddSportActivity.this, AuthActivity.class);
//            intent.putExtra("EXTRA_ACTION", "addsport");
//            startActivity(intent);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aggiungiButton:
                addSportRequest();
                break;
            default:
                break;
        }
    }

    public void addSportRequest() {
        EditText sportET = findViewById(R.id.sportET);
        String newSport = sportET.getText().toString().trim();
        if (this.sportsList.contains(newSport)) {
            sportET.setError("Sport già presente");
        } else {
//            String userEmail = mAuth.getCurrentUser().getEmail();
            Map<String, String> sportRequest = new HashMap<>();
//            sportRequest.put("user", userEmail);
            sportRequest.put("sport", newSport);
            db.collection("sportsRequests")
                    .add(sportRequest);

            Toast.makeText(AddSportActivity.this, "La richiesta è in attesa di essere approvata", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AddSportActivity.this, LandingActivity.class);
            startActivity(intent);
        }
    }
}
