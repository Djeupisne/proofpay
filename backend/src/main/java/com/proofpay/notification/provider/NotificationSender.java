package com.proofpay.notification.provider;

/**
 * Interface commune aux canaux d'envoi (SMS, e-mail, WhatsApp)
 * — §11 spécifications techniques.
 */
public interface NotificationSender {
    String getChannel(); // SMS, EMAIL, WHATSAPP, PUSH
    String send(String destination, String templateCode, String renderedMessage) throws Exception;
}
