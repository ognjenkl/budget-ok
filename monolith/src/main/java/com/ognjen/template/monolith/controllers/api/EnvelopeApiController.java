package com.ognjen.template.monolith.controllers.api;

import com.ognjen.template.monolith.models.Envelope;
import com.ognjen.template.monolith.models.Expense;
import com.ognjen.template.monolith.services.EnvelopeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/envelopes")
public class EnvelopeApiController {

    private final EnvelopeService envelopeService;

    public EnvelopeApiController(EnvelopeService envelopeService) {
        this.envelopeService = envelopeService;
    }

    @GetMapping
    public ResponseEntity<List<Envelope>> getAllEnvelopes() {
        List<Envelope> envelopes = envelopeService.getAllEnvelopes();
        return ResponseEntity.ok(envelopes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Envelope> getEnvelopeById(@PathVariable long id) {
        return envelopeService.getEnvelopeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Envelope> createEnvelope(@RequestBody CreateEnvelopeRequest request) {
        Envelope envelope = envelopeService.createEnvelope(request.getName(), request.getBudget());
        return ResponseEntity.ok(envelope);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Envelope> updateEnvelope(@PathVariable long id, @RequestBody UpdateEnvelopeRequest request) {
        try {
            Envelope envelope = envelopeService.updateEnvelope(id, request.getName(), request.getBudget());
            return ResponseEntity.ok(envelope);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvelope(@PathVariable long id) {
        envelopeService.deleteEnvelope(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/expenses")
    public ResponseEntity<Envelope> addExpense(@PathVariable long id, @RequestBody AddExpenseRequest request) {
        try {
            Envelope envelope = envelopeService.addExpense(id, request.getAmount(), request.getMemo(), request.getTransactionType());
            return ResponseEntity.ok(envelope);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Request DTOs
    public static class CreateEnvelopeRequest {
        private String name;
        private int budget;

        public CreateEnvelopeRequest() {}
        public CreateEnvelopeRequest(String name, int budget) {
            this.name = name;
            this.budget = budget;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getBudget() { return budget; }
        public void setBudget(int budget) { this.budget = budget; }
    }

    public static class UpdateEnvelopeRequest {
        private String name;
        private int budget;

        public UpdateEnvelopeRequest() {}
        public UpdateEnvelopeRequest(String name, int budget) {
            this.name = name;
            this.budget = budget;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getBudget() { return budget; }
        public void setBudget(int budget) { this.budget = budget; }
    }

    public static class AddExpenseRequest {
        private int amount;
        private String memo;
        private String transactionType;

        public AddExpenseRequest() {}
        public AddExpenseRequest(int amount, String memo, String transactionType) {
            this.amount = amount;
            this.memo = memo;
            this.transactionType = transactionType;
        }

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    }
}
