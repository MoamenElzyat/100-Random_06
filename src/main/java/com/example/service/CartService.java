package com.example.service;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CartService extends MainService<Cart> {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public ArrayList<Cart> getAll() {
        return new ArrayList<>(getCarts());
    }

    public ArrayList<Cart> getCarts() {
        return new ArrayList<>(cartRepository.getCarts());
    }

    @Override
    public Cart getById(UUID cartId) {
        return getCartById(cartId);
    }

    public Cart getCartById(UUID cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("Cart ID cannot be null");
        }
        System.out.println("All Carts: " + cartRepository.getCarts());
        return cartRepository.getCartById(cartId);
    }

    @Override
    public Cart add(Cart cart) {
        return cartRepository.addCart(cart);
    }

    @Override
    public void deleteById(UUID cartId) {
        deleteCartById(cartId);
    }

    public void deleteCartById(UUID cartId) {
        cartRepository.deleteCartById(cartId);
    }

    public Cart getCartByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return cartRepository.getCartByUserId(userId);
    }

    public void addProductToCart(UUID cartId, Product product) {
        cartRepository.addProductToCart(cartId, product);
    }

    public void deleteProductFromCart(UUID cartId, Product product) {
        cartRepository.deleteProductFromCart(cartId, product);
    }

    public void clearCart(UUID cartId) {
        Cart cart = cartRepository.getCartById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found!");
        }

        cart.getProducts().clear();
        cartRepository.saveAll(new ArrayList<>(cartRepository.getCarts()));
    }

    public double calculateTotalPrice(UUID userId) {
        Cart cart = getCartByUserId(userId);
        if (cart == null || cart.getProducts().isEmpty()) {
            return 0.0;
        }
        return cart.getProducts().stream().mapToDouble(Product::getPrice).sum();
    }

    public Cart addCart(Cart cart) {
        if (cart.getUserId() == null) {
            throw new IllegalArgumentException("Cart must have a userId");
        }

        if (getCartByUserId(cart.getUserId()) != null) {
            throw new IllegalStateException("User already has a cart");
        }

        cartRepository.addCart(cart);
        Cart savedCart = cartRepository.getCartByUserId(cart.getUserId()); // ✅ تأكد من أن العربة محفوظة
        System.out.println("Cart after saving: " + savedCart); // ✅ تصحيح الأخطاء

        return savedCart;
    }

    public static void main(String[] args) {
        CartRepository cartRepository = new CartRepository();
        ProductRepository productRepository = new ProductRepository();
        CartService cartService = new CartService(cartRepository, productRepository);

        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart testCart = new Cart(cartId, userId);

        cartRepository.saveAll(new ArrayList<>(Arrays.asList(testCart)));
        System.out.println("📌 Cart created: " + cartRepository.findAll());

        Product testProduct = new Product(UUID.randomUUID(), "m", 100.0);
        productRepository.save(testProduct);
        System.out.println("🛍️ Product to add: " + testProduct);

        cartService.addProductToCart(cartId, testProduct);

        List<Cart> carts = cartRepository.findAll();
        Cart updatedCart = carts.stream().filter(cart -> cart.getId().equals(cartId)).findFirst().orElse(null);

        if (updatedCart != null) {
            System.out.println("✅ Final cart after adding product: " + updatedCart);
        } else {
            System.out.println("❌ Cart not found after adding product!");
        }
    }
}