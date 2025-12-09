package com.choocapi.ecommercebackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.context.ProductContext;
import com.choocapi.ecommercebackend.dto.context.ProductSummary;
import com.choocapi.ecommercebackend.entity.Brand;
import com.choocapi.ecommercebackend.entity.Category;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.repository.BrandRepository;
import com.choocapi.ecommercebackend.repository.CategoryRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductContextProvider {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ObjectMapper objectMapper;

    /**
     * Get product context based on a search query
     * Searches products by keyword in name, description, and SKU
     * If no keyword provided or no results found, returns featured products
     * Returns up to 10 published products
     */
    public ProductContext getProductContext(String query) {
        List<Product> products = getProductsByKeyword(query);
        
        // If no products found or query is too generic, get featured/popular products
        if (products.isEmpty()) {
            products = getFeaturedProducts();
        }
        
        List<ProductSummary> productSummaries = products.stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());
        
        List<String> categories = categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .map(Category::getName)
                .collect(Collectors.toList());
        
        List<String> brands = brandRepository.findAll().stream()
                .filter(Brand::getIsActive)
                .map(Brand::getName)
                .collect(Collectors.toList());
        
        return ProductContext.builder()
                .products(productSummaries)
                .categories(categories)
                .brands(brands)
                .build();
    }
    
    /**
     * Get featured/popular products when no specific search query
     * Returns up to 30 published products, ordered by ID descending (newest first)
     */
    private List<Product> getFeaturedProducts() {
        Pageable pageable = PageRequest.of(0, 30);
        
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            Predicate isPublishedPredicate = criteriaBuilder.isTrue(root.get("isPublished"));
            query.orderBy(criteriaBuilder.desc(root.get("id")));
            return isPublishedPredicate;
        };
        
        return productRepository.findAll(spec, pageable).getContent();
    }

    /**
     * Search products by keyword
     * Searches in product name, description, SKU, category name, and brand name
     * Returns only published products, limited to 30 results
     */
    public List<Product> getProductsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getFeaturedProducts();
        }

        String searchTerm = "%" + keyword.toLowerCase() + "%";
        Pageable pageable = PageRequest.of(0, 30);
        
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchTerm);
            Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchTerm);
            Predicate skuPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("sku")), searchTerm);
            
            // Search in category name
            Predicate categoryPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("category").get("name")), searchTerm);
            
            // Search in brand name
            Predicate brandPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("brand").get("name")), searchTerm);
            
            Predicate isPublishedPredicate = criteriaBuilder.isTrue(root.get("isPublished"));
            
            Predicate searchPredicate = criteriaBuilder.or(
                    namePredicate, descriptionPredicate, skuPredicate, 
                    categoryPredicate, brandPredicate);
            return criteriaBuilder.and(searchPredicate, isPublishedPredicate);
        };
        
        return productRepository.findAll(spec, pageable).getContent();
    }

    /**
     * Get category information by name or slug
     * Returns category name and description if found
     */
    public String getCategoryInfo(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }

        return categoryRepository.findBySlug(categoryName.toLowerCase())
                .or(() -> categoryRepository.findAll().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                        .findFirst())
                .filter(Category::getIsActive)
                .map(category -> String.format("%s: %s", 
                        category.getName(), 
                        category.getDescription() != null ? category.getDescription() : ""))
                .orElse(null);
    }

    /**
     * Get brand information by name or slug
     * Returns brand name and description if found
     */
    public String getBrandInfo(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            return null;
        }

        return brandRepository.findBySlug(brandName.toLowerCase())
                .or(() -> brandRepository.findAll().stream()
                        .filter(b -> b.getName().equalsIgnoreCase(brandName))
                        .findFirst())
                .filter(Brand::getIsActive)
                .map(brand -> String.format("%s: %s", 
                        brand.getName(), 
                        brand.getDescription() != null ? brand.getDescription() : ""))
                .orElse(null);
    }

    /**
     * Map Product entity to ProductSummary DTO
     */
    private ProductSummary mapToProductSummary(Product product) {
        return ProductSummary.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .slug(product.getSlug())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .brand(product.getBrand() != null ? product.getBrand().getName() : null)
                .quantity(product.getQuantity())
                .isPublished(product.getIsPublished())
                .thumbnailUrl(extractPrimaryImage(product.getImageUrls()))
                .build();
    }

    private String extractPrimaryImage(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.isBlank()) {
            return null;
        }
        try {
            List<String> images = objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
            return images.isEmpty() ? null : images.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
