const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = functions.firestore;


 // Create and Deploy Your First Cloud Functions
 // https://firebase.google.com/docs/functions/write-firebase-functions

// exports.helloWorld = functions.https.onRequest((request, response) => {
// console.log('Hello world')
// response.send("Hello from Firebase!");
// });

exports.onRequestedItemAccepted = db.document('users/{userId}/requestedItems/{itemId}').onUpdate((change, context) => {
    console.log('request accepted!');
});