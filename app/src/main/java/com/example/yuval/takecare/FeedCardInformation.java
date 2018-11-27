package com.example.yuval.takecare;

public class FeedCardInformation {
    String title;
    int photoId;
    int userProfileId;
    String publisher;
    int itemCategoryId;
    int itemPickupMethodId;

    public FeedCardInformation(String title, int photoId, int userProfileId, String publisher,
                               int itemCategoryId, int itemPickupMethodId) {
        this.title = title;
        this.photoId = photoId;
        this.userProfileId = userProfileId;
        this.publisher = publisher;
        this.itemCategoryId = itemCategoryId;
        this.itemPickupMethodId = itemPickupMethodId;
    }
}
