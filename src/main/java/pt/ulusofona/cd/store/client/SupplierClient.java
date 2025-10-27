package pt.ulusofona.cd.store.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pt.ulusofona.cd.store.dto.SupplierDto;

@FeignClient(name = "order-service", url = "http://order-service:8082")
public interface SupplierClient {

    @GetMapping("/api/v1/suppliers/{id}")
    SupplierDto getSupplierById(@PathVariable("id") String id);
}