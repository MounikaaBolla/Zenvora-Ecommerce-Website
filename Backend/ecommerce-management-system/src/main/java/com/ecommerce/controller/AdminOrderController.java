package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Payment;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;

import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public AdminOrderController(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PaymentRepository paymentRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }


    // =========================================================
    // ADMIN ORDERS PAGE
    // =========================================================

    @GetMapping("/admin/orders")
    public String adminOrders(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");


        // Security check
        if (userId == null) {
            return "redirect:/login";
        }

        if (role == null ||
                !role.equalsIgnoreCase("ADMIN")) {

            return "redirect:/products";
        }


        // Get all orders
        List<Order> orders =
                orderRepository
                        .findAllByOrderByOrderDateDesc();


        // -----------------------------------------------------
        // Customer details
        // -----------------------------------------------------

        Map<Integer, User> customers =
                new HashMap<>();

        for (Order order : orders) {

            if (order.getUserId() != null) {

                User customer =
                        userRepository
                                .findById(order.getUserId())
                                .orElse(null);

                if (customer != null) {
                    customers.put(
                            order.getUserId(),
                            customer
                    );
                }
            }
        }


        // -----------------------------------------------------
        // Payment details
        // -----------------------------------------------------

        Map<Integer, Payment> payments =
                new HashMap<>();

        for (Order order : orders) {

            List<Payment> orderPayments =
                    paymentRepository
                            .findByOrderId(order.getOrderId());

            if (orderPayments != null &&
                    !orderPayments.isEmpty()) {

                // Latest payment record
                Payment payment =
                        orderPayments
                                .get(orderPayments.size() - 1);

                payments.put(
                        order.getOrderId(),
                        payment
                );
            }
        }


        // -----------------------------------------------------
        // Order items
        // -----------------------------------------------------

        Map<Integer, List<OrderItem>> orderItems =
                new HashMap<>();

        for (Order order : orders) {

            List<OrderItem> items =
                    orderItemRepository
                            .findByOrderId(
                                    order.getOrderId()
                            );

            orderItems.put(
                    order.getOrderId(),
                    items
            );
        }


        // -----------------------------------------------------
        // Product details for every order item
        // -----------------------------------------------------

        Map<Integer, Product> products =
                new HashMap<>();

        for (List<OrderItem> items : orderItems.values()) {

            for (OrderItem item : items) {

                if (item.getProductId() != null) {

                    Product product =
                            productRepository
                                    .findById(
                                            item.getProductId()
                                    )
                                    .orElse(null);

                    if (product != null) {

                        products.put(
                                item.getProductId(),
                                product
                        );
                    }
                }
            }
        }


        // Send data to Thymeleaf
        model.addAttribute(
                "orders",
                orders
        );

        model.addAttribute(
                "customers",
                customers
        );

        model.addAttribute(
                "payments",
                payments
        );

        model.addAttribute(
                "orderItems",
                orderItems
        );

        model.addAttribute(
                "products",
                products
        );


        return "admin-orders";
    }


    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @PostMapping("/admin/orders/update-status")
    public String updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam String status,
            HttpSession session) {


        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");


        // Security check
        if (userId == null) {
            return "redirect:/login";
        }

        if (role == null ||
                !role.equalsIgnoreCase("ADMIN")) {

            return "redirect:/products";
        }


        // Only allow valid order statuses
        if (!status.equals("PLACED") &&
                !status.equals("CONFIRMED") &&
                !status.equals("SHIPPED") &&
                !status.equals("DELIVERED") &&
                !status.equals("CANCELLED")) {

            return "redirect:/admin/orders";
        }


        Order order =
                orderRepository
                        .findById(orderId)
                        .orElse(null);


        if (order != null) {

            order.setStatus(status);

            orderRepository.save(order);
        }


        return "redirect:/admin/orders";
    }
}