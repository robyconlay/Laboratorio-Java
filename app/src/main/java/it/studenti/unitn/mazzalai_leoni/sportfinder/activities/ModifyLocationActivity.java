package it.studenti.unitn.mazzalai_leoni.sportfinder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderCommons;
import it.studenti.unitn.mazzalai_leoni.sportfinder.utils.SportFinderConstants;

public class ModifyLocationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MOD_LOC";
    private static final int MAX_IMAGE = 1;
    private static final int BORDER = 5;
    private static final int GET_IMAGE = 3;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private FirebaseAuth mAuth;
    private String user;
    private SportFinderCommons commons = new SportFinderCommons();
    private SwitchCompat hoursSwitch;
    private TimePicker openTime;
    private TimePicker closureTime;
    private ImageButton imageButton;
    private ImageView imageView;
    private View mainLayout; //contain all the elements
    private ImageView imagePreview; //to see the image selected
    private View imagePreviewLayout;
    private Button removeButton;
    private Button modifyButton;
    private TextInputEditText editText;
    private int imageCount;
    private Uri imageUri;
    private boolean previewToast;
    private boolean exitToast;
    private byte[] imageByte;
    private String id;
    private int pendingSuggestions;
    private String author;
    private boolean isAuthor;
    private int originalImageNumber;
    private Map<String, Object> hints;

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
        setContentView(R.layout.activity_modify_location);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        id = null;

        if (extras != null) {
            id = extras.getString("EXTRA_LOCATION_ID");
            pendingSuggestions = extras.getInt("EXTRA_PENDING_SUGGESTION");
            author = extras.getString("AUTHOR");
            originalImageNumber = extras.getInt("IMAGE");
        } else {
            Toast.makeText(this, getString(R.string.err_generic), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            sendToLocation(id);
        } else {
            user = mAuth.getCurrentUser().getEmail();
        }

        hoursSwitch = findViewById(R.id.openHoursSwitch);
        imageButton = findViewById(R.id.imageButton);
        imageView = findViewById(R.id.imageView);
        openTime = findViewById(R.id.firstSpinnerHours);
        closureTime = findViewById(R.id.secondSpinnerHours);
        mainLayout = findViewById(R.id.constraintLayout);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
        imagePreview = findViewById(R.id.imagePreview);
        removeButton = findViewById(R.id.removeButton);
        modifyButton = findViewById(R.id.modifyButton);
        editText = findViewById(R.id.modifyLocationEdit);

        imagePreview.setOnClickListener(this);
        hoursSwitch.setOnClickListener(this);
        imageButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);
        modifyButton.setOnClickListener(this);

        imageCount = 0;
        previewToast = false;
        exitToast = false;

        hints = new HashMap<>();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openHoursSwitch:
                openingHours();
                break;
            case R.id.imageButton:
                verifyStoragePermission(this);
                pickImage();
                break;
            case R.id.removeButton:
                removeImage();
                break;
            case R.id.modifyButton:
                proposeChanges();
                break;
            case R.id.imagePreview:
                switchVisibility();
                break;
            default:
                break;
        }
    }

    public void proposeChanges() {
        isAuthor = author.equals(mAuth.getCurrentUser().getEmail());
        if (imageCount == 0 && !hoursSwitch.isChecked() && editText.getText().toString().equals("")) {
            editText.setError(getString(R.string.mandatory_field));
            editText.requestFocus();
            return;
        }
        hints.put("locationId", id);
        hints.put("author", user);
        if (hoursSwitch.isChecked()) {
            int open = openTime.getMinute();
            if (open < 10) {
                String strOpen = formatMinutes(openTime.getMinute());
                hints.put("opening-time", openTime.getHour() + ":" + strOpen);
            } else {
                hints.put("opening-time", openTime.getHour() + ":" + open);
            }
            int close = closureTime.getMinute();
            if (close < 10) {
                String strClose = formatMinutes(closureTime.getMinute());
                hints.put("closure-time", closureTime.getHour() + ":" + strClose);
            } else {
                hints.put("closure-time", closureTime.getHour() + ":" + close);
            }
        }
        if (editText.getText() != null) {
            String msg = editText.getText().toString();
            hints.put("message", msg);
        }
        if (imageCount == 1) {
            String ref = id + "-" + pendingSuggestions;
            hints.put("image", ref);
        }

        if (hints.containsKey("image") || (hints.containsKey("message")) ||
                (hints.containsKey("opening-time") && hints.containsKey("closure-time"))) {
            doUpdates();
        }
    }

    private String formatMinutes(int minutes) {
        if (minutes == 0) {
            return "00";
        } else {
            return "0".concat(String.valueOf(minutes));
        }
    }

    private void doUpdates() {
        if (!hints.isEmpty()) {
            if (isAuthor) {
                publishChanges();
            } else {
                uploadChanges();
                if (hints.containsKey("image")) {
                    uploadSuggestedImage();
                } else {
                    sendToLocation(id);
                }
            }
        }
    }

    private void uploadChanges() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(SportFinderConstants.SUGGESTED_CHANGES_PATH)
                .document(id + "-" + pendingSuggestions);
        ref.set(hints);
        int pend = pendingSuggestions + 1;
        db.collection(SportFinderConstants.LOCATION_PATH).document(id)
                .update("pending-suggestion", pend);
    }

    private void publishChanges() {
        if (hints.containsKey("opening-time") && hints.containsKey("closure-time")) {
            String openingTime = hints.get("opening-time").toString();
            String closureTime = hints.get("closure-time").toString();
            commons.updateOpeningTimes(id, openingTime, closureTime);
        }
        if (hints.containsKey("image")) {
            publishImage(id, imageByte, originalImageNumber);
        } else {
            sendToLocation(id);
        }
    }

    private void uploadSuggestedImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef
                .child(SportFinderConstants.SUGGESTED_IMAGES_PATH + "/" + id + "/" + id + "-" + pendingSuggestions);
        UploadTask uploadTask = imageRef.putBytes(imageByte);
        uploadTask
                .addOnFailureListener(exception ->
                        Toast.makeText(getApplicationContext(), R.string.err_images, Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getApplicationContext(), getString(R.string.success_img), Toast.LENGTH_SHORT).show();
                    sendToLocation(id);
                });
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
            int imageCount = originalImageNumber + 1;
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(SportFinderConstants.LOCATION_PATH).document(id)
                    .update("image_count", imageCount)
                    .addOnCompleteListener(locTask -> sendToLocation(id));
        });
    }

    /**
     * prompt the user to pick an image
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
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
            preview();
            compressAndSetImage(imageUri);

            imageCount++;
            //disable the button after 1 image
            if (imageCount == MAX_IMAGE) {
                imageButton.setEnabled(false);
                imageButton.setVisibility(View.INVISIBLE);
            }
        }
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
            imageByte = streamOut.toByteArray();
            image.recycle();
        }
    }

    private void preview() {
        //set visible the correct ImageView
        imageView.setVisibility(View.VISIBLE);

        //toast only the first time
        if (!previewToast) {
            Toast.makeText(getApplicationContext(), R.string.img_preview, Toast.LENGTH_SHORT).show();
            previewToast = true;
        }
        //click on the icon
        imageView.setOnClickListener(v -> {

            //the main layout become invisibl
            mainLayout = findViewById(R.id.constraintLayout);
            mainLayout.setVisibility(View.INVISIBLE);

            //set the preview with the URI of the image
            imagePreview.setImageURI(this.imageUri);
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
        this.imageUri = null;
        this.imageByte = null;

        //make invisible the last imageVIew
        imageView.setVisibility(View.GONE);
        imageCount--;

        //back to the main layout
        switchVisibility();

        imageButton.setVisibility(View.VISIBLE);
        imageButton.setEnabled(true);

        Toast.makeText(getApplicationContext(), R.string.img_removed, Toast.LENGTH_SHORT).show();
    }

    public void switchVisibility() {
        //switch visibility
        imagePreview.setVisibility(View.GONE);
        imagePreviewLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
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

    public void sendToLocation(String id) {
        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra("EXTRA_LOCATION_ID", id);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.back_location)
                .setMessage(R.string.dati_andranno_persi)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                        ModifyLocationActivity.super.onBackPressed())
                .setNegativeButton(android.R.string.no, null).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
