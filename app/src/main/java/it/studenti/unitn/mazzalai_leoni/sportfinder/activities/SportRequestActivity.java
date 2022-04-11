package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.adapters.SportRequestAdapter;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.SportRequestItem;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class SportRequestActivity extends AppCompatActivity implements SportRequestAdapter.OnCheckListener {

    private static String TAG = "SportRequestActivity";

    private ArrayList<SportRequestItem> sportRequestList;
    private RecyclerView recyclerView;
    private SportRequestAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_requests);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.noRequestsTV).setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();

        sportRequestList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        adapter = new SportRequestAdapter(sportRequestList, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        db.collection(SportFinderConstants.SPORTS_REQUEST_PATH)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            findViewById(R.id.noRequestsTV).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.noRequestsTV).setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String sport = document.get("sport").toString();
//                                String user = document.get("user").toString();

                                sport = sport.substring(0, 1).toUpperCase() + sport.substring(1).toLowerCase();

                                sportRequestList.add(new SportRequestItem(document.getId(), sport));
                            }
                        }
                    } else {
                        Log.d(TAG, "Impossibile caricare le richieste", task.getException());
                    }
                    adapter.notifyDataSetChanged();
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
    public void onAcceptClick(int position) {
        SportRequestItem currentItem = sportRequestList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Accettare lo sport?")
                .setMessage("Lo sport sarà aggiunto agli sport validi")
                .setIcon(R.drawable.ic_baseline_pending_actions_24)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    HashMap<String, String> newDoc = new HashMap<>();
                    newDoc.put("sport", currentItem.getSport());
                    db.collection("sports")
                            .add(newDoc);

                    db.collection("sportsRequests")
                            .document(currentItem.getId())
                            .delete();
                    sportRequestList.remove(currentItem);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Sport accettato!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null).show();

        if (sportRequestList.size() == 0) {
            findViewById(R.id.noRequestsTV).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRejectClick(int position) {
        SportRequestItem currentItem = sportRequestList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Rifiutare lo sport?")
                .setMessage("La richiesta di questo sport sarà cancellata")
                .setIcon(R.drawable.ic_baseline_pending_actions_24)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    db.collection("sportsRequests")
                            .document(currentItem.getId())
                            .delete();
                    sportRequestList.remove(currentItem);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Sport rifiutato!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null).show();

        if (sportRequestList.size() == 0) {
            findViewById(R.id.noRequestsTV).setVisibility(View.VISIBLE);
        }
    }

/*    @Override
    public void onMoreActions(int position) {
        SportRequestItem currentItem = sportRequestList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Azioni disponibili")
                .setItems(R.array.azioniSportRequests, (dialog, which) -> {
                    switch (which) {
                        case 0: //Ban user
                            currentItem.getUser();
                            break;
                        default:
                            break;
                    }
                }).show();
    }*/
}