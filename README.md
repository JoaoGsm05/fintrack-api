# FinTrack API

API REST do FinTrack construída com Spring Boot.

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- Flyway
- H2 / PostgreSQL
- MapStruct
- Lombok

## Execução

### Testes

```powershell
.\scripts\mvn-java21.ps1 clean test
```

### Subir a API em dev

```powershell
.\scripts\mvn-java21.ps1 spring-boot:run -Dspring-boot.run.profiles=dev
```

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `CRUD /api/accounts`
- `CRUD /api/categories`
- `CRUD /api/transactions`
- `CRUD /api/budgets`
- `CRUD /api/recurring`
- `GET /api/reports/expenses-by-category`
- `GET /api/reports/export-expenses-csv`

## Estado atual

- suíte com `149` testes passando
- paginação estável com `PagedResponse`
- índices e ajustes de performance aplicados
- build estabilizado para Windows com caminho acentuado

Detalhes adicionais em [../docs/PROJECT-STATUS.md](../docs/PROJECT-STATUS.md).
