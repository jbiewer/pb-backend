package com.piggybank.components;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Bean
 *
 * Contains a constructor to initialize the Firebase application.
 * Credentials for initializing the Firebase application should be stored locally on the machine in a safe place.
 * To access the database on 'https://pb-firebase-test-default-rtdb.firebaseio.com/', Jacob has the credentials on
 * his machine and can send them to anyone who needs them.
 */
@Component
public class FirebaseInitializer {
    /**
     * Initializes the Firebase application.
     *
     * Uses locally-stored credentials to initialize the application as well as the associated database.
     * The path to the credentials (which is a private key) is assigned to the environment variable
     * GOOGLE_APPLICATION_CREDENTIALS in the shell environment the program is running in. The URL to the database
     * is located in 'src/main/resources/application.yml'.
     *
     * @param env - Environment the program is running in (injected as a bean by Spring).
     * @throws IOException - When the FirebaseOptions builder can't open the path assigned
     * to GOOGLE_APPLICATION_CREDENTIALS.
     */
    public FirebaseInitializer(Environment env) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl(env.getProperty("firebase.db.url"))
                .build();
         FirebaseApp.initializeApp(options);
    }
}
