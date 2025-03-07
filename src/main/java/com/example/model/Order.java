package com.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class Order {
    private UUID id;
    private UUID userId;
    private double totalPrice;
    private List<Product> products;

    public Order() {}

    public Order(UUID id, UUID userId, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = new ArrayList<>();
    }

    public Order(UUID id, UUID userId, double totalPrice, List<Product> products) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = new ArrayList<>(products); // Prevent external modification
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public double getTotalPrice() { return totalPrice; }
    public List<Product> getProducts() { return new ArrayList<>(products); } // Return a copy for safety

    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setProducts(List<Product> products) { this.products = new ArrayList<>(products); }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Double.compare(order.totalPrice, totalPrice) == 0 &&
                Objects.equals(id, order.id) &&
                Objects.equals(userId, order.userId) &&
                Objects.equals(products, order.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, totalPrice, products);
    }
}