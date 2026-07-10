package com.proofpay.common.audit;

import com.proofpay.transaction.domain.TransactionEvent;
import com.proofpay.transaction.infrastructure.TransactionEventRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Point d'entrée unique pour journaliser tout changement de statut ou action
 * sensible (règle métier #13 : chaque changement de statut doit être
 * enregistré dans un journal d'audit).
 */
@Component
public class AuditLogger {

    private final TransactionEventRepository eventRepository;

    public AuditLogger(TransactionEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void logTransactionEvent(UUID transactionId, String eventType,
                                     String previousStatus, String newStatus,
                                     UUID actorUserId, Map<String, Object> payload) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(transactionId)
                .eventType(eventType)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .actorUserId(actorUserId)
                .payload(payload)
                .createdAt(Instant.now())
                .build();
        eventRepository.save(event);
    }
}
