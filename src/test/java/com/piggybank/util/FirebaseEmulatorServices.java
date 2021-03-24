package com.piggybank.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.piggybank.model.Account;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

public class FirebaseEmulatorServices {
    /**
     * todo
     * @param collectionsDirectory
     * @throws IOException
     */
    public static void generateFirestoreData(File collectionsDirectory) throws IOException {
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

    public static <T> T get(String collectionId, String documentId, Class<T> modelClass)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = FirestoreClient.getFirestore()
                .collection(collectionId)
                .document(documentId)
                .get().get();
        return snapshot.toObject(modelClass);
    }

    public static void printFirestore(OutputStream outputStream) throws ExecutionException, InterruptedException {
        PrintStream printer = new PrintStream(outputStream);
        for (CollectionReference collection : FirestoreClient.getFirestore().listCollections()) {
            printer.println(collection.getId() + ":");
            for (DocumentReference document : collection.listDocuments()) {
                printer.println("\t" + document.getId() + ":");
                printer.println("\t\t" + document.get().get().getData());
            }
        }
    }

    private static void addAccounts(ObjectMapper mapper, File file) throws IOException {
        if (file.exists()) {
            CollectionReference collection = FirestoreClient.getFirestore().collection("Accounts");
            for (Account account : mapper.readValue(file, Account[].class)) {
                collection.document(account.getUsername()).set(account);
            }
        }
    }
}
