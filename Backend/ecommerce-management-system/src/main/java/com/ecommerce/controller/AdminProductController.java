
package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final JdbcTemplate jdbcTemplate;

    // =====================================================
    // PRODUCT IMAGE FOLDER
    // =====================================================

    private final Path uploadDirectory =
            Paths.get(
                    System.getProperty("user.dir"),
                    "src",
                    "main",
                    "resources",
                    "static",
                    "images",
                    "products"
            ).toAbsolutePath();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminProductController(
            ProductRepository productRepository,
            CartRepository cartRepository,
            JdbcTemplate jdbcTemplate) {

        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // ADMIN CHECK
    // =====================================================

    private boolean isAdmin(HttpSession session) {

        Integer userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        return userId != null
                && role != null
                && role.equalsIgnoreCase("ADMIN");
    }

    // =====================================================
    // VIEW ALL PRODUCTS
    // =====================================================

    @GetMapping("/admin/products")
    public String viewProducts(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        List<Product> products =
                productRepository.findAll();

        model.addAttribute("products", products);

        return "admin-products";
    }

    // =====================================================
    // ADD PRODUCT PAGE
    // =====================================================

    @GetMapping("/admin/products/add")
    public String addProductPage(
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        return "admin-add-product";
    }

    // =====================================================
    // ADD PRODUCT + IMAGE
    // =====================================================

    @PostMapping("/admin/products/add")
    public String addProduct(
            @RequestParam String productName,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam Integer categoryId,
            @RequestParam(value = "image", required = false)
                    MultipartFile image,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        try {

            Files.createDirectories(uploadDirectory);

            Product product = new Product();

            product.setProductName(productName);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setCategoryId(categoryId);

            if (image != null && !image.isEmpty()) {

                String originalFileName =
                        image.getOriginalFilename();

                String extension = "";

                if (originalFileName != null
                        && originalFileName.contains(".")) {

                    extension =
                            originalFileName.substring(
                                    originalFileName.lastIndexOf(".")
                            );
                }

                String fileName =
                        UUID.randomUUID() + extension;

                Path imagePath =
                        uploadDirectory.resolve(fileName).normalize();

                // Prevent writing outside the product image folder.
                if (!imagePath.startsWith(uploadDirectory.normalize())) {
                    return "redirect:/admin/products/add?error=image";
                }

                Files.copy(image.getInputStream(), imagePath);

                product.setImageName(fileName);
            }

            productRepository.save(product);

        } catch (IOException e) {

            e.printStackTrace();

            return "redirect:/admin/products/add?error=image";
        }

        return "redirect:/admin/products?success=added";
    }

    // =====================================================
    // EDIT PRODUCT PAGE
    // =====================================================

    @GetMapping("/admin/products/edit")
    public String editProductPage(
            @RequestParam Integer productId,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);

        if (product == null) {
            return "redirect:/admin/products?error=not-found";
        }

        model.addAttribute("product", product);

        return "admin-edit-product";
    }

    // =====================================================
    // UPDATE PRODUCT + OPTIONAL IMAGE
    // =====================================================

    @PostMapping("/admin/products/update")
    public String updateProduct(
            @RequestParam Integer productId,
            @RequestParam String productName,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam Integer categoryId,
            @RequestParam(value = "image", required = false)
                    MultipartFile image,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);

        if (product == null) {
            return "redirect:/admin/products?error=not-found";
        }

        try {

            product.setProductName(productName);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setCategoryId(categoryId);

            String oldImageName = product.getImageName();
            String newImageName = null;
            Path newImagePath = null;

            if (image != null && !image.isEmpty()) {

                Files.createDirectories(uploadDirectory);

                String originalFileName =
                        image.getOriginalFilename();

                String extension = "";

                if (originalFileName != null
                        && originalFileName.contains(".")) {

                    extension =
                            originalFileName.substring(
                                    originalFileName.lastIndexOf(".")
                            );
                }

                newImageName =
                        UUID.randomUUID() + extension;

                newImagePath =
                        uploadDirectory.resolve(newImageName).normalize();

                if (!newImagePath.startsWith(uploadDirectory.normalize())) {
                    return "redirect:/admin/products?error=image";
                }

                Files.copy(image.getInputStream(), newImagePath);

                product.setImageName(newImageName);
            }

            try {

                productRepository.saveAndFlush(product);

            } catch (RuntimeException e) {

                // Remove the newly uploaded file if saving fails.
                if (newImagePath != null) {
                    try {
                        Files.deleteIfExists(newImagePath);
                    } catch (IOException cleanupError) {
                        cleanupError.printStackTrace();
                    }
                }

                throw e;
            }

            // Delete the previous image only after the save succeeds.
            if (newImageName != null
                    && oldImageName != null
                    && !oldImageName.isEmpty()
                    && !oldImageName.equals(newImageName)) {

                try {

                    Path oldImage =
                            uploadDirectory.resolve(oldImageName).normalize();

                    if (oldImage.startsWith(uploadDirectory.normalize())) {
                        Files.deleteIfExists(oldImage);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {

            e.printStackTrace();

            return "redirect:/admin/products?error=image";
        }

        return "redirect:/admin/products?success=updated";
    }

    // =====================================================
    // DELETE PRODUCT SAFELY
    // =====================================================

    @PostMapping("/admin/products/delete")
    public String deleteProduct(
            @RequestParam Integer productId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/";
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);

        if (product == null) {
            return "redirect:/admin/products?error=not-found";
        }

        // -------------------------------------------------
        // CHECK ORDER HISTORY BEFORE CHANGING ANYTHING
        // -------------------------------------------------

        Integer orderItemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_items WHERE product_id = ?",
                Integer.class,
                productId
        );

        if (orderItemCount != null && orderItemCount > 0) {

            // Preserve products referenced by previous orders.
            return "redirect:/admin/products?error=has-orders";
        }

        // -------------------------------------------------
        // DELETE FROM CARTS
        // -------------------------------------------------

        cartRepository.deleteByProductId(productId);

        // -------------------------------------------------
        // DELETE PRODUCT FROM DATABASE
        // -------------------------------------------------

        try {

            productRepository.delete(product);
            productRepository.flush();

        } catch (RuntimeException e) {

            // A database constraint may still prevent deletion.
            e.printStackTrace();

            return "redirect:/admin/products?error=delete-failed";
        }

        // -------------------------------------------------
        // DELETE IMAGE AFTER SUCCESSFUL DATABASE DELETION
        // -------------------------------------------------

        if (product.getImageName() != null
                && !product.getImageName().isEmpty()) {

            try {

                Path imagePath =
                        uploadDirectory.resolve(
                                product.getImageName()
                        ).normalize();

                if (imagePath.startsWith(uploadDirectory.normalize())) {
                    Files.deleteIfExists(imagePath);
                }

            } catch (IOException e) {

                // The database deletion succeeded; log image cleanup errors.
                e.printStackTrace();
            }
        }

        return "redirect:/admin/products?success=deleted";
    }
}
