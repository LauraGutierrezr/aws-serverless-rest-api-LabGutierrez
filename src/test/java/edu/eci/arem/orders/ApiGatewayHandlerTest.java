package edu.eci.arem.orders;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import edu.eci.arem.orders.domain.OrderService;
import edu.eci.arem.orders.lambda.ApiGatewayHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiGatewayHandlerTest {

    private ApiGatewayHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiGatewayHandler(new OrderService());
    }

    private APIGatewayProxyResponseEvent call(String method, Map<String, String> path,
                                              Map<String, String> query, String body) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod(method)
                .withPathParameters(path)
                .withQueryStringParameters(query)
                .withBody(body);
        return handler.handleRequest(event, null);
    }

    @Test
    void postReturns201WithLocation() {
        var r = call("POST", null, null, "{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}");
        assertEquals(201, r.getStatusCode());
        assertTrue(r.getHeaders().get("Location").startsWith("/orders/ORD-"));
    }

    @Test
    void postInvalidReturns400() {
        var r = call("POST", null, null, "{\"customerId\":\"CUST-42\",\"quantity\":0}");
        assertEquals(400, r.getStatusCode());
        assertTrue(r.getBody().contains("BAD_REQUEST"));
    }

    @Test
    void getExistingReturns200() {
        var r = call("GET", Map.of("id", "ORD-42"), null, null);
        assertEquals(200, r.getStatusCode());
        assertTrue(r.getBody().contains("Keyboard"));
    }

    @Test
    void getMissingReturns404() {
        var r = call("GET", Map.of("id", "ORD-999"), null, null);
        assertEquals(404, r.getStatusCode());
        assertTrue(r.getBody().contains("ORDER_NOT_FOUND"));
    }

    @Test
    void searchByCustomerReturns200() {
        var r = call("GET", null, Map.of("customerId", "CUST-7"), null);
        assertEquals(200, r.getStatusCode());
        assertTrue(r.getBody().contains("ORD-42"));
    }

    @Test
    void searchWithoutCustomerReturns400() {
        assertEquals(400, call("GET", null, null, null).getStatusCode());
    }

    @Test
    void deleteReturns204Then404() {
        assertEquals(204, call("DELETE", Map.of("id", "ORD-42"), null, null).getStatusCode());
        assertEquals(404, call("DELETE", Map.of("id", "ORD-42"), null, null).getStatusCode());
    }
}
