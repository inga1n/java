package com.triton.triton.backend.model.repository;

import com.triton.triton.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
