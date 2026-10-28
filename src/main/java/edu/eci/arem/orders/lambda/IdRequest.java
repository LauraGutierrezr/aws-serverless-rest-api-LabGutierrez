package edu.eci.arem.orders.lambda;

/** Entrada que API Gateway (mapping template) construye a partir de /orders/{id}. */
public class IdRequest {
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
