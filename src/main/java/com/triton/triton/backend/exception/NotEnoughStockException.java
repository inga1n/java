package com.triton.triton.backend.exception;

public class NotEnoughStockException extends Exception {

    public NotEnoughStockException(String message) {
        super(message);
    }
}