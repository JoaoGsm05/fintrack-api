# CLAUDE.md — FinTrack API

## Estado Atual
- **Fase:** 2 em progresso — Passos 1–5 concluídos (Migration, Entidades, Repositories, DTOs, Specification)
- **Última tarefa concluída:** Fase 2 Passos 1–5 + fix MapStruct/Lombok records — 38/38 testes passando (2026-04-15)
- **Próxima tarefa:** Fase 2 — Passo 6: Exceções novas (ResourceNotFoundException, BusinessRuleException) + Services (AccountService, CategoryService, TransactionService)
- **Testes passando:** 38/38 (mvn clean test — BUILD SUCCESS 2026-04-15)

## Decisões Tomadas Nesta Sessão
- 2026-04-15 — Inicialização da memória do projeto via CLAUDE.md.
- 2026-04-15 — Definição de foco no Spring Security + JWT após setup de base.
- 2026-04-15 — Criação de BaseEntity (auditoria JPA), JpaConfig, Role, User, UserRepository, migration V1, SecurityConfig mínima.
- 2026-04-15 — Records Java 21 para todos os DTOs (imutáveis, sem boilerplate)
- 2026-04-15 — ProblemDetail RFC 7807 nativo do Spring 6 (sem biblioteca extra)
- 2026-04-15 — AuthService.refresh() sempre retorna novo par access+refresh (refresh token rotation)
- 2026-04-15 — GlobalExceptionHandler captura Exception genérica para nunca vazar stacktrace
- 2026-04-15 — Testes de integração com @SpringBootTest + H2 + Flyway (sem Testcontainers na Fase 1)
- 2026-04-15 — @Transactional nos testes de integração para rollback automático
- 2026-04-15 — @WebMvcTest para GlobalExceptionHandler (isola a camada web)
- 2026-04-15 — Fase 2: Migration V2 (accounts, categories, transactions) com índices, FK constraints, soft delete via deleted_at TIMESTAMP
- 2026-04-15 — Fase 2: Entidades com UUID como chave FK (não @ManyToOne) — serviços fazem lookup manual para evitar N+1
- 2026-04-15 — Fase 2: JpaSpecificationExecutor em todos os repositories para filtros dinâmicos
- 2026-04-15 — Fase 2: TransactionRepository inclui @Query JPQL para recalcular saldo (calculateBalanceByAccountId)
- 2026-04-15 — Fase 2: CategoryRepository expõe existsByParentIdAndDeletedAtIsNull para bloquear exclusão de categoria-pai com filhos
- 2026-04-15 — Fase 2: TransactionSpecification — cada filtro retorna null quando parâmetro é nulo (Spring Data ignora nulls)
- 2026-04-15 — fix: lombok-mapstruct-binding:0.2.0 adicionado ao annotationProcessorPaths para corrigir bug onde MapStruct 1.6.2 strip construtores canônicos de records Java 21

## Problemas Conhecidos
- @WebMvcTest com SecurityConfig que injeta JwtAuthenticationFilter exige @MockBean JwtService e @MockBean UserDetailsService no teste — resolvido em GlobalExceptionHandlerTest.
- mvn test (incremental) pode ter falso erro de compilação por cache de .class; usar mvn clean test em caso de dúvida.
- VS Code Java Extension pode travar arquivos em target/ — se "error while writing User$UserBuilder.class" aparecer, rodar mvn clean test novamente.

## Débitos Técnicos
- Configurar JWT_SECRET via variáveis de ambiente para evitar o uso do default em produção.

## Dependências Atuais
- Spring Boot Starter (Web, Data JPA, Security, Validation, Cache, AOP, Test)
- SpringDoc OpenAPI 2.6.0
- Flyway (Core + PostgreSQL)
- H2 Database / PostgreSQL Driver
- JJWT 0.12.6
- Lombok 1.18.34
- MapStruct 1.6.2
- lombok-mapstruct-binding 0.2.0  ← adicionado na Fase 2 para records
- Caffeine 3.1.8

## Fase 2 — Progresso

### Concluído
1. ✅ `V2__create_core_tables.sql` — tabelas: accounts, categories, transactions (+ índices)
2. ✅ Enums: `AccountType` (CHECKING, SAVINGS, CREDIT, CASH, INVESTMENT), `TransactionType` (INCOME, EXPENSE)
3. ✅ Entidades JPA: `Account`, `Category`, `Transaction` (herdam BaseEntity, soft delete via deletedAt)
4. ✅ Repositories: `AccountRepository`, `CategoryRepository`, `TransactionRepository` (+ JpaSpecificationExecutor)
5. ✅ DTOs (records): Request/UpdateRequest/Response para Account, Category, Transaction + TransactionFilterRequest
6. ✅ MapStruct mappers: `AccountMapper`, `CategoryMapper`, `TransactionMapper`
7. ✅ `TransactionSpecification` — 10 filtros dinâmicos (userId, deletedAt, date range, category, account, type, amount range, description LIKE)

### Pendente (próxima sessão)
8. ⏳ Exceções novas: `ResourceNotFoundException` (404), `BusinessRuleException` (422) + handlers no GlobalExceptionHandler
9. ⏳ Services: `AccountService`, `CategoryService`, `TransactionService` (sempre filtrar por userId do JWT)
10. ⏳ Controllers: `AccountController`, `CategoryController`, `TransactionController`
11. ⏳ Testes: unitários por service + integração por controller

## Fase 3 (futura)
- Budget, RecurringTransaction, Cache, Relatórios

## Comandos Úteis
- `mvn clean test` — roda todos os testes (usar sempre `clean` para evitar falso erro de cache)
- `mvn spring-boot:run -Pdev` — roda com perfil dev (H2)

## Ambiente de Desenvolvimento
- JDK 21: bundled no VS Code Extension (`~/.vscode/extensions/redhat.java-1.54.0-win32-x64/jre/21.0.10-win32-x86_64`)
- Maven: via Wrapper `./mvnw` (Maven 3.9.9 baixado em `~/.m2/wrapper/`)
- `mvn` não está no PATH do sistema — usar sempre via Wrapper
- VS Code pode travar arquivos em `target/` — se ocorrer, fechar VS Code ou rodar mvn novamente
