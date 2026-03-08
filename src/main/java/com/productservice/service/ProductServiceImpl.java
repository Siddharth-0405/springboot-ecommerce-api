package com.productservice.service;

import com.productservice.dto.PagedResponse;
import com.productservice.dto.ProductRequest;
import com.productservice.dto.ProductResponse;
import com.productservice.exception.DuplicateResourceException;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.model.Category;
import com.productservice.model.Product;
import com.productservice.repository.CategoryRepository;
import com.productservice.repository.ProductRepository;
import com.productservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final String CACHE_PRODUCTS      = "products";
    private static final String CACHE_PRODUCTS_PAGE = "products_page";

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository   reviewRepository;

    @Override
    @Transactional
    @CacheEvict(value = CACHE_PRODUCTS_PAGE, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }
        Category category = resolveCategory(request.getCategoryId());
        Product saved = productRepository.save(mapToEntity(request, category));
        log.info("Product created: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Cacheable(value = CACHE_PRODUCTS_PAGE, key = "'page:'+#page+':size:'+#size+':sort:'+#sortBy+':dir:'+#sortDir")
    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Product> productPage = productRepository.findByActiveTrue(PageRequest.of(page, size, sort));
        return buildPagedResponse(productPage);
    }

    @Override
    @Cacheable(value = CACHE_PRODUCTS, key = "#id")
    public ProductResponse getProductById(Long id) {
        return mapToResponse(findActive(id));
    }

    @Override
    @Transactional
    @Caching(
        put   = { @CachePut(value = CACHE_PRODUCTS, key = "#id") },
        evict = { @CacheEvict(value = CACHE_PRODUCTS_PAGE, allEntries = true) }
    )
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findActive(id);
        if (request.getSku() != null && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }
        Category category = resolveCategory(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : product.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(category);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CACHE_PRODUCTS,      key = "#id"),
        @CacheEvict(value = CACHE_PRODUCTS_PAGE, allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = findActive(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Cacheable(value = CACHE_PRODUCTS_PAGE,
        key = "'search:'+#name+':min:'+#minPrice+':max:'+#maxPrice+':p:'+#page+':s:'+#size")
    public PagedResponse<ProductResponse> searchProducts(String name, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Page<Product> result = productRepository.searchProducts(name, minPrice, maxPrice,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return buildPagedResponse(result);
    }

    // ── Stock endpoints ───────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = {CACHE_PRODUCTS, CACHE_PRODUCTS_PAGE}, allEntries = true)
    public ProductResponse addStock(Long id, int quantity) {
        Product product = findActive(id);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = {CACHE_PRODUCTS, CACHE_PRODUCTS_PAGE}, allEntries = true)
    public ProductResponse reduceStock(Long id, int quantity) {
        Product product = findActive(id);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return mapToResponse(productRepository.save(product));
    }

    @Cacheable(value = CACHE_PRODUCTS_PAGE, key = "'low-stock'")
    public PagedResponse<ProductResponse> getLowStockProducts(int page, int size) {
        // Products with stock < 10 but > 0
        Page<Product> result = productRepository.findByActiveTrue(
                PageRequest.of(page, size, Sort.by("stockQuantity").ascending()));
        List<Product> lowStock = result.getContent().stream()
                .filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() < 10)
                .toList();
        return PagedResponse.<ProductResponse>builder()
                .content(lowStock.stream().map(this::mapToResponse).toList())
                .pageNumber(page).pageSize(size)
                .totalElements(lowStock.size()).totalPages(1)
                .first(true).last(true).build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Product findActive(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .filter(Category::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private Product mapToEntity(ProductRequest req, Category category) {
        return Product.builder()
                .name(req.getName()).description(req.getDescription())
                .price(req.getPrice())
                .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                .sku(req.getSku()).active(true).category(category)
                .build();
    }

    public ProductResponse mapToResponse(Product p) {
        Double avgRating = reviewRepository.findAverageRatingByProductId(p.getId());
        long reviewCount = reviewRepository.countByProductId(p.getId());
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .price(p.getPrice()).stockQuantity(p.getStockQuantity())
                .sku(p.getSku()).imageUrl(p.getImageUrl()).active(p.getActive())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .averageRating(avgRating).reviewCount((int) reviewCount)
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> buildPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .first(page.isFirst()).last(page.isLast()).build();
    }
}
