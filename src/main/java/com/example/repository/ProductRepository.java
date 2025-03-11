package com.example.repository;

import com.example.model.Product;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("rawtypes")
@Repository
public class ProductRepository extends MainRepository<Product> {

    @Override
    protected String getDataPath() {
        return "src/main/java/com/example/data/products.json";
    }

    @Override
    protected Class<Product[]> getArrayType() {
        return Product[].class;
    }

    public Product addProduct(Product product) {
        List<Product> products = findAll();

        boolean exists = products.stream()
                .anyMatch(p -> p.getName() != null && p.getName().equalsIgnoreCase(product.getName()));

        if (exists) {
            throw new IllegalArgumentException("Product with the same name already exists.");
        }

        products.add(product);
        saveAll((ArrayList<Product>) products);
        return product;
    }

    public ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<>(findAll());
        System.out.println("Retrieved products: " + products);
        return products;
    }

    public Product getProductById(UUID productId) {
        return findAll().stream().filter(product -> product.getId().equals(productId)).findFirst().orElse(null);
    }

    public Product updateProduct(UUID productId, String newName, double newPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (newPrice < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }

        List<Product> products = findAll();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                product.setName(newName);
                product.setPrice(newPrice);
                saveAll((ArrayList<Product>) products);
                return product;
            }
        }

        throw new IllegalArgumentException("Product not found");
    }


    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        List<Product> products = findAll();

        boolean updated = false;
        for (Product product : products) {
            if (productIds.contains(product.getId())) {
                double newPrice = product.getPrice() * (1 - (discount / 100));
                product.setPrice(newPrice);
                updated = true;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("No matching products found");
        }

        saveAll((ArrayList<Product>) products);
    }

    public void deleteProductById(UUID productId) {
        List<Product> products = findAll();

        boolean removed = products.removeIf(product -> product.getId().equals(productId));

        if (!removed) {
            throw new IllegalArgumentException("Product not found");
        }

        saveAll((ArrayList<Product>) products);
    }
}