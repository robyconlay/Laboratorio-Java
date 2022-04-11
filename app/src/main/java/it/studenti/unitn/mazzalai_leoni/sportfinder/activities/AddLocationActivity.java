package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.authentication.AuthActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class AddLocationActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int GET_IMAGE = 3;
    public static final int MAX_IMAGE = 5;
    private static final String TAG = "AddLocationActivity";
    private static final int PERMISSION_ID = 44;
    private static final int BORDER = 5;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private FirebaseAuth mAuth;
    private TextInputEditText descriptionEditText; //input fields
    private ImageButton imageButton; //to select an image
    private Button removeButton;
    private LinearLayout sportsLinerarLayout; //select the sports
    private ProgressBar progressBar; //loading...
    private ImageView imageView; //set the images selected
    private View mainLayout; //contain all the elements
    private ImageView imagePreview; //to see the image selected
    private View imagePreviewLayout; //contain the imagePreview
    private int imageCount; //how many images selected
    private Map<String, Object> location; //object for the upload
    private final LocationCallback mLocationCallback = new LocationCallback() {

        /**
         * on result center map camera and save the user's country in storage
         */

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            //save the user country
            Geocoder gc = new Geocoder(getApplicationContext());
            try {
                List<Address> addressList = gc.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                Address address = addressList.get(0);
                String txt = getString(R.string.chosen_position) + " " + address.getAddressLine(0);
                ((TextView) findViewById(R.id.posizioneTV)).setText(txt);
                location.put("address", address.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private Map<String, Object> review;
    private int imageClick; //show which image was clicked for the preview
    private boolean previewToast;
    private boolean exitToast;
    private ArrayList<Uri> imageUriArray; //contains the URIs
    private ArrayList<byte[]> imageBytesArray;
    private FusedLocationProviderClient mFusedLocationClient;
    private SwitchCompat hoursSwitch;
    private TimePicker openTime;
    private TimePicker closureTime;
    private Uri imageUri;

    /*
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     */
    public static void verifyStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //setting some variables
        descriptionEditText = findViewById(R.id.descriptionEditText);
        imageButton = findViewById(R.id.imageButton);
        removeButton = findViewById(R.id.removeButton);
        removeButton.setOnClickListener(this);
        sportsLinerarLayout = findViewById(R.id.sportsLinerarLayout);
        progressBar = findViewById(R.id.progressBar3);
        hoursSwitch = findViewById(R.id.openHoursSwitch);
        hoursSwitch.setOnClickListener(this);
        openTime = findViewById(R.id.firstSpinnerHours);
        closureTime = findViewById(R.id.secondSpinnerHours);
        openTime.setEnabled(false);
        closureTime.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        imageCount = 0;
        previewToast = false;
        exitToast = false;
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
        //get the ref to the preview
        imagePreview = findViewById(R.id.imagePreview);
        imagePreview.setOnClickListener(this);
        mainLayout = findViewById(R.id.constraintLayout);
        imageUriArray = new ArrayList<>();
        imageBytesArray = new ArrayList<>();

        location = new HashMap<>();
        review = new HashMap<>();

        //setting the events
        findViewById(R.id.addButton).setOnClickListener(this);
        imageButton.setOnClickListener(this);
        findViewById(R.id.posizioneButton).setOnClickListener(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(SportFinderConstants.SPORTS_PATH)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String sport = (String) document.get("sport");
                            CheckBox cb = new CheckBox(getApplicationContext());
                            cb.setText(sport);
                            sportsLinerarLayout.addView(cb);
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "No sports available", task.getException());
                    }
                });


        //places for the addresses
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        //PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setHint(this.getString(R.string.add_location_address_hint));
            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    String name = place.getName(); // Via di Madonna Bianca
                    //String id = place.getId();  //Ei1WaWEgZGkgTWFkb25uYSBCaWFuY2EsIDM4MTIzIFRyZW50byBUTiwgSXRhbHkiLiosChQKEgmb7kVVFHSCRxFz8S_y1posrBIUChIJ253Oycp2gkcRR_WfE5mixic
                    String address = place.getAddress(); //38123 Trento TN, Italy
                    LatLng latLng = place.getLatLng();  //lat/lng: (46.0373486,11.1303665)
                    double lat = 0;
                    double lng = 0;
                    if (latLng != null) {
                        lat = latLng.latitude;
                        lng = latLng.longitude;
                    }

                    //build the location object
                    if (address != null && !address.contains(name)) {
                        location.put("address", name + ", " + address);
                    } else {
                        location.put("address", address);
                    }

                    location.put("lat", lat);
                    location.put("lng", lng);
                    ((TextView) findViewById(R.id.posizioneTV)).setText("La posizione scelta è: " + address);
                }


                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, getString(R.string.err_log) + status);
                    //Toast.makeText(getApplicationContext(), "Errore: 10001", Toast.LENGTH_SHORT).show();
                }
            });
        }

        verifyStoragePermission(this);
    }

    @Override
    public void onBackPressed() {
        if (imagePreviewLayout.getVisibility() == View.VISIBLE) {
            switchVisibility();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.indietro)
                    .setMessage(R.string.dati_andranno_persi)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                            AddLocationActivity.super.onBackPressed())
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        //catch the right view clicked
        switch (v.getId()) {
            case R.id.addButton:
                aggiungiLocation();
                break;
            case R.id.imageButton:
                pickImage();
                break;
            case R.id.imagePreview:
                switchVisibility();
                break;
            case R.id.removeButton:
                removeImage();
                break;
            case R.id.posizioneButton:
                fetchPosition();
                break;
            case R.id.openHoursSwitch:
                openingHours();
                break;
            default:
                break;
        }
    }

    /**
     * if at the start of the activity the user is not signed in, bring them to the AuthActivity
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(AddLocationActivity.this, AuthActivity.class);
            intent.putExtra("EXTRA_ACTION", "addlocation");
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * prompt the user to pick an image
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getText(R.string.pick_img)), GET_IMAGE);
    }

    /**
     * get URIs of images selected by the user
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE && resultCode == RESULT_OK) {
            //take the uri of the image
            imageUri = data.getData();
            imageUriArray.add(imageUri);
            preview(imageCount);
            compressAndSetImage(imageUri);
            imageCount++;
            //disable the button after 5 images
            if (imageCount == MAX_IMAGE) {
                imageButton.setEnabled(false);
                Toast.makeText(getApplicationContext(), R.string.img_number, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void compressAndSetImage(Uri imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTempStorage = new byte[SportFinderConstants.TEMP_STORAGE];
        options.inJustDecodeBounds = false;
        options.inSampleSize = SportFinderConstants.SAMPLE_SIZE;
        Bitmap image = null;
        try {
            InputStream input = getContentResolver().openInputStream(imageUri);
            image = BitmapFactory.decodeStream(input, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmapImage = ThumbnailUtils.extractThumbnail(image, imageView.getWidth() - BORDER, imageView.getHeight() - BORDER);
        imageView.setImageBitmap(bitmapImage);
        if (image != null) {
            ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, streamOut);
            byte[] byteArray = streamOut.toByteArray();
            imageBytesArray.add(byteArray);
            image.recycle();
        }
    }

    public void setImageView(int i) {
        switch (i) {
            //to select the correct ImageView
            //also to reset the ImageView for removed image
            case 0:
                imageView = findViewById(R.id.imageView1);
                break;
            case 1:
                imageView = findViewById(R.id.imageView2);
                break;
            case 2:
                imageView = findViewById(R.id.imageView3);
                break;
            case 3:
                imageView = findViewById(R.id.imageView4);
                break;
            case 4:
                imageView = findViewById(R.id.imageView5);
                break;
            default:
                break;
        }
    }

    private void preview(int i) {
        setImageView(i);
        //set visible the correct ImageView
        imageView.setVisibility(View.VISIBLE);

        //toast only the first time
        if (!previewToast) {
            Toast.makeText(getApplicationContext(), R.string.img_preview, Toast.LENGTH_SHORT).show();
            previewToast = true;
        }
        //click on the icon
        imageView.setOnClickListener(v -> {
            //set the variable image click with the right index
            imageClick = i;

            //the main layout become invisible
            mainLayout = findViewById(R.id.constraintLayout);
            mainLayout.setVisibility(View.INVISIBLE);

            //set the preview with the URI of the image
            imagePreview.setImageURI(imageUriArray.get(i));
            //set it visible
            imagePreviewLayout.setVisibility(View.VISIBLE);
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setBackgroundColor(Color.BLACK);

            //click on the image to go back
            //only the first time
            if (!exitToast) {
                Toast.makeText(getApplicationContext(), R.string.img_close_preview, Toast.LENGTH_SHORT).show();
                exitToast = true;
            }
        });
    }

    public void removeImage() {
        //remove the URI and the bytes of the image clicked from the array
        imageUriArray.remove(imageClick);
        imageBytesArray.remove(imageClick);

        //make invisible the last imageVIew
        imageView.setVisibility(View.GONE);
        imageCount--;

        //reset the imageView
        for (int i = 0; i < imageCount; i++) {
            setImageView(i);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(
                    imageBytesArray.get(i), 0, imageBytesArray.get(i).length));
        }

        imageButton.setEnabled(true);
        //back to the main layout
        switchVisibility();

        Toast.makeText(getApplicationContext(), R.string.img_removed, Toast.LENGTH_SHORT).show();
    }

    public void switchVisibility() {
        //switch visibility
        imagePreview.setVisibility(View.GONE);
        imagePreviewLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    private void setTimeErrors() {
        openTime.requestFocus();
        closureTime.requestFocus();
        Toast.makeText(getApplicationContext(), getString(R.string.invalid_hours), Toast.LENGTH_SHORT).show();
    }

    private void setTimes() {
        int open = openTime.getMinute();
        if (open < 10) {
            String strOpen = formatMinutes(openTime.getMinute());
            location.put("opening-time", openTime.getHour() + ":" + strOpen);
        } else {
            location.put("opening-time", openTime.getHour() + ":" + open);
        }
        int close = closureTime.getMinute();
        if (close < 10) {
            String strClose = formatMinutes(closureTime.getMinute());
            location.put("closure-time", closureTime.getHour() + ":" + strClose);
        } else {
            location.put("closure-time", closureTime.getHour() + ":" + close);
        }
    }

    private String formatMinutes(int minutes) {
        if (minutes == 0) {
            return "00";
        } else {
            return "0".concat(String.valueOf(minutes));
        }
    }

    void aggiungiLocation() {
        String descrizione = null;
        if (descriptionEditText.getText() != null) {
            descrizione = descriptionEditText.getText().toString();
            //description check
            if (descrizione.equals("")) {
                descriptionEditText.setError(getString(R.string.no_descr));
                descriptionEditText.requestFocus();
                return;
            }
        }

        ArrayList<String> sports = new ArrayList<>();
        //get which sports the user selected
        for (int i = 0; i < sportsLinerarLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) sportsLinerarLayout.getChildAt(i);
            if (cb.isChecked()) {
                sports.add(cb.getText().toString());
            }
        }

        if (sports.size() == 0) {
            ((CheckBox) sportsLinerarLayout.getChildAt(0)).setError(getString(R.string.no_sports));
            sportsLinerarLayout.requestFocus();
            return;
        } else {
            //put the sports in the object
            location.put("sports", sports);
        }

        //images checks
        if (imageUriArray == null || imageUriArray.size() == 0) {
            //display error
            Toast.makeText(this, R.string.no_image, Toast.LENGTH_SHORT).show();
            imageButton.requestFocus();
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            if (userEmail != null) {
                //put data in the location
                location.put("author", userEmail);
                review.put("user", userEmail);
                if (descrizione != null) {
                    review.put("description", descrizione.trim());
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        String timestamp = calendar.get(Calendar.DATE) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
        review.put("timestamp", timestamp);
        location.put("timestamp", timestamp);


        review.put("upvotes", new ArrayList<String>());
//        review.put("downvotes", new ArrayList<String>());

        review.put("timestamp", timestamp);
        review.put("votes", 0);


        if (hoursSwitch.isChecked()) {
            if (openTime.getHour() > closureTime.getHour()) {
                setTimeErrors();
            } else if (openTime.getHour() == closureTime.getHour()) {
                if (openTime.getMinute() > closureTime.getMinute()) {
                    setTimeErrors();
                } else {
                    setTimes();
                }
            } else {
                setTimes();
            }

        }

        /*
        proceed with the upload
         */
        //firebase references
        FirebaseFirestore dbUpload = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // refer to the collection "locations/"
        DocumentReference addedDocRef = dbUpload.collection("locations").document();
        // get the auto generated id
        String id = addedDocRef.getId();
        location.put("id", id);
        location.put("image_count", imageUriArray.size());

        location.put("pending-suggestion", 0);
        //upload
        addedDocRef.set(location);
        review.put("locationID", id);
        review.put("totalVotes", 0);
        dbUpload.collection("reviews")
                .add(review);

        /*
        upload the images
         */
        StorageReference imageRef;

        //for each image
        //compress, set the folder and upload
        for (int i = 0; i < imageBytesArray.size(); i++) {

            imageRef = storageRef.child("images/" + id + "/" + id + "-" + i);

            //uploading the image
            UploadTask uploadTask = imageRef.putBytes(imageBytesArray.get(i));
            uploadTask
                    .addOnFailureListener(exception -> Toast.makeText(getApplicationContext(), R.string.err_images, Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "Location inserita correttamente", Toast.LENGTH_SHORT).show());
        }
        progressBar.setVisibility(View.GONE);
        sendToMap();
    }

    /**
     * send to map
     */
    public void sendToMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void fetchPosition() {
        getLastLocation();
        Log.d(TAG, "fetchPosition: why it crashing tho");
    }

    /**
     * check if permissions are given,
     * check if location is enabled,
     * get last location from FusedLocationClient object
     */
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last location from FusedLocationClient object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        Geocoder gc = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            Address address = addressList.get(0);
                            if (address != null) {
                                double lat = address.getLatitude();
                                double lng = address.getLongitude();
                                this.location.put("lat", lat);
                                this.location.put("lng", lng);
                            } else {
                                Log.d(TAG, "Address is null");
                            }
                            String txt = getString(R.string.chosen_position) + " " + address.getAddressLine(0);
                            ((TextView) findViewById(R.id.posizioneTV)).setText(txt);
                            this.location.put("address", address.getAddressLine(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Toast.makeText(this, R.string.turn_on_loc, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available, request for permissions
            requestPermissions();
        }
    }

    private void openingHours() {
        if (hoursSwitch.isChecked()) {
            openTime.setEnabled(true);
            closureTime.setEnabled(true);
            openTime.setVisibility(View.VISIBLE);
            closureTime.setVisibility(View.VISIBLE);
        } else {
            openTime.setEnabled(false);
            closureTime.setEnabled(false);
            openTime.setVisibility(View.GONE);
            closureTime.setVisibility(View.GONE);
        }
    }

    /**
     * request new location data
     */
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    /**
     * method to check for permissions
     *
     * @return true if permissions are granted, else false
     */
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * method to request for permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /**
     * method to check if location is enabled
     *
     * @return true if gps is enabled
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * when permissions are granted, try to access user's location again
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}



