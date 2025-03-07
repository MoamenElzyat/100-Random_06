package com.example.service;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class ProductService extends MainService<Product> {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ArrayList<Product> getAll() {
        return productRepository.getProducts();
    }


    @Override
    public Product getById(UUID productId) {
        return productRepository.getProductById(productId);
    }

    @Override
    public Product add(Product product) {
        return addProduct(product);
    }


    @Override
    public void deleteById(UUID productId) {
        productRepository.deleteProductById(productId);
    }

    public Product addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }

        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        return productRepository.addProduct(product);
    }
    public ArrayList<Product> getProducts() {
        return productRepository.getProducts();
    }

    public Product getProductById(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productRepository.getProductById(productId);
    }

    public Product updateProduct(UUID productId, String newName, double newPrice) {
        return productRepository.updateProduct(productId, newName, newPrice);
    }

    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Product ID list cannot be null or empty");
        }

        productRepository.applyDiscount(discount, productIds);
    }

    public void deleteProductById(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID can't be null");
        }

        productRepository.deleteProductById(productId);
    }

    public static void main(String[] args) {
        ProductRepository productRepository = new ProductRepository();
        ProductService productService = new ProductService(productRepository);

        Product testProduct = new Product(UUID.randomUUID(), "Laptop", 1200.0);
        productService.addProduct(testProduct);

        ArrayList<Product> allProducts = productService.getAll();
        System.out.println("🔍 All Products after adding: " + allProducts);
    }

}