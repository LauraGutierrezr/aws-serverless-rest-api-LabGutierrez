package edu.eci.arem.orders.domain;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("Order " + id + " does not exist");
    }
}
