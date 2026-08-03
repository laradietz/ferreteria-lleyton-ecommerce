package com.ecommerce.api.service;

import com.ecommerce.api.dto.CategoryResponse;
import com.ecommerce.api.dto.CreateCategoryRequest;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    public CategoryResponse findById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest req) {
        if (categoryRepository.existsByNameIgnoreCase(req.name())) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }
        Category category = Category.builder()
                .name(req.name())
                .description(req.description())
                .active(true)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CreateCategoryRequest req) {
        Category category = findEntity(id);
        category.setName(req.name());
        category.setDescription(req.description());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(UUID id) {
        Category category = findEntity(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    Category findEntity(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getActive());
    }
}
