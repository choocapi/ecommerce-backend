package com.choocapi.ecommercebackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByParent(Category parent);
}


