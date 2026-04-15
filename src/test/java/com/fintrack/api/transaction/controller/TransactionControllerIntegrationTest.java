package com.fintrack.api.transaction.controller;

import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.transaction.entity.Transaction;
import com.fintrack.api.transaction.entity.TransactionType;
import com.fintrack.api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private Account conta;
    private String token;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste")
                .email("tx@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);

        conta = accountRepository.save(Account.builder()
                .userId(usuario.getId())
                .name("Nubank")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build());
    }

    private Transaction criarTransacao(BigDecimal valor, TransactionType tipo) {
        return transactionRepository.save(Transaction.builder()
                .userId(usuario.getId())
                .accountId(conta.getId())
                .type(tipo)
                .amount(valor)
                .date(LocalDate.now())
                .build());
    }

    // ── POST /api/transactions ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/transactions")
    class Create {

        @Test
        @DisplayName("Retorna 201 Created com dados da transação")
        void create_validRequest_returns201() throws Exception {
            mockMvc.perform(post("/api/transactions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "accountId": "%s",
                                        "type": "INCOME",
                                        "amount": 500.00,
                                        "date": "%s"
                                    }
                                    """.formatted(conta.getId(), LocalDate.now())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("INCOME"))
                    .andExpect(jsonPath("$.amount").value(500.00));
        }

        @Test
        @DisplayName("Retorna 422 quando conta não pertence ao usuário")
        void create_accountNotOwned_returns422() throws Exception {
            mockMvc.perform(post("/api/transactions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "accountId": "%s",
                                        "type": "INCOME",
                                        "amount": 100.00,
                                        "date": "%s"
                                    }
                                    """.formatted(UUID.randomUUID(), LocalDate.now())))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Retorna 422 quando amount é zero")
        void create_zeroAmount_returns422() throws Exception {
            mockMvc.perform(post("/api/transactions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "accountId": "%s",
                                        "type": "INCOME",
                                        "amount": 0,
                                        "date": "%s"
                                    }
                                    """.formatted(conta.getId(), LocalDate.now())))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Retorna 401 sem token")
        void create_noToken_returns401() throws Exception {
            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"accountId":"%s","type":"INCOME","amount":100,"date":"%s"}
                                    """.formatted(conta.getId(), LocalDate.now())))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /api/transactions ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/transactions")
    class ListAll {

        @Test
        @DisplayName("Retorna 200 com página de transações do usuário")
        void listAll_returnsUserTransactions() throws Exception {
            criarTransacao(new BigDecimal("100.00"), TransactionType.INCOME);
            criarTransacao(new BigDecimal("50.00"), TransactionType.EXPENSE);

            mockMvc.perform(get("/api/transactions")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("Não retorna transações de outro usuário")
        void listAll_doesNotReturnOtherUsersTransactions() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro")
                    .email("outro@tx.com")
                    .passwordHash(passwordEncoder.encode("senha123"))
                    .role(Role.USER)
                    .build());
            Account contaOutro = accountRepository.save(Account.builder()
                    .userId(outro.getId()).name("X").type(AccountType.CASH)
                    .balance(BigDecimal.ZERO).currency("BRL").build());
            transactionRepository.save(Transaction.builder()
                    .userId(outro.getId()).accountId(contaOutro.getId())
                    .type(TransactionType.INCOME).amount(new BigDecimal("200.00"))
                    .date(LocalDate.now()).build());

            mockMvc.perform(get("/api/transactions")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // ── GET /api/transactions/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class FindById {

        @Test
        @DisplayName("Retorna 200 com dados da transação")
        void findById_found_returns200() throws Exception {
            Transaction tx = criarTransacao(new BigDecimal("300.00"), TransactionType.INCOME);

            mockMvc.perform(get("/api/transactions/" + tx.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.amount").value(300.00));
        }

        @Test
        @DisplayName("Retorna 404 quando não encontrada")
        void findById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/transactions/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE /api/transactions/{id} ─────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/transactions/{id}")
    class Delete {

        @Test
        @DisplayName("Retorna 204 ao deletar transação do usuário")
        void delete_found_returns204() throws Exception {
            Transaction tx = criarTransacao(new BigDecimal("100.00"), TransactionType.INCOME);

            mockMvc.perform(delete("/api/transactions/" + tx.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Retorna 404 quando transação não encontrada")
        void delete_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/transactions/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
