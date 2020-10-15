package com.asfarus1.bankaccount.service;

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance;
import com.asfarus1.bankaccount.exceptions.NotFoundException;
import com.asfarus1.bankaccount.exceptions.ValidationException;
import com.asfarus1.bankaccount.model.Account;
import com.asfarus1.bankaccount.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccountServiceTest {
    private final AccountRepository mockRepository = mock(AccountRepository.class);
    private final AccountService service = new AccountService(mockRepository);
    private final Account demoAccount = new Account(1L, BigDecimal.TEN);
    private final Long notExistedId = 999999L;

    @BeforeEach
    void setUp() {
        when(mockRepository.findById(demoAccount.getId())).thenReturn(of(demoAccount));
        when(mockRepository.findForUpdate(demoAccount.getId())).thenReturn(of(demoAccount));
        when(mockRepository.findById(notExistedId)).thenReturn(empty());
        when(mockRepository.findForUpdate(notExistedId)).thenReturn(empty());
        when(mockRepository.save(any(Account.class))).thenReturn(demoAccount);
    }

    @Test
    void findById() {
        var account = service.findById(demoAccount.getId());
        assertThat(account.getId()).isEqualByComparingTo(demoAccount.getId());
        assertThat(account.getBalance()).isEqualByComparingTo(demoAccount.getBalance());
    }

    @Test
    void findById_NonExistentId() {
        assertThrows(NotFoundException.class, () -> service.findById(notExistedId));
    }

    @Test
    void withdrawal() {
        service.withdrawal(demoAccount.getId(), BigDecimal.ONE);
    }

    @Test
    void withdrawal_BalanceIsntEnough() {
        assertThrows(NotEnoughBalance.class, () ->
                service.withdrawal(demoAccount.getId(), demoAccount.getBalance().add(BigDecimal.ONE)));
    }

    @Test
    void withdrawal_NonExistentId() {
        assertThrows(NotFoundException.class, () ->
                service.withdrawal(notExistedId, BigDecimal.ONE));
    }

    @Test
    void withdrawal_NegativeSum() {
        assertThrows(ValidationException.class, () ->
                service.withdrawal(demoAccount.getId(), BigDecimal.ONE.negate()));
    }

    @Test
    void deposit() {
        service.deposit(demoAccount.getId(), BigDecimal.ONE);
    }

    @Test
    void deposit_NonExistentId() {
        assertThrows(NotFoundException.class, () ->
                service.deposit(notExistedId, BigDecimal.ONE));
    }

    @Test
    void deposit_NegativeSum() {
        assertThrows(ValidationException.class, () ->
                service.deposit(demoAccount.getId(), BigDecimal.ONE.negate()));
    }

    @Test
    void createAccount() {
        assertThat(service.createAccount()).isEqualTo(demoAccount);
    }
}
