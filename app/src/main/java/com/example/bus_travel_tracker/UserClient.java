package com.example.bus_travel_tracker;

import android.app.Application;

import com.google.firebase.firestore.auth.User;

public class UserClient extends Application {

    public User u = null;

    public User getUser() {
        return u;
    }

    public void setUser(User user) {
        this.u=u;
    }

}