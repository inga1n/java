package com.triton.triton.backend.service;

import com.triton.triton.backend.exception.NotEnoughStockException;
import com.triton.triton.backend.model.Address;
import com.triton.triton.backend.model.LocalUser;
import com.triton.triton.backend.model.WebOrder;

public interface ShoppingCartService {

    WebOrder checkout(LocalUser user, Address shippingAddress) throws NotEnoughStockException;
}