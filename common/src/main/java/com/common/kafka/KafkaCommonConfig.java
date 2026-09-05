package com.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/**
 * Shared Kafka configuration — mirrors Quarkus Kafka setup.
 * Topics follow the pattern: stats.pos.<domain>.event, email-service-topic-*
 */
@Configuration
public class KafkaCommonConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommonConfig.class);

    // === Topic names ===
    public static final String TOPIC_ORDER_EVENT = "stats.ecommerce.order.event";
    public static final String TOPIC_TRANSACTION_EVENT = "stats.ecommerce.transaction.event";
    public static final String TOPIC_ORDER_ITEM_EVENT = "stats.ecommerce.order_item.event";
    public static final String TOPIC_EMAIL_REGISTER = "email-service-topic-auth-register";
    public static final String TOPIC_EMAIL_FORGOT_PASSWORD = "email-service-topic-auth-forgot-password";
    public static final String TOPIC_EMAIL_VERIFY_SUCCESS = "email-service-topic-auth-verify-code-success";
    public static final String TOPIC_EMAIL_MERCHANT_CREATE = "email-service-topic-merchant-create";
    public static final String TOPIC_EMAIL_MERCHANT_UPDATE_STATUS = "email-service-topic-merchant-update-status";
    public static final String TOPIC_EMAIL_TRANSACTION_CREATE = "email-service-topic-transaction-create";
    public static final String TOPIC_MERCHANT_TRANSACTION_EVENT = "merchant-service-topic-transaction-event";
    public static final String TOPIC_TRANSACTION_MERCHANT_STATUS_EVENT = "transaction-service-topic-merchant-status-event";
    public static final String TOPIC_NOTIFICATION = "notification-topic";

    public static final int PARTITIONS = 3;
    public static final short REPLICATION = 1;

    @Bean
    @ConditionalOnMissingBean
    public StringJsonMessageConverter jsonMessageConverter() {
        return new StringJsonMessageConverter();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // === Topic definitions ===

    @Bean
    public NewTopic topicOrderEvent() {
        return TopicBuilder.name(TOPIC_ORDER_EVENT)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicTransactionEvent() {
        return TopicBuilder.name(TOPIC_TRANSACTION_EVENT)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicOrderItemEvent() {
        return TopicBuilder.name(TOPIC_ORDER_ITEM_EVENT)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailRegister() {
        return TopicBuilder.name(TOPIC_EMAIL_REGISTER)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailForgotPassword() {
        return TopicBuilder.name(TOPIC_EMAIL_FORGOT_PASSWORD)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailVerifySuccess() {
        return TopicBuilder.name(TOPIC_EMAIL_VERIFY_SUCCESS)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailMerchantCreate() {
        return TopicBuilder.name(TOPIC_EMAIL_MERCHANT_CREATE)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailMerchantUpdateStatus() {
        return TopicBuilder.name(TOPIC_EMAIL_MERCHANT_UPDATE_STATUS)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicEmailTransactionCreate() {
        return TopicBuilder.name(TOPIC_EMAIL_TRANSACTION_CREATE)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicMerchantTransactionEvent() {
        return TopicBuilder.name(TOPIC_MERCHANT_TRANSACTION_EVENT)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicTransactionMerchantStatusEvent() {
        return TopicBuilder.name(TOPIC_TRANSACTION_MERCHANT_STATUS_EVENT)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }

    @Bean
    public NewTopic topicNotification() {
        return TopicBuilder.name(TOPIC_NOTIFICATION)
            .partitions(PARTITIONS)
            .replicas(REPLICATION)
            .build();
    }
}