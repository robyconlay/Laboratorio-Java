package it.studenti.unitn.mazzalai_leoni.sportfinder.authentication;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;

public class SignupFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SignupFragment";

    private EditText emailText, passwordText;
    private CheckBox TOS;

    public SignupFragment() {
        super(R.layout.fragment_signup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailText = getView().findViewById(R.id.emailField);
        passwordText = getView().findViewById(R.id.passwordField);

        TOS = getView().findViewById(R.id.TOS);

        getView().findViewById(R.id.signupButton).setOnClickListener(this);
        getView().findViewById(R.id.signupWithGoogleButton).setOnClickListener(this);
        getView().findViewById(R.id.accediLink).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signupButton:
                onUserSignup();
                break;
            case R.id.signupWithGoogleButton:
                ((AuthActivity) getActivity()).onAuthWithGoogle();
                break;
            case R.id.accediLink:
                ((AuthActivity) getActivity()).changeAuthFragment();
                break;
            default:
                break;
        }
    }

    /**
     * handle basic user signup (email and password)
     */
    public void onUserSignup() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        //basic checks for email and password
        if (email.isEmpty()) {
            emailText.setError("Campo obbligatorio");
            emailText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordText.setError(getString(R.string.needed_field));
            passwordText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_mail));
            emailText.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordText.setError(getString(R.string.psw_length));
            passwordText.requestFocus();
            return;
        }
        if (!TOS.isChecked()) {
            TOS.setError(getString(R.string.terms_of_service));
            TOS.requestFocus();
            return;
        }

        ((AuthActivity) getActivity()).onUserSignup(email, password);
    }

    /**
     * handles firebase exceptions
     *
     * @param exception Firebase exception
     */
    public void showError(Exception exception) {
        try {
            throw exception;
            //handle Firebase Auth exceptions (when task fails)
        } catch (FirebaseAuthWeakPasswordException e) {
            passwordText.setError(getString(R.string.weak_psw));
            passwordText.requestFocus();
            return;
        } catch (FirebaseAuthInvalidCredentialsException e) {
            emailText.setError(getString(R.string.invalid_mail));
            emailText.requestFocus();
            return;
        } catch (FirebaseAuthUserCollisionException e) {
            emailText.setError(getString(R.string.used_mail));
            emailText.requestFocus();
            return;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
