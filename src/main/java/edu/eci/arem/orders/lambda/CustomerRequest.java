package edu.eci.arem.orders.lambda;

/** Entrada que API Gateway (mapping template) construye a partir de ?customerId=... */
public class CustomerRequest {
    private String customerId;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
