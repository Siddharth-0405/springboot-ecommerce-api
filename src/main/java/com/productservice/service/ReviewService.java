package com.productservice.service;

import com.productservice.dto.PagedResponse;
import com.productservice.dto.ReviewRequest;
import com.productservice.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse addReview(Long productId, ReviewRequest request, String userEmail);
    PagedResponse<ReviewResponse> getProductReviews(Long productId, int page, int size);
    ReviewResponse updateReview(Long productId, ReviewRequest request, String userEmail);
    void deleteReview(Long productId, String userEmail);
}
