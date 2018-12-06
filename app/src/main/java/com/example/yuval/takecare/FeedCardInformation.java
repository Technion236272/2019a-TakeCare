package com.example.yuval.takecare;

public class FeedCardInformation {
    String title;
    String photoURL;
    int userProfileId;
    String publisher;
    int itemCategoryId;
    int itemPickupMethodId;

    public FeedCardInformation(String title, String photoURL, int userProfileId, String publisher,
                               int itemCategoryId, int itemPickupMethodId) {
        this.title = title;
        this.photoURL = photoURL;
        this.userProfileId = userProfileId;
        this.publisher = publisher;
        this.itemCategoryId = itemCategoryId;
        this.itemPickupMethodId = itemPickupMethodId;
    }
}
