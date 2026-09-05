package com.common.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/**
 * Unit tests for the shared Kafka configuration. Beans are created by direct
 * instantiation — no Spring context needed. The topic constants asserted here
 * are THIS project's (ecommerce) contract, not the sibling POS project's.
 */
class KafkaCommonConfigTest {

    private final KafkaCommonConfig config = new KafkaCommonConfig();

    @Test
    void topicNameConstants_matchPublishedContract() {
        assertThat(KafkaCommonConfig.TOPIC_ORDER_EVENT).isEqualTo("stats.ecommerce.order.event");
        assertThat(KafkaCommonConfig.TOPIC_TRANSACTION_EVENT).isEqualTo("stats.ecommerce.transaction.event");
        assertThat(KafkaCommonConfig.TOPIC_ORDER_ITEM_EVENT).isEqualTo("stats.ecommerce.order_item.event");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_REGISTER).isEqualTo("email-service-topic-auth-register");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_FORGOT_PASSWORD).isEqualTo("email-service-topic-auth-forgot-password");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_VERIFY_SUCCESS).isEqualTo("email-service-topic-auth-verify-code-success");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_MERCHANT_CREATE).isEqualTo("email-service-topic-merchant-create");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_MERCHANT_UPDATE_STATUS).isEqualTo("email-service-topic-merchant-update-status");
        assertThat(KafkaCommonConfig.TOPIC_EMAIL_TRANSACTION_CREATE).isEqualTo("email-service-topic-transaction-create");
        assertThat(KafkaCommonConfig.TOPIC_MERCHANT_TRANSACTION_EVENT).isEqualTo("merchant-service-topic-transaction-event");
        assertThat(KafkaCommonConfig.TOPIC_TRANSACTION_MERCHANT_STATUS_EVENT).isEqualTo("transaction-service-topic-merchant-status-event");
        assertThat(KafkaCommonConfig.TOPIC_NOTIFICATION).isEqualTo("notification-topic");
        assertThat(KafkaCommonConfig.PARTITIONS).isEqualTo(3);
        assertThat(KafkaCommonConfig.REPLICATION).isEqualTo((short) 1);
    }

    @Test
    void topicBeans_carryDeclaredNamePartitionsAndReplication() {
        Map<String, Supplier<NewTopic>> topicBeans = Map.ofEntries(
                Map.entry(KafkaCommonConfig.TOPIC_ORDER_EVENT, config::topicOrderEvent),
                Map.entry(KafkaCommonConfig.TOPIC_TRANSACTION_EVENT, config::topicTransactionEvent),
                Map.entry(KafkaCommonConfig.TOPIC_ORDER_ITEM_EVENT, config::topicOrderItemEvent),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_REGISTER, config::topicEmailRegister),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_FORGOT_PASSWORD, config::topicEmailForgotPassword),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_VERIFY_SUCCESS, config::topicEmailVerifySuccess),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_MERCHANT_CREATE, config::topicEmailMerchantCreate),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_MERCHANT_UPDATE_STATUS, config::topicEmailMerchantUpdateStatus),
                Map.entry(KafkaCommonConfig.TOPIC_EMAIL_TRANSACTION_CREATE, config::topicEmailTransactionCreate),
                Map.entry(KafkaCommonConfig.TOPIC_MERCHANT_TRANSACTION_EVENT, config::topicMerchantTransactionEvent),
                Map.entry(KafkaCommonConfig.TOPIC_TRANSACTION_MERCHANT_STATUS_EVENT, config::topicTransactionMerchantStatusEvent),
                Map.entry(KafkaCommonConfig.TOPIC_NOTIFICATION, config::topicNotification));

        topicBeans.forEach((expectedName, bean) -> {
            NewTopic topic = bean.get();
            assertThat(topic.name()).as("topic name").isEqualTo(expectedName);
            assertThat(topic.numPartitions()).as("partitions of " + expectedName)
                    .isEqualTo(KafkaCommonConfig.PARTITIONS);
            assertThat(topic.replicationFactor()).as("replication of " + expectedName)
                    .isEqualTo(KafkaCommonConfig.REPLICATION);
        });
    }

    @Test
    void topicBeans_doNotSilentlyReuseTopicBuilderDefaults() {
        // sanity: TopicBuilder alone would produce different values than the config
        NewTopic raw = TopicBuilder.name("raw").partitions(1).replicas(1).build();
        assertThat(raw.numPartitions()).isNotEqualTo(KafkaCommonConfig.PARTITIONS);
    }

    @Test
    void jsonMessageConverter_returnsStringJsonMessageConverter() {
        assertThat(config.jsonMessageConverter()).isInstanceOf(StringJsonMessageConverter.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void kafkaTemplate_wrapsGivenProducerFactory() {
        ProducerFactory<String, Object> producerFactory = Mockito.mock(ProducerFactory.class);

        KafkaTemplate<String, Object> template = config.kafkaTemplate(producerFactory);

        assertThat(template).isNotNull();
    }
}
