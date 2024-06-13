package com.triton.triton.backend.service;

import org.springframework.stereotype.Service;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.WebOrder;
import com.triton.triton.backend.model.repository.WebOrderRepository;

import java.util.List;

/**
 * Service for handling order actions.
 */
@Service
public class OrderService {

    /** The Web Order DAO. */
    private WebOrderRepository webOrderRepository;

    /**
     * Constructor for spring injection.
     * @param webOrderRepository
     */
    public OrderService(WebOrderRepository webOrderRepository) {
        this.webOrderRepository = webOrderRepository;
    }

    /**
     * Gets the list of orders for a given user.
     * @param user The user to search for.
     * @return The list of orders.
     */
    public List<WebOrder> getOrders(LocalUser user) {
        return webOrderRepository.findByUser(user);
    }

}