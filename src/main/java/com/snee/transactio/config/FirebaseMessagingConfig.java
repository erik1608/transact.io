package com.snee.transactio.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseMessagingConfig {

    private final String mFirebaseSecretPath;

    public FirebaseMessagingConfig(
            @Value("${fcm.config.path}") String firebaseSecretPath
    ) {
        mFirebaseSecretPath = firebaseSecretPath;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials creds = GoogleCredentials.fromStream(
                new ClassPathResource(mFirebaseSecretPath).getInputStream()
        );
        FirebaseOptions opts = FirebaseOptions.builder()
                .setCredentials(creds)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(opts);
        return FirebaseMessaging.getInstance(app);
    }
}
