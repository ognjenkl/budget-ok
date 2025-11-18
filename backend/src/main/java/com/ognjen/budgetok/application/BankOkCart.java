package com.ognjen.budgetok.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BankOkCart {
    private long id;
    private long userId;
    private int totalProducts;
    private int totalQuantity;
    private double total;
    @JsonProperty("discountedTotal")
    private double discountedTotal;
    private List<BankOkProduct> products;

    public BankOkCart() {
    }

    public BankOkCart(long id, long userId, int totalProducts, int totalQuantity, double total, List<BankOkProduct> products) {
        this.id = id;
        this.userId = userId;
        this.totalProducts = totalProducts;
        this.totalQuantity = totalQuantity;
        this.total = total;
        this.products = products;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscountedTotal() {
        return discountedTotal;
    }

    public void setDiscountedTotal(double discountedTotal) {
        this.discountedTotal = discountedTotal;
    }

    public List<BankOkProduct> getProducts() {
        return products;
    }

    public void setProducts(List<BankOkProduct> products) {
        this.products = products;
    }
}
