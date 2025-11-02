package pt.ulusofona.cd.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulusofona.cd.store.client.OrderClient;
import pt.ulusofona.cd.store.client.SupplierClient;
import pt.ulusofona.cd.store.dto.ProductRequest;
import pt.ulusofona.cd.store.dto.SupplierDto;
import pt.ulusofona.cd.store.exception.ProductNotFoundException;
import pt.ulusofona.cd.store.mapper.ProductMapper;
import pt.ulusofona.cd.store.model.Product;
import pt.ulusofona.cd.store.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderClient supplierClient;
    private final OrderClient orderClient;

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (request.getSupplierId() != null) {
            try {
                SupplierDto supplier = supplierClient.getSupplierById(request.getSupplierId().toString());
                if (!supplier.isActive()) {
                    throw new IllegalArgumentException("Supplier is not active");
                }
            } catch (Exception e) {
                System.out.println(e);
                throw new IllegalArgumentException("Invalid supplier ID: " + request.getSupplierId());
            }
        }
        Product product = ProductMapper.toEntity(request);
        return productRepository.save(product);
    }

    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsBySupplier(UUID supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    @Transactional
    public Product updateProduct(UUID id, ProductRequest productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setSku(productDetails.getSku());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCurrency(productDetails.getCurrency());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public boolean removeStock(UUID id, int quantity) {
        Product product = getProductById(id);

        if(product.isDiscontinued() || product.getStock() < quantity) {
            return false;
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean addStock(UUID id, int quantity) {
        Product product = getProductById(id);
        if (product.isDiscontinued()) {
            return false;
        }
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public Product setDiscontinued(UUID id) {
        Product product = getProductById(id);

        if (product.isDiscontinued()) {
            throw new IllegalStateException("Product is already discontinued: " + id);
        }

        boolean hasPendingOrders = orderClient.hasPendingOrdersForProduct(id);
        if (hasPendingOrders) {
            throw new IllegalStateException("Cannot discontinue product with pending orders: " + id);
        }

        product.setDiscontinued(true);
        return productRepository.save(product);
    }

    @Transactional
    public Product setProductsInactiveBySupplier(UUID id) {
        Product product = getProductById(id);
        if (product.isDiscontinued()) {
            throw new IllegalStateException("Product is already discontinued: " + id);
        }
        boolean hasPendingOrders = orderClient.hasPendingOrdersForProduct(id);
        if (hasPendingOrders) {
            throw new IllegalStateException("Cannot discontinue product with pending orders: " + id);
        }
        product.setDiscontinued(false);
        return productRepository.save(product);
    }


    public int setProductsInactiveBySupplierId(String supplierId) {
        Product product = getProductById(UUID.fromString(supplierId));
        if (product.isDiscontinued()) {
            throw new IllegalStateException("Product is already discontinued: " + supplierId);
        }
        boolean hasPendingOrders = orderClient.hasPendingOrdersForProduct(UUID.fromString(supplierId));
        if (hasPendingOrders) {
            throw new IllegalStateException("Cannot discontinue product with pending orders: " + supplierId);
        }
        product.setDiscontinued(false);
        return productRepository.save(product).getStock();
    }
}