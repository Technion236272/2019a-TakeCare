package com.syv.takecare.takecare;

import java.util.List;

public class FeedCardInformation {
    private String title;
    private String photo;
    private String publisher;
    private String category;
    private String pickupMethod;
    private List<String> tags;
    private int status;

    /**
     * Empty constructor for the FirestoreRecyclerAdapter
     */
    public FeedCardInformation(){

    }

    public FeedCardInformation(String title, String photo, String publisher,
                               String category, String pickupMethod, int status) {
        this.title = title;
        this.photo = photo;
        this.publisher = publisher;
        this.category = category;
        this.pickupMethod = pickupMethod;
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getCategory() {
        return category;
    }

    public String getPickupMethod() {
        return pickupMethod;
    }

    public int getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPickupMethod(String pickupMethod) {
        this.pickupMethod = pickupMethod;
    }
}
