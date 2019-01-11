const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = functions.firestore;

// -- End of initialization --



exports.onRequestedItemUpdated = db.document('users/{userId}/requestedItems/{itemId}').onUpdate((snap, context) => {

	console.log("Request status change!");
	console.log("Snapshot: ", snap);
	console.log("snap.after: ", snap.after);
	console.log("snap.after.data(): ", snap.after.data());

	let requestStatus = snap.after.data().requestStatus;
	if (requestStatus === 0) {
		// User's requested item was accepted!
		console.log("Event: user accepted request");
		return admin.firestore()
			.collection('users')
			.doc(context.params.userId)
			.get()
			.then(doc => {

				console.log(doc.data().name + " had his item request accepted!");
				const msg = "Good news " + doc.data().name + "!\nYour requested item was accepted!";
				let tokens = doc.data().tokens
				const payload = {

					notification: {
//						data_type: "request_accepted_message",
						title: "TakeCare",
						body: msg
					}

				};
				console.log("Sending notification");
				return admin.messaging().sendToDevice(tokens, payload)
					.then(function(response) {
						console.log("Successfully sent accepted request message to " + doc.data().name +"\nResponse: ", response);
						return response;
					})
					.catch(function(error) {
						console.log("Error sending accepted item message to " + doc.data().name + "\nError message: ", error)
					});

			});
	}
	// Event was not an accepted item for the user
	return null;
});