# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Project foundation with Spring Boot 3.3.5 and Java 21.
- PostgreSQL and H2 (dev/test) configuration.
- JPA Auditing with `BaseEntity` for automatic `createdAt` and `updatedAt`.
- User entity with UUID and Role-based security.
- Flyway migration for `users` table.
- JWT Authentication (Access + Refresh Tokens).
- Spring Security configuration with stateless session policy.
- Global Exception Handling using ProblemDetail (RFC 7807).
- Auth Controller with `/auth/register` and `/auth/login` endpoints.
- Java Records for DTOs (RegisterRequest, LoginRequest, AuthenticationResponse).
