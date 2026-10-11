
package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Homepage and product search
    @GetMapping("/products")
    public String showProducts(
            @RequestParam(required = false) String search,
            Model model) {

        List<Product> products;

        if (search == null || search.trim().isEmpty()) {

            products = productRepository.findByActiveTrue();

        } else {

            products = productRepository
                    .findByActiveTrueAndProductNameContainingIgnoreCase(
                            search.trim()
                    );
        }

        Map<Integer, List<Product>> productsByCategory =
                products.stream()
                        .filter(product -> product.getCategoryId() != null)
                        .collect(Collectors.groupingBy(
                                Product::getCategoryId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        model.addAttribute("products", products);
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("search", search);

        return "index";
    }
}
