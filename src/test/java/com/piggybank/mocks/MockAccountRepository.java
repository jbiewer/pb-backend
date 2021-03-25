package com.piggybank.mocks;

import com.piggybank.model.Account;
import com.piggybank.repository.AccountRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MockAccountRepository {
    public static void reset(AccountRepository repository) {
        // test(message)
        when(repository.test(null)).thenReturn("Success! No message supplied");
        when(repository.test(anyString())).thenAnswer(answer -> "Success! Here is your message: " + answer.getArgument(0));

        // create(newAccount)
        try {
            when(repository.create(any(Account.class))).thenReturn("Account created successfully!");
            when(repository.create(argThat((Account account) -> account.getType() == null)))
                    .thenThrow(new IllegalArgumentException("Must specify account type"));
            when(repository.create(argThat((Account account) -> account.getType() == Account.AccountType.MERCHANT && account.getBankAccount() == null)))
                    .thenThrow(new IllegalArgumentException("Merchant account must have a bank account"));
            when(repository.create(argThat((Account account) -> account.getEmail() == null)))
                    .thenThrow(new IllegalArgumentException("Must specify account email"));
            when(repository.create(argThat((Account account) -> account.getPassword() == null)))
                    .thenThrow(new IllegalArgumentException("Must specify account password"));
        } catch (Throwable t) { /* ignore */ }

        // get(email)
        try {
            when(repository.get(anyString())).thenCallRealMethod();
        } catch (Throwable t) { /* ignore */ }
    }
}
