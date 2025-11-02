package pt.ulusofona.cd.store.event;

import org.springframework.kafka.annotation.KafkaListener;
import pt.ulusofona.cd.store.dto.OrderCancelledEvent;
import pt.ulusofona.cd.store.dto.OrderConfirmedEvent;

public class OrderEventConsumer {

    @KafkaListener(topics = "orders.confirmed.v1", groupId = "product-service")
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        System.out.println("ProductService recebeu OrderConfirmedEvent: " + event);

    }

    @KafkaListener(topics = "orders.cancelled.v1", groupId = "product-service")
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        System.out.println("ProductService recebeu OrderCancelledEvent: " + event);
    }
}
