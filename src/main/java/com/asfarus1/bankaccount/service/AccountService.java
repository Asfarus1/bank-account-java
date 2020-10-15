package com.asfarus1.bankaccount.service;

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance;
import com.asfarus1.bankaccount.exceptions.NotFoundException;
import com.asfarus1.bankaccount.exceptions.ValidationException;
import com.asfarus1.bankaccount.model.Account;
import com.asfarus1.bankaccount.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static java.text.MessageFormat.format;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository repository;

    public Account findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public void withdrawal(Long id, BigDecimal sum) {
        checkPositive(sum);
        Account account = getForUpdate(id);
        BigDecimal balance = account.getBalance();
        if (balance.compareTo(sum) < 0) throw new NotEnoughBalance("Not enough money");
        account.setBalance(balance.subtract(sum));
    }

    @Transactional
    public void deposit(Long id, BigDecimal sum) {
        checkPositive(sum);
        Account account = getForUpdate(id);
        account.setBalance(account.getBalance().add(sum));
    }

    public Account createAccount() {
        return repository.save(new Account());
    }

    private NotFoundException notFound(Long id) {
        return new NotFoundException(format("Account with id='{0}' not found", id));
    }

    private void checkPositive(BigDecimal sum) {
        if (BigDecimal.ZERO.compareTo(sum) >= 0)
            throw new ValidationException("Sum must be positive");
    }

    private Account getForUpdate(Long id) {
        return repository.findForUpdate(id).orElseThrow(() -> notFound(id));
    }
}
