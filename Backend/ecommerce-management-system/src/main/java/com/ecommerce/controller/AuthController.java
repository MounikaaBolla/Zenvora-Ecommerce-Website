package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // ==============================
    // REGISTER PAGE
    // ==============================

    @GetMapping("/register")
    public String showRegisterPage() {

        return "register";
    }


    // ==============================
    // REGISTER USER
    // ==============================

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            Model model) {

        if (userRepository.findByEmail(email).isPresent()) {

            model.addAttribute(
                    "error",
                    "Email already registered!"
            );

            return "register";
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);

        user.setRole("CUSTOMER");

        userRepository.save(user);

        return "redirect:/login";
    }


    // ==============================
    // LOGIN PAGE
    // ==============================

    @GetMapping("/login")
    public String showLoginPage() {

        return "login";
    }


    // ==============================
    // LOGIN USER
    // ==============================

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        var userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Invalid email or password!"
            );

            return "login";
        }

        User user = userOptional.get();


        // ==============================
        // CHECK PASSWORD
        // ==============================

        if (!user.getPassword().equals(password)) {

            model.addAttribute(
                    "error",
                    "Invalid email or password!"
            );

            return "login";
        }


        // ==============================
        // STORE USER SESSION
        // ==============================

        session.setAttribute(
                "userId",
                user.getUserId()
        );

        session.setAttribute(
                "userName",
                user.getFullName()
        );

        session.setAttribute(
                "role",
                user.getRole()
        );


        // ==============================
        // ADMIN REDIRECT
        // ==============================

        if (user.getRole() != null
                && user.getRole().equalsIgnoreCase("ADMIN")) {

            return "redirect:/admin";
        }


        // ==============================
        // CUSTOMER REDIRECT
        // ==============================

        return "redirect:/products";
    }


    // ==============================
    // LOGOUT
    // ==============================

    @GetMapping("/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }

}