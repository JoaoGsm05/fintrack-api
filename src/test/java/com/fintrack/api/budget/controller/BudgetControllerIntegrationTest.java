package com.fintrack.api.budget.controller;

import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
import com.fintrack.api.budget.entity.Budget;
import com.fintrack.api.budget.entity.BudgetPeriod;
import com.fintrack.api.budget.repository.BudgetRepository;
import com.fintrack.api.category.entity.Category;
import com.fintrack.api.category.repository.CategoryRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private String token;
    private Category categoria;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste Budget")
                .email("budget@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);

        categoria = categoryRepository.save(Category.builder()
                .userId(usuario.getId())
                .name("Alimentacao")
                .build());
    }

    private Budget criarBudget() {
        return budgetRepository.save(Budget.builder()
                .userId(usuario.getId())
                .categoryId(categoria.getId())
                .amount(new BigDecimal("500.00"))
                .period(BudgetPeriod.MONTHLY)
                .startDate(java.time.LocalDate.of(2026, 4, 1))
                .endDate(java.time.LocalDate.of(2026, 4, 30))
                .build());
    }

    @Nested
    @DisplayName("POST /api/budgets")
    class Create {

        @Test
        @DisplayName("Retorna 201 Created com dados do orcamento")
        void create_valid_returns201() throws Exception {
            mockMvc.perform(post("/api/budgets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "categoryId": "%s",
                                        "amount": 500.00,
                                        "period": "MONTHLY",
                                        "startDate": "2026-04-01",
                                        "endDate": "2026-04-30"
                                    }
                                    """, categoria.getId())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.categoryId").value(categoria.getId().toString()))
                    .andExpect(jsonPath("$.amount").value(500.00))
                    .andExpect(jsonPath("$.period").value("MONTHLY"))
                    .andExpect(jsonPath("$.spentAmount").exists())
                    .andExpect(jsonPath("$.remainingAmount").exists());
        }

        @Test
        @DisplayName("Retorna 401 sem token de autenticacao")
        void create_noToken_returns401() throws Exception {
            mockMvc.perform(post("/api/budgets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"categoryId":"00000000-0000-0000-0000-000000000001","amount":100,"period":"MONTHLY","startDate":"2026-04-01","endDate":"2026-04-30"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Retorna 422 quando campos obrigatorios ausentes")
        void create_missingFields_returns422() throws Exception {
            mockMvc.perform(post("/api/budgets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"amount": 500.00}
                                    """))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Retorna 404 quando categoria nao pertence ao usuario")
        void create_invalidCategory_returns404() throws Exception {
            mockMvc.perform(post("/api/budgets")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "categoryId": "%s",
                                        "amount": 500.00,
                                        "period": "MONTHLY",
                                        "startDate": "2026-04-01",
                                        "endDate": "2026-04-30"
                                    }
                                    """, UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/budgets")
    class ListAll {

        @Test
        @DisplayName("Retorna 200 com pagina de orcamentos do usuario")
        void listAll_returnsUserBudgets() throws Exception {
            criarBudget();

            mockMvc.perform(get("/api/budgets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Nao retorna orcamentos de outro usuario")
        void listAll_doesNotReturnOtherUsersBudgets() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro")
                    .email("outro_budget@test.com")
                    .passwordHash(passwordEncoder.encode("senha123"))
                    .role(Role.USER)
                    .build());
            Category catOutro = categoryRepository.save(Category.builder()
                    .userId(outro.getId()).name("Cat Outro").build());
            budgetRepository.save(Budget.builder()
                    .userId(outro.getId()).categoryId(catOutro.getId())
                    .amount(new BigDecimal("100.00")).period(BudgetPeriod.MONTHLY)
                    .startDate(java.time.LocalDate.of(2026, 4, 1))
                    .endDate(java.time.LocalDate.of(2026, 4, 30))
                    .build());

            mockMvc.perform(get("/api/budgets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/budgets/{id}")
    class FindById {

        @Test
        @DisplayName("Retorna 200 com dados do orcamento")
        void findById_found_returns200() throws Exception {
            Budget budget = criarBudget();

            mockMvc.perform(get("/api/budgets/" + budget.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(budget.getId().toString()));
        }

        @Test
        @DisplayName("Retorna 404 quando orcamento nao existe")
        void findById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/budgets/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/budgets/{id}")
    class Delete {

        @Test
        @DisplayName("Retorna 204 No Content ao deletar orcamento")
        void delete_found_returns204() throws Exception {
            Budget budget = criarBudget();

            mockMvc.perform(delete("/api/budgets/" + budget.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Retorna 404 quando orcamento nao encontrado")
        void delete_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/budgets/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
