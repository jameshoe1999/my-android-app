package com.hellojames.loginwithfirebase.data;

public interface OnLoginListener<T> {
    void OnComplete(Result<T> result);
}
