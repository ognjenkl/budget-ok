package com.ognjen.budgetok.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("envelope_items")
public class Expense {
    @Id
    private Long id;
    private int amount;
    private String memo;
    @Column("transaction_type")
    private String transactionType;
}
