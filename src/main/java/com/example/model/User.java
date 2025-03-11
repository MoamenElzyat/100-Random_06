package com.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class User {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("orders")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Order> orders;

    public User() {
        this.id = UUID.randomUUID();
        this.orders = new ArrayList<>();
    }

    public User(UUID id, String name) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.name = name;
        this.orders = new ArrayList<>();
    }

    public User(UUID id, String name, List<Order> orders) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.name = name;
        this.orders = (orders != null) ? orders : new ArrayList<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) {
        if (id == null) {
          //  throw new IllegalArgumentException("User ID cannot be null!");
            id : UUID.randomUUID();
        }
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = (orders != null) ? orders : new ArrayList<>(); }

    public void addOrder(Order order) {
        if (order != null) {
            this.orders.add(order);
        }
    }


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
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }


}