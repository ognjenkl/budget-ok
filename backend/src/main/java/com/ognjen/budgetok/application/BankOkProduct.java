package com.ognjen.budgetok.application;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankOkProduct {
    private long id;
    private String title;
    private double price;
    private int quantity;
    private double total;
    @JsonProperty("discountedPrice")
    private double discountedPrice;

    public BankOkProduct() {
    }

    public BankOkProduct(long id, String title, double price, int quantity, double total) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
}
