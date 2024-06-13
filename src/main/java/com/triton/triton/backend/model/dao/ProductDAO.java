package com.triton.triton.backend.model.dao;

import com.triton.triton.backend.model.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductDAO extends ListCrudRepository<Product, Long> {
}
