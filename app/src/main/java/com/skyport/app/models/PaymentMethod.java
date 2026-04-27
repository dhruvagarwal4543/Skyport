package com.skyport.app.models;

public class PaymentMethod {
    private String id;
    private String type; // "Credit Card" or "Debit Card"
    private String last4;
    private String name;
    private String expiry;

    public PaymentMethod() {
        // Default constructor required for calls to DataSnapshot.getValue(PaymentMethod.class)
    }

    public PaymentMethod(String type, String last4, String name, String expiry) {
        this.type = type;
        this.last4 = last4;
        this.name = name;
        this.expiry = expiry;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public String getLast4() { return last4; }
    public String getName() { return name; }
    public String getExpiry() { return expiry; }
}
