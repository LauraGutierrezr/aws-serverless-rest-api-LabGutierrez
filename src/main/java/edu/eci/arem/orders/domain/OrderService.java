package edu.eci.arem.orders.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lógica de aplicación. No conoce HTTP, API Gateway ni Lambda.
 * El almacenamiento es en memoria: NO es persistente (los entornos de Lambda son efímeros).
 */
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(100);

    public OrderService() {
        orders.put("ORD-001", new Order("ORD-001", "CUST-42", "MacBook Air", 1));
        orders.put("ORD-42", new Order("ORD-42", "CUST-7", "Keyboard", 2));
    }

    public Order createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Request body is required");
        }
        if (isBlank(request.getCustomerId())) {
            throw new InvalidOrderException("customerId is required");
        }
        if (isBlank(request.getProduct())) {
            throw new InvalidOrderException("product is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidOrderException("quantity must be greater than 0");
        }
        String id = "ORD-" + sequence.incrementAndGet();
        Order order = new Order(id, request.getCustomerId(), request.getProduct(), request.getQuantity());
        orders.put(id, order);
        return order;
    }

    public Order getOrder(String id) {
        if (isBlank(id)) {
            throw new InvalidOrderException("id is required");
        }
        Order order = orders.get(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        if (isBlank(customerId)) {
            throw new InvalidOrderException("customerId is required");
        }
        List<Order> result = new ArrayList<>();
        for (Order o : orders.values()) {
            if (o.getCustomerId().equals(customerId)) {
                result.add(o);
            }
        }
        return result;
    }

    public void deleteOrder(String id) {
        if (isBlank(id)) {
            throw new InvalidOrderException("id is required");
        }
        if (orders.remove(id) == null) {
            throw new OrderNotFoundException(id);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
