package com.syv.takecare.takecare;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class RequesterCardInformation {
    @ServerTimestamp
    Date timestamp;
    DocumentReference userRef;

    /**
     * Empty constructor for the FirestoreRecyclerAdapter
     */
    public RequesterCardInformation(){

    }
    public RequesterCardInformation(Date timestamp, DocumentReference userRef) {
        this.timestamp = timestamp;
        this.userRef = userRef;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }
}
