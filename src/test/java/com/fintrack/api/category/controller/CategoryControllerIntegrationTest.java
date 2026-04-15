package com.fintrack.api.category.controller;

import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.auth.security.JwtService;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User usuario;
    private String token;

    @BeforeEach
    void setUp() {
        usuario = userRepository.save(User.builder()
                .name("Teste")
                .email("cat@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .build());
        token = jwtService.generateAccessToken(usuario);
    }

    private Category criarCategoria(String nome, UUID parentId) {
        return categoryRepository.save(Category.builder()
                .userId(usuario.getId())
                .parentId(parentId)
                .name(nome)
                .icon("🍔")
                .color("#FF5733")
                .build());
    }

    // ── POST /api/categories ──────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/categories")
    class Create {

        @Test
        @DisplayName("Retorna 201 Created com dados da categoria")
        void create_rootCategory_returns201() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Alimentação","icon":"🍔","color":"#FF5733"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Alimentação"))
                    .andExpect(jsonPath("$.color").value("#FF5733"));
        }

        @Test
        @DisplayName("Retorna 404 quando parentId não existe")
        void create_invalidParentId_returns404() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"parentId":"%s","name":"Sub"}
                                    """.formatted(UUID.randomUUID())))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Retorna 422 quando nome está em branco")
        void create_blankName_returns422() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":""}
                                    """))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Retorna 401 sem token")
        void create_noToken_returns401() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"Alimentação"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /api/categories ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/categories")
    class ListAll {

        @Test
        @DisplayName("Retorna 200 com lista de categorias do usuário")
        void listAll_returnsUserCategories() throws Exception {
            criarCategoria("Alimentação", null);
            criarCategoria("Transporte", null);

            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Não retorna categorias de outro usuário")
        void listAll_doesNotReturnOtherUsersCategories() throws Exception {
            User outro = userRepository.save(User.builder()
                    .name("Outro")
                    .email("outro@cat.com")
                    .passwordHash(passwordEncoder.encode("senha123"))
                    .role(Role.USER)
                    .build());
            categoryRepository.save(Category.builder()
                    .userId(outro.getId())
                    .name("Categoria alheia")
                    .build());

            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /api/categories/{id} ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/categories/{id}")
    class FindById {

        @Test
        @DisplayName("Retorna 200 com dados da categoria")
        void findById_found_returns200() throws Exception {
            Category cat = criarCategoria("Alimentação", null);

            mockMvc.perform(get("/api/categories/" + cat.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Alimentação"));
        }

        @Test
        @DisplayName("Retorna 404 quando não existe")
        void findById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/categories/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ── DELETE /api/categories/{id} ───────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class Delete {

        @Test
        @DisplayName("Retorna 204 ao deletar categoria sem dependências")
        void delete_noDependencies_returns204() throws Exception {
            Category cat = criarCategoria("Para Deletar", null);

            mockMvc.perform(delete("/api/categories/" + cat.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Retorna 422 ao deletar categoria com subcategorias ativas")
        void delete_hasActiveChildren_returns422() throws Exception {
            Category pai = criarCategoria("Pai", null);
            criarCategoria("Filho", pai.getId());

            mockMvc.perform(delete("/api/categories/" + pai.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Retorna 404 ao deletar categoria não encontrada")
        void delete_notFound_returns404() throws Exception {
            mockMvc.perform(delete("/api/categories/" + UUID.randomUUID())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
