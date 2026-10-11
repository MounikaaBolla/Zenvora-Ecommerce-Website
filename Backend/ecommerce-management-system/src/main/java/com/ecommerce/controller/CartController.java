package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartController(
            CartRepository cartRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }


    // ==================================================
    // ADD PRODUCT TO CART
    // ==================================================

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Integer productId,
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        // User must be logged in
        if (userId == null) {
            return "redirect:/login";
        }


        // Find product
        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);

        if (product == null) {
            return "redirect:/products";
        }


        // Check whether product is already in cart
        List<Cart> cartItems =
                cartRepository.findByUserId(userId);

        Cart existingCart = null;

        for (Cart cart : cartItems) {

            if (cart.getProductId().equals(productId)) {

                existingCart = cart;
                break;
            }
        }


        // ==============================================
        // PRODUCT ALREADY IN CART
        // ==============================================

        if (existingCart != null) {

            existingCart.setQuantity(
                    existingCart.getQuantity() + 1
            );

            cartRepository.save(existingCart);

        }

        // ==============================================
        // NEW PRODUCT
        // ==============================================

        else {

            Cart cart = new Cart();

            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(1);

            cartRepository.save(cart);
        }


        return "redirect:/cart";
    }


    // ==================================================
    // VIEW CART
    // ==================================================

    @GetMapping("/cart")
    public String viewCart(
            HttpSession session,
            Model model) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }


        List<Cart> cartItems =
                cartRepository.findByUserId(userId);

        List<Product> products =
                new ArrayList<>();

        double grandTotal = 0;


        for (Cart item : cartItems) {

            Product product =
                    productRepository
                            .findById(item.getProductId())
                            .orElse(null);

            products.add(product);


            if (product != null) {

                grandTotal +=
                        product.getPrice()
                        * item.getQuantity();
            }
        }


        model.addAttribute(
                "cartItems",
                cartItems
        );

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "grandTotal",
                grandTotal
        );


        return "cart";
    }


    // ==================================================
    // INCREASE QUANTITY
    // ==================================================

    @PostMapping("/cart/increase")
    public String increaseQuantity(
            @RequestParam Integer cartId,
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }


        Cart cart =
                cartRepository
                        .findById(cartId)
                        .orElse(null);


        if (cart != null
                && cart.getUserId().equals(userId)) {

            cart.setQuantity(
                    cart.getQuantity() + 1
            );

            cartRepository.save(cart);
        }


        return "redirect:/cart";
    }


    // ==================================================
    // DECREASE QUANTITY
    // ==================================================

    @PostMapping("/cart/decrease")
    public String decreaseQuantity(
            @RequestParam Integer cartId,
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }


        Cart cart =
                cartRepository
                        .findById(cartId)
                        .orElse(null);


        if (cart != null
                && cart.getUserId().equals(userId)) {

            if (cart.getQuantity() > 1) {

                cart.setQuantity(
                        cart.getQuantity() - 1
                );

                cartRepository.save(cart);

            } else {

                cartRepository.delete(cart);
            }
        }


        return "redirect:/cart";
    }


    // ==================================================
    // REMOVE FROM CART
    // ==================================================

    @PostMapping("/cart/remove")
    public String removeFromCart(
            @RequestParam Integer cartId,
            HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }


        Cart cart =
                cartRepository
                        .findById(cartId)
                        .orElse(null);


        if (cart != null
                && cart.getUserId().equals(userId)) {

            cartRepository.delete(cart);
        }


        return "redirect:/cart";
    }

}