package com.fintrack.api.account.controller;

import com.fintrack.api.account.entity.Account;
import com.fintrack.api.account.entity.AccountType;
import com.fintrack.api.account.repository.AccountRepository;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
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
class AccountControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private String token;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste")
                .email("conta@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);
    }

    private Account criarConta(String nome, AccountType tipo) {
        return accountRepository.save(Account.builder()
                .userId(usuario.getId())
                .name(nome)
                .type(tipo)
                .balance(BigDecimal.ZERO)
                .currency("BRL")
                .build());
    }

    // ── POST /api/accounts ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/accounts")
    class Create {

        @Test
        @DisplayName("Retorna 201 Created com dados da conta")
        void create_validRequest_returns201() throws Exception {
            mockMvc.perform(post("/api/accounts")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Nubank",
                                        "type": "CHECKING",
                                        "balance": 0,
                                        "currency": "BRL"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Nubank"))
                    .andExpect(jsonPath("$.type").value("CHECKING"))
                    .andExpect(jsonPath("$.currency").value("BRL"));
        }

        @Test
        @DisplayName("Retorna 422 quando nome está em branco")
        void create_blankName_returns422() throws Exception {
            mockMvc.perform(post("/api/accounts")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "",
                                        "type": "CHECKING",
                                        "currency": "BRL"
                                    }
                                    """))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Retorna 401 sem token de autenticação")
        void create_noToken_returns401() throws Exception {
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"X","type":"CHECKING","currency":"BRL"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /api/accounts ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/accounts")
    class ListAll {

        @Test
        @DisplayName("Retorna 200 com lista de contas do usuário")
        void listAll_returnsUserAccounts() throws Exception {
            criarConta("Nubank", AccountType.CHECKING);
            criarConta("Poupança", AccountType.SAVINGS);

            mockMvc.perform(get("/api/accounts")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Não retorna contas de outro usuário")
        void listAll_doesNotReturnOtherUsersAccounts() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro")
                    .email("outro@test.com")
                    .passwordHash(passwordEncoder.encode("senha123"))
                    .role(Role.USER)
                    .build());
            accountRepository.save(Account.builder()
                    .userId(outro.getId())
                    .name("Conta alheia")
                    .type(AccountType.CHECKING)
                    .balance(BigDecimal.ZERO)
                    .currency("BRL")
                    .build());

            mockMvc.perform(get("/api/accounts")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /api/accounts/{id} ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/accounts/{id}")
    class FindById {

        @Test
        @DisplayName("Retorna 200 com dados da conta")
        void findById_found_returns200() throws Exception {
            Account conta = criarConta("Nubank", AccountType.CHECKING);

            mockMvc.perform(get("/api/accounts/" + conta.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nubank"));
        }

        @Test
        @DisplayName("Retorna 404 quando conta não existe")
        void findById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/accounts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ── PUT /api/accounts/{id} ────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/accounts/{id}")
    class Update {

        @Test
        @DisplayName("Retorna 200 com dados atualizados")
        void update_validRequest_returns200() throws Exception {
            Account conta = criarConta("Nubank", AccountType.CHECKING);

            mockMvc.perform(put("/api/accounts/" + conta.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Nubank Premium","type":"SAVINGS","currency":"BRL"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nubank Premium"))
                    .andExpect(jsonPath("$.type").value("SAVINGS"));
        }

        @Test
        @DisplayName("Retorna 404 ao tentar atualizar conta de outro usuário")
        void update_otherUsersAccount_returns404() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro")
                    .email("outro2@test.com")
                    .passwordHash(passwordEncoder.encode("senha123"))
                    .role(Role.USER)
                    .build());
            Account contaAlheia = accountRepository.save(Account.builder()
                    .userId(outro.getId())
                    .name("Conta alheia")
                    .type(AccountType.CHECKING)
                    .balance(BigDecimal.ZERO)
                    .currency("BRL")
                    .build());

            mockMvc.perform(put("/api/accounts/" + contaAlheia.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Hack","type":"CHECKING","currency":"BRL"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE /api/accounts/{id} ─────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/accounts/{id}")
    class Delete {

        @Test
        @DisplayName("Retorna 204 No Content ao deletar conta sem transações")
        void delete_noTransactions_returns204() throws Exception {
            Account conta = criarConta("Para Deletar", AccountType.CASH);

            mockMvc.perform(delete("/api/accounts/" + conta.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Retorna 404 quando conta não encontrada")
        void delete_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/accounts/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
