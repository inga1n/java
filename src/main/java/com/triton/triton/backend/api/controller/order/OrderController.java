package com.triton.triton.backend.api.controller.order;

import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.WebOrder;
import com.triton.triton.backend.model.repository.WebOrderDAO;
import com.triton.triton.backend.service.OrderService;
import com.triton.triton.backend.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller to handle requests to create, update and view orders.
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private WebOrderDAO WebOrderDAO;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /** The Order Service. */
    private OrderService orderService;

    /**
     * Constructor for spring injection.
     * @param orderService
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Endpoint to get all orders for a specific user.
     * @param user The user provided by spring security context.
     * @return The list of orders the user had made.
     */
    @GetMapping
    public List<WebOrder> getOrders(@AuthenticationPrincipal LocalUser user) {
        return orderService.getOrders(user);
    }

}