package com.pocketsurvivor.controller;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
        Authentication auth,
        @Valid @RequestBody ExpenseRequest req
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Logged", expenseService.addExpense(userId, req)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getTodayExpenses(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getTodayExpenses(userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(
        Authentication auth,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(expenseService.getExpenses(userId, from, to)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
        Authentication auth,
        @PathVariable Long id
    ) {
        Long userId = (Long) auth.getPrincipal();
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
