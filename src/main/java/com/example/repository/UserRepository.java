package com.example.repository;

import com.example.model.Order;
import com.example.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository extends MainRepository<User> {

    @Override
    protected String getDataPath() {
        return "src/main/java/com/example/data/users.json";
    }

    @Override
    protected Class<User[]> getArrayType() {
        return User[].class;
    }

    public ArrayList<User> getUsers() {
        return (findAll()); //
    }

    public User getUserById(UUID userId) {
        List<User> users = findAll();
        System.out.println("All users: " + users); // Debugging
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public User addUser(User user) {
        save(user);
        return user;
    }

    public void addOrderToUser(UUID userId, Order order, OrderRepository orderRepository) {
        List<User> users = new ArrayList<>(findAll());
        for (User user : users) {
            if (user.getId().equals(userId)) {
                user.getOrders().add(order);
                saveAll((ArrayList<User>) users);
                orderRepository.addOrder(order);
                return;
            }
        }

        System.out.println("User not found!");
    }

    public void removeOrderFromUser(UUID userId, UUID orderId, OrderRepository orderRepository) {
        List<User> users = new ArrayList<>(findAll());

        for (User user : users) {
            if (user.getId().equals(userId)) {
                boolean removed = user.getOrders().removeIf(order -> order.getId().equals(orderId));

                if (removed) {
                    saveAll((ArrayList<User>) users);
                    orderRepository.deleteOrderById(orderId);
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
        List<User> users = new ArrayList<>(findAll());
        users.removeIf(user -> user.getId().equals(userId));
        saveAll((ArrayList<User>) users);
    }


    public void save(User user) {
        List<User> users = getUsers();

        // Remove old version of user if exists
        users.removeIf(existingUser -> existingUser.getId().equals(user.getId()));

        // Add updated user
        users.add(user);

        // Persist the updated list
        saveAll((ArrayList<User>) users);
    }

}