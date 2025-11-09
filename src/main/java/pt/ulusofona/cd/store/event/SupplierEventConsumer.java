package pt.ulusofona.cd.store.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import pt.ulusofona.cd.store.dto.MessageEnvelope;
import pt.ulusofona.cd.store.dto.OrderCancelledPayload;
import pt.ulusofona.cd.store.dto.OrderConfirmedPayload;
import pt.ulusofona.cd.store.dto.SupplierDeactivatedPayload;
import pt.ulusofona.cd.store.service.ProductService;

@Component
@RequiredArgsConstructor
public class SupplierEventConsumer {

    private final ProductService productService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "${supplier.events.supplier-deactivated-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(MessageEnvelope<SupplierDeactivatedPayload> envelope) {
        SupplierDeactivatedPayload payload = envelope.getPayload();
        String supplierId = payload.getSupplierId();

        System.out.println("Received envelope (SupplierDeactivated): " + envelope);

        // Simulação de falha para testar retry
        if (supplierId.startsWith("FAIL")) {
            throw new RuntimeException("Simulated failure for supplier: " + supplierId);
        }

        int updatedCount = productService.setProductsInactiveBySupplierId(supplierId);
        System.out.println("Successfully set " + updatedCount + " products to inactive for supplier " + supplierId);
    }

    @DltHandler
    public void handleSupplierDeactivatedDlt(MessageEnvelope<SupplierDeactivatedPayload> failedEnvelope) {
        System.err.println("Message moved to DLT (SupplierDeactivated): " + failedEnvelope);
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "orders.confirmed.v1", groupId = "supplier-service")
    public void handleOrderConfirmedEvent(MessageEnvelope<OrderConfirmedPayload> envelope) {
        System.out.println("SupplierService recebeu OrderConfirmed envelope: " + envelope);
        OrderConfirmedPayload payload = envelope.getPayload();

        if (payload.getOrderId().startsWith("FAIL")) {
            throw new RuntimeException("Simulated failure for order: " + payload.getOrderId());
        }

        System.out.println("Fornecedor notificado: pedido confirmado " + payload.getOrderId());
    }

    @DltHandler
    public void handleOrderConfirmedDlt(MessageEnvelope<OrderConfirmedPayload> failedEnvelope) {
        System.err.println("Message moved to DLT (OrderConfirmed): " + failedEnvelope);
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "orders.cancelled.v1", groupId = "supplier-service")
    public void handleOrderCancelledEvent(MessageEnvelope<OrderCancelledPayload> envelope) {
        System.out.println("SupplierService recebeu OrderCancelled envelope: " + envelope);
        OrderCancelledPayload payload = envelope.getPayload();

        if (payload.getOrderId().startsWith("FAIL")) {
            throw new RuntimeException("Simulated failure for order: " + payload.getOrderId());
        }

        System.out.println("Fornecedor notificado: pedido cancelado " + payload.getOrderId());
    }

    @DltHandler
    public void handleOrderCancelledDlt(MessageEnvelope<OrderCancelledPayload> failedEnvelope) {
        System.err.println("Message moved to DLT (OrderCancelled): " + failedEnvelope);
    }
}
