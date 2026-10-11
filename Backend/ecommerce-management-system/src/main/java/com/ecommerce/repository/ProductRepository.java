package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByActiveTrue();

    List<Product> findByActiveTrueAndProductNameContainingIgnoreCase(
            String productName
    );
}