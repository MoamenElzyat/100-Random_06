package com.example.repository;

import com.example.model.Order;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("rawtypes")
@Repository
public class OrderRepository extends MainRepository<Order> {

    public OrderRepository() {
    }

    @Override
    protected String getDataPath() {
        return "data/orders.json"; // Path to store orders JSON
    }

    @Override
    protected Class<Order[]> getArrayType() {
        return Order[].class; // Correct return type for deserialization
    }

    // Add a New Order
    public void addOrder(Order order) {
        save(order); // Save new order to JSON
    }

    // Get All Orders
    public ArrayList<Order> getOrders() {
        return findAll(); // Retrieve all orders
    }

    //  Get Specific Order by ID
    public Order getOrderById(UUID orderId) {
        return findAll().stream().filter(order -> order.getId().equals(orderId)).findFirst().orElse(null);
    }

    //  Delete a Specific Order by ID
    public void deleteOrderById(UUID orderId) {
        List<Order> orders = findAll();
        orders.removeIf(order -> order.getId().equals(orderId));
        saveAll(new ArrayList<>(orders)); // Save updated order list
    }
}