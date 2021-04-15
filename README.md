# Back-end for Piggy Bank

### Java Requirement

Java JDK 11 is required.
Once downloaded, make sure to assign the "JAVA_HOME" environment variable to the JDK bin directory.
Quit any terminal that is running, and try building and running again.


### Building & Running

First, make sure to follow the **Linking Application to Firebase** section, so the Firebase Admin SDK can interact
with the cloud Firebase services.

To start the application, execute the following:

`$ ./gradlew bootRun`

To clean up the build directory, execute the following:

`$ ./gradlew clean`

To run the testing suite, follow the **Testing** section on how to set up your environment.


### Linking Application to Firebase

In the Firebase console, go to the settings and click on the 'Service accounts' tab. You'll need to 'Generate a new
private key' (button on the bottom) and store it locally as `secret/pb-firebase-pk.json`. When the application is executed,
the build script will assign the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path of that file.


### Testing

You will need the Firebase CLI tool installed to run the testing suite as it spawns a locally-running
emulator for each Firebase service enabled: https://firebase.google.com/docs/cli

The testing suite uses JUnit as the testing framework and Mockito to provide mocks for pure unit testing
of a component. Spring annotations are used to inject Mockito mocks as dependency beans.

Once that is installed, execute the following:

`$ ./gradlew test`

The most recent test results can be viewed from the browser by opening the following URI:

`file://<path_to_repo>/build/reports/tests/test/index.html`

To see code coverage, execute the following:

`$ ./gradlew jacocoTestReport`

The most recent code coverage report can be viewed from the browser by opening the following URI:

`file://<path_to_repo>/build/reports/tests/test/index.html`


### Session management

Firebase Auth is used to manage session cookies, and the implementations are based on the following tutorial:
https://firebase.google.com/docs/auth/admin/manage-cookies

Basically, there are three components of session management:
1. Create a session upon log-in/account creation
2. Validate an existing session for all incoming requests
3. Clear and revoke a session cookie upon log-out.
