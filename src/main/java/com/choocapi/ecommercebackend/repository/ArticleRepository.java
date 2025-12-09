package com.choocapi.ecommercebackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.choocapi.ecommercebackend.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    Optional<Article> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
