package com.syv.takecare.takecare.utilities;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class RequestedItemsInformation {
    private DocumentReference itemRef;
    private int requestStatus;
    @ServerTimestamp
    private Date timestamp;

    /**
     * Empty constructor for the FirestoreRecyclerAdapter
     */
    public RequestedItemsInformation(){

    }

    public RequestedItemsInformation(DocumentReference itemRef, int requestStatus, Date timestamp) {
        this.itemRef = itemRef;
        this.requestStatus = requestStatus;
        this.timestamp = timestamp;
    }

    public DocumentReference getItemRef() {
        return itemRef;
    }

    public int getRequestStatus() {
        return requestStatus;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
