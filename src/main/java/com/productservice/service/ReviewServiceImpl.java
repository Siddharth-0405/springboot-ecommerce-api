package com.productservice.service;

import com.productservice.dto.PagedResponse;
import com.productservice.dto.ReviewRequest;
import com.productservice.dto.ReviewResponse;
import com.productservice.exception.DuplicateResourceException;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.model.Product;
import com.productservice.model.Review;
import com.productservice.model.User;
import com.productservice.repository.ProductRepository;
import com.productservice.repository.ReviewRepository;
import com.productservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository  reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository    userRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "products_page"}, allEntries = true)
    public ReviewResponse addReview(Long productId, ReviewRequest request, String userEmail) {
        Product product = findProduct(productId);
        User user = findUser(userEmail);

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Override
    public PagedResponse<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        Page<Review> reviewPage = reviewRepository.findByProductId(
                productId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        List<ReviewResponse> content = reviewPage.getContent().stream().map(this::toResponse).toList();

        return PagedResponse.<ReviewResponse>builder()
                .content(content).pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize()).totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages()).last(reviewPage.isLast()).first(reviewPage.isFirst())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "products_page"}, allEntries = true)
    public ReviewResponse updateReview(Long productId, ReviewRequest request, String userEmail) {
        User user = findUser(userEmail);
        Review review = reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "products_page"}, allEntries = true)
    public void deleteReview(Long productId, String userEmail) {
        User user = findUser(userEmail);
        Review review = reviewRepository.findByProductIdAndUserId(productId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .reviewerName(r.getUser().getFirstName() + " " + r.getUser().getLastName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
