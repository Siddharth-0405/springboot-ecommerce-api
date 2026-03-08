package com.productservice.service;

import com.productservice.dto.CartItemRequest;
import com.productservice.dto.CartResponse;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.model.*;
import com.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository    cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository    userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        Cart cart = getOrCreateCart(findUser(userEmail));
        return toResponse(cart);
    }

    @Override
    public CartResponse addItem(String userEmail, CartItemRequest request) {
        User user    = findUser(userEmail);
        Cart cart    = getOrCreateCart(user);
        Product prod = productRepository.findById(request.getProductId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        // If product already in cart, increase quantity
        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> cart.getItems().add(CartItem.builder().cart(cart).product(prod).quantity(request.getQuantity()).build())
                );

        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(String userEmail, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(findUser(userEmail));
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        Cart cart = getOrCreateCart(findUser(userEmail));
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(findUser(userEmail));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartResponse.CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .imageUrl(item.getProduct().getImageUrl())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalItems(itemResponses.stream().mapToInt(CartResponse.CartItemResponse::getQuantity).sum())
                .totalAmount(total)
                .build();
    }
}
