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
				var photo = "NA";
				if (typeof itemDoc.data().photo !== 'undefined') {
					photo = itemDoc.data().photo;
				}
				const payload = {

					data: {
						display_status: "admin_broadcast",
						notification_type: "ACCEPTED_ITEM",
						title: itemDoc.data().title,
						body: msg,
						photo: photo
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
// Note to self: next time use transactions - wtf is this code??
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
			return admin.firestore()
			.collection('chats')
			.doc(context.params.chatId)
			.get()
			.then(chatDoc => {
				console.log('Creating a chat notification for: ', receiverDoc.data().name);
				let tokens = receiverDoc.data().tokens
				var photo = null;
				if (typeof senderDoc.data().profilePicture !== 'undefined') {
					photo = senderDoc.data().profilePicture;
				}
				const payload = {

					data: {
						display_status: "admin_broadcast",
						notification_type: "CHAT",
						title: "New message from " + senderDoc.data().name,
						body: snap.data().message,
						sender_id: senderDoc.data().uid,
						sender_photo_url: photo,
						chat_id: chatDoc.data().chat,
						item_id: chatDoc.data().item
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
});


// Listens to item creations.
// Sends a notification to all the users who have wish-listed at least one keyword that the published item was posted with
exports.onItemCreatedNotifications = db.document('items/{itemId}').onCreate((snap, context) => {
	console.log('Item creation event');
	const itemKeywords = snap.data().tags;
	console.log('Item has the following tags: ', itemKeywords);

	if (typeof itemKeywords === 'undefined' ||
		itemKeywords === null) {
		// Item has no associated keywords - finish
		return null;
	}

	let tokens = [];

    var photo = "NA";
    if (typeof snap.data().photo !== 'undefined') {
        photo = snap.data().photo;
    }
    const payload = {

        data: {
            display_status: "admin_broadcast",
            notification_type: "FAVORITES",
            title: "Favorites",
            body: "One of your favorites has been posted: \"" + snap.data().title + "\".\nClick here to check out the feed!",
            photo: photo
        }

    };

    return admin.firestore()
    .collection('users')
    .get()
    .then(querySnapshot => {
        console.log('Iterating over all the users in the db');
        for (let i = 0; i < querySnapshot.size; i++) {
            const data = querySnapshot.docs[i].data();
            const userKeywords = data.tags;
            if (typeof userKeywords === 'undefined' ||
                userKeywords === null) {
                continue;
            }

            var hasMatch = false;

            for (let j = 0; j < itemKeywords.length; j++) {
                if (userKeywords.includes(itemKeywords[j])) {
                    hasMatch = true;
                }
            }

            if (hasMatch) {
                // Match found
                console.log('Found a user interested in this item: ', data.name);
                if (data.uid === snap.data().publisher) {
                    // User is the item's publisher
                    console.log('...but the interested user is the publisher! I won\'t send a notification to them\n');
                    continue;
                }

                tokens = tokens.concat(data.tokens);
            }
        }

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


// Listens to request creations.
// Sends a notification to the user who posted the item when a request is made
exports.onRequestCreatedNotify = db.document('users/{userId}/requestedItems/{requestId}').onCreate((snap, context) => {
    var ref = snap.data().itemRef;
    console.log('Request made for: ', ref);
	return admin.firestore()
	.doc(ref.path)
	.get()
	.then(itemDoc => {
		return admin.firestore()
		.collection('users')
		.doc(itemDoc.data().publisher)
		.get()
		.then(userDoc => {
			return admin.firestore()
			.collection('users')
			.doc(context.params.userId)
			.get()
			.then(selfDoc => {
				console.log('Sending request notification to: ', userDoc.data().name);
				let tokens = userDoc.data().tokens
				var photo = "NA";
				if (typeof selfDoc.data().profilePicture !== 'undefined') {
					photo = selfDoc.data().profilePicture;
				}
				const msg = selfDoc.data().name + " is interested in your listing of \"" + itemDoc.data().title + "\".\nClick here to view your listings";

				const payload = {

					data: {
						display_status: "admin_broadcast",
						notification_type: "REQUESTED",
						title: itemDoc.data().title,
						body: msg,
						photo: photo
					}

				};
				console.log("Sending notification");
				return admin.messaging().sendToDevice(tokens, payload)
				.then(function(response) {
					console.log("Successfully sent request message to " + userDoc.data().name +"\nResponse: ", response);
					return response;
				})
				.catch(function(error) {
					console.log("Error sending request message to " + userDoc.data().name + "\nError message: ", error)
				});
			});
		});
	});
});

