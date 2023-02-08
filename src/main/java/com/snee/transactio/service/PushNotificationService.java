package com.snee.transactio.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.snee.transactio.db.entities.user.UserDevice;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PushNotificationService {

    private static final String PLATFORM_ANDROID = "Android";

    private final FirebaseMessaging mFirebaseMessaging;

    public PushNotificationService(FirebaseMessaging firebaseMessaging) {
        mFirebaseMessaging = firebaseMessaging;
    }

    public void sendNotification(
            String title,
            String body,
            HashMap<String, String> message,
            UserDevice device
    ) {
        if (PLATFORM_ANDROID.equals(device.getPlatform())) {
            try {
                sendFCMNotification(title, body, message, device);
            } catch (FirebaseMessagingException ignored) {
            }
        }
    }

    private void sendFCMNotification(
            String title,
            String body,
            HashMap<String, String> message,
            UserDevice device
    ) throws FirebaseMessagingException {

        Notification notification = Notification
                .builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message messageObj = Message
                .builder()
                .setToken(device.getPushRegistrationId())
                .setNotification(notification)
                .putAllData(message)
                .build();
        mFirebaseMessaging.send(messageObj);
    }
}
