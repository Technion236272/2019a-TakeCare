package com.example.yuval.takecare;

public class RequesterCardInformation {
    private String userID;
    private String timestamp;
    private String photoURL;
    private float rating;

    /**
     * Empty constructor for the FirestoreRecyclerAdapter
     */
    public RequesterCardInformation(){

    }
    public RequesterCardInformation(String userName, String timestamp, String photoID, float rating) {
        this.userID = userName;
        this.timestamp = timestamp;
        this.photoURL = photoID;
        this.rating = rating;
    }


    public String getUserID() {
        return userID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public float getRating() {
        return rating;
    }
}
