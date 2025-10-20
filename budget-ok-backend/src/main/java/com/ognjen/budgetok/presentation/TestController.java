package com.ognjen.budgetok.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("This is an ADMIN endpoint");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<String> anyUser() {
        return ResponseEntity.ok("This is a USER endpoint");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test")
    public String test() {
        return "Hello, BudgetOk!";
    }
}
