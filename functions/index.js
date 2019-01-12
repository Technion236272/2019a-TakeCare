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


//TODO: UNTESTED & UNDEPLOYED! Need to flush DB & add userId field to users' document before deploying
// Listens to item creations.
// Sends a notification to all the users who have chosen a keyword that the published item was posted with
exports.onItemCreated = db.document('items/{itemId}').onCreate((snap, context) => {
	console.log('Item creation event');
	const itemKeywords = snap.data().tags;
	console.log('Item has the following tags: ', itemKeywords);

	if (itemKeywords == 'undefined' ||
		itemKeywords == null) {
		// User has no favorite keywords - finish
		return null;
	}

	return admin.firestore()
		.collection('users')
		.get()
		.then(function(querySnapshot) {
			return querySnapshot.forEach(function(doc) {
				const userKeywords = doc.data().tags;
				if (userKeywords == 'undefined' ||
					userKeywords == null) {
						// User has no favorite keywords - finish
						return null;
				}

				// Perform intersection
				userKeywords.filter(tag => -1 !== itemKeywords.indexOf(tag));
				if (userKeywords != 'undefined' &&
					userKeywords != null &&
					userKeywords.length != null &&
					userKeywords.length > 0) {
					// Match found
					console.log('Found a user interested in this item: ', doc.data().name);

					if (doc.data().userId == snap.data().publisher) {
						// User is the item's publisher
						console.log('...but the interested user is the publisher! I won\'t send a notification for them');
						return null;
					}

					const payload = {

						data: {
							display_status: "admin_broadcast",
							title: "TakeCare",
							body: "One of your favorites has been posted! Click here to check out the feed"
						}

					};

					console.log("Sending notification");
					return admin.messaging().sendToDevice(tokens, payload)
					.then(function(response) {
						console.log("Successfully sent wish-listed item notification to " + doc.data().name +"\nResponse: ", response);
						return response;
					})
					.catch(function(error) {
						console.log("Error sending wish-listed item notification " + doc.data().name + "\nError message: ", error)
					});

				} else {
					// No match - finish
					return null;
				}
			})
		})
});