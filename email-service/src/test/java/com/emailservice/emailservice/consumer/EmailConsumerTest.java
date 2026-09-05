package com.emailservice.emailservice.consumer;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EmailConsumer}. The consumer only logs the received
 * Kafka messages, so the honest contract to verify is: it can be instantiated
 * with no dependencies and consumes messages of any content (including empty
 * strings) without throwing.
 */
class EmailConsumerTest {

    private final EmailConsumer emailConsumer = new EmailConsumer();

    @Test
    void handleRegister_acceptsSampleMessage() {
        assertThatCode(() -> emailConsumer.handleRegister(
                "{\"userId\":\"u-1\",\"username\":\"johndoe\",\"email\":\"john@example.com\"}"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleForgotPassword_acceptsSampleMessage() {
        assertThatCode(() -> emailConsumer.handleForgotPassword(
                "{\"email\":\"john@example.com\",\"resetCode\":\"123456\"}"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleMerchantCreate_acceptsSampleMessage() {
        assertThatCode(() -> emailConsumer.handleMerchantCreate(
                "{\"merchantId\":\"m-1\",\"email\":\"merchant@example.com\",\"name\":\"Toko A\"}"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleMerchantStatus_acceptsSampleMessage() {
        assertThatCode(() -> emailConsumer.handleMerchantStatus(
                "{\"merchantId\":\"m-1\",\"email\":\"merchant@example.com\",\"status\":\"APPROVED\"}"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleTransactionCreate_acceptsSampleMessage() {
        assertThatCode(() -> emailConsumer.handleTransactionCreate(
                "{\"transactionId\":\"TRX-1\",\"email\":\"john@example.com\",\"total\":25000}"))
                .doesNotThrowAnyException();
    }

    @Test
    void allListeners_acceptEmptyStringMessage() {
        assertThatCode(() -> {
            emailConsumer.handleRegister("");
            emailConsumer.handleForgotPassword("");
            emailConsumer.handleMerchantCreate("");
            emailConsumer.handleMerchantStatus("");
            emailConsumer.handleTransactionCreate("");
        }).doesNotThrowAnyException();
    }
}
