package com.productservice.service;

import com.productservice.dto.CategoryRequest;
import com.productservice.dto.CategoryResponse;
import com.productservice.exception.DuplicateResourceException;
import com.productservice.exception.ResourceNotFoundException;
import com.productservice.model.Category;
import com.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        Category saved = categoryRepository.save(
                Category.builder().name(request.getName()).description(request.getDescription()).build()
        );
        log.info("Category created: {}", saved.getName());
        return toResponse(saved);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findActive(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = findActive(id);
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Category soft-deleted: {}", id);
    }

    private Category findActive(Long id) {
        return categoryRepository.findById(id)
                .filter(Category::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .active(c.getActive())
                .productCount((int) c.getProducts().stream().filter(p -> p.getActive()).count())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
