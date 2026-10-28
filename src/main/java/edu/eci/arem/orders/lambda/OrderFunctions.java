package edu.eci.arem.orders.lambda;

import edu.eci.arem.orders.domain.CreateOrderRequest;
import edu.eci.arem.orders.domain.InvalidOrderException;
import edu.eci.arem.orders.domain.Order;
import edu.eci.arem.orders.domain.OrderNotFoundException;
import edu.eci.arem.orders.domain.OrderService;

import java.util.List;
import java.util.function.Supplier;

/**
 * Arquitectura A (integración no-proxy / mapping templates).
 * El runtime de Lambda convierte JSON -> POJO y POJO -> JSON.
 * Los errores se lanzan con un prefijo que API Gateway usa (regex) para elegir el status code.
 * Handlers:
 *   edu.eci.arem.orders.lambda.OrderFunctions::createOrder
 *   edu.eci.arem.orders.lambda.OrderFunctions::getOrder
 *   edu.eci.arem.orders.lambda.OrderFunctions::getOrdersByCustomer
 */
public class OrderFunctions {

    private static final OrderService SERVICE = new OrderService();

    public Order createOrder(CreateOrderRequest request) {
        return call(() -> SERVICE.createOrder(request));
    }

    public Order getOrder(IdRequest request) {
        return call(() -> SERVICE.getOrder(request == null ? null : request.getId()));
    }

    public List<Order> getOrdersByCustomer(CustomerRequest request) {
        return call(() -> SERVICE.getOrdersByCustomer(request == null ? null : request.getCustomerId()));
    }

    private static <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (InvalidOrderException e) {
            throw new RuntimeException("BAD_REQUEST: " + e.getMessage());
        } catch (OrderNotFoundException e) {
            throw new RuntimeException("ORDER_NOT_FOUND: " + e.getMessage());
        }
    }
}
