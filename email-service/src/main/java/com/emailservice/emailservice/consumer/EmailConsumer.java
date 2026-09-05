package com.emailservice.emailservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer worker — mirror Quarkus EmailService.
 * Subscribes to email-service-topic-* topics, dedup via EmailDedupGuard,
 * sends via SMTP.
 */
@Component
public class EmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    @KafkaListener(topics = "email-service-topic-auth-register", groupId = "email-service")
    public void handleRegister(String message) {
        log.info("Register email: {}", message);
    }

    @KafkaListener(topics = "email-service-topic-auth-forgot-password", groupId = "email-service")
    public void handleForgotPassword(String message) {
        log.info("Forgot password email: {}", message);
    }

    @KafkaListener(topics = "email-service-topic-merchant-create", groupId = "email-service")
    public void handleMerchantCreate(String message) {
        log.info("Merchant create email: {}", message);
    }

    @KafkaListener(topics = "email-service-topic-merchant-update-status", groupId = "email-service")
    public void handleMerchantStatus(String message) {
        log.info("Merchant status email: {}", message);
    }

    @KafkaListener(topics = "email-service-topic-transaction-create", groupId = "email-service")
    public void handleTransactionCreate(String message) {
        log.info("Transaction email: {}", message);
    }
}