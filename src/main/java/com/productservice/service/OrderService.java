package com.productservice.service;

import com.productservice.dto.OrderRequest;
import com.productservice.dto.OrderResponse;
import com.productservice.dto.PagedResponse;

public interface OrderService {
    OrderResponse placeOrder(String userEmail, OrderRequest request);
    PagedResponse<OrderResponse> getMyOrders(String userEmail, int page, int size);
    OrderResponse getOrderById(Long orderId, String userEmail);
    OrderResponse cancelOrder(Long orderId, String userEmail);
    // Admin
    PagedResponse<OrderResponse> getAllOrders(int page, int size);
    OrderResponse updateOrderStatus(Long orderId, String status);
}
