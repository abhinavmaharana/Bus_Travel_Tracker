package com.example.bus_travel_tracker;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

public class user implements Parcelable{

    //Initialising variable
    private String email;
    private String user_id;
    private String username;

    public user(String email, String user_id, String username, String avatar) {
        //Assigning vairiable
        this.email = email;
        this.user_id = user_id;
        this.username = username;

    }

    public user() {

    }

    protected user(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();

    }

    public static final Creator<user> CREATOR = new Creator<user>() {
        @SuppressLint("RestrictedApi")
        @Override
        public user createFromParcel(Parcel in) { return new user(in); }

        @Override
        public user[] newArray(int size) { return new user[size]; }
    };



    public static Creator<user> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
    }

}
