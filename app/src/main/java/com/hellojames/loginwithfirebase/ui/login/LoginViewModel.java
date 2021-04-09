package com.hellojames.loginwithfirebase.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hellojames.loginwithfirebase.R;
import com.hellojames.loginwithfirebase.data.LoginRepository;
import com.hellojames.loginwithfirebase.data.Result;
import com.hellojames.loginwithfirebase.data.model.LoggedInUser;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<LoggedInUser> loggedInUser = new MutableLiveData<>();
    private final LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        loggedInUser.setValue(loginRepository.getLoggedInUser());
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        loginRepository.login(username, password, this::resultCallback);
    }

    public void register(String username, String password) {
        loginRepository.register(username, password, this::resultCallback);
    }

    private void resultCallback(Result<LoggedInUser> result) {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loggedInUser.postValue(data);
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        } else {
            if (((Result.Error) result).getError() instanceof FirebaseAuthEmailException) {
                loginFormState.setValue(new LoginFormState(R.string.invalid_email, null));
            } else if (((Result.Error) result).getError() instanceof FirebaseAuthInvalidCredentialsException) {
                loginFormState.setValue(new LoginFormState(null, R.string.incorrect_password));
            } else if (((Result.Error) result).getError() instanceof FirebaseAuthWeakPasswordException) {
                loginFormState.setValue(new LoginFormState(null, R.string.weak_password));
            } else if (((Result.Error) result).getError() instanceof FirebaseAuthUserCollisionException) {
                loginFormState.setValue(new LoginFormState(R.string.collision_email, null));
            } else if (((Result.Error) result).getError() instanceof FirebaseAuthInvalidUserException) {
                loginFormState.setValue(new LoginFormState(R.string.inexist_email, null));
            } else {
                loginFormState.setValue(new LoginFormState(false));
            }
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void logout() {
        loginRepository.logout();
    }

    public boolean isUserLoggedIn() {
        return loginRepository.isLoggedIn();
    }

    public MutableLiveData<LoggedInUser> getLoggedInUser() {
        return loggedInUser;
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
