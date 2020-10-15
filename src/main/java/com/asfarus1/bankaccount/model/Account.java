package com.asfarus1.bankaccount.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
}

