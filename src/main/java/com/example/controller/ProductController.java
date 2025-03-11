package com.example.controller;

import com.example.model.Product;
import com.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product newProduct = productService.addProduct(product);
        return ResponseEntity.ok(newProduct);
    }

    @GetMapping("/")
    public ResponseEntity<ArrayList<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID productId) {
        Product product = productService.getProductById(productId);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID productId, @RequestBody Map<String, Object> body) {
        System.out.println("Received request body: " + body);

        String newName = (String) body.get("newName");
        Double newPrice = body.get("newPrice") instanceof Number ? ((Number) body.get("newPrice")).doubleValue() : null;

        if (newName == null || newPrice == null) {
            System.out.println("Invalid input: newName=" + newName + ", newPrice=" + newPrice);
            return ResponseEntity.badRequest().build();
        }

        Product updatedProduct = productService.updateProduct(productId, newName, newPrice);
        return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
    }

    @PutMapping("/applyDiscount")
    public ResponseEntity<String> applyDiscount(@RequestParam double discount, @RequestBody ArrayList<UUID> productIds) {
        productService.applyDiscount(discount, productIds);
        return ResponseEntity.ok("Discount applied successfully");
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProductById(@PathVariable UUID productId) {
        productService.deleteProductById(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }
}