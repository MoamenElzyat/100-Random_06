package com.example.controller;

import com.example.model.Order;
import com.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/")
    public ResponseEntity<String> addOrder(@RequestBody Order order) {
        orderService.addOrder(order);
        return ResponseEntity.ok("Order added successfully");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @GetMapping("/")
    public ResponseEntity<ArrayList<Order>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrderById(@PathVariable UUID id) {
        try {
            orderService.deleteOrderById(id);
            return ResponseEntity.ok("Order deleted successfully"); // Return 200 OK with success message
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("Order not found"); // Return 200 OK with "Order not found" message
        }
    }
}