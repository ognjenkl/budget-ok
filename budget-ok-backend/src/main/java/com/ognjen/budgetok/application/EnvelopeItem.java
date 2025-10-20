package com.ognjen.budgetok.application;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("envelope_items")
public class EnvelopeItem {
    @Id
    private Long id;
    
    private String description;
    private double amount;
    private LocalDate date;
    private String category;
    
    // Reference to the envelope this item belongs to
    private Long envelopeId;
}
