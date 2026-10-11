package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Customer: view their own orders
    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId);

    // Admin: view all orders, newest first
    List<Order> findAllByOrderByOrderDateDesc();
}