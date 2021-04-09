package com.hellojames.loginwithfirebase.data.model;

import com.google.firebase.auth.FirebaseUser;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private final String userId;
    private final String displayName;
    private final String email;

    public LoggedInUser(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
    }

    public LoggedInUser(FirebaseUser firebaseUser) {
        this.userId = firebaseUser.getUid();
        this.displayName = firebaseUser.getDisplayName();
        this.email = firebaseUser.getEmail();
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }
}
