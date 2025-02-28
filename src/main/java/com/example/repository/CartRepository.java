package com.example.repository;

import com.example.model.Cart;
import com.example.model.Product;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("rawtypes")
@Repository
public class CartRepository extends MainRepository<Cart> {

    public CartRepository() {
    }

    @Override
    protected String getDataPath() {
        return "data/carts.json"; // Path to store carts JSON
    }

    @Override
    protected Class<Cart[]> getArrayType() {
        return Cart[].class; // Correct return type for deserialization
    }

    public Cart addCart(Cart cart) {
        save(cart); // Save new cart to JSON
        return cart;
    }

    public ArrayList<Cart> getCarts() {
        return findAll(); // Retrieve all carts
    }

    public Cart getCartById(UUID cartId) {
        return findAll().stream().filter(cart -> cart.getId().equals(cartId)).findFirst().orElse(null);
    }

    public Cart getCartByUserId(UUID userId) {
        return findAll().stream().filter(cart -> cart.getUserId().equals(userId)).findFirst().orElse(null);
    }

    public void addProductToCart(UUID cartId, Product product) {
        List<Cart> carts = findAll();

        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                cart.getProducts().add(product); // Add product to cart
                saveAll(new ArrayList<>(carts)); // Save updated cart list
                return;
            }
        }

        System.out.println("Cart not found!");
    }

    public void deleteProductFromCart(UUID cartId, Product product) {
        List<Cart> carts = findAll();

        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                cart.getProducts().removeIf(p -> p.getId().equals(product.getId())); // Remove product
                saveAll(new ArrayList<>(carts));
                return;
            }
        }

        System.out.println("Cart not found!");
    }

    public void deleteCartById(UUID cartId) {
        List<Cart> carts = findAll();
        carts.removeIf(cart -> cart.getId().equals(cartId));
        saveAll(new ArrayList<>(carts)); // Save updated cart list
    }
}