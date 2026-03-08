package com.productservice.service;

import com.productservice.dto.PagedResponse;
import com.productservice.dto.ProductRequest;
import com.productservice.dto.ProductResponse;
import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    PagedResponse<ProductResponse> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);
}
