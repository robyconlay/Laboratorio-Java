package it.studenti.unitn.mazzalai_leoni.sportfinder.authentication;


import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.studenti.unitn.mazzalai_leoni.sportfinder.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LoginFragment";

    private EditText emailText, passwordText;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailText = getView().findViewById(R.id.emailField);
        passwordText = getView().findViewById(R.id.passwordField);

        getView().findViewById(R.id.loginButton).setOnClickListener(this);
        getView().findViewById(R.id.loginWithGoogleButton).setOnClickListener(this);
        getView().findViewById(R.id.registratiLink).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                onUserLogin();
                break;
            case R.id.loginWithGoogleButton:
                ((AuthActivity) getActivity()).onAuthWithGoogle();
                break;
            case R.id.registratiLink:
                ((AuthActivity) getActivity()).changeAuthFragment();
            default:
                break;
        }
    }

    /**
     * handle basic user login (email and password basic checks)
     */
    public void onUserLogin() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (email.isEmpty()) {
            emailText.setError(getString(R.string.needed_field));
            emailText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_mail));
            emailText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordText.setError(getString(R.string.needed_field));
            passwordText.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordText.setError(getString(R.string.psw_length));
            passwordText.requestFocus();
            return;
        }

        ((AuthActivity) getActivity()).onUserLogin(email, password);
    }

}