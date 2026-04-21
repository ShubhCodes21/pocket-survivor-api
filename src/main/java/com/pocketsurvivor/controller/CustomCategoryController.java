package com.pocketsurvivor.controller;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.service.CustomCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CustomCategoryController {

    private final CustomCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomCategoryResponse>>> getCategories(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getUserCategories(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomCategoryResponse>> createCategory(
        Authentication auth,
        @Valid @RequestBody CustomCategoryRequest req
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Created", categoryService.createCategory(userId, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
        Authentication auth,
        @PathVariable Long id
    ) {
        Long userId = (Long) auth.getPrincipal();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
