package it.studenti.unitn.mazzalai_leoni.sportfinder.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import it.studenti.unitn.mazzalai_leoni.sportfinder.activities.AddLocationActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.activities.AddSportActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.activities.LandingActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.activities.MapsActivity;
import it.studenti.unitn.mazzalai_leoni.sportfinder.R;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ProgressBar progressBar;

    /**
     * initialize layout, GoogleSignIn and Firebase auth
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, LoginFragment.class, null, "LOGIN_FRAGMENT")
                    .commit();
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * if at the start of the activity the user is signed in, bring them to the mapActivity or the addlocationActivity
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String action = extras.getString("EXTRA_ACTION");
                if (action != null) {
                    switch (action) {
                        case "addlocation":
                            intent = new Intent(this, AddLocationActivity.class);
                            break;
                        case "addsport":
                            intent = new Intent(this, AddSportActivity.class);
                            break;
                        default:
                            break;
                    }
                }
            }
            startActivity(intent);
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
//                NavUtils.navigateUpFromSameTask(this);
                startActivity(new Intent(this, LandingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * finds which Fragment is currently displayed, removes it and adds the "complementary" one
     */
    public void changeAuthFragment() {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("LOGIN_FRAGMENT");
        if (loginFragment != null && loginFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(loginFragment)
                    .add(R.id.fragment_container_view, SignupFragment.class, null, "SIGNUP_FRAGMENT")
                    .commit();
            return;
        }

        SignupFragment signupFragment = (SignupFragment) getSupportFragmentManager().findFragmentByTag("SIGNUP_FRAGMENT");
        if (signupFragment != null && signupFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(signupFragment)
                    .add(R.id.fragment_container_view, LoginFragment.class, null, "LOGIN_FRAGMENT")
                    .commit();
            return;
        }
    }

    /**
     * handle firebase account creation using email and password
     * on success start new intent, on failure show a Toast to the user
     *
     * @param email    user's email
     * @param password user's password
     * @throws Exception firebase exception
     */
    public void onUserSignup(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        //create user in the firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(AuthActivity.this, LandingActivity.class);
                Bundle extras = AuthActivity.this.getIntent().getExtras();
                if (extras != null) {
                    String action = extras.getString("EXTRA_ACTION");
                    if (action != null && action == "addlocation") {
                        intent = new Intent(AuthActivity.this, AddLocationActivity.class);
                    }
                    if (action != null && action == "addsport") {
                        intent = new Intent(AuthActivity.this, AddSportActivity.class);
                    }
                }
                AuthActivity.this.startActivity(intent);
            } else {
                Toast.makeText(AuthActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();

                SignupFragment signupFragment = (SignupFragment) getSupportFragmentManager().findFragmentByTag("SIGNUP_FRAGMENT");
                signupFragment.showError(task.getException());
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    /**
     * handle firebase account login using email and password
     * on success start new intent, on failure show a Toast to the user
     *
     * @param email    user's email
     * @param password user's password
     */
    public void onUserLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, LandingActivity.class);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String action = extras.getString("EXTRA_ACTION");
                    if (action != null && action == "addlocation") {
                        intent = new Intent(this, AddLocationActivity.class);
                    }
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
            }
        });
        progressBar.setVisibility(View.GONE);
    }

    /**
     * start new Google SignInIntent so that the user can authenticate into google
     */
    public void onAuthWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * method called on return of signInIntent
     * on failure show a Toast to the user
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * sign in to firebase authentication using Google credentials
     * on success start new intent, on failure show a Toast to the user
     *
     * @param idToken account token for getting credentials from google
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        Intent intent = new Intent(this, LandingActivity.class);
                        Bundle extras = getIntent().getExtras();
                        if (extras != null) {
                            String action = extras.getString("EXTRA_ACTION");
                            if (action != null && action == "addlocation") {
                                intent = new Intent(this, AddLocationActivity.class);
                            }
                        }
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
        progressBar.setVisibility(View.INVISIBLE);
    }
}

