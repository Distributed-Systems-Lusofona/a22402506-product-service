package pt.ulusofona.cd.store.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pt.ulusofona.cd.store.dto.SupplierDto;

import java.util.Arrays;
import java.util.UUID;

@FeignClient(name = "order-service", url = "http://order-service:8083")
public interface OrderClient {

    @GetMapping("/api/v1/orders/{id}")
    SupplierDto getSupplierById(@PathVariable("id") String id);

    @GetMapping("/has-pending/{productId}")
    boolean hasPendingOrdersForProduct(@PathVariable("productId") UUID productId);
}