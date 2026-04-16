package com.fintrack.api.recurring.scheduler;

import com.fintrack.api.recurring.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    /**
     * Roda todos os dias às 01:00 da manhã para processar transações recorrentes.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void runRecurringTransactions() {
        log.info("Iniciando processamento de transações recorrentes...");
        try {
            recurringTransactionService.processRecurringTransactions();
            log.info("Processamento de transações recorrentes finalizado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao processar transações recorrentes: ", e);
        }
    }
}
