# CLAUDE.md — FinTrack API

## Estado Atual
- **Fase:** 1 concluída → iniciando Fase 2
- **Última tarefa concluída:** Testes unitários (JwtService, AuthService) + testes de integração (AuthController, GlobalExceptionHandler) — 38/38 passando (2026-04-15)
- **Próxima tarefa:** Fase 2 — Passo 1: Migration V2 + Entidades Account, Category, Transaction
- **Testes passando:** 38/38 (mvn clean test — BUILD SUCCESS 2026-04-15)

## Decisões Tomadas Nesta Sessão
- 2026-04-15 — Inicialização da memória do projeto via CLAUDE.md.
- 2026-04-15 — Definição de foco no Spring Security + JWT após setup de base.
- 2026-04-15 — Criação de BaseEntity (auditoria JPA), JpaConfig (@EnableJpaAuditing + AuditorAware), Role (enum), User (entidade com soft delete + UserDetails), UserRepository, migration V1 (H2/PostgreSQL compatível), SecurityConfig mínima (stateless, CSRF off, rotas públicas liberadas).
- 2026-04-15 — Records Java 21 para todos os DTOs (imutáveis, sem boilerplate)
- 2026-04-15 — ProblemDetail RFC 7807 nativo do Spring 6 (sem biblioteca extra)
- 2026-04-15 — `AuthService.refresh()` sempre retorna novo par access+refresh (refresh token rotation)
- 2026-04-15 — `GlobalExceptionHandler` captura `Exception` genérica para nunca vazar stacktrace
- 2026-04-15 — Testes de integração com `@SpringBootTest` + H2 + Flyway (sem Testcontainers na Fase 1 — PostgreSQL-specific features não usadas ainda)
- 2026-04-15 — `@Transactional` nos testes de integração para rollback automático (banco limpo entre testes)
- 2026-04-15 — `@WebMvcTest` para GlobalExceptionHandler (isola a camada web sem subir contexto completo)

## Problemas Conhecidos
- `@WebMvcTest` com `SecurityConfig` que injeta `JwtAuthenticationFilter` exige `@MockBean JwtService` e `@MockBean UserDetailsService` no teste — resolvido em GlobalExceptionHandlerTest.
- `mvn test` (incremental) pode ter falso erro de compilação por cache de `.class`; usar `mvn clean test` em caso de dúvida.

## Débitos Técnicos
- Configurar `JWT_SECRET` via variáveis de ambiente para evitar o uso do default em produção.

## Dependências Atuais
- Spring Boot Starter (Web, Data JPA, Security, Validation, Cache, AOP, Test)
- SpringDoc OpenAPI 2.6.0
- Flyway (Core + PostgreSQL)
- H2 Database / PostgreSQL Driver
- JJWT 0.12.6
- Lombok 1.18.34
- MapStruct 1.6.2
- Caffeine 3.1.8

## Próxima Fase — Fase 2 (ordem obrigatória)
1. `V2__create_core_tables.sql` — tabelas: accounts, categories, transactions
2. Entidades JPA: `Account`, `Category`, `Transaction` (herdam BaseEntity, com deletedAt)
3. Repositories: `AccountRepository`, `CategoryRepository`, `TransactionRepository` (+ JpaSpecificationExecutor)
4. DTOs (records): `XxxRequest`, `XxxUpdateRequest`, `XxxResponse` + MapStruct mappers
5. `TransactionSpecification` — filtros dinâmicos por data, categoria, conta, tipo, valor, descrição
6. Services: `AccountService`, `CategoryService`, `TransactionService` (sempre filtrar por userId do JWT)
7. Controllers: `AccountController`, `CategoryController`, `TransactionController`
8. Novas exceções: `ResourceNotFoundException` (404), `BusinessRuleException` (422) + handlers no GlobalExceptionHandler
9. Testes: unitários por service + integração por controller

## Comandos Úteis
- `mvn clean test` — roda todos os testes (usar sempre `clean` para evitar falso erro de cache)
- `mvn spring-boot:run -Pdev` — roda com perfil dev (H2)

## Ambiente de Desenvolvimento
- JDK 21: bundled no VS Code Extension (`~/.vscode/extensions/redhat.java-1.54.0-win32-x64/jre/21.0.10-win32-x86_64`)
- Maven: via Wrapper `./mvnw` (Maven 3.9.9 baixado em `~/.m2/wrapper/`)
- `mvn` não está no PATH do sistema — usar sempre via Wrapper
