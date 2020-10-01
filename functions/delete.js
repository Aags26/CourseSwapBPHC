const functions = require('firebase-functions');
const admin = require('firebase-admin');
const db = admin.firestore();

exports.onDeleteRequest = functions.firestore
    .document('users/{userId}/swap/{courseId}')
    .onDelete((snap, context) => {

        const data = snap.data();
        const match = data['match'];
        const assigned = data['assignedCourse'];
        const desired = data['desiredCourse'];

        if (match !== "none") {
            return db.doc('users/' + match).get().then(userSnap => {
                return db.doc('users/' + match + '/swap/' + desired).get().then(swapSnap => {
    
                    const userData = userSnap.data();
                    const swapData = swapSnap.data();
    
                     return db.collectionGroup("swap")
                     .where("assignedCourse", "==", swapData['desiredCourse'])
                     .where("desiredCourse", "==", swapData['assignedCourse'])
                     .where("match", "==", "none")
                     .get().then( swapSnapshots => {
                        if (!swapSnapshots.empty) {
    
                            var min = swapSnapshots.docs[0].createTime.seconds;
                            var swapDoc = swapSnapshots.docs[0];
                    
                            swapSnapshots.forEach(doc => {
                                if (doc.createTime.seconds < min) {
                                    min = doc.createTime.seconds;
                                    swapDoc = doc;
                                }
                            });
                    
                            swapDoc.ref.parent.parent.get().then(matchedUserDoc => {
                    
                                const matchedUserData = matchedUserDoc.data();
                    
                                const notificationBody = "Found a user ready to swap " + swapData['desiredCourse'] +
                                    " with " + swapData['assignedCourse'];
                    
                                const payload = {
                                    notification: {
                                        title: "Match found!",
                                        body: notificationBody,
                                        clickAction: "MatchedUserActivity"
                                    },
                                    data: {
                                        USER_EMAIL: userData['userEmail'] + "",
                                        USER_PHONE: userData['userPhoneNumber'] + "",
                                        USER_ASSIGNED: swapData['assignedCourse'] + "",
                                        USER_DESIRED: swapData['desiredCourse'] + ""
                                    }
                                };
                    
                                return admin.messaging().sendToDevice(matchedUserData['userNotificationToken'], payload)
                                    .then((response) => {
                    
                                        const notificationBody = "Found a user ready to swap " + swapData['assignedCourse'] +
                                            " with " + swapData['desiredCourse'];
                    
                                        const payload = {
                                            notification: {
                                                title: "Match found!",
                                                body: notificationBody,
                                                clickAction: "MatchedUserActivity"
                                            },
                                            data: {
                                                USER_EMAIL: matchedUserData['userEmail'] + "",
                                                USER_PHONE: matchedUserData['userPhoneNumber'] + "",
                                                USER_ASSIGNED: swapData['desiredCourse'] + "",
                                                USER_DESIRED: swapData['assignedCourse'] + ""
                                            }
                                        };
                                        return admin.messaging().sendToDevice(userData['userNotificationToken'], payload)
                                        .then(response => {
                                            return db.doc('users/' + match + '/swap/' + desired).set({
                                                match: matchedUserDoc.id
                                            }).then(response => {
                                                return db.doc('users/' + matchedUserDoc.id + '/swap/' + assigned).set({
                                                    match: match
                                                })                                        
                                            })
                                        })
                                    })
                            });
                        }
                    })
                }) 
            })
        }
        
    })