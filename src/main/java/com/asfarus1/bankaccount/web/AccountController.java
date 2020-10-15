package com.asfarus1.bankaccount.web;

import com.asfarus1.bankaccount.model.Account;
import com.asfarus1.bankaccount.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {
    private final AccountService service;

    @PostMapping
    public ResponseEntity<Account> create() {
        var account = service.createAccount();
        return ResponseEntity.created(URI.create("/accounts/" + account.getId())).body(account);
    }

    @GetMapping("{id}")
    public ResponseEntity<Account> get(@PathVariable Long id) {
        var account = service.findById(id);
        return ResponseEntity.ok().body(account);
    }

    @PostMapping("{id}/withdrawal")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void withdrawal(@PathVariable Long id, @RequestBody BigDecimal sum) {
        service.withdrawal(id, sum);
    }

    @PostMapping("{id}/deposit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deposit(@PathVariable Long id, @RequestBody BigDecimal sum) {
        service.deposit(id, sum);
    }
}
