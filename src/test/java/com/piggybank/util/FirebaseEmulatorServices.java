package com.piggybank.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseEmulatorServices {
    private static final Map<String, Object[]> cache = new HashMap<>();

    /**
     * Given a directory containing JSON files representing collections of models, generates collections to be loaded
     * into the Firestore database.
     *
     * @param collectionsDirectory Directory containing JSON files where each JSON file is an array of one type
     *                             of model.
     * @throws IOException When an error occurs trying to open the directory.
     * @throws ExecutionException When an error occurs adding to Firestore.
     * @throws InterruptedException When the requests to add to Firestore are interrupted.
     */
    public static void loadFirestoreDocuments(File collectionsDirectory) throws IOException, ExecutionException, InterruptedException {
        if (!collectionsDirectory.isDirectory()) {
            throw new IllegalArgumentException("collections directory was not a directory.");
        }

        ObjectMapper mapper = new ObjectMapper();
        addAccounts(mapper, new File(collectionsDirectory.getCanonicalPath(), "accounts.json"));
        // add other files...
    }

    /**
     * Clears all documents from each collection in the locally running Firestore instance/emulator.
     *
     * @throws IOException When an error occurs sending an HTTP request.
     * @throws InterruptedException When the HTTP request sent is interrupted.
     */
    public static void clearFirestoreDocuments() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9001/emulator/v1/projects/piggybank-104d3/databases/(default)/documents");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() != HttpStatus.SC_OK) {
            System.err.println("Failed to clear data from database.");
        }
    }

    /**
     * Given a path to a document, retrieves the document and converts it to a POJO with the type represented
     * by the 'modelClass' parameter.
     *
     * @param collectionId ID of the collection the document exists in.
     * @param documentId ID of the document.
     * @param modelClass POJO class used to cast the document's data to.
     * @param <T> POJO type to cast the document's data to.
     * @return POJO of type T.
     * @throws ExecutionException When an error occurs retrieving from Firestore.
     * @throws InterruptedException When the requests to get data from Firestore are interrupted.
     */
    public static <T> T getFromFirestore(String collectionId, String documentId, Class<T> modelClass)
            throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> futureSnapshot = FirestoreClient.getFirestore()
                .collection(collectionId)
                .document(documentId)
                .get();
        return futureSnapshot.get().toObject(modelClass);
    }

    /**
     * Given an output stream, prints out the contents of the database into the output stream.
     * Primarily used for simple debugging.
     *
     * @param outputStream Stream to print the database to.
     * @throws ExecutionException When an error occurs retrieving from Firestore.
     * @throws InterruptedException When the requests to get data from Firestore are interrupted.
     */
    public static void printFirestore(OutputStream outputStream) throws ExecutionException, InterruptedException {
        PrintStream printer = new PrintStream(outputStream);
        printer.println("Printing contents of database...");
        for (CollectionReference collection : FirestoreClient.getFirestore().listCollections()) {
            printer.println(collection.getId() + ":");
            for (DocumentReference document : collection.listDocuments()) {
                printer.println("\t" + document.getId() + ":");
                printer.println("\t\t" + document.get().get().getData());
            }
        }
    }

    /**
     * Adds JSON files representing the Account model to the Firestore instance/emulator.
     *
     * @param mapper Mapper to read JSON data into Account POJOs.
     * @param file JSON file containing array of Account models.
     * @throws IOException When an error occurs reading from the JSON file.
     * @throws ExecutionException When an error occurs adding to Firestore.
     * @throws InterruptedException When the requests to add to Firestore are interrupted.
     */
    private static void addAccounts(ObjectMapper mapper, File file) throws IOException, ExecutionException, InterruptedException {
        cache.putIfAbsent("Accounts", mapper.readValue(file, Account[].class));

        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        CollectionReference collection = FirestoreClient.getFirestore().collection("Accounts");
        for (Account account : (Account[]) cache.get("Accounts")) {
            futures.add(collection.document(account.getEmail()).set(account));
        }

        for (ApiFuture<WriteResult> future : futures) {
            future.get();
        }
    }
}
