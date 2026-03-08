package com.productservice.service;

import com.productservice.dto.AdminStatsResponse;
import com.productservice.model.OrderStatus;
import com.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository     userRepository;
    private final OrderRepository    orderRepository;

    @Cacheable(value = "admin_stats", key = "'overview'")
    public AdminStatsResponse getOverview() {
        return AdminStatsResponse.builder()
                .totalProducts(productRepository.count())
                .totalCategories(categoryRepository.count())
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .totalRevenue(orderRepository.calculateTotalRevenue())
                .lowStockProducts(productRepository
                        .countByActiveTrueAndStockQuantityLessThanAndStockQuantityGreaterThan(10, 0))
                .outOfStockProducts(productRepository
                        .countByActiveTrueAndStockQuantity(0))
                .build();
    }
}
