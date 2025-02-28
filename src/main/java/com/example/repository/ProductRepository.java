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
        return "data/products.json";
    }

    @Override
    protected Class<Product[]> getArrayType() {
        return Product[].class; // Correctly defines the type
    }

    // Add a new product
    public Product addProduct(Product product) {
        save(product);
        return product;
    }

    // Get all products
    public ArrayList<Product> getProducts() {
        return findAll();
    }

    public Product getProductById(UUID productId) {
        return findAll().stream().filter(product -> product.getId().equals(productId)).findFirst().orElse(null);
    }

    public String updateProduct(UUID productId, String newName, double newPrice) {
        List<Product> products = findAll();
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                product.setName(newName); // Update name
                product.setPrice(newPrice); // Update price
                saveAll(new ArrayList<>(products)); // Save updated list to JSON
                return "Product updated successfully!";
            }
        }
        return "Product not found!";
    }


    public void applyDiscount(double discount, ArrayList<UUID> productIds) {
        List<Product> products = findAll();

        for (Product product : products) {
            if (productIds.contains(product.getId())) {
                double newPrice = product.getPrice() * (1 - (discount / 100)); //  discount
                product.setPrice(newPrice);
            }
        }

        saveAll(new ArrayList<>(products)); // Save updated product list
    }

    public void deleteProductById(UUID productId) {
        List<Product> products = findAll();
        products.removeIf(product -> product.getId().equals(productId));
        saveAll(new ArrayList<>(products));
    }
}