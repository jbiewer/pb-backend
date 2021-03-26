package com.piggybank.mocks;

import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;
import org.springframework.lang.NonNull;

import static com.piggybank.model.Account.AccountType;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * todo
 */
public class MockAccountRepository {
    public static final String VALID_CUSTOMER_EMAIL = "user1@email.com";
    public static final String VALID_MERCHANT_EMAIL = "user2@email.com";

    public static final String VALID_CUSTOMER_PASSWORD = "user1-pw";
    public static final String VALID_MERCHANT_PASSWORD = "user2-pw";

    public static void reset(@NonNull AccountRepository repository) {
        try {

            // test(message)
            doReturn("Success! No message supplied")
                    .when(repository)
                    .test(null);
            doAnswer(answer -> "Success! Here is your message: " + answer.getArgument(0))
                    .when(repository).test(anyString());

            // create(newAccount)
            doReturn("Account created successfully!")
                    .when(repository)
                    .create(any(Account.class));
            doThrow(IllegalArgumentException.class)
                    .when(repository)
                    .create(argThat(account ->
                            account.getType() == null ||
                            (account.getType() == AccountType.MERCHANT && account.getBankAccount() == null) ||
                            account.getEmail() == null ||
                            account.getPassword() == null
                    ));

            // login(email, password)
            doThrow(IllegalArgumentException.class)
                    .when(repository)
                    .login(anyString(), anyString());
            doReturn("Login successful!")
                    .when(repository)
                    .login(VALID_CUSTOMER_EMAIL, VALID_CUSTOMER_PASSWORD);
            doReturn("Login successful!")
                    .when(repository)
                    .login(VALID_MERCHANT_EMAIL, VALID_MERCHANT_PASSWORD);

            // update(email, content)
            Account anyAccount = any(Account.class);
            String anyValidEmail = or(VALID_CUSTOMER_EMAIL, VALID_MERCHANT_EMAIL);
            String anyInvalidEmail = not(or(VALID_CUSTOMER_EMAIL, VALID_MERCHANT_EMAIL));
            doThrow(IllegalArgumentException.class)
                    .when(repository)
                    .update(anyInvalidEmail, anyAccount);
            doReturn("Account successfully updated!")
                    .when(repository)
                    .update(anyValidEmail, anyAccount);

            // get(email)
//            when(repository.get(anyString())).thenCallRealMethod();

        } catch (Throwable t) { /* ignore */ }
    }

}
