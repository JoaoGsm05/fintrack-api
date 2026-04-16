package com.fintrack.api.report.controller;

import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private String token;
    private Account conta;
    private Category categoria;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste Report")
                .email("report@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);

        conta = accountRepository.save(Account.builder()
                .userId(usuario.getId())
                .name("Conta Report")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build());

        categoria = categoryRepository.save(Category.builder()
                .userId(usuario.getId())
                .name("Alimentação")
                .build());
    }

    private void criarDespesa(BigDecimal valor, java.time.LocalDate data) {
        transactionRepository.save(Transaction.builder()
                .userId(usuario.getId())
                .accountId(conta.getId())
                .categoryId(categoria.getId())
                .type(TransactionType.EXPENSE)
                .amount(valor)
                .description("Despesa teste")
                .date(data)
                .build());
    }

    // ── GET /api/reports/expenses-by-category ─────────────────────────────────

    @Nested
    @DisplayName("GET /api/reports/expenses-by-category")
    class ExpensesByCategory {

        @Test
        @DisplayName("Retorna 200 com summaries quando há despesas no período")
        void getExpenses_withData_returns200() throws Exception {
            criarDespesa(new BigDecimal("150.00"), java.time.LocalDate.of(2026, 4, 10));

            mockMvc.perform(get("/api/reports/expenses-by-category")
                            .header("Authorization", "Bearer " + token)
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].categoryName").value("Alimentação"))
                    .andExpect(jsonPath("$[0].percentage").value(100.0));
        }

        @Test
        @DisplayName("Retorna 200 com lista vazia quando não há despesas")
        void getExpenses_noData_returnsEmpty() throws Exception {
            mockMvc.perform(get("/api/reports/expenses-by-category")
                            .header("Authorization", "Bearer " + token)
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Retorna 401 sem token de autenticação")
        void getExpenses_noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/reports/expenses-by-category")
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Não retorna despesas de outro usuário")
        void getExpenses_isolationByUser() throws Exception {
            // Cria despesa para outro usuário no mesmo período
            User outro = userRepository.save(User.builder()
                    .name("Outro").email("outro_report@test.com")
                    .passwordHash(passwordEncoder.encode("senha123")).role(Role.USER).build());
            Account contaOutro = accountRepository.save(Account.builder()
                    .userId(outro.getId()).name("Conta Outro")
                    .type(AccountType.CHECKING).balance(BigDecimal.ZERO).currency("BRL").build());
            transactionRepository.save(Transaction.builder()
                    .userId(outro.getId()).accountId(contaOutro.getId())
                    .type(TransactionType.EXPENSE).amount(new BigDecimal("999.00"))
                    .description("Despesa outro").date(java.time.LocalDate.of(2026, 4, 15)).build());

            mockMvc.perform(get("/api/reports/expenses-by-category")
                            .header("Authorization", "Bearer " + token)
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /api/reports/export-expenses-csv ──────────────────────────────────

    @Nested
    @DisplayName("GET /api/reports/export-expenses-csv")
    class ExportCsv {

        @Test
        @DisplayName("Retorna 200 com Content-Type text/csv e dados corretos")
        void exportCsv_withData_returnsCsvFile() throws Exception {
            criarDespesa(new BigDecimal("200.00"), java.time.LocalDate.of(2026, 4, 5));

            mockMvc.perform(get("/api/reports/export-expenses-csv")
                            .header("Authorization", "Bearer " + token)
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("relatorio_despesas_")))
                    .andExpect(content().contentTypeCompatibleWith("text/csv"))
                    .andExpect(content().encoding("UTF-8"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Categoria;Valor Total;Percentual")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Alimenta")));
        }

        @Test
        @DisplayName("Retorna 401 sem token")
        void exportCsv_noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/reports/export-expenses-csv")
                            .param("start", "2026-04-01")
                            .param("end", "2026-04-30"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
