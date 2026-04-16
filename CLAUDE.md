# CLAUDE.md - FinTrack API

## Estado Atual

- Fase funcional atual: backend consolidado + frontend operacional conectado
- Validacao backend mais recente: `152` testes passando em `.\scripts\mvn-java21.ps1 clean test`
- Validacao frontend mais recente: `npm run build` concluido em `fintrack-web`
- Script recomendado para execucao local do Maven: `scripts/mvn-java21.ps1`

## O que foi concluido

- autenticacao com refresh token
- contas, categorias e transacoes com CRUD completo no backend
- budgets, recorrencia e relatorios
- `PagedResponse` para paginacao estavel
- indices e ajustes de performance
- CORS para integracao com o frontend
- refresh automatico de token no frontend
- roteamento `/` e `/dashboard` no frontend
- CRUD visual para contas, categorias e transacoes no frontend
- build Maven estabilizado em Windows com caminho acentuado por compilacao forkada e `UTF-8`
- alertas de budget sem reenvio repetitivo por threshold (`80%` e `100%`)
- indice PostgreSQL de descricao alinhado ao filtro `lower(description) LIKE 'prefix%'`

## Arquivos mais relevantes desta fase

- `pom.xml`
- `scripts/mvn-java21.ps1`
- `src/main/java/com/fintrack/api/shared/config/SecurityConfig.java`
- `src/main/java/com/fintrack/api/shared/dto/PagedResponse.java`
- `src/main/java/com/fintrack/api/account/**`
- `src/main/java/com/fintrack/api/category/**`
- `src/main/java/com/fintrack/api/transaction/**`
- `../fintrack-web/lib/api.ts`
- `../fintrack-web/hooks/use-fintrack-session.ts`
- `../fintrack-web/proxy.ts`
- `../fintrack-web/components/auth-page.tsx`
- `../fintrack-web/components/dashboard-page.tsx`

## Problemas Conhecidos

- busca por descricao ainda usa prefixo; nao ha trigram ou full text de PostgreSQL
- alertas de budget ainda nao usam fila nem retentativa de e-mail
- budgets ainda nao tem CRUD visual no frontend
- falta empacotamento de deploy do projeto completo
- aviso do Spring Security sobre `AuthenticationProvider` e `UserDetailsService` ainda existe, mas nao bloqueia build nem testes

## Proxima etapa sugerida

Ordem recomendada:

1. Dockerizacao do backend e banco
2. documentacao OpenAPI mais rica
3. CRUD de budgets no frontend
4. deploy em ambiente publico

Roadmap detalhado em `../docs/NEXT-STEPS.md`.
