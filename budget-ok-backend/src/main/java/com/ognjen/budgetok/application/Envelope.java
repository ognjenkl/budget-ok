package com.ognjen.budgetok.application;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("envelopes")
public class Envelope {

    @Id
    private Long id;
    private String name;
    private int budget;
    
    @MappedCollection(idColumn = "envelope_id")
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    public boolean hasExpenses() {
        return expenses != null && !expenses.isEmpty();
    }

    public int getExpenseAmount(String memo) {
        if (expenses == null) {
            throw new IllegalArgumentException("Expense not found");
        }
        return expenses.stream()
                .filter(expense -> expense.getMemo().equals(memo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"))
                .getAmount();
    }

    public boolean add(Expense expense) {
        if (expenses == null) {
            expenses = new ArrayList<>();
        }
        return expenses.add(expense);
    }
}
