package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@SuppressWarnings("rawtypes")
public class UserRepository extends MainRepository<User> {

    @Override
    protected String getDataPath() {
        return "data/users.json"; // JSON file path
    }

    @Override
    protected Class<User[]> getArrayType() {
        return User[].class; // Correct return type
    }

    public ArrayList<User> getUsers() {
        return findAll(); // Uses MainRepository method
    }

    public User getUserById(UUID userId) {
        return findAll().stream().filter(user -> user.getId().equals(userId)).findFirst().orElse(null);
    }

    public User addUser(User user) {
        save(user); // Uses save() from MainRepository
        return user;
    }

    // Add Order to User
    public void addOrderToUser(UUID userId, Order order, OrderRepository orderRepository) {
        List<User> users = findAll();

        for (User user : users) {
            if (user.getId().equals(userId)) {
                user.getOrders().add(order); // Add order to user's order list
                saveAll(new ArrayList<>(users)); // Save updated users to JSON

                //  Save the order in `orders.json`
                orderRepository.addOrder(order);
                return;
            }
        }

        System.out.println(" User not found!");
    }

    // Remove Order from User
    public void removeOrderFromUser(UUID userId, UUID orderId, OrderRepository orderRepository) {
        List<User> users = findAll();

        for (User user : users) {
            if (user.getId().equals(userId)) {
                boolean removed = user.getOrders().removeIf(order -> order.getId().equals(orderId));

                if (removed) {
                    saveAll(new ArrayList<>(users)); //  Save updated user data
                    orderRepository.deleteOrderById(orderId); //  Delete from orders.json
                    return;
                } else {
                    System.out.println("Order not found in user's order list!");
                    return;
                }
            }
        }

        System.out.println("User not found!");
    }

    public void deleteUserById(UUID userId) {
        List<User> users = findAll();
        users.removeIf(user -> user.getId().equals(userId));
        saveAll(new ArrayList<>(users)); // Update JSON file
    }
}