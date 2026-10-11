package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByUserIdAndProductId(
            Integer userId,
            Integer productId
    );

    List<Cart> findByUserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.productId = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
}