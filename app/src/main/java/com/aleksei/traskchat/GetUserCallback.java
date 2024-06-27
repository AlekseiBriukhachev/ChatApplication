package com.aleksei.traskchat;

public interface GetUserCallback {
    void onSuccess(ChatUser user);
    void onFailure(String error);
}
