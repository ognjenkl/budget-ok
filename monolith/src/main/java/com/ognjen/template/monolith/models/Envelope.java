package com.ognjen.template.monolith.models;

import java.util.ArrayList;
import java.util.List;

public class Envelope {
    private long id;
    private String name;
    private int budget;
    private List<Expense> expenses;

    public Envelope() {
        this.expenses = new ArrayList<>();
    }

    public Envelope(String name, int budget) {
        this.name = name;
        this.budget = budget;
        this.expenses = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
    }

    public int getBalance() {
        int spent = 0;
        for (Expense expense : expenses) {
            if ("WITHDRAW".equals(expense.getTransactionType())) {
                spent += expense.getAmount();
            } else if ("DEPOSIT".equals(expense.getTransactionType())) {
                spent -= expense.getAmount();
            }
        }
        return budget - spent;
    }
}
