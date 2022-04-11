package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.ReportItem;
import it.studenti.unitn.mazzalai_leoni.sportfinder.adapters.ReportAdapter;

public class ReportActivity extends AppCompatActivity implements ReportAdapter.OnReportActionsListener {

    private static String TAG = "ReportActivity";

    private FirebaseFirestore db;

    private ArrayList<ReportItem> reportList;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.noReportsTV).setVisibility(View.VISIBLE);
        findViewById(R.id.noReportsTV).setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();

        reportList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        adapter = new ReportAdapter(reportList, this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        db.collection("reports")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    findViewById(R.id.noReportsTV).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.noReportsTV).setVisibility(View.GONE);

                    for (DocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        String text = document.get("text").toString();
                        String timesReported = document.get("timesReported").toString();
                        String reviewID = document.get("reviewID").toString();

                        reportList.add(new ReportItem(id, reviewID, text, timesReported));
                    }
                }
            } else {
                Log.d(TAG, "Impossibile caricare le segnalazioni", task.getException());
            }
            adapter.notifyDataSetChanged();
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, LandingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMoreActions(int position) {
        ReportItem currentItem = reportList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Azioni disponibili")
                .setItems(R.array.azioniSegnalazioni, (dialog, which) -> {
                    switch (which) {
                        case 0://Ignore segnalazione
                            new AlertDialog.Builder(this)
                                    .setTitle("Ignorare segnalazione?")
                                    .setMessage("La segnalazione sarà ignorata")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, (dialog2, whichButton) -> {
                                        db.collection("reports")
                                                .document(currentItem.getId())
                                                .delete();
                                        reportList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Segnalazione rimossa!", Toast.LENGTH_SHORT).show();

                                        if (reportList.size() == 0) {
                                            findViewById(R.id.noReportsTV).setVisibility(View.VISIBLE);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null).show();
                            break;
                        case 1://Rimuovere la recensione attiva
                            new AlertDialog.Builder(this)
                                    .setTitle("Rimuovere recensione?")
                                    .setMessage("La segnalazione e la recensione originale saranno cancellati")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, (dialog2, whichButton) -> {
                                        db.collection("reviews")
                                                .document(currentItem.getReviewID())
                                                .delete();

                                        db.collection("reports")
                                                .document(currentItem.getId())
                                                .delete();
                                        reportList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Recensione rimossa!", Toast.LENGTH_SHORT).show();

                                        if (reportList.size() == 0) {
                                            findViewById(R.id.noReportsTV).setVisibility(View.VISIBLE);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null).show();
                            break;
                        default:
                            break;
                    }
                }).show();

    }

   /* @Override
    public void onDelete(int position) {
        ReportItem currentItem = reportList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Rimuovere report?")
                .setMessage("Non sarà possibile eseguire altre azioni su questo report")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    db.collection("reports")
                            .document(currentItem.getId())
                            .delete();
                    reportList.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Report rimosso!", Toast.LENGTH_SHORT).show();

                })
                .setNegativeButton(android.R.string.no, null).show();
    }*/
}
