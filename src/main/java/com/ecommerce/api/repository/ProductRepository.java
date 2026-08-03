package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByActiveTrue();

    List<Product> findAllByCategoryIdAndActiveTrue(UUID categoryId);

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
          AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    List<Product> search(@Param("name") String name);

    boolean existsBySkuIgnoreCase(String sku);
}
