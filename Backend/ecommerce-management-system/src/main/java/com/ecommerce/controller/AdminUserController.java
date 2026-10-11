package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/users")
    public String viewUsers(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        if (userId == null
                || role == null
                || !role.equalsIgnoreCase("ADMIN")) {

            return "redirect:/";
        }

        List<User> users =
                userRepository.findAll();

        model.addAttribute("users", users);

        return "admin-users";
    }
}