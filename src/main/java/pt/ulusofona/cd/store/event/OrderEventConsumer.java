package pt.ulusofona.cd.store.event;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import pt.ulusofona.cd.store.dto.MessageEnvelope;
import pt.ulusofona.cd.store.dto.OrderCancelledPayload;
import pt.ulusofona.cd.store.dto.OrderConfirmedPayload;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "orders.confirmed.v1", groupId = "product-service")
    public void handleOrderConfirmedEvent(MessageEnvelope<OrderConfirmedPayload> envelope) {
        System.out.println("ProductService recebeu envelope: " + envelope);
        OrderConfirmedPayload payload = envelope.getPayload();

        // Simulação de falha (para testar Retry e DLT)
        if (payload.getOrderId().startsWith("FAIL")) {
            throw new RuntimeException("Simulated failure for order: " + payload.getOrderId());
        }

        System.out.println("Stock atualizado para OrderConfirmed: " + payload.getOrderId());
    }

    @DltHandler
    public void handleOrderConfirmedDlt(MessageEnvelope<OrderConfirmedPayload> failedEnvelope) {
        System.err.println("Mensagem movida para DLT (OrderConfirmed): " + failedEnvelope);
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "orders.cancelled.v1", groupId = "product-service")
    public void handleOrderCancelledEvent(MessageEnvelope<OrderCancelledPayload> envelope) {
        System.out.println("ProductService recebeu envelope: " + envelope);
        OrderCancelledPayload payload = envelope.getPayload();

        if (payload.getOrderId().startsWith("FAIL")) {
            throw new RuntimeException("Simulated failure for order: " + payload.getOrderId());
        }

        System.out.println("Stock restaurado para OrderCancelled: " + payload.getOrderId());
    }

    @DltHandler
    public void handleOrderCancelledDlt(MessageEnvelope<OrderCancelledPayload> failedEnvelope) {
        System.err.println("Mensagem movida para DLT (OrderCancelled): " + failedEnvelope);
    }
}
