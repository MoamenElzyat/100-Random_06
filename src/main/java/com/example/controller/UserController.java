package com.example.controller;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.User;
import com.example.model.Product;
import com.example.service.UserService;
import com.example.service.CartService;
import com.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;

    public UserController(UserService userService, CartService cartService, ProductService productService) {
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
    }

    @PostMapping("/")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        User newUser = userService.add(user);
        return newUser != null
                ? ResponseEntity.ok("User added successfully")
                : ResponseEntity.ok("User already exists");
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getUsers();
        return users.isEmpty()
                ? ResponseEntity.ok().body(new ArrayList<>())
                : ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<String> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return user != null
                ? ResponseEntity.ok().body(user.toString())
                : ResponseEntity.ok("User not found");
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable UUID userId) {
        List<Order> orders = userService.getOrdersByUserId(userId);
        return orders.isEmpty()
                ? ResponseEntity.ok().body(new ArrayList<>())
                : ResponseEntity.ok(orders);
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> addOrderToUser(@PathVariable UUID userId) {
        User user = userService.getById(userId);
        if (user == null) return ResponseEntity.ok("User not found");

        userService.addOrderToUser(userId);
        return ResponseEntity.ok("Order added successfully");
    }

    @PostMapping("/{userId}/removeOrder")
    public ResponseEntity<String> removeOrderFromUser(@PathVariable UUID userId, @RequestParam UUID orderId) {
        User user = userService.getById(userId);
        if (user == null) return ResponseEntity.ok("User not found");

        userService.removeOrderFromUser(userId, orderId);
        return ResponseEntity.ok("Order removed successfully");
    }

    @DeleteMapping("/{userId}/emptyCart")
    public ResponseEntity<String> emptyCart(@PathVariable UUID userId) {
        User user = userService.getById(userId);
        if (user == null) return ResponseEntity.ok("User not found");

        userService.emptyCart(userId);
        return ResponseEntity.ok("Cart emptied successfully");
    }

    @PutMapping("/addProductToCart")
    public ResponseEntity<String> addProductToCart(@RequestParam UUID userId, @RequestParam UUID productId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.ok("User not found");
        }

        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.ok("Product not found");
        }

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            cart = new Cart(UUID.randomUUID(), userId, new ArrayList<>());
            cartService.addCart(cart);

            cart = cartService.getCartByUserId(userId);
            if (cart == null) {
                return ResponseEntity.ok("Failed to create cart");
            }
        }

        if (cart.getProducts() == null) {
            cart.setProducts(new ArrayList<>());
        }

        cartService.addProductToCart(cart.getId(), product);

        Cart updatedCart = cartService.getCartByUserId(userId);
        if (updatedCart == null || updatedCart.getProducts().isEmpty()) {
            return ResponseEntity.ok("Failed to add product to cart");
        }

        return ResponseEntity.ok("Product added to cart");
    }

    @PutMapping("/deleteProductFromCart")
    public ResponseEntity<String> deleteProductFromCart(@RequestParam UUID userId, @RequestParam UUID productId) {
        User user = userService.getUserById(userId);
        if (user == null) return ResponseEntity.ok("User not found");

        Product product = productService.getProductById(productId);
        if (product == null) return ResponseEntity.ok("Product not found");

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) return ResponseEntity.ok("Cart not found");

        cartService.deleteProductFromCart(userId, product);

        cart = cartService.getCartByUserId(userId);
        return cart.getProducts().isEmpty()
                ? ResponseEntity.ok("Cart is empty")
                : ResponseEntity.ok("Product deleted from cart");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable UUID userId) {
        User user = userService.getById(userId);
        if (user == null) return ResponseEntity.ok("User not found");

        userService.deleteUserById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}