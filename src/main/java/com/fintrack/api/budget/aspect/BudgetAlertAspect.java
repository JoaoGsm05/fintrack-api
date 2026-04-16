package com.fintrack.api.budget.aspect;

import com.fintrack.api.auth.entity.User;
import com.fintrack.api.auth.repository.UserRepository;
import com.fintrack.api.budget.dto.BudgetResponse;
import com.fintrack.api.budget.service.BudgetService;
import com.fintrack.api.shared.service.EmailService;
import com.fintrack.api.transaction.dto.TransactionRequest;
import com.fintrack.api.transaction.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertAspect {

    private final BudgetService budgetService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @AfterReturning(
            pointcut = "execution(* com.fintrack.api.transaction.service.TransactionService.create(..)) && args(userId, request)",
            argNames = "userId,request"
    )
    public void checkBudgetAfterTransaction(UUID userId, TransactionRequest request) {
        if (request.type() != TransactionType.EXPENSE || request.categoryId() == null) {
            return;
        }

        userRepository.findById(userId).ifPresent(user ->
                budgetService.listAllByCategory(userId, request.categoryId())
                        .forEach(budget -> evaluateAlert(user, budget))
        );
    }

    private void evaluateAlert(User user, BudgetResponse budget) {
        if (budget.amount().signum() <= 0) {
            return;
        }

        double usageRatio = budget.spentAmount().doubleValue() / budget.amount().doubleValue();

        try {
            if (usageRatio >= 1.0) {
                if (budgetService.markAlert100AsSent(user.getId(), budget.id())) {
                    sendAlert(user, budget, "LIMITE ATINGIDO (100%)",
                            "Voce atingiu 100% do seu orcamento para a categoria " + budget.categoryName() + ".");
                }
            } else if (usageRatio >= 0.8 && budgetService.markAlert80AsSent(user.getId(), budget.id())) {
                sendAlert(user, budget, "Alerta de Orcamento (80%)",
                        "Atencao! Voce ja utilizou 80% do seu orcamento planejado para " + budget.categoryName() + ".");
            }
        } catch (Exception e) {
            log.warn("Falha ao disparar alerta de orcamento para o usuario {} e budget {}",
                    user.getId(), budget.id(), e);
        }
    }

    private void sendAlert(User user, BudgetResponse budget, String subject, String message) {
        log.info("Enviando alerta de orcamento para o usuario: {}", user.getEmail());
        emailService.sendSimpleMessage(
                user.getEmail(),
                "[FinTrack] " + subject,
                "Ola " + user.getName() + ",\n\n" + message + "\n\nLimite: R$ " + budget.amount()
                        + "\nUtilizado: R$ " + budget.spentAmount()
        );
    }
}
