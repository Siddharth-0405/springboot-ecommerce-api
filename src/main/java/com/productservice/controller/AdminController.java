package com.productservice.controller;

import com.productservice.dto.*;
import com.productservice.service.AdminStatsService;
import com.productservice.service.OrderService;
import com.productservice.service.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only operations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminStatsService statsService;
    private final OrderService      orderService;
    private final ProductServiceImpl productService;

    // ── Dashboard Stats ───────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Dashboard overview stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getOverview()));
    }

    // ── Order Management ──────────────────────────────────────────────────────

    @GetMapping("/orders")
    @Operation(summary = "List all orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(page, size)));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status (PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateOrderStatus(id, status), "Order status updated to " + status));
    }

    // ── Stock Management ──────────────────────────────────────────────────────

    @PutMapping("/products/{id}/stock/add")
    @Operation(summary = "Add stock to a product")
    public ResponseEntity<ApiResponse<ProductResponse>> addStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.addStock(id, quantity), "Stock added"));
    }

    @PutMapping("/products/{id}/stock/reduce")
    @Operation(summary = "Reduce stock from a product")
    public ResponseEntity<ApiResponse<ProductResponse>> reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.reduceStock(id, quantity), "Stock reduced"));
    }

    @GetMapping("/products/low-stock")
    @Operation(summary = "Get products with less than 10 units in stock")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getLowStock(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(productService.getLowStockProducts(page, size)));
    }
}
