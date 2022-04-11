package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderCommons;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class ApproveChangesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "APP_CHNG";
    private SportFinderCommons commons = new SportFinderCommons();

    private String changesId;
    private String locationId;
    private String author;
    private String openingTime;
    private String closureTime;
    private String imageId;
    private byte[] image;
    private String message;
    private int imageCount;
    private String imagePath;

    private TextView msgTextView;
    private TextView authorTextView;
    private TextView openingTimes;
    private ImageView imageView;

    private Button approveAll;
    private Button rejectAll;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_changes);

        progressBar = findViewById(R.id.progressBarApprove);
        progressBar.setVisibility(View.VISIBLE);

        msgTextView = findViewById(R.id.message);
        authorTextView = findViewById(R.id.author);
        openingTimes = findViewById(R.id.openingTimesTV);
        imageView = findViewById(R.id.imageViewApprove);

        approveAll = findViewById(R.id.approveAll);
        approveAll.setBackgroundColor(Color.GREEN);
        approveAll.setOnClickListener(this);
        rejectAll = findViewById(R.id.rejectAll);
        rejectAll.setBackgroundColor(Color.RED);
        rejectAll.setOnClickListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        changesId = null;

        if (extras != null) {
            changesId = extras.getString("EXTRA_CHANGES_ID");
            imageCount = extras.getInt("EXTRA_IMAGE_COUNT");
        }

        if (changesId == null) {
            Toast.makeText(this, getString(R.string.err_log), Toast.LENGTH_SHORT).show();
            sendToLandingActivity();
        } else {
            getSuggestedChange();
        }

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

    private void getSuggestedChange() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(SportFinderConstants.SUGGESTED_CHANGES_PATH).document(changesId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot result = task.getResult();
                    if (result != null) {
                        if (result.get("message") != null) {
                            message = result.get("message").toString();
                            msgTextView.setText(message);
                        }

                        if (result.get("author") != null) {
                            author = result.get("author").toString();
                            String text = getString(R.string.changes_by) + " " + author;
                            authorTextView.setText(text);
                        }

                        if (result.get("locationId") != null) {
                            locationId = result.get("locationId").toString();
                        }

                        if (result.get("opening-time") != null && result.get("closure-time") != null) {
                            openingTime = result.get("opening-time").toString();
                            closureTime = result.get("closure-time").toString();
                            String text = getString(R.string.open) + " " + getString(R.string.from) +
                                    " " + openingTime + " " + getString(R.string.until) + " " + closureTime;
                            openingTimes.setText(text);
                        } else {
                            openingTime = null;
                            closureTime = null;
                        }

                        if (result.get("image") != null) {
                            imageId = result.get("image").toString();
                            getImage();
                        } else {
                            image = null;
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                   Toast.makeText(getApplicationContext(), getString(R.string.err_log), Toast.LENGTH_SHORT).show();
                   progressBar.setVisibility(View.GONE);
                   sendToLandingActivity();
                });
    }

    private void getImage() {
        //create the reference and the path
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef;
        String root = SportFinderConstants.SUGGESTED_IMAGES_PATH + "/";
        String specId = locationId + "/";

        imagePath = root + specId + imageId;
        imageRef = storageRef.child(imagePath);
        imageRef.getBytes(SportFinderConstants.MAX_DIM)
                .addOnSuccessListener(bytes -> {
                    //create a drawable from the bytes
                    image = bytes;
                    Drawable d = Drawable
                            .createFromStream(new ByteArrayInputStream(bytes), null);
                    //display the drawable
                    imageView.setImageDrawable(d);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(exception ->
                    Toast.makeText(getApplicationContext(), R.string.err_images, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.approveAll:
                if (image != null && openingTimes != null && closureTime != null) {
                    commons.updateOpeningTimes(locationId, openingTime, closureTime);
                    publishImage(locationId, image, imageCount);
                } else if (image != null) {
                    publishImage(locationId, image, imageCount);
                } else if (openingTimes != null && closureTime != null) {
                    commons.updateOpeningTimes(locationId, openingTime, closureTime);
                }
                deleteSuggestedChanges();
                sendToLocation(locationId);
            case R.id.rejectAll:
                deleteSuggestedChanges();
                deleteSuggestedImage();
                sendToLocation(locationId);
        }
    }

    private void deleteSuggestedChanges() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(SportFinderConstants.LOCATION_PATH).document(locationId)
                .get()
                .addOnCompleteListener(task -> {
                   long pend = (long) task.getResult().get("pending-suggestion");
                   pend--;
                   db.collection(SportFinderConstants.LOCATION_PATH).document(locationId)
                           .update("pending-suggestion", pend);
                });
        db.collection(SportFinderConstants.SUGGESTED_CHANGES_PATH).document(changesId)
                .delete()
                .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
    }

    public void publishImage(String id, byte[] image, int originalImageNumber) {
        String root = SportFinderConstants.IMAGES_PATH;
        String separator = "/";
        String highScore = "-";
        String path = root + separator + id + separator + id + highScore + originalImageNumber;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child(path);
        UploadTask uploadTask = imageRef.putBytes(image);
        uploadTask.addOnCompleteListener(task -> {
            Log.d(TAG, "image saved");
            deleteSuggestedImage();
            int imageCount = originalImageNumber + 1;
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(SportFinderConstants.LOCATION_PATH).document(id)
                    .update("image_count", imageCount);
        });
    }

    private void deleteSuggestedImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference imageRef = storageReference.child(imagePath);
        imageRef.delete();
    }

    public void sendToLocation(String id) {
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("EXTRA_LOCATION_ID", id);
        startActivity(intent);
    }

    private void sendToLandingActivity() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
    }
}
