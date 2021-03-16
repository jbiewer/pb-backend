# Back-end for Piggy Bank


### Building & Running
`$ ./gradlew bootRun`

### Clean up build directory
`$ ./gradlew clean`

### Linking Application to Firebase

1. In the Firebase console, go to the settings and click on the 'Service accounts' tab. You'll need to 'Generate 
   a new private key' (button on the bottom) and store it somewhere locally on your machine, NOT in a Git repo.
2. Next, whenever you run the application, you'll need to set the environment variable 'GOOGLE_APPLICATION_CREDENTIALS'
   to the location of the private key file.
   
Ex.: `$ export GOOGLE_APPLICATION_CREDENTIALS="/path/to/priv/key.json"`
