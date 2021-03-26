package com.piggybank.mocks;

import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
import org.springframework.lang.NonNull;

import static com.piggybank.model.Account.AccountType;
import static com.piggybank.model.Account.filterSensitiveData;
import static com.piggybank.util.FirebaseEmulatorServices.getFromFirestore;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * todo
 */
public class MockAccountRepository {
    public static final String VALID_CUSTOMER_EMAIL = "user1@email.com";
    public static final String VALID_CUSTOMER_PASSWORD = "user1-pw";
    public static final String VALID_CUSTOMER_USERNAME = "user1";

    public static final String VALID_MERCHANT_EMAIL = "user2@email.com";
    public static final String VALID_MERCHANT_PASSWORD = "user2-pw";
    public static final String VALID_MERCHANT_USERNAME = "user2";

    public static void reset(@NonNull AccountRepository repository) {
        try {
            // test(message)
            when(repository.test(isNull())).thenReturn("Success! No message supplied");
            when(repository.test(anyString()))
                    .thenAnswer(answer -> "Success! Here is your message: " + answer.getArgument(0));

            // create(newAccount)
            when(repository.create(any(Account.class))).thenReturn("Account created successfully!");
            when(repository.create(argThat(account ->
                    account.getType() == null ||
                    (account.getType() == AccountType.MERCHANT && account.getBankAccount() == null) ||
                    account.getEmail() == null ||
                    account.getPassword() == null
            ))).thenThrow(IllegalArgumentException.class);

            // login(email, password)
            when(repository.login(anyString(), anyString()))
                    .thenThrow(IllegalArgumentException.class);
            when(repository.login(eq(VALID_CUSTOMER_EMAIL), eq(VALID_CUSTOMER_PASSWORD)))
                    .thenReturn("Login successful!");
            when(repository.login(eq(VALID_MERCHANT_EMAIL), eq(VALID_MERCHANT_PASSWORD)))
                    .thenReturn("Login successful!");

            // update(email, content)
            when(repository.update(or(eq(VALID_CUSTOMER_EMAIL), eq(VALID_MERCHANT_EMAIL)), any()))
                    .thenReturn("Account successfully updated!");
            when(repository.update(not(or(eq(VALID_CUSTOMER_EMAIL), eq(VALID_MERCHANT_EMAIL))), any()))
                    .thenThrow(IllegalArgumentException.class);

            // get(email)
            Account customerAccount = getFromFirestore("Accounts", VALID_CUSTOMER_EMAIL, Account.class);
            filterSensitiveData(customerAccount);
            when(repository.get(eq(VALID_CUSTOMER_EMAIL))).thenReturn(customerAccount);
            when(repository.get(not(eq(VALID_CUSTOMER_EMAIL)))).thenThrow(IllegalArgumentException.class);

            // usernameExists(username)
            when(repository.usernameExists(eq(VALID_CUSTOMER_USERNAME))).thenReturn(true);
            when(repository.usernameExists(not(eq(VALID_CUSTOMER_USERNAME)))).thenReturn(false);

        } catch (Throwable t) { /* ignore */ }
    }

}
