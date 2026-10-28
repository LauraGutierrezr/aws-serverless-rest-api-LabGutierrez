package edu.eci.arem.orders.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import edu.eci.arem.orders.domain.CreateOrderRequest;
import edu.eci.arem.orders.domain.InvalidOrderException;
import edu.eci.arem.orders.domain.Order;
import edu.eci.arem.orders.domain.OrderNotFoundException;
import edu.eci.arem.orders.domain.OrderService;

import java.util.Map;

/**
 * Arquitectura B (Lambda proxy integration).
 * Adaptador HTTP delgado: extrae parámetros, invoca la lógica y traduce resultados/errores a HTTP.
 * Handler: edu.eci.arem.orders.lambda.ApiGatewayHandler::handleRequest
 */
public class ApiGatewayHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson GSON = new Gson();
    private static final OrderService DEFAULT_SERVICE = new OrderService();

    private final OrderService service;

    public ApiGatewayHandler() {
        this(DEFAULT_SERVICE);
    }

    public ApiGatewayHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            String method = event.getHttpMethod();
            Map<String, String> path = event.getPathParameters();
            Map<String, String> query = event.getQueryStringParameters();
            String id = path == null ? null : path.get("id");

            if ("POST".equals(method) && id == null) {
                CreateOrderRequest req = GSON.fromJson(event.getBody(), CreateOrderRequest.class);
                Order created = service.createOrder(req);
                return response(201, GSON.toJson(created), Map.of("Location", "/orders/" + created.getId()));
            }
            if ("GET".equals(method) && id != null) {
                return response(200, GSON.toJson(service.getOrder(id)), Map.of());
            }
            if ("GET".equals(method)) {
                String customerId = query == null ? null : query.get("customerId");
                return response(200, GSON.toJson(service.getOrdersByCustomer(customerId)), Map.of());
            }
            if ("DELETE".equals(method) && id != null) {
                service.deleteOrder(id);
                return response(204, null, Map.of());
            }
            return error(405, "METHOD_NOT_ALLOWED", method + " is not supported on this resource");
        } catch (InvalidOrderException e) {
            return error(400, "BAD_REQUEST", e.getMessage());
        } catch (JsonSyntaxException e) {
            return error(400, "BAD_REQUEST", "Request body is not valid JSON");
        } catch (OrderNotFoundException e) {
            return error(404, "ORDER_NOT_FOUND", e.getMessage());
        } catch (RuntimeException e) {
            return error(500, "INTERNAL_ERROR", "Unexpected error");
        }
    }

    private static APIGatewayProxyResponseEvent error(int status, String code, String message) {
        return response(status, GSON.toJson(Map.of("error", code, "message", message)), Map.of());
    }

    private static APIGatewayProxyResponseEvent response(int status, String body, Map<String, String> extraHeaders) {
        Map<String, String> headers = new java.util.HashMap<>(extraHeaders);
        headers.put("Content-Type", "application/json");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(headers)
                .withBody(body);
    }
}
