package com.syv.takecare.takecare.POJOs;


import com.google.firebase.Timestamp;

public class ChatCardInformation {

    private String title;
    private String item;
    private String itemPhoto;
    private String giver;
    private String giverName;
    private String giverPhoto;
    private String taker;
    private String takerName;
    private String takerPhoto;
    private Timestamp timestamp;
    private String chat;


    public ChatCardInformation(){ }

    public ChatCardInformation(String title, String item, String itemPhoto, String giver, String giverName,
                               String giverPhoto, String taker, String takerName, String takerPhoto,
                               Timestamp timestamp, String chat) {
        this.title = title;
        this.item = item;
        this.itemPhoto = itemPhoto;
        this.giver = giver;
        this.giverName = giverName;
        this.giverPhoto = giverPhoto;
        this.taker = taker;
        this.takerName = takerName;
        this.takerPhoto = takerPhoto;
        this.timestamp = timestamp;
        this.chat = chat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemPhoto() {
        return itemPhoto;
    }

    public void setItemPhoto(String itemPhoto) {
        this.itemPhoto = itemPhoto;
    }

    public String getGiver() {
        return giver;
    }

    public void setGiver(String giver) {
        this.giver = giver;
    }

    public String getGiverName() {
        return giverName;
    }

    public void setGiverName(String giverName) {
        this.giverName = giverName;
    }

    public String getGiverPhoto() {
        return giverPhoto;
    }

    public void setGiverPhoto(String giverPhoto) {
        this.giverPhoto = giverPhoto;
    }

    public String getTaker() {
        return taker;
    }

    public void setTaker(String taker) {
        this.taker = taker;
    }

    public String getTakerName() {
        return takerName;
    }

    public void setTakerName(String takerName) {
        this.takerName = takerName;
    }

    public String getTakerPhoto() {
        return takerPhoto;
    }

    public void setTakerPhoto(String takerPhoto) {
        this.takerPhoto = takerPhoto;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }
}
