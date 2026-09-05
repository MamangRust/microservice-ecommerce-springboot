package com.order.order.outbox;

import com.order.order.entity.OrderOutbox;
import com.order.order.entity.OutboxStatus;
import com.order.order.repository.OrderOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderOutboxPublisher {

    private static final int MAX_ATTEMPTS = 5;

    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderOutboxPublisher(OrderOutboxRepository outboxRepository,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishPending() {
        List<OrderOutbox> pending = outboxRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING);
        for (OrderOutbox outbox : pending) {
            try {
                kafkaTemplate.send(outbox.getTopic(), outbox.getAggregateId(), outbox.getPayload()).get();
                outbox.setStatus(OutboxStatus.PROCESSED);
                outbox.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(outbox);
                log.debug("Published order outbox {} to {}", outbox.getEventId(), outbox.getTopic());
            } catch (Exception e) {
                outbox.setAttempts(outbox.getAttempts() + 1);
                outbox.setLastError(e.getMessage());
                if (outbox.getAttempts() >= MAX_ATTEMPTS) {
                    outbox.setStatus(OutboxStatus.FAILED);
                }
                outboxRepository.save(outbox);
                log.error("Failed to publish order outbox {}: {}", outbox.getEventId(), e.getMessage());
            }
        }
    }
}