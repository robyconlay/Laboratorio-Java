package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.adapters.ReviewAdapter;
import it.studenti.unitn.mazzalai_leoni.sportfinder.items.ReviewItem;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, ReviewAdapter.OnReviewActionsListener {

    private static final String TAG = "LocationActivity";
    private FirebaseFirestore db;

    private TextView addressText;
    private ProgressBar progressBar;
    private Button modifyLocation;
    private ArrayList<Drawable> images;
    private ImageView imageView;
    private ImageView prevImage;
    private ImageView nextImage;
    private int selectedImage;
    private boolean reviewInserted = false;
    private ArrayList<ReviewItem> reviewList;
    private String author;
    private LatLng locationLatLng = null;
    private String locationAddress;
    private String locationID = null;
    private TextView sports;
    private TextView openingTimes;
    private Integer pendingSuggestions;

    private FirebaseAuth mAuth;
    private String user;
    private View mainLayout;
    private View imagePreviewLayout;
    private ImageView imagePreview;

    //number of images of the selected location
    private int IMAGE_COUNT;

    private ReviewAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mainLayout = findViewById(R.id.mainLayout);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayoutLocation);
        imagePreview = findViewById(R.id.imagePreviewLocation);
        imagePreviewLayout.setActivated(false);
        mainLayout.setActivated(true);

        //bind the references
        addressText = findViewById(R.id.addressText);
        sports = findViewById(R.id.sportList);
        openingTimes = findViewById(R.id.openHoursTV);
        addressText.setText("");
        sports.setText("");
        openingTimes.setText("");
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        Button addReviewButton = findViewById(R.id.addReviewButton);
        modifyLocation = findViewById(R.id.modifyButton);

        images = new ArrayList<>();
        reviewList = new ArrayList<>();

        addReviewButton.setOnClickListener(this);
        findViewById(R.id.shareButton).setOnClickListener(this);
        modifyLocation.setOnClickListener(this);

        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();

        //needs an id to load the location (otherwise the page would just be blank)
        Bundle extras = getIntent().getExtras();
        String id = null;

        if (extras != null) {
            id = extras.getString("EXTRA_LOCATION_ID");
        }
        if (id != null) {
            //call the method to get the location from firebase
            getLocation(id);
        } else {
            //getting here from a link
            //check if all is ok
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                            }

                            // Handle the deep link. For example, open the linked content,
                            // or apply promotional credit to the user's account.
                            if (deepLink != null) {
                                String id = deepLink.getQueryParameter("id");
                                getLocation(id);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.err_sharing, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                startActivity(intent);
                            }
                            // ...
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "getDynamicLink:onFailure", e);
                    Toast.makeText(getApplicationContext(), R.string.err_sharing, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (mAuth.getCurrentUser() == null) {
            //disable the review button if not logged
            addReviewButton.setEnabled(false);
            Toast.makeText(this, getString(R.string.no_review), Toast.LENGTH_SHORT).show();

            modifyLocation.setEnabled(false);
            Toast.makeText(this, getString(R.string.cannot_modify), Toast.LENGTH_SHORT).show();
        } else {
            user = mAuth.getCurrentUser().getEmail();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ReviewAdapter(reviewList, this);

        //recycle view for the reviews
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mainLayout.isActivated()) {
                    this.onBackPressed();
                } else if (imagePreviewLayout.isActivated()) {
                    switchVisibility();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //method to take the data from firebase
    public void getLocation(String id) {
        locationID = id;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //get the reference of the collection with a query (id = id)
        db.collection(SportFinderConstants.LOCATION_PATH)
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //handle more than one result
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //take the data of the location we need
                                String address = document.get("address").toString();
                                addressText.setText(address);
                                String imgCount = document.get("image_count").toString();
                                IMAGE_COUNT = Integer.parseInt(imgCount);
                                author = document.get("author").toString();
                                String creationTime = getString(R.string.loc_created_at) + " " + document.get("timestamp").toString();
                                ((TextView) findViewById(R.id.locTimestampTV)).setText(creationTime);

                                String opening = "";
                                if (document.contains("opening-time") && document.contains("closure-time")) {
                                    String openTime = document.get("opening-time").toString();
                                    String closeTime = document.get("closure-time").toString();
                                    opening = getString(R.string.open) + " " + getString(R.string.from) +
                                            " " + openTime + " " + getString(R.string.until) + " " + closeTime;
                                } else {
                                    opening = getString(R.string.no_opening_time_info);
                                }
                                openingTimes.setText(opening);

                                ArrayList<String> sportsAvailable = (ArrayList<String>) document.get("sports");
                                for (int i = 0; i < sportsAvailable.size(); i++) {
                                    String sport = sportsAvailable.get(i);
                                    sports.append(sport);
                                    if (i < sportsAvailable.size() - 1) {
                                        sports.append(", ");
                                    }
                                }

                                locationAddress = address;
                                locationLatLng = new LatLng((double) document.get("lat"), (double) document.get("lng"));
                                String ps = document.get("pending-suggestion").toString();
                                pendingSuggestions = Integer.parseInt(ps);

                                if (pendingSuggestions > 0) {
                                    checkSuggestions();
                                }

                            }

                            //once you have the data take the images and the reviews
                            getImages(id, IMAGE_COUNT);
                            getReviews(id, db);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.err_location), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, getString(R.string.err_location), task.getException());
                        }
                    }
                });
    }

    //check if the current user already suggest a change
    private void checkSuggestions() {
        int i = 0;
        AtomicBoolean found = new AtomicBoolean(false);
        while (i < pendingSuggestions && !found.get()) {
            Task<DocumentSnapshot> doc = db.collection(SportFinderConstants.SUGGESTED_CHANGES_PATH)
                    .document(locationID + "-" + i)
                    .get();
            doc.addOnCompleteListener(task -> {
                DocumentSnapshot result = doc.getResult();
                if (result.get("author") != null) {
                    String user = result.get("author").toString();
                    if (user.equals(this.user)) {
                        modifyLocation.setEnabled(false);
                        found.set(true);
                    }
                }
            });
            i++;
        }
    }

    //request the reviews from the db
    public void getReviews(String id, FirebaseFirestore db) {
        //REVIEW_COUNT will access all the reviews on firebase
        db.collection(SportFinderConstants.REVIEWS_PATH)
                .whereEqualTo("locationID", id)
                .orderBy("totalVotes", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // take the results
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String user = document.get("user").toString();
                                String text = document.get("description").toString();
                                String timestamp = document.get("timestamp").toString();
                                ArrayList<String> upvotes = (ArrayList<String>) document.get("upvotes");
                                boolean isAuthor = author.equals(user);

                                Log.d(TAG, "onComplete: upvotes" + upvotes);
                                reviewList.add(new ReviewItem(document.getId(), text, user, isAuthor, timestamp, upvotes));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void getImages(String id, int IMAGE_COUNT) {
        //create the reference and the path
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef;
        String path = "";
        String root = SportFinderConstants.IMAGES_PATH + "/";
        String specId = id + "/" + id + "-";

        for (int i = 0; i < IMAGE_COUNT; i++) {
            path = root + specId + i;
            imageRef = storageRef.child(path);
            imageRef.getBytes(SportFinderConstants.MAX_DIM)
                    .addOnSuccessListener(bytes -> {
                        //create a drawable from the bytes
                        Drawable d = Drawable
                                .createFromStream(new ByteArrayInputStream(bytes), null);
                        //call the method to display the drawable
                        addImage(d);
                    }).addOnFailureListener(exception ->
                    Toast.makeText(getApplicationContext(), R.string.err_images, Toast.LENGTH_SHORT).show());
        }
        progressBar.setVisibility(View.GONE);
        imageView.setOnClickListener(this);
    }

    public void addImage(Drawable d) {
        images.add(d);
        if (images.size() == IMAGE_COUNT) {
            //once having all the images display them
            setImageView();
        }
    }

    public void setImageView() {
        //display first image
        imageView.setImageDrawable(images.get(0));
        selectedImage = 0;

        //bind the view for switch imahe
        prevImage = findViewById(R.id.prevImage);
        nextImage = findViewById(R.id.nextImage);

        prevImage.setEnabled(false);
        prevImage.setBackgroundColor(Color.LTGRAY);

        if (images.size() == 1) {
            nextImage.setEnabled(false);
            nextImage.setBackgroundColor(Color.LTGRAY);
        } else {
            nextImage.setOnClickListener(this);
        }
    }

    /**
     * handle layout click events
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        //catch the right view clicked
        switch (v.getId()) {
            case R.id.addReviewButton:
                addReview();
                break;
            case R.id.nextImage:
                goNext();
                break;
            case R.id.prevImage:
                goPrev();
                break;
            case R.id.shareButton:
                shareLocation();
                break;
            case R.id.modifyButton:
                sendToModifyLocation();
                break;
            case R.id.imageView:
                openPreview();
                break;
            case R.id.imagePreviewLocation:
                switchVisibility();
                break;
            default:
                break;
        }
    }

    public void switchVisibility() {
        //switch visibility
        imagePreview.setVisibility(View.GONE);
        imagePreviewLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        imagePreviewLayout.setActivated(false);
        mainLayout.setActivated(true);
    }

    private void openPreview() {
        Drawable d = images.get(selectedImage);

        mainLayout.setVisibility(View.INVISIBLE);

        //set the preview with the URI of the image
        imagePreview.setImageDrawable(d);
        //set it visible
        imagePreviewLayout.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setBackgroundColor(Color.BLACK);

        mainLayout.setActivated(false);
        imagePreviewLayout.setActivated(true);
        imagePreview.setOnClickListener(this);
    }

    private void sendToModifyLocation() {
        Intent intent = new Intent(this, ModifyLocationActivity.class);
        intent.putExtra("EXTRA_LOCATION_ID", locationID);
        intent.putExtra("EXTRA_PENDING_SUGGESTION", pendingSuggestions);
        intent.putExtra("AUTHOR", author);
        intent.putExtra("IMAGE", IMAGE_COUNT);
        startActivity(intent);
    }

    /**
     * create AlertDialog to get text from user, and add it to firebase and update the UI
     * allows the user to create only one review per activity lifetime
     */
    public void addReview() {
        if (this.reviewInserted) {
            Toast.makeText(getApplicationContext(), R.string.just_reviewed, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.write_review));

        final EditText input = new EditText(getBaseContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.location_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get the user to check if author of the location
                String userEmail = mAuth.getCurrentUser().getEmail();
                boolean isAuthor = author.equals(userEmail);

                //extract the text
                String text = input.getText().toString();
                Calendar calendar = Calendar.getInstance();
                String timestamp = calendar.get(Calendar.DATE) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

                //upload on firebase
                //build the object
                Map<String, Object> review = new HashMap<>();
                review.put("locationID", locationID);
                review.put("user", userEmail);
                review.put("description", text.trim());
                review.put("timestamp", timestamp);
                review.put("upvotes", new ArrayList<String>());
                review.put("totalVotes", 0);

                DocumentReference ref = db.collection("reviews").document();
                ref.set(review);

                //add to the list recycler
                reviewList.add(new ReviewItem(ref.getId(), text, userEmail, isAuthor, timestamp));
                adapter.notifyDataSetChanged();

                reviewInserted = true;

                Toast.makeText(getApplicationContext(), R.string.review_added, Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton(getString(R.string.location_annulla), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }

    //method to switch to the next image
    public void goNext() {
        selectedImage++;
        imageView.setImageDrawable(images.get(selectedImage));
        int ctrl = images.size() - 1;
        if (ctrl == selectedImage) {
            nextImage.setEnabled(false);
            nextImage.setBackgroundColor(Color.LTGRAY);
        }
        enablePrev();
    }

    //method to switch to the preview image
    public void goPrev() {
        selectedImage--;
        imageView.setImageDrawable(images.get(selectedImage));
        if (0 == selectedImage) {
            prevImage.setEnabled(false);
            prevImage.setBackgroundColor(Color.LTGRAY);
        }
        enableNext();
    }

    //methods to enable the view Prev and Next
    private void enablePrev() {
        prevImage.setEnabled(true);
        prevImage.setBackgroundColor(Color.WHITE);
        prevImage.setOnClickListener(this);
    }

    private void enableNext() {
        nextImage.setEnabled(true);
        nextImage.setBackgroundColor(Color.WHITE);
        nextImage.setOnClickListener(this);
    }

    /**
     * send user to Google Maps, where they can get directions
     */
    public void openLocationInMaps() {
        Uri mapUri = Uri.parse("http://maps.google.com/maps?q=loc:" + locationLatLng.latitude + "," + locationLatLng.longitude + " (" + locationAddress + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * share the location via lat, lng and title
     */
    private void shareLocation() {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://sport_finder_studenti_unitn_it/location?id=" + locationID))
                .setDomainUriPrefix("https://sportfinder.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildDynamicLink();
        Uri dynamicLinkUri = dynamicLink.getUri();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " + locationAddress);
        shareIntent.putExtra(Intent.EXTRA_TEXT, dynamicLinkUri.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    @Override
    public void onMoreActions(int position) {
        ReviewItem currentItem = reviewList.get(position);
        HashMap<String, Object> report = new HashMap<>();
        new AlertDialog.Builder(this)
                .setTitle("Azioni disponibili")
                .setItems(R.array.azioniReview, (dialog, which) -> {
                    switch (which) {
                        case 0://segnala
                            db.collection("reports")
                                    .whereEqualTo("text", currentItem.getReview())
                                    .whereEqualTo("offender", currentItem.getUser())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.getResult().size() == 0) {
                                            report.put("text", currentItem.getReview());
                                            report.put("offender", currentItem.getUser());
                                            report.put("timesReported", 1);
                                            report.put("reviewID", currentItem.getId());
                                            db.collection("reports")
                                                    .add(report);
                                        } else {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                DocumentReference ref = db.collection("reports")
                                                        .document(document.getId());
                                                ref.update("timesReported", (((long) document.get("timesReported")) + 1));
                                            }
                                        }
                                    });
                            reviewList.remove(currentItem);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Commento segnalato!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }).show();
    }

    @Override
    public void onUpvoteReview(int position) {
        ReviewItem currentItem = reviewList.get(position);
        currentItem.novote(mAuth.getCurrentUser().getEmail());
        currentItem.upvote(mAuth.getCurrentUser().getEmail());
        adapter.notifyDataSetChanged();

        DocumentReference ref = db.collection("reviews")
                .document(currentItem.getId());
        ref.update("upvotes", FieldValue.arrayUnion(mAuth.getCurrentUser().getEmail()));
        ref.update("totalVotes", currentItem.getTotalVotes());
    }

    @Override
    public void onNoVoteReview(int position) {
        ReviewItem currentItem = reviewList.get(position);
        currentItem.novote(mAuth.getCurrentUser().getEmail());
        adapter.notifyDataSetChanged();

        DocumentReference ref = db.collection("reviews")
                .document(currentItem.getId());
        ref.update("upvotes", FieldValue.arrayRemove(mAuth.getCurrentUser().getEmail()));
        ref.update("totalVotes", currentItem.getTotalVotes());
    }
}

