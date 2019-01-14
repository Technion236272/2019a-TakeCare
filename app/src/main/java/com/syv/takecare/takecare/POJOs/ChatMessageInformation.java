package com.syv.takecare.takecare.POJOs;

import com.google.firebase.Timestamp;

public class ChatMessageInformation {
    private String message;
    private String sender;
    private String receiver;
    private Timestamp timestamp;


    public ChatMessageInformation(){

    }

    public ChatMessageInformation(String message, String sender, String receiver, Timestamp timestamp) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
