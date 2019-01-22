package com.syv.takecare.takecare;

import com.google.firebase.Timestamp;
import com.syv.takecare.takecare.POJOs.ChatMessageInformation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ChatMessageTest {
    static private String message;
    static private String sender;
    static private String receiver;
    static private Timestamp timestamp;

    static private ChatMessageInformation chat;

    @BeforeClass
    public static void setUpTest() {
        message = "This is a message";
        sender = "Sender 1";
        receiver = "Receiver 1";
        timestamp = new Timestamp(new Date(1200));
    }

    @Before
    public void initializeTest() {
        chat = new ChatMessageInformation(message, sender, receiver, timestamp);
    }

    @Test
    public void getAndSetTest() {
        assertEquals(chat.getMessage(), "This is a message");
        assertEquals(chat.getSender(), "Sender 1");
        assertEquals(chat.getReceiver(), "Receiver 1");
        assertEquals(chat.getTimestamp(), new Timestamp(new Date(1200)));

        chat.setMessage("This is a new message");
        chat.setSender("Sender 2");
        chat.setReceiver("Receiver 2");
        chat.setTimestamp(new Timestamp(new Date(3000)));

        assertEquals(chat.getMessage(), "This is a new message");
        assertEquals(chat.getSender(), "Sender 2");
        assertEquals(chat.getReceiver(), "Receiver 2");
        assertEquals(chat.getTimestamp(), new Timestamp(new Date(3000)));
    }
}
