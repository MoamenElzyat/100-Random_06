package com.example.service;

import com.example.model.Order;
import com.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@SuppressWarnings("rawtypes")
public class OrderService extends MainService<Order> {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public ArrayList<Order> getAll() {
        return orderRepository.getOrders();
    }

    @Override
    public Order getById(UUID orderId) {
        return orderRepository.getOrderById(orderId);
    }

    @Override
    public Order add(Order order) {
        addOrder(order);
        return order;
    }

    @Override
    public void deleteById(UUID orderId) {
        deleteOrderById(orderId);
    }

    public void addOrder(Order order) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        boolean exists = orderRepository.getOrders().stream()
                .anyMatch(existingOrder -> existingOrder.getId().equals(order.getId()));

        if (exists) {
            throw new IllegalArgumentException("Order with this ID already exists");
        }

        orderRepository.save(order);
    }

    public ArrayList<Order> getOrders() {
        return orderRepository.getOrders();
    }

    public Order getOrderById(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        return orderRepository.getOrderById(orderId);
    }

    public void deleteOrderById(UUID orderId) throws IllegalArgumentException {
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        orderRepository.deleteOrderById(orderId);
    }
}