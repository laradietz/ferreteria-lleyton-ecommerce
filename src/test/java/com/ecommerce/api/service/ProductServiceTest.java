package com.ecommerce.api.service;

import com.ecommerce.api.dto.CreateProductRequest;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryService categoryService;
    @InjectMocks private ProductService productService;

    @Test
    void create_lanzaBusinessException_siElSkuYaExiste() {
        var request = new CreateProductRequest("HM-0001", "Martillo", "desc",
                new BigDecimal("1000"), 10, null, UUID.randomUUID());

        when(productRepository.existsBySkuIgnoreCase("HM-0001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_guardaElProducto_cuandoElSkuNoExiste() {
        UUID categoryId = UUID.randomUUID();
        var category = Category.builder().id(categoryId).name("Herramientas").active(true).build();
        var request = new CreateProductRequest("HM-0002", "Destornillador", "desc",
                new BigDecimal("500"), 20, null, categoryId);

        when(productRepository.existsBySkuIgnoreCase("HM-0002")).thenReturn(false);
        when(categoryService.findEntity(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = productService.create(request);

        assertThat(response.sku()).isEqualTo("HM-0002");
        assertThat(response.categoryName()).isEqualTo("Herramientas");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findById_lanzaResourceNotFound_siNoExiste() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
