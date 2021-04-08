package com.example.loginwithfirebase.data;

import com.example.loginwithfirebase.data.model.LoggedInUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private final LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
            FirebaseUser firebaseUser = dataSource.getUser();
            if (firebaseUser != null) {
                instance.setLoggedInUser(new LoggedInUser(firebaseUser));
            }
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    public LoggedInUser getLoggedInUser() {
        return user;
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public void register(String username, String password, OnLoginListener<LoggedInUser> listener) {
        // handle register
        Task<AuthResult> authResultTask = dataSource.register(username, password);
        authResultTask.addOnCompleteListener((resultTask) -> authResultCallback(resultTask, listener));
    }

    public void login(String username, String password, OnLoginListener<LoggedInUser> listener) {
        // handle login
        Task<AuthResult> authResultTask = dataSource.login(username, password);
        authResultTask.addOnCompleteListener((resultTask) -> authResultCallback(resultTask, listener));
    }

    private void authResultCallback(Task<AuthResult> authResult, OnLoginListener<LoggedInUser> listener) {
        try {
            if (authResult.isSuccessful()) {
                FirebaseUser user = Objects.requireNonNull(authResult.getResult()).getUser();
                LoggedInUser loggedInUser = new LoggedInUser(user);
                setLoggedInUser(loggedInUser);
                listener.OnComplete(new Result.Success<LoggedInUser>(loggedInUser));
            } else {
                listener.OnComplete(new Result.Error(authResult.getException()));
            }
        } catch (NullPointerException e) {
            listener.OnComplete(new Result.Error(authResult.getException()));
        }
    }
}
