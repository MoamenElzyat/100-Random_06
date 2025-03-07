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

        // ✅ البحث عن العربة الموجودة بدلاً من السماح بإنشاء عربة جديدة
        Optional<Cart> existingCart = carts.stream()
                .filter(c -> c.getUserId().equals(cart.getUserId()))
                .findFirst();

        if (existingCart.isPresent()) {
            System.out.println("🛒 User already has a cart: " + existingCart.get());
            return existingCart.get(); // ✅ أعد العربة الموجودة
        }

        // ✅ إذا لم تكن هناك عربة، أضف عربة جديدة
        carts.add(cart);
        saveAll(carts);

        System.out.println("✅ New cart created for user: " + cart.getUserId());
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
    public void deleteProductFromCart(UUID userId, Product product) {
        List<Cart> carts = findAll();

        Cart cart = carts.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart not found!"));

        boolean removed = cart.getProducts().removeIf(p -> p.getId().equals(product.getId()));

        if (!removed) {
            throw new IllegalArgumentException("Product not found in cart!");
        }

        saveAll(carts);
        System.out.println("✅ Product removed from cart successfully!");
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
            objectMapper.writeValue(new File(getDataPath()), carts);
            System.out.println("Saved carts: " + carts);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to JSON file", e);
        }
    }
}