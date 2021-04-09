package com.hellojames.loginwithfirebase.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hellojames.loginwithfirebase.MainActivity;
import com.hellojames.loginwithfirebase.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        if (loginViewModel.isUserLoggedIn()) {

            updateUiWithUser(new LoggedInUserView(null));
        }
        setContentView(R.layout.activity_login);

        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.signUp);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginResult().observe(this, (Observer<LoginResult>) loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);
        });

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState.getPasswordError() != null) {
                password.setError(getString(loginFormState.getPasswordError()));
            } else {
                password.setError(null);
            }
            if (loginFormState.getUsernameError() != null) {
                email.setError(getString(loginFormState.getUsernameError()));
            } else {
                email.setError(null);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(email.getText().toString(),
                        password.getText().toString());
                loginButton.setEnabled(loginViewModel.getLoginFormState().getValue().isDataValid());
                registerButton.setEnabled(loginViewModel.getLoginFormState().getValue().isDataValid());
            }
        };
        email.addTextChangedListener(afterTextChangedListener);
        password.addTextChangedListener(afterTextChangedListener);
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(email.getText().toString(),
                        password.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(email.getText().toString(),
                    password.getText().toString());
        });
        registerButton.setOnClickListener((v) -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.register(email.getText().toString(),
                    password.getText().toString());
        });
    }

    private void updateUiWithUser(@Nullable LoggedInUserView view) {
        String welcome = view.getDisplayName() == null ? "Welcome!"
                : String.format("Welcome, %s!", view.getDisplayName());
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent redirectToMain = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(redirectToMain);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
