package com.fintrack.api.recurring.controller;

import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
import com.fintrack.api.recurring.entity.RecurringFrequency;
import com.fintrack.api.recurring.entity.RecurringTransaction;
import com.fintrack.api.recurring.repository.RecurringTransactionRepository;
import com.fintrack.api.transaction.entity.TransactionType;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecurringTransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private RecurringTransactionRepository recurringRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private String token;
    private Account conta;
    private Category categoria;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste Recurring")
                .email("recurring@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);

        conta = accountRepository.save(Account.builder()
                .userId(usuario.getId())
                .name("Conta Corrente")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build());

        categoria = categoryRepository.save(Category.builder()
                .userId(usuario.getId())
                .name("Streaming")
                .build());
    }

    private RecurringTransaction criarRecorrente() {
        return recurringRepository.save(RecurringTransaction.builder()
                .userId(usuario.getId())
                .accountId(conta.getId())
                .categoryId(categoria.getId())
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.90"))
                .description("Netflix")
                .frequency(RecurringFrequency.MONTHLY)
                .nextOccurrence(java.time.LocalDate.of(2026, 5, 1))
                .active(true)
                .build());
    }

    private String bodyValido() {
        return String.format("""
                {
                    "accountId": "%s",
                    "categoryId": "%s",
                    "type": "EXPENSE",
                    "amount": 45.90,
                    "description": "Netflix",
                    "frequency": "MONTHLY",
                    "nextOccurrence": "2026-05-01"
                }
                """, conta.getId(), categoria.getId());
    }

    // ── POST /api/recurring ───────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/recurring")
    class Create {

        @Test
        @DisplayName("Retorna 201 Created com dados da transação recorrente")
        void create_valid_returns201() throws Exception {
            mockMvc.perform(post("/api/recurring")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyValido()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").value("Netflix"))
                    .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Retorna 401 sem token")
        void create_noToken_returns401() throws Exception {
            mockMvc.perform(post("/api/recurring")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyValido()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Retorna 422 quando campos obrigatórios ausentes")
        void create_missingFields_returns422() throws Exception {
            mockMvc.perform(post("/api/recurring")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 45.90}
                                    """))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // ── GET /api/recurring ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/recurring")
    class ListAll {

        @Test
        @DisplayName("Retorna 200 com lista de transações recorrentes")
        void listAll_returnsUserRecurrings() throws Exception {
            criarRecorrente();

            mockMvc.perform(get("/api/recurring")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Não retorna transações recorrentes de outro usuário")
        void listAll_doesNotReturnOthersData() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro").email("outro_rec@test.com")
                    .passwordHash(passwordEncoder.encode("senha123")).role(Role.USER).build());
            Account contaOutro = accountRepository.save(Account.builder()
                    .userId(outro.getId()).name("Conta Outro")
                    .type(AccountType.CHECKING).balance(BigDecimal.ZERO).currency("BRL").build());
            recurringRepository.save(RecurringTransaction.builder()
                    .userId(outro.getId()).accountId(contaOutro.getId())
                    .type(TransactionType.EXPENSE).amount(new BigDecimal("10.00"))
                    .frequency(RecurringFrequency.WEEKLY)
                    .nextOccurrence(java.time.LocalDate.of(2026, 5, 1)).active(true).build());

            mockMvc.perform(get("/api/recurring")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /api/recurring/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/recurring/{id}")
    class FindById {

        @Test
        @DisplayName("Retorna 200 com dados da transação recorrente")
        void findById_found_returns200() throws Exception {
            RecurringTransaction rec = criarRecorrente();

            mockMvc.perform(get("/api/recurring/" + rec.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(rec.getId().toString()));
        }

        @Test
        @DisplayName("Retorna 404 quando não encontrada")
        void findById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/recurring/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PUT /api/recurring/{id} ───────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/recurring/{id}")
    class Update {

        @Test
        @DisplayName("Retorna 200 com dados atualizados")
        void update_valid_returns200() throws Exception {
            RecurringTransaction rec = criarRecorrente();
            String bodyAtualizado = String.format("""
                    {
                        "accountId": "%s",
                        "categoryId": "%s",
                        "type": "EXPENSE",
                        "amount": 59.90,
                        "description": "Netflix Premium",
                        "frequency": "MONTHLY",
                        "nextOccurrence": "2026-06-01"
                    }
                    """, conta.getId(), categoria.getId());

            mockMvc.perform(put("/api/recurring/" + rec.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyAtualizado))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Netflix Premium"))
                    .andExpect(jsonPath("$.amount").value(59.90));
        }
    }

    // ── DELETE /api/recurring/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/recurring/{id}")
    class Delete {

        @Test
        @DisplayName("Retorna 204 No Content ao deletar")
        void delete_found_returns204() throws Exception {
            RecurringTransaction rec = criarRecorrente();

            mockMvc.perform(delete("/api/recurring/" + rec.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Retorna 404 quando não encontrada")
        void delete_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/recurring/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
