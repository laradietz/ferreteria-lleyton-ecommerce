package com.ecommerce.api.service;

import com.ecommerce.api.dto.CreateProductRequest;
import com.ecommerce.api.dto.ProductResponse;
import com.ecommerce.api.dto.UpdateProductRequest;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public List<ProductResponse> findAll() {
        return productRepository.findAllByActiveTrue().stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> findByCategory(UUID categoryId) {
        return productRepository.findAllByCategoryIdAndActiveTrue(categoryId)
                .stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> search(String name) {
        return productRepository.search(name).stream().map(this::toResponse).toList();
    }

    public ProductResponse findById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest req) {
        if (productRepository.existsBySkuIgnoreCase(req.sku())) {
            throw new BusinessException("Ya existe un producto con ese SKU");
        }
        Category category = categoryService.findEntity(req.categoryId());

        Product product = Product.builder()
                .sku(req.sku())
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .stock(req.stock())
                .imageUrl(req.imageUrl())
                .category(category)
                .active(true)
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest req) {
        Product product = findEntity(id);
        Category category = categoryService.findEntity(req.categoryId());

        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setStock(req.stock());
        product.setImageUrl(req.imageUrl());
        product.setCategory(category);
        product.setActive(req.active());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = findEntity(id);
        product.setActive(false);
        productRepository.save(product);
    }

    Product findEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getPrice(), p.getStock(), p.getImageUrl(), p.getActive(),
                p.getCategory().getId(), p.getCategory().getName());
    }
}
