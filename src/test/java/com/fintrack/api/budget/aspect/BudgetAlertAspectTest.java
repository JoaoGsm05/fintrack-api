package com.fintrack.api.budget.aspect;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.entity.Role;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.entity.BudgetPeriod;
import com.fintrack.api.budget.service.BudgetService;
import com.fintrack.api.shared.service.EmailService;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetAlertAspectTest {

    @Mock private BudgetService budgetService;
    @Mock private EmailService emailService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BudgetAlertAspect budgetAlertAspect;

    private UUID userId;
    private UUID categoryId;
    private UUID budgetId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Teste")
                .email("teste@fintrack.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("checkBudgetAfterTransaction()")
    class CheckBudgetAfterTransaction {

        @Test
        @DisplayName("Dispara alerta de 80% apenas na primeira vez que o threshold e marcado")
        void sends80PercentAlertOnlyOnce() {
            TransactionRequest request = new TransactionRequest(
                    UUID.randomUUID(),
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("50.00"),
                    "Mercado",
                    LocalDate.of(2026, 4, 16)
            );
            BudgetResponse budget = new BudgetResponse(
                    budgetId,
                    userId,
                    categoryId,
                    "Alimentacao",
                    new BigDecimal("100.00"),
                    BudgetPeriod.MONTHLY,
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 30),
                    new BigDecimal("80.00"),
                    new BigDecimal("20.00")
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(budgetService.listAllByCategory(userId, categoryId)).thenReturn(List.of(budget));
            when(budgetService.markAlert80AsSent(userId, budgetId)).thenReturn(true);

            budgetAlertAspect.checkBudgetAfterTransaction(userId, request);

            verify(budgetService).markAlert80AsSent(userId, budgetId);
            verify(emailService).sendSimpleMessage(
                    user.getEmail(),
                    "[FinTrack] Alerta de Orcamento (80%)",
                    "Ola " + user.getName() + ",\n\nAtencao! Voce ja utilizou 80% do seu orcamento planejado para Alimentacao."
                            + "\n\nLimite: R$ 100.00\nUtilizado: R$ 80.00"
            );
        }

        @Test
        @DisplayName("Nao reenvia alerta de 80% quando o threshold ja foi registrado")
        void doesNotResend80PercentAlertWhenAlreadyMarked() {
            TransactionRequest request = new TransactionRequest(
                    UUID.randomUUID(),
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("50.00"),
                    "Mercado",
                    LocalDate.of(2026, 4, 16)
            );
            BudgetResponse budget = new BudgetResponse(
                    budgetId,
                    userId,
                    categoryId,
                    "Alimentacao",
                    new BigDecimal("100.00"),
                    BudgetPeriod.MONTHLY,
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 30),
                    new BigDecimal("85.00"),
                    new BigDecimal("15.00")
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(budgetService.listAllByCategory(userId, categoryId)).thenReturn(List.of(budget));
            when(budgetService.markAlert80AsSent(userId, budgetId)).thenReturn(false);

            budgetAlertAspect.checkBudgetAfterTransaction(userId, request);

            verify(budgetService).markAlert80AsSent(userId, budgetId);
            verify(emailService, never()).sendSimpleMessage(any(), any(), any());
        }

        @Test
        @DisplayName("Dispara alerta de 100% mesmo que o de 80% ja tenha ocorrido antes")
        void sends100PercentAlertIndependently() {
            TransactionRequest request = new TransactionRequest(
                    UUID.randomUUID(),
                    categoryId,
                    TransactionType.EXPENSE,
                    new BigDecimal("50.00"),
                    "Mercado",
                    LocalDate.of(2026, 4, 16)
            );
            BudgetResponse budget = new BudgetResponse(
                    budgetId,
                    userId,
                    categoryId,
                    "Alimentacao",
                    new BigDecimal("100.00"),
                    BudgetPeriod.MONTHLY,
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 30),
                    new BigDecimal("100.00"),
                    BigDecimal.ZERO
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(budgetService.listAllByCategory(userId, categoryId)).thenReturn(List.of(budget));
            when(budgetService.markAlert100AsSent(userId, budgetId)).thenReturn(true);

            budgetAlertAspect.checkBudgetAfterTransaction(userId, request);

            verify(budgetService).markAlert100AsSent(userId, budgetId);
            verify(emailService).sendSimpleMessage(
                    user.getEmail(),
                    "[FinTrack] LIMITE ATINGIDO (100%)",
                    "Ola " + user.getName() + ",\n\nVoce atingiu 100% do seu orcamento para a categoria Alimentacao."
                            + "\n\nLimite: R$ 100.00\nUtilizado: R$ 100.00"
            );
            verify(budgetService, never()).markAlert80AsSent(userId, budgetId);
        }
    }
}
