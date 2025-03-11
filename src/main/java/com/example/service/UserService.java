package com.example.service;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.CartRepository;
import com.example.repository.OrderRepository;
import com.example.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService extends MainService<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;

    public UserService(UserRepository userRepository, OrderRepository orderRepository, CartService cartService, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @Override
    public ArrayList<User> getAll() {
        return userRepository.getUsers();
    }

    public ArrayList<User> getUsers() {
        return userRepository.getUsers();
    }

    @Override
    public User getById(UUID userId) {
        return getUserById(userId);
    }

    public User getUserById(UUID userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public User add(User user) {
        return addUser(user);
    }

    public User addUser(User user) {
        if (user == null || user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        boolean userExists = userRepository.getUsers().stream()
                .anyMatch(existingUser -> existingUser.getId().equals(user.getId()));

        if (userExists) {
            return null;
        }

        User addedUser = userRepository.addUser(user);
        if (cartService.getCartByUserId(user.getId()) == null) {
            cartService.addCart(new Cart(UUID.randomUUID(), user.getId(), new ArrayList<>()));
        }
        return addedUser;
    }

    @Override
    public void deleteById(UUID userId) {
        deleteUserById(userId);
    }

    public void deleteUserById(UUID userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new IllegalStateException("User not found with ID: " + userId);
        }

        user.getOrders().forEach(order -> orderRepository.deleteOrderById(order.getId()));

        Cart cart = cartService.getCartByUserId(userId);
        if (cart != null) {
            cartRepository.deleteCartById(cart.getId());  //  delete cart
        }

        userRepository.deleteUserById(userId);
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        User user = userRepository.getUserById(userId);
        return (user != null) ? new ArrayList<>(user.getOrders()) : new ArrayList<>();
    }


    public void addOrderToUser(UUID userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new IllegalStateException("User not found with ID: " + userId);
        }

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null || cart.getProducts().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot place order.");
        }

        double totalPrice = cartService.calculateTotalPrice(userId);
        Order newOrder = new Order(UUID.randomUUID(), userId, totalPrice, new ArrayList<>(cart.getProducts()));

        orderRepository.save(newOrder);
        user.getOrders().add(newOrder);
        userRepository.save(user);

        emptyCart(userId);
    }

    public void removeOrderFromUser(UUID userId, UUID orderId) {
        User user = userRepository.getUserById(userId);

        if (user == null) {
            throw new IllegalStateException("User not found with ID: "+userId );
        }

        if (user.getOrders().isEmpty()) {
            throw new IllegalStateException("User has no orders to remove");
        }

        System.out.println("Before removal, user orders: " + user.getOrders());

        boolean removed = user.getOrders().removeIf(order -> order.getId().equals(orderId));

        if (!removed) {
            throw new IllegalArgumentException("Order not found for user");
        }

        userRepository.save(user);

        System.out.println("After removal, user orders: " + user.getOrders());
    }

    public void emptyCart(UUID userId) {
        Cart cart = cartRepository.getCartByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found");
        }

        cart.setProducts(new ArrayList<>());
        cartRepository.deleteCartById(cart.getId());
        cartRepository.addCart(cart);
    }

    private void updateCartInRepository(Cart cart) {
        List<Cart> carts = cartRepository.getCarts();

        for (int i = 0; i < carts.size(); i++) {
            if (carts.get(i).getId().equals(cart.getId())) {
                carts.set(i, cart);
                cartRepository.saveAll(new ArrayList<>(carts));
                return;
            }
        }
    }

    private void updateUserInRepository(User user) {
        List<User> users = userRepository.findAll();
        boolean userExists = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                userExists = true;
                break;
            }
        }

        if (!userExists) {
            users.add(user);
        }

        userRepository.saveAll((ArrayList<User>) users);
    }
}