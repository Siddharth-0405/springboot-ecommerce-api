package com.productservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStatsResponse {
    private long totalProducts;
    private long totalCategories;
    private long totalUsers;
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue;
    private long lowStockProducts;
    private long outOfStockProducts;
}
