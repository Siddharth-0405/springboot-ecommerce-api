package com.productservice.service;

import com.productservice.dto.OrderRequest;
import com.productservice.dto.OrderResponse;
import com.productservice.dto.PagedResponse;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.model.*;
import com.productservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository   orderRepository;
    private final CartRepository    cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository    userRepository;
    private final EmailService      emailService;

    @Override
    @Transactional
    public OrderResponse placeOrder(String userEmail, OrderRequest request) {
        User user = findUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

        // Build order items & deduct stock
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
        }).toList();

        BigDecimal total = orderItems.stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(request.getShippingAddress())
                .build();

        orderItems.forEach(item -> { item.setOrder(order); order.getItems().add(item); });
        Order saved = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        emailService.sendOrderConfirmationEmail(user.getEmail(), user.getFirstName(),
                saved.getId(), total.toPlainString());

        log.info("Order #{} placed by {}", saved.getId(), userEmail);
        return toResponse(saved);
    }

    @Override
    public PagedResponse<OrderResponse> getMyOrders(String userEmail, int page, int size) {
        User user = findUser(userEmail);
        Page<Order> orderPage = orderRepository.findByUserId(user.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return buildPagedResponse(orderPage);
    }

    @Override
    public OrderResponse getOrderById(Long orderId, String userEmail) {
        User user = findUser(userEmail);
        Order order = findOrder(orderId);
        // Users can only see their own orders; admins can see all (checked via role in controller)
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String userEmail) {
        User user = findUser(userEmail);
        Order order = findOrder(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that is already " + order.getStatus());
        }

        // Restore stock
        order.getItems().forEach(item -> {
            Product p = item.getProduct();
            p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
            productRepository.save(p);
        });

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public PagedResponse<OrderResponse> getAllOrders(int page, int size) {
        Page<Order> orderPage = orderRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return buildPagedResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = findOrder(orderId);
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        emailService.sendOrderStatusUpdateEmail(
                order.getUser().getEmail(), order.getUser().getFirstName(), orderId, status);

        return toResponse(updated);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private PagedResponse<OrderResponse> buildPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .quantity(i.getQuantity())
                        .priceAtPurchase(i.getPriceAtPurchase())
                        .subtotal(i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .customerEmail(order.getUser().getEmail())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
