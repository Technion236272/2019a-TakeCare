const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const db = functions.firestore;

// -- End of initialization --


// Listens to item request changes.
// Sends a notification to the user who requested the item when their item is accepted
exports.onRequestedItemUpdated = db.document('users/{userId}/requestedItems/{itemId}').onUpdate((snap, context) => {
	console.log("Request status change event");
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

					data: {
						display_status: "admin_broadcast",
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


// Listens to item deletions.
// Removes all requests for this item in all existing users' documents
exports.onItemRemoved = db.document('items/{itemId}').onDelete((snap, context) => {
	console.log("Item deleted event");
	console.log("Item information: ", snap.data());
	const itemId = context.params.itemId;

	return admin.firestore()
		.collection('items')
		.doc(itemId)
		.collection('requestedBy')
		.get()
		.then(function(querySnapshot) {
			return querySnapshot.forEach(function(doc) {
				const userRef = doc.data().userRef;
				console.log("Found a user who requested the deleted item: ", userRef);
				return admin.firestore()
					.doc(userRef.path)
					.collection('requestedItems')
					.doc(itemId)
					.delete()
					.then(function() {
						console.log("Request successfully deleted!");
						return null;
					}).catch(function(error) {
						console.error("Error removing request: ", error);
						return null;
					});
			})
		})
		.catch(function(error) {
			console.log("Error getting requested users documents: ", error);
		});
});