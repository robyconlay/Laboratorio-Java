package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.authentication.AuthActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "MAPS";
    private static final int PERMISSION_ID = 44;
    private String selectedSport;
    private Spinner sportSelect;
    private FirebaseAuth mAuth;
    private GoogleMap googleMap;
    private final LocationCallback mLocationCallback = new LocationCallback() {

        /**
         * on result center map camera and save the user's country in storage
         */
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            centerPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 12);

            //save the user country
            Geocoder gc = new Geocoder(getApplicationContext());
            try {
                List<Address> addressList = gc.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                Address address = addressList.get(0);
                String user_country = address.getCountryName();
                SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.saved_user_country), user_country);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);


        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        //PlacesClient placesClient = Places.createClient(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //setting listeners on the elements
        findViewById(R.id.addLocationButton).setOnClickListener(this);
        findViewById(R.id.localizeButton).setOnClickListener(this);

        //bind the filter
        sportSelect = findViewById(R.id.sportSelect);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                String name = place.getName(); // Via di Madonna Bianca
                //String id = place.getId();  //Ei1WaWEgZGkgTWFkb25uYSBCaWFuY2EsIDM4MTIzIFRyZW50byBUTiwgSXRhbHkiLiosChQKEgmb7kVVFHSCRxFz8S_y1posrBIUChIJ253Oycp2gkcRR_WfE5mixic
                String address = place.getAddress(); //Via di Madonna Bianca, 38123 Trento TN, Italy
                if (!address.contains(name)) {
                    Log.d(TAG, name + ", " + address);
                } else {
                    Log.d(TAG, address);
                }
                LatLng latLng = place.getLatLng();  //lat/lng: (46.0373486,11.1303665)
                centerPosition(latLng, -1);
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Posizione cercata")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            }
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), getString(R.string.suggestion_bar), Toast.LENGTH_SHORT).show();
            }
        });

        //method to get the sports
        getSportsConfirmed();
    }


    @Override
    public void onClick(View v) {
        //check which view was clicked
        switch (v.getId()) {
            case R.id.localizeButton:
                getLastLocation();
                break;
            case R.id.addLocationButton:
                openAddLocationActivity();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LandingActivity.class));
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * <p>
     * try to center map on user's country, instead of randomly, by getting the country's name if it was saved in storage
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        //set the default country
        String defaultCountry = getString(R.string.default_user_country);
        String user_country = sharedPreferences.getString(getString(R.string.saved_user_country), defaultCountry);

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(user_country, 1);
            Address address = addressList.get(0);
            //center on the country
            centerPosition(new LatLng(address.getLatitude(), address.getLongitude()), 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //method to receive the marker
        getMarker();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    //to get the markers
    private void getMarker() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //check the selection
        if (selectedSport == null || selectedSport.equals(SportFinderConstants.ALL_SPORTS)) {
            //getting all the locations
            db.collection("locations")
                    .get()
                    .addOnCompleteListener(task -> {
                        //call the specific method
                        getLocations(task);
                    });
        } else {
            //get the locations of the selected sport
            db.collection(SportFinderConstants.LOCATION_PATH)
                    .whereArrayContains("sports", selectedSport)
                    .get()
                    .addOnCompleteListener(task -> {
                        //call the specific method
                        getLocations(task);
                    });
        }
    }

    private void getLocations(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            //delete the markers
            googleMap.clear();

            //handle more than one result
            for (QueryDocumentSnapshot document : task.getResult()) {
                //extract data for each document
                String id = document.get("id").toString();
                String tmpLat = document.get("lat").toString();
                String tmpLng = document.get("lng").toString();
                double lat = Double.parseDouble(tmpLat);
                double lng = Double.parseDouble(tmpLng);
                LatLng marker = new LatLng(lat, lng);

                //use the listener to handle the click event
                GoogleMap.OnMarkerClickListener listener = setListener();

                //set the id as the title of the marker
                googleMap.addMarker(new MarkerOptions().position(marker).title(id));
                googleMap.setOnMarkerClickListener(listener);
            }
        } else {
            Toast.makeText(this, R.string.err_firebase, Toast.LENGTH_SHORT).show();
        }
    }

    //retrieving the listener
    private GoogleMap.OnMarkerClickListener setListener() {
        return marker -> {
            //get the id and call openLocationView
            String id = marker.getTitle();
            centerPosition(marker.getPosition(), -1);
            return openLocationView(id);
        };
    }

    /**
     * called on the user click on the plus button
     * if the user is not logged in, bring them to the LoginSignupActivity first
     */
    private void openAddLocationActivity() {
        FirebaseUser user = mAuth.getCurrentUser();

        Intent intent;
        if (user != null) {
            intent = new Intent(this, AddLocationActivity.class);
        } else {
            intent = new Intent(this, AuthActivity.class);
            intent.putExtra("EXTRA_ACTION", "addlocation");
        }
        startActivity(intent);
    }

    private void getSportsConfirmed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(SportFinderConstants.SPORTS_PATH)
                .get()
                .addOnCompleteListener(task -> {
                    insertSportsInFilter(task);
                });
    }

    private void insertSportsInFilter(Task<QuerySnapshot> task) {
        //bind the filter
        //Spinner sportSelect = findViewById(R.id.sportSelect);

        ArrayList<String> sports = new ArrayList<>();
        sports.add(SportFinderConstants.ALL_SPORTS);

        if (task.getResult() == null) {
            Toast.makeText(this, getString(R.string.err_sports), Toast.LENGTH_SHORT).show();
        } else {
            for (QueryDocumentSnapshot document : task.getResult()) {
                String result = (String) document.get("sport");
                sports.add(result);
            }
        }

        //get the values for the filter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sports);

        sportSelect.setAdapter(adapter);
        //when the selected sport change triggers the event
        sportSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //take the selected sport
                selectedSport = parent.getSelectedItem().toString();
                //call the method that change the markers
                getMarker();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                        centerPosition(new LatLng(location.getLatitude(), location.getLongitude()), 12);
//                        googleMap.addCircle(new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude())));

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

    /**
     * move camera to current user location
     */
    private void centerPosition(LatLng latLng, float zoom) {
        if (zoom != -1) {
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } else {
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public boolean openLocationView(String id) {
        if (id.equals("")) {
            Toast.makeText(this, R.string.err_open_loc, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            //call the intent and pass the id of the location as extras
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra("EXTRA_LOCATION_ID", id);
            startActivity(intent);
            return true;
        }
    }

}