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

	public FirebaseMessagingConfig(@Value("${fcm.config.path}") String firebaseSecretPath) {
		mFirebaseSecretPath = firebaseSecretPath;
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new ClassPathResource(mFirebaseSecretPath).getInputStream());
		FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
		FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions);
		return FirebaseMessaging.getInstance(app);
	}
}
