package com.example.MiniProject1;

import com.example.model.Cart;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.repository.OrderRepository;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.service.CartService;
import com.example.service.OrderService;
import com.example.service.ProductService;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceTest {

    private UserService userService;
    private OrderService orderService;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private CartService cartService;
    private ProductService productService;
    private ProductRepository productRepository;
    private CartRepository cartRepository;

    private final String usersFilePath = "src/main/java/com/example/data/users.json";
    private final String productsFilePath = "src/main/java/com/example/data/products.json";
    private final String ordersFilePath = "src/main/java/com/example/data/orders.json";
    private final String cartsFilePath = "src/main/java/com/example/data/carts.json";

    @BeforeEach
    void setUp() {
        ensureFileExists(usersFilePath);
        ensureFileExists(productsFilePath);
        ensureFileExists(ordersFilePath);
        ensureFileExists(cartsFilePath);

        userRepository = new UserRepository();
        orderRepository = new OrderRepository();
        cartRepository = new CartRepository();
        productRepository = new ProductRepository();

        cartService = new CartService(cartRepository, productRepository);
        productService = new ProductService(productRepository);
        orderService = new OrderService(orderRepository);
        userService = new UserService(userRepository, orderRepository, cartService, cartRepository);

        userRepository.saveAll(new ArrayList<>());
        productRepository.saveAll(new ArrayList<>());
        cartRepository.saveAll(new ArrayList<>());
        orderRepository.saveAll(new ArrayList<>());
    }


    private void ensureFileExists(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        try {
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[]");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JSON file: " + filePath, e);
        }
    }

    // ----------------------------------------------------------------------
    // 7.2.2.1 - Add User Tests
    // ----------------------------------------------------------------------

    @Test
    void testAddUser_Success() {
        User user = new User(UUID.randomUUID(), "Moamen Ahmed");
        User result = userService.addUser(user);

        assertNotNull(result, "User should be successfully added");
        assertEquals("Moamen Ahmed", result.getName(), "User name should match");
        assertNotNull(result.getId(), "User ID should be assigned");
    }

    @Test
    void testAddUser_DuplicateUser() {
        User user = new User(UUID.randomUUID(), "Moamen Ahmed");
        userService.addUser(user);

        User duplicateUser = new User(user.getId(), "Moamen Ahmed");
        User result = userService.addUser(duplicateUser);

        assertNull(result, "Duplicate user should not be added");
    }

    @Test
    void testAddUser_InvalidData() {
        User user = new User(null, "");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.addUser(user));
        assertEquals("User name cannot be null or empty", exception.getMessage(), "Error message should match");
    }

    // ----------------------------------------------------------------------
    // 7.2.2.2 - Get Users Tests
    // ----------------------------------------------------------------------

    @Test
    void testGetUsers_NotEmptyList() {
        User user1 = new User(UUID.randomUUID(), "Moamen 1");
        User user2 = new User(UUID.randomUUID(), "Moamen 2");

        userService.addUser(user1);
        userService.addUser(user2);

        List<User> users = userService.getUsers();

        assertNotNull(users, "User list should not be null");
        assertEquals(2, users.size(), "User list size should be 2");
        assertTrue(users.contains(user1), "First user should be in the list");
        assertTrue(users.contains(user2), "Second user should be in the list");
    }

    @Test
    void testGetUsers_EmptyList() {
        List<User> users = userService.getUsers();
        assertNotNull(users, "User list should not be null");
        assertTrue(users.isEmpty(), "User list should be empty");
    }

    @Test
    void testGetUsers_ReturnsNewInstance() {
        userService.addUser(new User(UUID.randomUUID(), "User A"));
        List<User> firstCall = userService.getUsers();
        List<User> secondCall = userService.getUsers();

        assertNotSame(firstCall, secondCall, "Each call to getUsers() should return a new instance");
    }

    // ----------------------------------------------------------------------
    // 7.2.2.3 - Get User by ID Tests
    // ----------------------------------------------------------------------

    @Test
    void testGetUserById_UserExists() {
        User user = new User(UUID.randomUUID(), "Moamen Test");
        userService.addUser(user);

        User retrievedUser = userService.getUserById(user.getId());

        assertNotNull(retrievedUser, "Retrieved user should not be null");
        assertEquals(user.getId(), retrievedUser.getId(), "User ID should match");
        assertEquals(user.getName(), retrievedUser.getName(), "User name should match");
    }

    @Test
    void testGetUserById_UserNotFound() {
        User retrievedUser = userService.getUserById(UUID.randomUUID());
        assertNull(retrievedUser, "Should return null for a non-existing user");
    }

    @Test
    void testGetUserById_AfterMultipleUsersAdded() {
        User user1 = new User(UUID.randomUUID(), "User One");
        User user2 = new User(UUID.randomUUID(), "User Two");

        userService.addUser(user1);
        userService.addUser(user2);

        User retrievedUser = userService.getUserById(user2.getId());

        assertNotNull(retrievedUser, "Retrieved user should not be null");
        assertEquals(user2.getId(), retrievedUser.getId(), "User ID should match");
        assertEquals("User Two", retrievedUser.getName(), "User name should match");
    }

    // ----------------------------------------------------------------------
    // 7.2.2.4 - Get Orders by User ID Tests
    // ----------------------------------------------------------------------
    @Test
    void testGetOrdersByUserId_WithOrders() {
        User user = new User(UUID.randomUUID(), "Moamen With Orders");
        userService.addUser(user);

        Order order1 = new Order(UUID.randomUUID(), user.getId(), 500.0, new ArrayList<>());
        Order order2 = new Order(UUID.randomUUID(), user.getId(), 1000.0, new ArrayList<>());

        user.getOrders().add(order1);
        user.getOrders().add(order2);

        userRepository.deleteUserById(user.getId());
        userRepository.save(user);

        User updatedUser = userRepository.getUserById(user.getId());
        List<Order> orders = updatedUser.getOrders();

        assertNotNull(orders, "Orders list should not be null");
        assertEquals(2, orders.size(), "Orders list should contain exactly 2 orders");
        assertTrue(orders.contains(order1), "Orders list should contain the first order");
        assertTrue(orders.contains(order2), "Orders list should contain the second order");
    }

    @Test
    void testGetOrdersByUserId_NoOrders() {
        User user = new User(UUID.randomUUID(), "Moamen No Orders");
        userService.addUser(user);

        List<Order> orders = userService.getOrdersByUserId(user.getId());

        assertNotNull(orders, "Orders list should not be null");
        assertTrue(orders.isEmpty(), "Orders list should be empty");
    }

    @Test
    void testGetOrdersByUserId_NonExistentUser() {
        List<Order> orders = userService.getOrdersByUserId(UUID.randomUUID());

        assertNotNull(orders, "Orders list should not be null");
        assertTrue(orders.isEmpty(), "Orders list should be empty for a non-existent user");
    }

    // ----------------------------------------------------------------------
    // 7.2.2.5 - Add Order to User Tests
    // ----------------------------------------------------------------------

    @Test
    public void testAddOrderToUser_Success() {

        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Moamen");
        userRepository.save(user);

        Cart cart = new Cart(UUID.randomUUID(), userId);
        cartRepository.save(cart);

        Product product = new Product(UUID.randomUUID(), "chair", 100.0);
        productService.addProduct(product);

        cartService.addProductToCart(cart.getId(), product);
        userService.addOrderToUser(userId);

        User updatedUser = userRepository.getUserById(userId);
        assertFalse(updatedUser.getOrders().isEmpty(), "User should have at least one order");
        assertEquals(1, updatedUser.getOrders().size(), "User should have exactly one order");

        Cart updatedCart = cartService.getCartByUserId(userId);
        assertNotNull(updatedCart, "Cart should still exist after placing the order");
        assertTrue(updatedCart.getProducts().isEmpty(), "Cart should be empty after placing the order");
    }

    @Test
    void testAddOrderToUser_EmptyCart() {
        User user = new User(UUID.randomUUID(), "Moamen");
        userService.addUser(user);

        Exception exception = assertThrows(IllegalStateException.class, () -> userService.addOrderToUser(user.getId()));
        assertEquals("Cart is empty. Cannot place order.", exception.getMessage(), "Error message should match!");
    }

    @Test
    void testAddOrderToUser_UserNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertThrows(IllegalStateException.class,
                () -> userService.addOrderToUser(nonExistentUserId),
                "Expected an IllegalStateException when no cart is found!"
        );
    }



    // ----------------------------------------------------------------------
    // 7.2.2.6 - Empty Cart
    // ----------------------------------------------------------------------

    @Test
    void testEmptyCart_WithProducts() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(UUID.randomUUID(), userId, new ArrayList<>());

        Product product = new Product(UUID.randomUUID(), "bed", 14500.0);
        cart.getProducts().add(product);
        cartRepository.addCart(cart);

        Cart storedCart = cartRepository.getCartByUserId(userId);
        assertNotNull(storedCart, "Cart should exist");
        assertFalse(storedCart.getProducts().isEmpty(), "Cart should have products before emptying");

        userService.emptyCart(userId);

        Cart updatedCart = cartRepository.getCartByUserId(userId);
        assertTrue(updatedCart.getProducts().isEmpty(), "Cart should be empty after calling emptyCart");
    }


    @Test
    void testEmptyCart_AlreadyEmpty() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(UUID.randomUUID(), userId, new ArrayList<>());
        cartRepository.addCart(cart);

        assertTrue(cart.getProducts().isEmpty(), "Cart should already be empty");

        userService.emptyCart(userId);

        assertTrue(cart.getProducts().isEmpty(), "Cart should remain empty after calling emptyCart");
    }


    @Test
    void testEmptyCart_NonExistentCart() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.emptyCart(userId);
        }, "Expected emptyCart() to throw IllegalArgumentException for non-existent cart");
    }

    // ----------------------------------------------------------------------
    // 7.2.2.7 - Remove Order
    // ----------------------------------------------------------------------

    @Test
    void testRemoveOrderFromUser_Success() {
        User user = new User(UUID.randomUUID(), "Moamen With Orders");
        userService.addUser(user);

        Order order = new Order(UUID.randomUUID(), user.getId(), 500.0, new ArrayList<>());
        orderRepository.save(order);

        user.getOrders().add(order);
        userRepository.save(user);

        User savedUser = userService.getUserById(user.getId());
        assertNotNull(savedUser, "User should exist after saving");
        assertEquals(1, savedUser.getOrders().size(), "User should have 1 order before removal");

        System.out.println("User orders before removal: " + savedUser.getOrders());

        userService.removeOrderFromUser(user.getId(), order.getId());

        User updatedUser = userService.getUserById(user.getId());
        assertNotNull(updatedUser, "User should still exist");
        assertFalse(updatedUser.getOrders().contains(order), "Order should be removed");
        assertEquals(0, updatedUser.getOrders().size(), "User should have no orders left");

        System.out.println("User orders after removal: " + updatedUser.getOrders());
    }



    @Test
    void testRemoveOrderFromUser_NonExistentOrder() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Moamen With Orders");
        userService.addUser(user);

        Order existingOrder = new Order(UUID.randomUUID(), userId, 500.0, new ArrayList<>());
        user.getOrders().add(existingOrder);
        userRepository.save(user);

        User savedUser = userRepository.getUserById(userId);
        System.out.println(" User after saving: " + savedUser);

        assertNotNull(savedUser, "User should be retrieved from repository");
        assertEquals(1, savedUser.getOrders().size(), "User should have one order before testing removal");

        UUID nonExistentOrderId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.removeOrderFromUser(userId, nonExistentOrderId);
        });

        assertEquals("Order not found for user", exception.getMessage(), "Exception message should match");

        User updatedUser = userRepository.getUserById(userId);
        assertNotNull(updatedUser, "User should still exist");
        assertEquals(1, updatedUser.getOrders().size(), "User's order list should remain unchanged");
        assertTrue(updatedUser.getOrders().contains(existingOrder), "Existing order should not be removed");
    }
    @Test
    void testRemoveOrderFromUser_NonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            userService.removeOrderFromUser(nonExistentUserId, orderId);
        });

        assertEquals("User not found with ID: " + nonExistentUserId, exception.getMessage(),
                "Should throw exception when removing order from non-existent user");
    }


    // ----------------------------------------------------------------------
    // 7.2.2.8 - Delete User Tests
    // ----------------------------------------------------------------------
    @Test
    void testDeleteUserById_WithOrdersAndCart() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "User ");
        userService.addUser(user);

        Order order = new Order(UUID.randomUUID(), userId, 300.0, new ArrayList<>());
        orderRepository.save(order);
        user.getOrders().add(order);
        userRepository.save(user);



        assertNotNull(userService.getById(userId), "User should exist before deletion");
        assertFalse(userService.getOrdersByUserId(userId).isEmpty(), "User should have orders before deletion");
        assertNotNull(cartService.getCartByUserId(userId), "User should have a cart before deletion");

        userService.deleteUserById(userId);

        assertNull(userService.getById(userId), "User should be removed");
        assertTrue(userService.getOrdersByUserId(userId).isEmpty(), "User's orders should be removed");
        assertNull(cartService.getCartByUserId(userId), "User's cart should be removed");
    }
    @Test
    void testDeleteUserById() {
        User user = new User(UUID.randomUUID(), "User To Delete");
        userService.addUser(user);

        assertNotNull(userService.getById(user.getId()), "User should exist before deletion");

        userService.deleteUserById(user.getId());

        assertNull(userService.getById(user.getId()), "User should be removed");
    }

    @Test
    void testDeleteNonExistentUser() {
        UUID nonExistentId = UUID.randomUUID();

        assertNull(userService.getUserById(nonExistentId), "Non-existent user should not be found");

        Exception exception = assertThrows(IllegalStateException.class,
                () -> userService.deleteUserById(nonExistentId),
                "Expected an IllegalStateException when deleting a non-existent user"
        );

        assertEquals("User not found with ID: " + nonExistentId, exception.getMessage(),
                "Error message should match");
    }

 /////////////////////////////////////////////////////////////////////////////////////////////////////




    // ----------------------------------------------------------------------
    // 7.3.2.1 - add prou
    // ----------------------------------------------------------------------

 @Test
 void testAddProduct_ValidProduct() {
     Product product = new Product(null, "Laptop", 120000.0);

     Product addedProduct = productService.addProduct(product);

     assertNotNull(addedProduct, "Product should not be null after adding");
     assertNotNull(addedProduct.getId(), "Product ID should be assigned");
     assertEquals("Laptop", addedProduct.getName(), "Product name should match");
     assertEquals(120000.0, addedProduct.getPrice(), 0.01, "Product price should match");
 }


    @Test
    void testAddProduct_NullName() {
        Product product = new Product(null, null, 500.0);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(product);
        }, "Adding a product with a null name should throw an IllegalArgumentException");
    }




    @Test
    void testAddProduct_DuplicateProduct() {
        Product product1 = new Product(null, "haand", 1200.0);
        Product product2 = new Product(null, "haand", 1200.0);

        productService.addProduct(product1);


        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(product2);
        });

        assertEquals("Product with the same name already exists.", exception.getMessage());
    }

    // ----------------------------------------------------------------------
    // 7.3.2.2 - get all
    // ----------------------------------------------------------------------

    @Test
    void testGetProducts_NotEmpty() {
        Product product1 = new Product(UUID.randomUUID(), "poster", 500.0);
        Product product2 = new Product(UUID.randomUUID(), "charger", 800.0);
        productService.addProduct(product1);
        productService.addProduct(product2);

        List<Product> products = productService.getProducts();

        assertNotNull(products, "Products list should not be null");
        assertEquals(2, products.size(), "There should be 2 products in the list");
        assertTrue(products.contains(product1), "Product list should contain poster");
        assertTrue(products.contains(product2), "Product list should contain charger");
    }

    @Test
    void testGetProducts_EmptyList() {
       // userRepository.saveAll(new ArrayList<>());

        List<Product> products = productService.getProducts();

        assertNotNull(products, "Products list should not be null");
        assertTrue(products.isEmpty(), "Products list should be empty");
    }

    @Test
    void testGetProducts_AfterAddingAndDeletingProduct() {
        Product product = new Product(UUID.randomUUID(), "Tablet", 50000.0);
        productService.addProduct(product);

        List<Product> productsAfterAdd = productService.getProducts();
        assertTrue(productsAfterAdd.contains(product), "Product list should contain Tablet");

        productService.deleteProductById(product.getId());

        List<Product> productsAfterDelete = productService.getProducts();
        assertFalse(productsAfterDelete.contains(product), "Product list should not contain Tablet after deletion");
    }

    // ----------------------------------------------------------------------
    // 7.3.2.2 - get by id
    // ----------------------------------------------------------------------

    @Test
    void testGetProductById_ExistingProduct() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "table", 400.0);
        productRepository.addProduct(product);

        Product retrievedProduct = productService.getProductById(productId);

        assertNotNull(retrievedProduct, "Product should not be null");
        assertEquals(productId, retrievedProduct.getId(), "Product ID should match");
        assertEquals("table", retrievedProduct.getName(), "Product name should match");
    }

    @Test
    void testGetProductById_NonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();

        Product retrievedProduct = productService.getProductById(nonExistentProductId);

        assertNull(retrievedProduct, "Product should be null if it does not exist");
    }

    @Test
    void testGetProductById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> productService.getProductById(null),
                "Should throw IllegalArgumentException for null product ID");
    }

    // ----------------------------------------------------------------------
    // 7.3.2.3 - update
    // ----------------------------------------------------------------------
    @Test
    void testUpdateProduct_Success() {
        Product product = new Product(UUID.randomUUID(), "Old Laptop", 100000.0);
        productService.addProduct(product);

        Product updatedProduct = productService.updateProduct(product.getId(), "New Laptop", 120000.0);

        assertNotNull(updatedProduct, "Updated product should not be null");
        assertEquals("New Laptop", updatedProduct.getName(), "Product name should be updated");
        assertEquals(120000.0, updatedProduct.getPrice(), 0.01, "Product price should be updated");
    }
    @Test
    void testUpdateProduct_NonExistentProduct() {
        UUID nonExistentId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(nonExistentId, "New Name", 500.0);
        });

        assertEquals("Product not found", exception.getMessage(), "Error message should match");
    }
    @Test
    void testUpdateProduct_InvalidValues() {
        Product product = new Product(UUID.randomUUID(), "Old Name", 900.0);
        productService.addProduct(product);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(null, "New Name", 1000.0);
        }, "Should throw error for null product ID");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(product.getId(), "", 1000.0);
        }, "Should throw error for empty product name");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(product.getId(), "New Name", -10.0);
        }, "Should throw error for negative price");
    }

    // ----------------------------------------------------------------------
    // 7.3.2.4 - discount
    //

    @Test
    void testApplyDiscount_Success() {
        Product product1 = new Product(UUID.randomUUID(), "camera", 200000.0);
        Product product2 = new Product(UUID.randomUUID(), "piano", 150000.0);
        productService.addProduct(product1);
        productService.addProduct(product2);

        ArrayList<UUID> productIds = new ArrayList<>(List.of(product1.getId(), product2.getId()));

        productService.applyDiscount(10, productIds);

        Product updatedProduct1 = productService.getProductById(product1.getId());
        Product updatedProduct2 = productService.getProductById(product2.getId());

        assertEquals(180000, updatedProduct1.getPrice(), 0.01);
        assertEquals(135000, updatedProduct2.getPrice(), 0.01);
    }


    @Test
    void testApplyDiscount_InvalidDiscount() {
        ArrayList<UUID> productIds = new ArrayList<>(List.of(UUID.randomUUID()));

        assertThrows(IllegalArgumentException.class, () -> {
            productService.applyDiscount(-5, productIds);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            productService.applyDiscount(150, productIds);
        });
    }

    @Test
    void testApplyDiscount_NonExistentProducts() {
        ArrayList<UUID> productIds = new ArrayList<>(List.of(UUID.randomUUID(), UUID.randomUUID()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.applyDiscount(10, productIds);
        });

        assertEquals("No matching products found", exception.getMessage());
    }

    // ----------------------------------------------------------------------
    // 7.3.2.5 - delete
    //

    @Test
    void testDeleteProductById_Success() {
        Product product = new Product(UUID.randomUUID(), "Headphones", 1500.0);
        productService.addProduct(product);

        productService.deleteProductById(product.getId());

        assertNull(productService.getProductById(product.getId()), "Product should be deleted");
    }


    @Test
    void testDeleteProductById_NonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProductById(nonExistentProductId);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void testDeleteProductById_NullId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProductById(null);
        });

        assertEquals("Product ID can't be null", exception.getMessage());
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    // ----------------------------------------------------------------------
    // 7.3.3.1 - add cart
    //-----------------------------------------------------------------------

    @Test
    void testAddCart_Success() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId);

        Cart addedCart = cartService.addCart(cart);

        assertNotNull(addedCart, "Added cart should not be null");
        assertEquals(cartId, addedCart.getId(), "Cart ID should match");
        assertEquals(userId, addedCart.getUserId(), "User ID should match");

        Cart retrievedCart = cartService.getCartById(cartId);
        assertNotNull(retrievedCart, "Cart should be retrievable after adding");
    }
    @Test
    void testAddCart_NullUserId() {
        Cart cart = new Cart(UUID.randomUUID(), null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addCart(cart);
        });

        assertEquals("Cart must have a userId", exception.getMessage(), "Should throw an exception for null userId");
    }
    @Test
    void testAddCart_DuplicateUserCart() {
        UUID userId = UUID.randomUUID();
        Cart cart1 = new Cart(UUID.randomUUID(), userId);
        Cart cart2 = new Cart(UUID.randomUUID(), userId);

        cartService.addCart(cart1);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            cartService.addCart(cart2);
        });

        assertEquals("User already has a cart", exception.getMessage(), "Should not allow duplicate carts for the same user");
    }

    // ----------------------------------------------------------------------
    // 7.3.3.2 - get all carts
    //

    @Test
    void testGetCarts_NotEmptyList() {
        Cart cart1 = new Cart(UUID.randomUUID(), UUID.randomUUID());
        Cart cart2 = new Cart(UUID.randomUUID(), UUID.randomUUID());

        cartRepository.addCart(cart1);
        cartRepository.addCart(cart2);

        ArrayList<Cart> carts = cartService.getCarts();

        assertNotNull(carts, "Carts list should not be null");
        assertEquals(2, carts.size(), "There should be 2 carts");
        assertTrue(carts.contains(cart1), "Cart1 should be in the list");
        assertTrue(carts.contains(cart2), "Cart2 should be in the list");
    }
    @Test
    void testGetCarts_EmptyList() {
        cartRepository.saveAll(new ArrayList<>());

        ArrayList<Cart> carts = cartService.getCarts();

        assertNotNull(carts, "Carts list should not be null");
        assertTrue(carts.isEmpty(), "Carts list should be empty");
    }
    @Test
    void testGetCarts_ReturnsNewInstance() {
        cartRepository.addCart(new Cart(UUID.randomUUID(), UUID.randomUUID()));

        ArrayList<Cart> firstCall = cartService.getCarts();
        ArrayList<Cart> secondCall = cartService.getCarts();

        assertNotSame(firstCall, secondCall, "Each call to getCarts() should return a new instance");
    }

    // ----------------------------------------------------------------------
    // 7.3.3.3 - get by id
    //-----------------------------------------------------------------------



    @Test
    void testGetCartById_CartExists() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId);

        cartRepository.addCart(cart);

        Cart retrievedCart = cartService.getCartById(cartId);

        assertNotNull(retrievedCart, "Cart should not be null");
        assertEquals(cartId, retrievedCart.getId(), "Cart ID should match");
        assertEquals(userId, retrievedCart.getUserId(), "User ID should match");
    }

    @Test
    void testGetCartById_CartNotFound() {
        UUID nonExistentCartId = UUID.randomUUID();

        Cart retrievedCart = cartService.getCartById(nonExistentCartId);

        assertNull(retrievedCart, "Cart should be null if it does not exist");
    }

    @Test
    void testGetCartById_NullId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.getCartById(null);
        });

        assertEquals("Cart ID cannot be null", exception.getMessage(), "Should throw an exception when passing null");
    }
    // ----------------------------------------------------------------------
    // 7.3.3.4 - get user's cart
    //-----------------------------------------------------------------------

    @Test
    void testGetCartByUserId_UserHasCart() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId);

        cartRepository.addCart(cart);

        Cart retrievedCart = cartService.getCartByUserId(userId);

        assertNotNull(retrievedCart, "Cart should not be null");
        assertEquals(cartId, retrievedCart.getId(), "Cart ID should match");
        assertEquals(userId, retrievedCart.getUserId(), "User ID should match");
    }

    @Test
    void testGetCartByUserId_UserHasNoCart() {
        UUID userId = UUID.randomUUID();

        Cart retrievedCart = cartService.getCartByUserId(userId);

        assertNull(retrievedCart, "Cart should be null if the user has no cart");
    }

    @Test
    void testGetCartByUserId_NullUserId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.getCartByUserId(null);
        });

        assertEquals("User ID cannot be null", exception.getMessage(), "Should throw an exception when passing null");
    }
    // ----------------------------------------------------------------------
    // 7.3.3.5 - add pro to cart
    //-----------------------------------------------------------------------
    @Test
    void testAddProductToCart_Success() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId);
        cartRepository.addCart(cart);

        Product product = new Product(UUID.randomUUID(), "VR", 4200.0);
        productService.addProduct(product);

        cartService.addProductToCart(cartId, product);

        Cart updatedCart = cartService.getCartById(cartId);

        System.out.println("Updated Cart: " + updatedCart);

        assertNotNull(updatedCart, "Cart should exist");
        assertFalse(updatedCart.getProducts().isEmpty(), "Cart should contain products");
        assertEquals(1, updatedCart.getProducts().size(), "Cart should contain exactly one product");
    }

    @Test
    void testAddProductToCart_NonExistentCart() {
        Product product = new Product(UUID.randomUUID(), "iPhone", 8000.0);
        productService.addProduct(product);

        UUID nonExistentCartId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addProductToCart(nonExistentCartId, product);
        });

        assertEquals("Cart not found!", exception.getMessage(), "Should throw exception for non-existent cart");
    }

    @Test
    void testAddProductToCart_NullProduct() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, userId);
        cartRepository.addCart(cart);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.addProductToCart(cartId, null);
        });

        assertEquals("Product cannot be null", exception.getMessage(), "Should throw exception for null product");
    }

    // ----------------------------------------------------------------------
    // 7.3.3.6 - delete pro from cart
    //-----------------------------------------------------------------------
    @Test
    void testDeleteProductFromCart_Success() {
        User user = new User(UUID.randomUUID(), "Test User");
        userService.addUser(user);

        Cart cart = new Cart(user.getId(), UUID.randomUUID());
        cartService.addCart(cart);
        UUID cartId = cart.getId();

        Product product = new Product(UUID.randomUUID(), "Headphones", 250.0);
        cartService.addProductToCart(cartId, product);
        cartRepository.save(cart);
        System.out.println("All carts after saving: " + cartRepository.findAll());

        cartService.deleteProductFromCart(cartId, product);

        Cart updatedCart = cartService.getCartById(cartId);
        assertNotNull(updatedCart, "Cart should still exist");
        assertTrue(updatedCart.getProducts().isEmpty(), "Cart should be empty after removing the product");
    }


    @Test
    void testDeleteProductFromCart_ProductNotInCart() {
        User user = new User(UUID.randomUUID(), "Test User");
        userService.addUser(user);

          Cart cart = cartService.getCartByUserId(user.getId());
        assertNotNull(cart, "Cart should be created automatically");
        UUID cartId = cart.getId();

        Product existingProduct = new Product(UUID.randomUUID(), "watch", 1500.0);
        cart.getProducts().add(existingProduct);
        cartRepository.save(cart);

        Product nonExistentProduct = new Product(UUID.randomUUID(), "Mouse", 500.0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.deleteProductFromCart(cartId, nonExistentProduct);
        });

        assertEquals("Cart is empty or products list is null!", exception.getMessage());
    }

    @Test
    void testDeleteProductFromCart_NonExistentCart() {
        UUID nonExistentCartId = UUID.randomUUID();
        Product product = new Product(UUID.randomUUID(), "Keyboard", 1200.0);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.deleteProductFromCart(nonExistentCartId, product);
        });

        assertEquals("Cart not found!", exception.getMessage(), "Should throw exception for non-existent cart");
    }

    // ----------------------------------------------------------------------
    // 7.3.3.7 - delete the cart by id
    //-----------------------------------------------------------------------

    @Test
    void testDeleteCartById_Success() {
        // Arrange
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart(cartId, UUID.randomUUID());
        cartRepository.addCart(cart);

        cartService.deleteCartById(cartId);

        Cart deletedCart = cartService.getCartById(cartId);
        assertNull(deletedCart, "Cart should be deleted");
    }


    @Test
    void testDeleteCartById_NonExistentCart() {
        UUID nonExistentCartId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.deleteCartById(nonExistentCartId);
        });

        assertEquals("Cart not found", exception.getMessage(), "Should throw exception for non-existent cart");
    }

    @Test
    void testDeleteCartById_OnlyRemovesSpecifiedCart() {
        UUID cartIdToDelete = UUID.randomUUID();
        UUID cartIdToKeep = UUID.randomUUID();
        Cart cartToDelete = new Cart(cartIdToDelete, UUID.randomUUID());
        Cart cartToKeep = new Cart(cartIdToKeep, UUID.randomUUID());

        cartRepository.addCart(cartToDelete);
        cartRepository.addCart(cartToKeep);

        cartService.deleteCartById(cartIdToDelete);

        assertNull(cartService.getCartById(cartIdToDelete), "Deleted cart should not exist");
        assertNotNull(cartService.getCartById(cartIdToKeep), "Other cart should still exist");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////




    // ----------------------------------------------------------------------
    // 7.5.2.1 - add order
    // ----------------------------------------------------------------------

    @Test
    void testAddOrder_Success() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 500.0, new ArrayList<>());

        orderService.addOrder(order);

        Order retrievedOrder = orderService.getOrderById(order.getId());
        assertNotNull(retrievedOrder, "Order should be successfully added");
        assertEquals(order.getId(), retrievedOrder.getId(), "Order ID should match");
        assertEquals(500.0, retrievedOrder.getTotalPrice(), 0.01, "Total price should match");
    }

    @Test
    void testAddOrder_NullId() {
        Order order = new Order(null, UUID.randomUUID(), 300.0, new ArrayList<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.addOrder(order);
        });

        assertEquals("Order ID cannot be null", exception.getMessage(), "Should throw exception for null order ID");
    }

    @Test
    void testAddOrder_DuplicateOrder() {
        // Arrange
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 700.0, new ArrayList<>());
        orderService.addOrder(order);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.addOrder(order);
        });

        assertEquals("Order with this ID already exists", exception.getMessage(), "Should throw exception for duplicate order");
    }



    // ----------------------------------------------------------------------
    // 7.5.2.2 - get all orders
    // ----------------------------------------------------------------------
    @Test
    void testGetOrders_NotEmpty() {
        Order order1 = new Order(UUID.randomUUID(), UUID.randomUUID(), 500.0, new ArrayList<>());
        Order order2 = new Order(UUID.randomUUID(), UUID.randomUUID(), 1000.0, new ArrayList<>());

        orderService.addOrder(order1);
        orderService.addOrder(order2);

        List<Order> orders = orderService.getOrders();

        assertNotNull(orders, "Orders list should not be null");
        assertEquals(2, orders.size(), "Orders list should contain 2 orders");
        assertTrue(orders.contains(order1), "Orders should contain order1");
        assertTrue(orders.contains(order2), "Orders should contain order2");
    }

    @Test
    void testGetOrders_EmptyList() {
        List<Order> orders = orderService.getOrders();

        assertNotNull(orders, "Orders list should not be null");
        assertTrue(orders.isEmpty(), "Orders list should be empty when no orders exist");
    }

    @Test
    void testGetOrders_ReturnsNewInstance() {
        orderService.addOrder(new Order(UUID.randomUUID(), UUID.randomUUID(), 300.0, new ArrayList<>()));

        List<Order> firstCall = orderService.getOrders();
        List<Order> secondCall = orderService.getOrders();

        assertNotSame(firstCall, secondCall, "Each call to getOrders() should return a new instance");
    }

    // ----------------------------------------------------------------------
    // 7.5.2.3 - get by id
    // ----------------------------------------------------------------------
    @Test
    void testGetOrderById_ExistingOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(orderId, UUID.randomUUID(), 500.0, new ArrayList<>());
        orderService.addOrder(order);

        Order retrievedOrder = orderService.getOrderById(orderId);

        assertNotNull(retrievedOrder, "Order should not be null");
        assertEquals(orderId, retrievedOrder.getId(), "Order ID should match");
        assertEquals(500.0, retrievedOrder.getTotalPrice(), 0.01, "Order total price should match");
    }

    @Test
    void testGetOrderById_NonExistentOrder() {
        UUID nonExistentOrderId = UUID.randomUUID();

        Order retrievedOrder = orderService.getOrderById(nonExistentOrderId);

        assertNull(retrievedOrder, "Order should be null if it does not exist");
    }

    @Test
    void testGetOrderById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(null),
                "Should throw IllegalArgumentException for null order ID");
    }


    // ----------------------------------------------------------------------
    // 7.5.2.4 - delete by id
    // ----------------------------------------------------------------------

    @Test
    void testDeleteOrderById_Success() {
        Order order = new Order(UUID.randomUUID(), UUID.randomUUID(), 500.0, new ArrayList<>());
        orderService.addOrder(order);

        orderService.deleteOrderById(order.getId());

        assertNull(orderService.getOrderById(order.getId()), "Order should be removed after deletion");
    }

    @Test
    void testDeleteOrderById_NonExistentOrder() {
        UUID nonExistentOrderId = UUID.randomUUID();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.deleteOrderById(nonExistentOrderId);
        });

        assertEquals("Order not found", exception.getMessage(), "Should throw exception for non-existent order");
    }

    @Test
    void testDeleteOrderById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> orderService.deleteOrderById(null),
                "Should throw IllegalArgumentException for null order ID");
    }
}