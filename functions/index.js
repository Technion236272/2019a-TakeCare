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

			return admin.firestore()
			.collection('items')
			.doc(context.params.itemId)
			.get()
			.then(itemDoc => {
				console.log(doc.data().name + " had his item request accepted!");
				const msg = "Good news " + doc.data().name + "!\nYour request for " + itemDoc.data().title + " was accepted!\nClick here to check it out";
				let tokens = doc.data().tokens
				var photo = null;
				if (typeof itemDoc.data().photo !== 'undefined') {
					photo = itemDoc.data().photo;
				}
				const payload = {

					data: {
						display_status: "admin_broadcast",
						notification_type: "ACCEPTED_ITEM",
						title: "TakeCare",
						body: msg,
						item_id: photo
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


// Listens to new messages.
// Increases a counter that represents the amount of messages in this chat room.
// Updates the timestamp of the last received message in this chat room.
exports.onMessageSent = db.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
	const chatDocRef = admin.firestore()
		.collection('chats')
		.doc(context.params.chatId);

	return admin.firestore().runTransaction(function(transaction) {
		return transaction.get(chatDocRef).then(function(chatDoc) {
			if (!chatDoc.exists) {
				throw new "Chat room does not exist";
			}
			transaction.update(chatDocRef, { messagesCount : chatDoc.data().messagesCount + 1});
			transaction.update(chatDocRef, { timestamp : snap.data().timestamp});
			return null;
		});
	}).then(function() {
		console.log("Updated messages counter");
		return null;
	}).catch(function(error) {
		console.log("Error updating messages counter: ", error);
		return null;
	});

});



//Listens to new messages.
// Sends a notification to the receiving end of a chat message
exports.onMessageSentNotify = db.document('chats/{chatId}/messages/{messageId}').onCreate((snap, context) => {
	return admin.firestore()
	.collection('users')
	.doc(snap.data().receiver)
	.get()
	.then(receiverDoc => {
		return admin.firestore()
		.collection('users')
		.doc(snap.data().sender)
		.get()
		.then(senderDoc => {
			console.log('Creating a chat notification for: ', receiverDoc.data().name);
			let tokens = receiverDoc.data().tokens
			const msg = senderDoc.data().name + " has sent you a new message";
			var photo = null;
			if (typeof senderDoc.data().profilePicture !== 'undefined') {
				photo = senderDoc.data().profilePicture;
			}
			const payload = {

				data: {
					display_status: "admin_broadcast",
					notification_type: "CHAT",
					title: "TakeCare",
					body: msg,
					sender_id: senderDoc.data().uid,
					sender_photo_url: photo
				}

			};

			console.log("Sending notification");
			return admin.messaging().sendToDevice(tokens, payload)
			.then(function(response) {
				console.log("Successfully sent chat notification\nResponse: ", response);
				return response;
			})
			.catch(function(error) {
				console.log("Error sending chat notification\nError message: ", error)
			});
		});
	});
});



//TODO: UNTESTED & UNDEPLOYED! Need to flush DB & add uid field to users' document before deploying
// Listens to item creations.
// Sends a notification to all the users who have chosen a keyword that the published item was posted with
exports.onItemCreatedNotifications = db.document('items/{itemId}').onCreate((snap, context) => {
	console.log('Item creation event');
	const itemKeywords = snap.data().tags;
	console.log('Item has the following tags: ', itemKeywords);

	if (itemKeywords === 'undefined' ||
		itemKeywords === null) {
		// User has no favorite keywords - finish
		return null;
	}

	let tokens = [];

    const payload = {
        //TODO: change this to "data" and add relevant fields
        notification: {
            title: "TakeCare",
            body: "One of your favorites has been posted! Click here to check out the feed"
        }
    };


    return admin.firestore()
    .collection('users')
    .get()
    .then(function(querySnapshot) {
        return querySnapshot.foreach(function(doc) {
            console.log('Checking user: ', doc.data().name);
            const userKeywords = doc.data().tags;
            if (userKeywords === 'undefined' ||
                userKeywords === null) {
                    return null;
            }

            var hasMatch = false;
            for (var i = 0; i < itemKeywords.length; i++) {
                if (userKeywords.includes(itemKeywords[i])) {
                    hasMatch = true;
                }
            }

            if (hasMatch) {
                // Match found
                console.log('Found a user interested in this item: ', doc.data().name);

                if (doc.data().uid === snap.data().publisher) {
                    // User is the item's publisher
                    console.log('...but the interested user is the publisher! I won\'t send a notification for them\nFOR TESTING PURPOSES I WILL!');
//    						return null;
                }

                tokens = tokens.concat(doc.data().tokens);
                return null;
            }
        })
    })
    .then(function() {
        console.log("Sending notification");
        return admin.messaging().sendToDevice(tokens, payload)
        .then(function(response) {
            console.log("Successfully sent wish-listed item notification to " + doc.data().name +"\nResponse: ", response);
            return response;
        })
        .catch(function(error) {
            console.log("Error sending wish-listed item notification " + doc.data().name + "\nError message: ", error)
        });
    })
});


// Listens to item creations.
// Creates a tag document for each tag associated with the uploaded item
exports.onItemCreatedAddTags = db.document('items/{itemID}').onCreate((snap, context) => {
	console.log('Item creation event');
	const itemKeywords = snap.data().tags;
	if (itemKeywords === null ||
		itemKeywords === 'undefined') {
			return null;
	}

	var batch = admin.firestore().batch();

	for (var i = 0; i < itemKeywords.length; i++) {
	    console.log('Creating document for: ', itemKeywords[i]);

		var tagObject = {
			tag : itemKeywords[i],
		};

		var tagDocRef = admin.firestore().doc('tags/' + itemKeywords[i]);
		batch.set(tagDocRef, { tag : itemKeywords[i] });
	}

	batch.commit().then(function () {
	    console.log('Finished adding the item keywords');
	    return 0;
	})
	.catch(function(error) {
	    console.log('Error adding the item keywords: ', error);
	    return null;
	});
})
