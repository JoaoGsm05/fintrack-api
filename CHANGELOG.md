# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added — Fase 2: Core de Negócio (2026-04-15)
- `ResourceNotFoundException` (404) e `BusinessRuleException` (422) com ProblemDetail RFC 7807.
- `GlobalExceptionHandler`: handler para `AccessDeniedException` (403) e exceções novas de domínio.
- `SecurityConfig`: `HttpStatusEntryPoint(401)` para requisições sem token (sem redirect para login).
- `AccountService`, `CategoryService`, `TransactionService`: CRUD completo com soft delete e validações:
  - AccountService: bloqueia exclusão de conta com transações ativas.
  - CategoryService: bloqueia exclusão de categoria-pai com filhos ou transações.
  - TransactionService: recalcula saldo via `calculateBalanceByAccountId()` em todo CRUD.
- `AccountController`, `CategoryController`, `TransactionController`: endpoints REST completos.
  - Todos exigem autenticação; userId extraído via `@AuthenticationPrincipal`.
  - `GET /api/transactions` com paginação (`Pageable` + `TransactionFilterRequest`).
- 18 testes unitários (Mockito) + 36 testes de integração (@SpringBootTest + H2).
- **97/97 testes passando — BUILD SUCCESS.**

### Added — Fase 2: Estrutura de Domínio (2026-04-15)
- Migration `V2__create_core_tables.sql`: tabelas `accounts`, `categories`, `transactions` com índices e soft delete.
- Enums: `AccountType` (CHECKING, SAVINGS, CREDIT, CASH, INVESTMENT), `TransactionType` (INCOME, EXPENSE).
- Entidades JPA: `Account`, `Category`, `Transaction` (herdam `BaseEntity`, soft delete via `deletedAt`).
- Repositories com `JpaSpecificationExecutor`; `calculateBalanceByAccountId` JPQL no `TransactionRepository`.
- DTOs como records Java 21: Request, UpdateRequest, Response por domínio + `TransactionFilterRequest`.
- MapStruct mappers: `AccountMapper`, `CategoryMapper`, `TransactionMapper`.
- `TransactionSpecification`: 10 filtros dinâmicos com null-safe (userId, tipo, conta, categoria, datas, valor, descrição).
- Fix: `lombok-mapstruct-binding:0.2.0` adicionado para corrigir bug com records Java 21 + MapStruct 1.6.2.

### Added — Fase 1: Autenticação e Fundação (2026-04-15)
- Project foundation with Spring Boot 3.3.5 and Java 21.
- PostgreSQL and H2 (dev/test) configuration with Flyway migrations.
- JPA Auditing with `BaseEntity` for automatic `createdAt` and `updatedAt`.
- User entity with UUID and Role-based security.
- Flyway migration `V1` for `users` table.
- JWT Authentication (Access + Refresh Tokens with rotation).
- Spring Security stateless configuration.
- Global Exception Handling using ProblemDetail (RFC 7807).
- Auth Controller: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`.
- Java Records for all DTOs (immutable, zero boilerplate).
- Maven Wrapper (`mvnw`) adicionado ao projeto.
