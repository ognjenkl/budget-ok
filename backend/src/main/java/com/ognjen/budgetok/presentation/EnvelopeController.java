package com.ognjen.budgetok.presentation;

import com.ognjen.budgetok.application.Envelope;
import com.ognjen.budgetok.application.EnvelopeService;
import com.ognjen.budgetok.application.ExpenseDto;
import com.ognjen.budgetok.application.TransferRequest;
import com.ognjen.budgetok.application.TransferResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envelopes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EnvelopeController {

    private final EnvelopeService envelopeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Envelope createEnvelope(@RequestBody Envelope envelope) {
        return envelopeService.create(envelope);
    }

    @GetMapping
    public List<Envelope> getAllEnvelopes() {
        return envelopeService.getAll();
    }

    @GetMapping("/{id}")
    public Envelope getEnvelopeById(@PathVariable long id) {
        return envelopeService.getById(id);
    }

    @PutMapping("/{id}")
    public Envelope updateEnvelope(@PathVariable long id, @RequestBody Envelope envelope) {
        return envelopeService.update(id, envelope);
    }

    @PatchMapping("/{id}")
    public Envelope patchEnvelope(@PathVariable long id, @RequestBody Envelope envelope) {
        return envelopeService.update(id, envelope);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEnvelope(@PathVariable long id) {
        envelopeService.delete(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/expenses")
    public Envelope addExpenseToEnvelope(@PathVariable long id, @RequestBody ExpenseDto expenseDto) {
        return envelopeService.addExpense(id, expenseDto);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            TransferResponse response = envelopeService.transfer(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(e.getMessage())
                );
            } else if (e.getMessage().contains("Insufficient balance")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(e.getMessage())
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(e.getMessage())
            );
        }
    }

    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
