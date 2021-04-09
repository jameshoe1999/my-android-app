package com.hellojames.loginwithfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.hellojames.loginwithfirebase.data.model.LoggedInUser;
import com.hellojames.loginwithfirebase.ui.login.LoginActivity;
import com.hellojames.loginwithfirebase.ui.login.LoginViewModel;
import com.hellojames.loginwithfirebase.ui.login.LoginViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {

    private TextView displayUserName;
    private LoginViewModel loginViewModel;
    private MutableLiveData<LoggedInUser> currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        displayUserName = findViewById(R.id.Greeting);

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);
        currentUser = loginViewModel.getLoggedInUser();
        LoggedInUser user = currentUser.getValue();
        if (user != null) {
            if (user.getDisplayName() == null) {
                editProfileNameDialog();
            }
            updateDisplayUserName(user.getDisplayName());
        }

        currentUser.observe(this, loggedInUser -> {
            updateDisplayUserName(loggedInUser.getDisplayName());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_signout) {
            loginViewModel.logout();
            Intent exitMain = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(exitMain);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_edit_name) {
            editProfileNameDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editProfileNameDialog() {
        View editNameInputView = View.inflate(this, R.layout.layout_edit_name, null);
        EditText editNameText = editNameInputView.findViewById(R.id.editProfileNameField);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_username)
                .setView(editNameInputView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String newName = editNameText.getText().toString();
                    if (!newName.isEmpty()) {
                        updateProfileDisplayName(newName, dialog);
                    }
                })
                .setNegativeButton(R.string.cancel, ((dialog, which) -> {
                    dialog.dismiss();
                }));
        dialogBuilder.show();
    }

    private void updateProfileDisplayName(String newName, DialogInterface dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            user.updateProfile(changeRequest)
                    .addOnCompleteListener((result) -> {
                        if (result.isSuccessful()) {
                            dialog.dismiss();
                            currentUser.setValue(new LoggedInUser(user));
                            Toast.makeText(this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = result.getException().getMessage();
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateDisplayUserName(String name) {
        String greeting = String.format("Welcome, %s!", name);
        displayUserName.setText(greeting);
    }
}
