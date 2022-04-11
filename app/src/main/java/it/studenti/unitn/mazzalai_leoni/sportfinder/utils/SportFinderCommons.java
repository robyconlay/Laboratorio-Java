package it.studenti.unitn.mazzalai_leoni.sportfinder.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SportFinderCommons extends AppCompatActivity {

    private static final String TAG = "COMMONS";

    public void updateOpeningTimes(String id, String openingTime, String closureTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference doc = db.collection(SportFinderConstants.LOCATION_PATH)
                .document(id);

        //check if opening time and closure time exists
        db.collection(SportFinderConstants.LOCATION_PATH).document(id).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot loc = task.getResult();
                    if (loc.contains("opening-time") && loc.contains("closure-time")) {
                        doc.update("opening-time", openingTime);
                        doc.update("closure-time", closureTime);
                    } else {
                        Map<String, Object> times = new HashMap<>();
                        times.put("opening-time", openingTime);
                        times.put("closure-time", closureTime);
                        doc.set(times, SetOptions.merge());
                    }
                });
    }



}
