package com.pocketsurvivor.controller;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringExpenseResponse>>> getRecurring(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(recurringService.getUserRecurring(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> create(
        Authentication auth,
        @Valid @RequestBody RecurringExpenseRequest req
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Created", recurringService.create(userId, req)));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<RecurringExpenseResponse>> toggle(
        Authentication auth,
        @PathVariable Long id
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(recurringService.toggle(userId, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
        Authentication auth,
        @PathVariable Long id
    ) {
        Long userId = (Long) auth.getPrincipal();
        recurringService.delete(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
