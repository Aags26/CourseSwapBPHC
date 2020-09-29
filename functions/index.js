const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();
const rdb = admin.database().ref("/match");

exports.findMatchAndNotifyUser = functions.firestore
    .document('users/{userId}/swap/{courseId}')
    .onCreate((snap, context) => {

        const data = snap.data();
        const user = context.params.userId;
        
        console.log(user);

        const assignedCourse = data['assignedCourse'];
        const desiredCourse = data['desiredCourse'];

        return db.collectionGroup("swap")
            .where("assignedCourse", "==", desiredCourse)
            .where("desiredCourse", "==", assignedCourse)
            .get().then(swapSnapshots => {

                if (!swapSnapshots.empty) {

                    var min = swapSnapshots.docs[0].createTime.seconds;
                    var swapDoc = swapSnapshots.docs[0];

                    swapSnapshots.forEach(doc => {
                        if (doc.createTime.seconds < min) {
                            min = doc.createTime.seconds;
                            swapDoc = doc;
                        }
                    });

                    swapDoc.ref.parent.parent.get().then(userDoc => {

                        const userData = userDoc.data();
                        const token_recepient = userData['userNotificationToken'];

                        const notificationBody = "Found a user ready to swap " + desiredCourse + 
                                                " with " + assignedCourse;

                        return db.doc("users/" + user).get().then(senderSnapshot => {
                            const sender = senderSnapshot.data();
                            const token_sender = sender['userNotificationToken'];

                            const payload = {

                                notification: {
                                    title: "Match found!",
                                    body: notificationBody,
                                    clickAction: "MatchedUserActivity"
                                },
                                data: {
                                    USER_EMAIL: sender['userEmail'] + "",
                                    USER_PHONE: sender['userPhoneNumber'] + "",
                                    USER_ASSIGNED: assignedCourse + "",
                                    USER_DESIRED: desiredCourse + ""
                                }
    
                            }
    
                            return admin.messaging().sendToDevice(token_recepient, payload)
                            .then((response) => {

                                const notificationBody = "Found a user ready to swap " + assignedCourse + 
                                                " with " + desiredCourse;

                                const payload = {

                                    notification: {
                                        title: "Match found!",
                                        body: notificationBody,
                                        clickAction: "MatchedUserActivity"
                                    },
                                    data: {
                                        USER_EMAIL: userData['userEmail'] + "",
                                        USER_PHONE: userData['userPhoneNumber'] + "",
                                        USER_ASSIGNED: desiredCourse + "",
                                        USER_DESIRED: assignedCourse + ""
                                    }

                                }
                                return admin.messaging().sendToDevice(token_sender, payload)
                            })
                            .catch((error) => {
                                console.log("Error sending message: ", error);
                            });

                        });
                    });
                }
                
            });

    });

