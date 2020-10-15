package com.asfarus1.bankaccount.web;

import com.asfarus1.bankaccount.model.Account;
import com.asfarus1.bankaccount.repository.AccountRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccountControllerTest {

    private final Long nonExistedId = 9999L;
    private final String notFoundMsg = format("Account with id='{0}' not found", nonExistedId);
    public final String sumMustBePositiveMsg = "Sum must be positive";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountRepository repository;

    @Test
    void create() throws Exception {
        var body = mockMvc.perform(post("/accounts"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        var account = mapper.readValue(body, Account.class);
        assertThat(account.getBalance(), comparesEqualTo(BigDecimal.ZERO));
    }

    @Test
    void get() throws Exception {
        var createdAccount = createAccount();
        var body = findMockMvc(createdAccount.getId(), OK)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        var account = mapper.readValue(body, Account.class);
        assertThat(account.getId(), equalTo(createdAccount.getId()));
        assertThat(account.getBalance(), comparesEqualTo(createdAccount.getBalance()));
    }


    @Test
    void get_NonExistentId() throws Exception {

        findMockMvc(nonExistedId, HttpStatus.NOT_FOUND)
                .andExpect(content().string(
                        equalTo(notFoundMsg)));
    }

    @Test
    void withdrawal() throws Exception {
        var createdAccount = createAccount();
        var sum = BigDecimal.ONE;
        withdrawalMockMvc(createdAccount.getId(), sum, ACCEPTED);
        accountHasBalance(createdAccount.getId(), createdAccount.getBalance().subtract(sum));
    }

    @Test
    void withdrawal_BalanceIsntEnough() throws Exception {
        var createdAccount = createAccount();
        String NOT_ENOUGH_MONEY_MSG = "Not enough money";
        withdrawalMockMvc(createdAccount.getId(), createdAccount.getBalance().add(BigDecimal.ONE), FORBIDDEN)
                .andExpect(content().string(equalTo(NOT_ENOUGH_MONEY_MSG)));
    }

    @Test
    void withdrawal_NonExistentId() throws Exception {
        withdrawalMockMvc(nonExistedId, BigDecimal.ONE, HttpStatus.NOT_FOUND)
                .andExpect(content().string(equalTo(notFoundMsg)));
    }

    @Test
    void withdrawalNegativeSum() throws Exception {
        var createdAccount = createAccount();
        withdrawalMockMvc(createdAccount.getId(), BigDecimal.ONE.negate(), BAD_REQUEST)
                .andExpect(content().string(equalTo(sumMustBePositiveMsg)));
    }

    @Test
    void withdrawal_LostUpdateTest() throws JsonProcessingException {
        var countQueries = 10000;
        var createdAccount = repository.save(new Account(null, BigDecimal.valueOf(countQueries)));

        var content = post(
                format("/accounts/{0}/withdrawal", createdAccount.getId()))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(BigDecimal.ONE));

        IntStream.range(0, countQueries)
                .parallel()
                .forEach(i ->
                {
                    try {
                        mockMvc.perform(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        accountHasBalance(createdAccount.getId(), BigDecimal.ZERO);
    }

    @Test
    void deposit() throws Exception {
        var createdAccount = createAccount();
        var sum = BigDecimal.ONE;
        depositMockMvc(createdAccount.getId(), sum, ACCEPTED);
        accountHasBalance(createdAccount.getId(), createdAccount.getBalance().add(sum));
    }

    @Test
    void deposit_NoExistentId() throws Exception {
        depositMockMvc(nonExistedId, BigDecimal.ONE, HttpStatus.NOT_FOUND)
                .andExpect(content().string(equalTo(notFoundMsg)));
    }

    @Test
    void deposit_NegativeSum() throws Exception {
        var createdAccount = createAccount();
        depositMockMvc(createdAccount.getId(), BigDecimal.ONE.negate(), BAD_REQUEST)
                .andExpect(content().string(equalTo(sumMustBePositiveMsg)));
    }

    @Test
    void deposit_LostUpdateTest() throws JsonProcessingException {
        var createdAccount = createAccount();
        var sum = BigDecimal.ONE;

        var content = post(
                format("/accounts/{0,number,#}/deposit", createdAccount.getId()))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(sum));

        int countQueries = 10000;
        IntStream.range(0, countQueries)
                .parallel()
                .forEach(i ->
                        {
                            try {
                                mockMvc.perform(content);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );

        BigDecimal expectedBalance = createdAccount.getBalance().add(BigDecimal.valueOf(countQueries));
        accountHasBalance(createdAccount.getId(), expectedBalance);
    }

    private Account createAccount() {
        return repository.save(new Account(null, BigDecimal.TEN));
    }

    private ResultActions findMockMvc(Long id, HttpStatus status) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .get("/accounts/" + id))
                .andDo(print())
                .andExpect(status().is(status.value()));
    }

    private ResultActions withdrawalMockMvc(Long id, BigDecimal sum, HttpStatus status) throws Exception {
        return postMockMvc(format("/accounts/{0,number,#}/withdrawal", id), sum, status);
    }

    private ResultActions depositMockMvc(Long id, BigDecimal sum, HttpStatus status) throws Exception {
        return postMockMvc(format("/accounts/{0,number,#}/deposit", id), sum, status);
    }

    private void accountHasBalance(Long id, BigDecimal expectedBalance) {
        var actual = repository.findById(id).orElse(null);
        assertThat(actual, notNullValue());
        assertThat(actual.getBalance(), comparesEqualTo(expectedBalance));
    }

    private ResultActions postMockMvc(String url, Object content, HttpStatus status) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(content)))
                .andDo(print())
                .andExpect(status().is(status.value()));
    }
}
