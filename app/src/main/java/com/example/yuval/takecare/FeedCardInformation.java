package com.example.yuval.takecare;

public class FeedCardInformation {
    String title;
    String photoURL;
    String userPictureURL;
    String publisher;
    int itemCategoryId;
    int itemPickupMethodId;

    public FeedCardInformation(String title, String photoURL, String userPictureURL, String publisher,
                               int itemCategoryId, int itemPickupMethodId) {
        this.title = title;
        this.photoURL = photoURL;
        this.userPictureURL = userPictureURL;
        this.publisher = publisher;
        this.itemCategoryId = itemCategoryId;
        this.itemPickupMethodId = itemPickupMethodId;
    }
}
