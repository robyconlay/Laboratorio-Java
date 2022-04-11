package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.adapters.MyLocationAdapter;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.MyLocationItem;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class MyLocationsActivity extends AppCompatActivity implements MyLocationAdapter.OnLocationActionsListener {

    private static final String TAG = "MyLocationsActivity";

    private ArrayList<MyLocationItem> myLocationsList;
    private RecyclerView recyclerView;
    private MyLocationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_locations);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.noLocationsTV).setVisibility(View.INVISIBLE);

        myLocationsList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        adapter = new MyLocationAdapter(myLocationsList, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        db.collection(SportFinderConstants.LOCATION_PATH)
                .whereEqualTo("author", mAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                findViewById(R.id.noLocationsTV).setVisibility(View.VISIBLE);
                            } else {
                                findViewById(R.id.noLocationsTV).setVisibility(View.GONE);
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String address = document.get("address").toString();
                                    String timestamp = document.get("timestamp").toString();
                                    String suggestionsCount = document.get("pending-suggestion").toString();

                                    myLocationsList.add(new MyLocationItem(document.getId(), address, timestamp, suggestionsCount));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.err_firebase), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, getString(R.string.err_firebase), task.getException());
                        }
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOpenLocation(int position) {
        MyLocationItem currentItem = myLocationsList.get(position);
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("EXTRA_LOCATION_ID", currentItem.getId());
        startActivity(intent);
    }

    @Override
    public void onOpenSuggestions(int position) {
        MyLocationItem currentItem = myLocationsList.get(position);
        Intent intent = new Intent(this, SuggestedChangesListActivity.class);
        intent.putExtra("EXTRA_LOCATION_ID", currentItem.getId());
        startActivity(intent);
    }

}

