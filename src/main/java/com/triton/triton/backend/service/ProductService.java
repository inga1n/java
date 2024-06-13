package com.triton.triton.backend.service;

import java.util.List;

import com.triton.triton.backend.model.Product;
import com.triton.triton.backend.model.repository.ProductDAO;
import org.springframework.stereotype.Service;

/**
 * Service for handling product actions.
 */
@Service
public class ProductService {

    /** The Product DAO. */
    private ProductDAO productDAO;

    /**
     * Constructor for spring injection.
     * @param productDAO
     */
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Gets the all products available.
     * @return The list of products.
     */
    public List<Product> getProducts() {
        return productDAO.findAll();
    }

}
