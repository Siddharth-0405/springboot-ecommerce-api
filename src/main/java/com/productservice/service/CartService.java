package com.productservice.service;

import com.productservice.dto.CartItemRequest;
import com.productservice.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String userEmail);
    CartResponse addItem(String userEmail, CartItemRequest request);
    CartResponse updateItem(String userEmail, Long cartItemId, int quantity);
    CartResponse removeItem(String userEmail, Long cartItemId);
    void clearCart(String userEmail);
}
