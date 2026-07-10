package com.proofpay.notification.application;

import com.proofpay.notification.domain.Notification;
import com.proofpay.notification.domain.NotificationChannel;
import com.proofpay.notification.infrastructure.NotificationRepository;
import com.proofpay.notification.provider.NotificationSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Envoi asynchrone des notifications transactionnelles (§11 spécifications
 * techniques). Règle métier #23 : les notifications doivent être envoyées
 * même en cas d'échec d'une opération -> l'échec d'envoi n'interrompt jamais
 * le flux métier appelant (capturé et journalisé localement).
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<String, NotificationSender> sendersByChannel;

    public NotificationService(NotificationRepository notificationRepository,
                               List<NotificationSender> senders) {
        this.notificationRepository = notificationRepository;
        this.sendersByChannel = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, Function.identity()));
    }

    /**
     * Méthode synchrone pour les notifications critiques.
     * Utilisée lorsque la transaction doit être garantie avant l'envoi.
     *
     * @param userId ID de l'utilisateur destinataire
     * @param transactionId ID de la transaction concernée
     * @param channel Canal de notification (SMS, EMAIL, etc.)
     * @param templateCode Code du template de message
     * @param destination Destinataire (numéro de téléphone, email, etc.)
     * @param renderedMessage Message rendu à envoyer
     */
    @Transactional
    public void notifySync(UUID userId, UUID transactionId, NotificationChannel channel,
                           String templateCode, String destination, String renderedMessage) {
        // Créer et sauvegarder la notification
        Notification notification = Notification.builder()
                .userId(userId)
                .transactionId(transactionId)
                .channel(channel)
                .templateCode(templateCode)
                .destination(destination)
                .status("PENDING")
                .createdAt(Instant.now())
                .build();
        notification = notificationRepository.save(notification);

        try {
            // Envoyer via le provider approprié
            NotificationSender sender = sendersByChannel.get(channel.name());
            if (sender == null) {
                throw new IllegalStateException("Aucun sender trouvé pour le canal: " + channel);
            }
            String providerMessageId = sender.send(destination, templateCode, renderedMessage);

            // Mettre à jour le statut
            notification.setStatus("SENT");
            notification.setProviderMessageId(providerMessageId);
            notification.setSentAt(Instant.now());
        } catch (Exception e) {
            // En cas d'échec, marquer comme échoué
            notification.setStatus("FAILED");
            // Optionnel: stocker l'erreur dans un champ dédié
        }
        notificationRepository.save(notification);
    }

    /**
     * Méthode asynchrone pour les notifications non critiques.
     * L'échec d'envoi n'interrompt pas le flux principal.
     * Cette méthode est annotée @Async et s'exécute dans un thread séparé.
     *
     * @param userId ID de l'utilisateur destinataire
     * @param transactionId ID de la transaction concernée
     * @param channel Canal de notification (SMS, EMAIL, etc.)
     * @param templateCode Code du template de message
     * @param destination Destinataire (numéro de téléphone, email, etc.)
     * @param renderedMessage Message rendu à envoyer
     */
    @Async
    @Transactional
    public void notify(UUID userId, UUID transactionId, NotificationChannel channel,
                       String templateCode, String destination, String renderedMessage) {
        // Délègue à la méthode synchrone pour éviter la duplication de code
        notifySync(userId, transactionId, channel, templateCode, destination, renderedMessage);
    }
}