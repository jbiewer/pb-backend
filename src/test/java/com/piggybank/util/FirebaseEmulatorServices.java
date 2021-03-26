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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirebaseEmulatorServices {
    /**
     * todo
     * @param collectionsDirectory
     * @throws IOException
     */
    public static void generateFirestoreData(File collectionsDirectory) throws IOException, ExecutionException, InterruptedException {
        if (!collectionsDirectory.isDirectory()) {
            throw new IllegalArgumentException("collections directory was not a directory.");
        }

        ObjectMapper mapper = new ObjectMapper();
        addAccounts(mapper, new File(collectionsDirectory.getCanonicalPath(), "accounts.json"));
        // todo: add other files.
    }

    /**
     * todo
     * @throws IOException
     * @throws InterruptedException
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
     * todo
     * @param collectionId
     * @param documentId
     * @param modelClass
     * @param <T>
     * @return
     */
    public static <T> T getFromFirestore(String collectionId, String documentId, Class<T> modelClass) throws Throwable {
        ApiFuture<DocumentSnapshot> futureSnapshot = FirestoreClient.getFirestore()
                .collection(collectionId)
                .document(documentId)
                .get();
        try {
            return futureSnapshot.get().toObject(modelClass);
        } catch (InterruptedException | ExecutionException e) {
            throw e.getCause();
        }
    }

    /**
     * todo
     * @param outputStream
     * @throws ExecutionException
     * @throws InterruptedException
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
     * todo
     */
    private static void addAccounts(ObjectMapper mapper, File file) throws IOException, ExecutionException, InterruptedException {
        List<ApiFuture<WriteResult>> futures = new ArrayList<>();
        if (file.exists()) {
            CollectionReference collection = FirestoreClient.getFirestore().collection("Accounts");
            for (Account account : mapper.readValue(file, Account[].class)) {
                futures.add(collection.document(account.getEmail()).set(account));
            }
        }
        for (ApiFuture<WriteResult> future : futures) {
            future.get();
        }
    }
}
