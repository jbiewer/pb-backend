package com.piggybank.util;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class FirebaseEmulatorService {
    public static void clearFirestoreDocuments() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9001/emulator/v1/projects/pb-emulator-project/databases/(default)/documents");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        Assertions.assertEquals(response.statusCode(), 200);
    }
}
