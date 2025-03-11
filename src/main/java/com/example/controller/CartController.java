package com.example.controller;

import com.example.model.Cart;
import com.example.model.Product;
import com.example.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/")
    public ResponseEntity<Cart> addCart(@RequestBody Cart cart) {
        Cart newCart = cartService.add(cart);
        return ResponseEntity.ok(newCart);
    }

    @GetMapping("/")
    public ResponseEntity<ArrayList<Cart>> getCarts() {
        return ResponseEntity.ok((ArrayList<Cart>) cartService.getCarts());
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<Cart> getCartById(@PathVariable UUID cartId) {
        Cart cart = cartService.getById(cartId);
        return cart != null ? ResponseEntity.ok(cart) : ResponseEntity.notFound().build();
    }



    @PutMapping("/addProduct/{cartId}")
    public ResponseEntity<String> addProductToCart(@PathVariable UUID cartId, @RequestBody Product product) {
        Cart cart = cartService.getCartById(cartId);
        if (cart == null) {
            return ResponseEntity.ok("Cart not found");
        }

        cartService.addProductToCart(cartId, product);
        return ResponseEntity.ok("Product added to cart");
    }


    @DeleteMapping("/delete/{cartId}")
    public ResponseEntity<String> deleteCartById(@PathVariable UUID cartId) {
        Cart cart = cartService.getCartById(cartId);
        if (cart == null) {
            return ResponseEntity.ok("Cart not found");
        }

        cartService.deleteCartById(cartId);
        return ResponseEntity.ok("Cart deleted successfully");
    }
}