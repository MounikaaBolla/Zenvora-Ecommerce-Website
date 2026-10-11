package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderController(
            CartRepository cartRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }


    // =========================
    // CHECKOUT
    // =========================

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        List<Cart> cartItems =
                cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<Product> products = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;


        for (Cart cart : cartItems) {

            Product product =
                    productRepository
                            .findById(cart.getProductId())
                            .orElse(null);

            products.add(product);


            if (product != null) {

                BigDecimal itemTotal =
                        BigDecimal.valueOf(product.getPrice())
                                .multiply(
                                        BigDecimal.valueOf(
                                                cart.getQuantity()
                                        )
                                );

                total = total.add(itemTotal);
            }
        }


        model.addAttribute("cartItems", cartItems);

        model.addAttribute("products", products);

        model.addAttribute("total", total);


        return "checkout";
    }


    // =========================
    // PLACE ORDER
    // =========================

    @PostMapping("/order/place")
    public String placeOrder(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");


        if (userId == null) {
            return "redirect:/login";
        }


        List<Cart> cartItems =
                cartRepository.findByUserId(userId);


        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }


        BigDecimal total =
                BigDecimal.ZERO;


        // =========================
        // CHECK STOCK FIRST
        // =========================

        for (Cart cart : cartItems) {

            Product product =
                    productRepository
                            .findById(cart.getProductId())
                            .orElse(null);


            if (product == null) {
                return "redirect:/cart";
            }


            if (cart.getQuantity() > product.getQuantity()) {

                model.addAttribute(
                        "error",
                        "Insufficient stock for product: "
                                + product.getProductName()
                );

                return "cart";
            }


            BigDecimal itemTotal =
                    BigDecimal.valueOf(product.getPrice())
                            .multiply(
                                    BigDecimal.valueOf(
                                            cart.getQuantity()
                                    )
                            );


            total = total.add(itemTotal);
        }


        // =========================
        // CREATE ORDER
        // =========================

        Order order = new Order();

        order.setUserId(userId);

        order.setTotalAmount(total);


        Order savedOrder =
                orderRepository.save(order);


        // =========================
        // CREATE ORDER ITEMS
        // AND REDUCE STOCK
        // =========================

        for (Cart cart : cartItems) {

            Product product =
                    productRepository
                            .findById(cart.getProductId())
                            .orElse(null);


            if (product != null) {

                OrderItem orderItem =
                        new OrderItem();


                orderItem.setOrderId(
                        savedOrder.getOrderId()
                );


                orderItem.setProductId(
                        product.getProductId()
                );


                orderItem.setQuantity(
                        cart.getQuantity()
                );


                orderItem.setPrice(
                        BigDecimal.valueOf(
                                product.getPrice()
                        )
                );


                orderItemRepository.save(
                        orderItem
                );


                // =========================
                // REDUCE PRODUCT STOCK
                // =========================

                int remainingStock =
                        product.getQuantity()
                                - cart.getQuantity();


                product.setQuantity(
                        remainingStock
                );


                productRepository.save(
                        product
                );
            }
        }


        // =========================
        // CLEAR CART
        // =========================

        cartRepository.deleteAll(
                cartItems
        );


        // =========================
        // PAYMENT SESSION DATA
        // =========================

        session.setAttribute(
                "paymentOrderId",
                savedOrder.getOrderId()
        );


        session.setAttribute(
                "paymentTotal",
                total
        );


        return "redirect:/payment";
    }


    // =========================
    // PAYMENT
    // =========================

    @GetMapping("/payment")
    public String payment(
            HttpSession session,
            Model model) {

        Integer orderId =
                (Integer) session.getAttribute(
                        "paymentOrderId"
                );


        BigDecimal total =
                (BigDecimal) session.getAttribute(
                        "paymentTotal"
                );


        if (orderId == null || total == null) {
            return "redirect:/cart";
        }


        model.addAttribute(
                "orderId",
                orderId
        );


        model.addAttribute(
                "totalAmount",
                total
        );


        return "payment";
    }


    // =========================
    // ORDER SUCCESS
    // =========================

    @GetMapping("/order-success")
    public String orderSuccess(
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute(
                        "userId"
                );


        if (userId == null) {
            return "redirect:/login";
        }


        return "order-success";
    }


    // =========================
    // MY ORDERS
    // =========================

    @GetMapping("/orders")
    public String myOrders(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute(
                        "userId"
                );


        if (userId == null) {
            return "redirect:/login";
        }


        List<Order> orders =
                orderRepository
                        .findByUserIdOrderByOrderDateDesc(
                                userId
                        );


        model.addAttribute(
                "orders",
                orders
        );


        List<List<OrderItem>> allOrderItems =
                new ArrayList<>();


        List<List<Product>> allProducts =
                new ArrayList<>();


        for (Order order : orders) {

            List<OrderItem> orderItems =
                    orderItemRepository
                            .findByOrderId(
                                    order.getOrderId()
                            );


            allOrderItems.add(
                    orderItems
            );


            List<Product> products =
                    new ArrayList<>();


            for (OrderItem item : orderItems) {

                Product product =
                        productRepository
                                .findById(
                                        item.getProductId()
                                )
                                .orElse(null);


                products.add(
                        product
                );
            }


            allProducts.add(
                    products
            );
        }


        model.addAttribute(
                "allOrderItems",
                allOrderItems
        );


        model.addAttribute(
                "allProducts",
                allProducts
        );


        return "orders";
    }
}