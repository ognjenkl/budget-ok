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

    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestBody TransferRequest request) {
        try {
            Envelope sourceEnvelope = envelopeService.transferAmount(
                    request.getSourceEnvelopeId(),
                    request.getTargetEnvelopeId(),
                    request.getAmount(),
                    request.getMemo()
            );
            return ResponseEntity.ok(new TransferResponse(sourceEnvelope, request.getTargetEnvelopeId(), "Transfer successful"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(new ErrorResponse("Envelope not found: " + e.getMessage()));
            } else if (e.getMessage().contains("Insufficient balance")) {
                return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
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

    public static class TransferRequest {
        private long sourceEnvelopeId;
        private long targetEnvelopeId;
        private int amount;
        private String memo;

        public TransferRequest() {}
        public TransferRequest(long sourceEnvelopeId, long targetEnvelopeId, int amount, String memo) {
            this.sourceEnvelopeId = sourceEnvelopeId;
            this.targetEnvelopeId = targetEnvelopeId;
            this.amount = amount;
            this.memo = memo;
        }

        public long getSourceEnvelopeId() { return sourceEnvelopeId; }
        public void setSourceEnvelopeId(long sourceEnvelopeId) { this.sourceEnvelopeId = sourceEnvelopeId; }
        public long getTargetEnvelopeId() { return targetEnvelopeId; }
        public void setTargetEnvelopeId(long targetEnvelopeId) { this.targetEnvelopeId = targetEnvelopeId; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }

    public static class TransferResponse {
        private Envelope sourceEnvelope;
        private long targetEnvelopeId;
        private String message;

        public TransferResponse() {}
        public TransferResponse(Envelope sourceEnvelope, long targetEnvelopeId, String message) {
            this.sourceEnvelope = sourceEnvelope;
            this.targetEnvelopeId = targetEnvelopeId;
            this.message = message;
        }

        public Envelope getSourceEnvelope() { return sourceEnvelope; }
        public void setSourceEnvelope(Envelope sourceEnvelope) { this.sourceEnvelope = sourceEnvelope; }
        public long getTargetEnvelopeId() { return targetEnvelopeId; }
        public void setTargetEnvelopeId(long targetEnvelopeId) { this.targetEnvelopeId = targetEnvelopeId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse() {}
        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
