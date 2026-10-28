package edu.eci.arem.orders;

import edu.eci.arem.orders.domain.CreateOrderRequest;
import edu.eci.arem.orders.domain.InvalidOrderException;
import edu.eci.arem.orders.domain.Order;
import edu.eci.arem.orders.domain.OrderNotFoundException;
import edu.eci.arem.orders.domain.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService();
    }

    @Test
    void createsValidOrder() {
        Order order = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        assertNotNull(order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void retrievesExistingOrder() {
        Order order = service.getOrder("ORD-42");
        assertEquals("CUST-7", order.getCustomerId());
    }

    @Test
    void failsWhenOrderDoesNotExist() {
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("ORD-999"));
    }

    @Test
    void findsOrdersByCustomer() {
        service.createOrder(new CreateOrderRequest("CUST-42", "Mouse", 3));
        List<Order> orders = service.getOrdersByCustomer("CUST-42");
        assertEquals(2, orders.size());
        assertTrue(service.getOrdersByCustomer("CUST-100").isEmpty());
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(InvalidOrderException.class, () -> service.createOrder(new CreateOrderRequest(null, "X", 1)));
        assertThrows(InvalidOrderException.class, () -> service.createOrder(new CreateOrderRequest("C", "", 1)));
        assertThrows(InvalidOrderException.class, () -> service.createOrder(new CreateOrderRequest("C", "X", 0)));
        assertThrows(InvalidOrderException.class, () -> service.getOrdersByCustomer(" "));
    }

    @Test
    void deletesExistingOrder() {
        service.deleteOrder("ORD-42");
        assertThrows(OrderNotFoundException.class, () -> service.getOrder("ORD-42"));
    }

    @Test
    void deleteFailsWhenOrderDoesNotExist() {
        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder("ORD-999"));
    }
}
