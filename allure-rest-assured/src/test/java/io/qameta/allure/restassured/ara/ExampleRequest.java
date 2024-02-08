package io.qameta.allure.restassured.ara;

import java.util.Objects;

public class ExampleRequest {

    private int id;
    private String customer;
    private int quantity;
    private float price;

    public ExampleRequest() {
    }

    public ExampleRequest(
            int id,
            String customer,
            int quantity,
            float price
    ) {
        this.id = id;
        this.customer = customer;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExampleRequest) obj;
        return this.id == that.id &&
                Objects.equals(this.customer, that.customer) &&
                this.quantity == that.quantity &&
                Float.floatToIntBits(this.price) == Float.floatToIntBits(that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer, quantity, price);
    }

    @Override
    public String toString() {
        return "ExampleRequest[" +
                "id=" + id + ", " +
                "customer=" + customer + ", " +
                "quantity=" + quantity + ", " +
                "price=" + price + ']';
    }

}
