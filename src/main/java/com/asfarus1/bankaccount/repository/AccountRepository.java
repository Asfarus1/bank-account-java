package com.asfarus1.bankaccount.repository;

import com.asfarus1.bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a from Account a where a.id = :id")
    Optional<Account> findForUpdate(Long id);
}
