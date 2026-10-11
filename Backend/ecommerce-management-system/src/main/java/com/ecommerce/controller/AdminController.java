package com.ecommerce.controller;

import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminController(
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository) {

        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }


    // ==============================
    // ADMIN DASHBOARD
    // ==============================

    @GetMapping("/admin")
    public String adminDashboard(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");


        // ==============================
        // ADMIN SECURITY
        // ==============================

        if (userId == null
                || role == null
                || !role.equalsIgnoreCase("ADMIN")) {

            return "redirect:/";
        }


        // ==============================
        // DASHBOARD COUNTS
        // ==============================

        long totalProducts =
                productRepository.count();

        long totalUsers =
                userRepository.count();

        long totalOrders =
                orderRepository.count();


        model.addAttribute(
                "totalProducts",
                totalProducts
        );

        model.addAttribute(
                "totalUsers",
                totalUsers
        );

        model.addAttribute(
                "totalOrders",
                totalOrders
        );


        return "admin";
    }
}