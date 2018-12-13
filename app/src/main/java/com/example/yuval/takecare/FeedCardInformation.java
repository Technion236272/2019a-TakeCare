package com.example.yuval.takecare;

public class FeedCardInformation {
    private String title;
    private String photo;
    private String userPictureURL;
    private String publisher;
    private int itemCategoryId;

    private int itemPickupMethodId;

    /**
     * Empty constructor for the FirestoreRecyclerAdapter
     */
    public FeedCardInformation(){

    }

    public FeedCardInformation(String title, String photoURL, String userPictureURL, String publisher,
                               int itemCategoryId, int itemPickupMethodId) {
        this.title = title;
        this.photo = photo;
        this.userPictureURL = userPictureURL;
        this.publisher = publisher;
        this.itemCategoryId = itemCategoryId;
        this.itemPickupMethodId = itemPickupMethodId;
    }

    public String getTitle() {
        return title;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUserPictureURL() {
        return userPictureURL;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getItemCategoryId() {
        return itemCategoryId;
    }

    public int getItemPickupMethodId() {
        return itemPickupMethodId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPhotoURL(String photo) {
        this.photo = photo;
    }

    public void setUserPictureURL(String userPictureURL) {
        this.userPictureURL = userPictureURL;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setItemCategoryId(int itemCategoryId) {
        this.itemCategoryId = itemCategoryId;
    }

    public void setItemPickupMethodId(int itemPickupMethodId) {
        this.itemPickupMethodId = itemPickupMethodId;
    }
}
