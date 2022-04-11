package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import it.studenti.unitn.mazzalai_leoni.sportfinder.adapters.SuggestedChangesAdapter;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.SuggestedChangesItem;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class SuggestedChangesListActivity extends AppCompatActivity implements SuggestedChangesAdapter.OnSuggestedChangeClickListener {

    private static final String TAG = "SuggestedChangesListActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<SuggestedChangesItem> changesList;
    private SuggestedChangesAdapter adapter;

    private String image_count;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugested_changes_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        changesList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new SuggestedChangesAdapter(changesList, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        String locID = null;
        try {
            locID = getIntent().getExtras().getString("EXTRA_LOCATION_ID");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_generic), Toast.LENGTH_SHORT).show();
            this.onBackPressed();
        }

        db.collection(SportFinderConstants.LOCATION_PATH)
                .whereEqualTo("id", locID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ((TextView) findViewById(R.id.nomeLocationTV)).setText(document.get("address").toString());
                                image_count = document.get("image_count").toString();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.err_firebase), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, getString(R.string.err_firebase), task.getException());
                        }
                    }
                });

        db.collection(SportFinderConstants.SUGGESTED_CHANGES_PATH)
                .whereEqualTo("locationId", locID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                boolean hasImage = document.get("image") != null;
                                String message = document.get("message").toString();
                                String author = document.get("author").toString();
                                boolean hasTime = document.get("opening-time") != null;
                                changesList.add(new SuggestedChangesItem(document.getId(), author, message, hasImage, hasTime));
                            }
                            adapter.notifyDataSetChanged();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(int position) {
        SuggestedChangesItem currentItem = changesList.get(position);
        Intent intent = new Intent(this, ApproveChangesActivity.class);
        intent.putExtra("EXTRA_CHANGES_ID", currentItem.getId());
        intent.putExtra("EXTRA_IMAGE_COUNT", image_count);
        startActivity(intent);
    }
}
