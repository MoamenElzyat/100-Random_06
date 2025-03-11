package com.example.repository;

import com.example.model.Cart;
import com.example.model.Product;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("rawtypes")
@Repository
public class CartRepository extends MainRepository<Cart> {

    public CartRepository() {
    }

    @Override
    protected String getDataPath() {
        return "src/main/java/com/example/data/carts.json";
    }

    @Override
    protected Class<Cart[]> getArrayType() {
        return Cart[].class;
    }

    public Cart addCart(Cart cart) {
        if (cart == null || cart.getUserId() == null) {
            throw new IllegalArgumentException("Invalid cart data");
        }

        List<Cart> carts = findAll();

        Optional<Cart> existingCart = carts.stream()
                .filter(c -> c.getUserId().equals(cart.getUserId()))
                .findFirst();

        if (existingCart.isPresent()) {
            Cart oldCart = existingCart.get();
            if (oldCart.getProducts() != null && !oldCart.getProducts().isEmpty()) {
                cart.getProducts().addAll(oldCart.getProducts());
            }
            carts.remove(oldCart);
            System.out.println("Existing cart overridden for user: " + cart.getUserId());
        } else {
            System.out.println("New cart created for user: " + cart.getUserId());
        }

        carts.add(cart);
        saveAll(carts);
        return cart;
    }

    public List<Cart> getCarts() {
        return findAll();
    }

    public Cart getCartById(UUID cartId) {
        return findAll().stream()
                .filter(cart -> cart.getId().equals(cartId))
                .findFirst()
                .orElse(null);
    }

    public Cart getCartByUserId(UUID userId) {
        return findAll().stream()
                .filter(cart -> cart.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void addProductToCart(UUID cartId, Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        List<Cart> carts = findAll();
        System.out.println("Carts before adding product: " + carts);
        System.out.println("Looking for cart ID: " + cartId);
        boolean updated = false;

        for (Cart cart : carts) {
            if (cart.getId().equals(cartId)) {
                if (cart.getProducts() == null) {
                    cart.setProducts(new ArrayList<>());
                }

                System.out.println("Before adding product: " + cart.getProducts());
                cart.getProducts().add(product);
                System.out.println("After adding product: " + cart.getProducts());

                updated = true;
                break;
            }
        }

        if (updated) {
            saveAll(carts);
            System.out.println(" Carts after saving: " + findAll());
        } else {
            throw new IllegalArgumentException("Cart not found!");
        }
    }
    public void deleteProductFromCart(UUID cartId, Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        List<Cart> carts = findAll();
        System.out.println("Carts before Deleting product: " + carts);
        System.out.println("Looking for cart ID: " + cartId);

        Cart cart = carts.stream()
                .filter(c -> c.getId().equals(cartId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart not found!"));

        if (cart.getProducts() == null || cart.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty or products list is null!");
        }

        boolean removed = cart.getProducts().removeIf(p -> p.getId().equals(product.getId()));

        if (!removed) {
            throw new IllegalArgumentException("Product not found in cart!");
        }

        saveAll(carts);
        System.out.println("Carts after saving: " + findAll());
    }


    public void deleteCartById(UUID cartId) {
        List<Cart> carts = findAll();
        System.out.println("Before deletion: " + carts);

        boolean removed = carts.removeIf(cart -> cart.getId().equals(cartId));
        if (removed) {
            System.out.println("Cart deleted: " + cartId);
        } else {
            throw new IllegalArgumentException("Cart not found");
        }

        saveAll(new ArrayList<>(carts));
        System.out.println("After deletion: " + findAll());
    }




    public ArrayList<Cart> findAll() {
        try {
            File file = new File(getDataPath());
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Cart.class);
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from JSON file", e);
        }
    }

    public void saveAll(List<Cart> carts) {
        try {
            File file = new File(getDataPath());
            objectMapper.writeValue(file, carts);
            System.out.println("Saved carts: " + carts);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to JSON file", e);
        }
    }
}