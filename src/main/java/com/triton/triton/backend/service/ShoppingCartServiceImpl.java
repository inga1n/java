package com.triton.triton.backend.service;

import com.triton.triton.backend.exception.NotEnoughStockException;
import com.triton.triton.backend.model.*;
import com.triton.triton.backend.model.repository.InventoryRepository;
import com.triton.triton.backend.model.repository.WebOrderQuantitiesRepository;
import com.triton.triton.backend.model.repository.WebOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private WebOrderRepository webOrderRepository;

    @Autowired
    private WebOrderQuantitiesRepository webOrderQuantitiesDAO;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public WebOrder checkout(LocalUser user, Address shippingAddress) throws NotEnoughStockException {
        ShoppingCart cart = user.getShoppingCart();
        WebOrder webOrder = new WebOrder();

        List<WebOrderQuantities> orderQuantities = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            // Проверка наличия товара
            if (product.getInventory().getQuantity() < quantity) {
                throw new NotEnoughStockException("Not enough stock for product: " + product.getName());
            }

            // Создание WebOrderQuantities
            WebOrderQuantities orderQuantity = new WebOrderQuantities();
            orderQuantity.setProduct(product);
            orderQuantity.setQuantity(quantity);
            orderQuantity.setOrder(webOrder);
            orderQuantities.add(webOrderQuantitiesDAO.save(orderQuantity)); // Сохраняем WebOrderQuantities

            // Уменьшение количества товара в Inventory
            Inventory inventory = product.getInventory();
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventoryRepository.save(inventory); // Сохраняем Inventory
        }

        webOrder.setUser(user);
        webOrder.setAddress(shippingAddress);
        webOrder.setQuantities(orderQuantities);

        // Сохранение заказа
        return webOrderRepository.save(webOrder); // Возвращаем созданный заказ
    }
}