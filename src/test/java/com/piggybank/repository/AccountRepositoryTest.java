package com.piggybank.repository;

import com.google.cloud.firestore.Firestore;
import com.piggybank.model.Account;
import com.piggybank.model.Customer;
import com.piggybank.util.FirebaseService;
import com.piggybank.util.MockModels;
import net.bytebuddy.implementation.bytecode.Throw;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @AfterEach
    public void afterEach() throws IOException, InterruptedException {
        FirebaseService.clearFirestoreDocuments();
    }

    /**
     * todo
     */
    @Test
    public void testTest() {
        String result = repository.test("test");
        Assertions.assertEquals(result, "Success! Here is your message: test");
    }

    /**
     *
     */
    @Test
    public void testCreateCustomer() {
        try {
            Customer customer = MockModels.mockCustomer();
            // account creation...
            repository.create(customer);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }
}
