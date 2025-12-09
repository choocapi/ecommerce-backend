package com.choocapi.ecommercebackend.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.ArticleRequest;
import com.choocapi.ecommercebackend.dto.response.ArticleResponse;
import com.choocapi.ecommercebackend.entity.Article;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.ArticleMapper;
import com.choocapi.ecommercebackend.repository.ArticleRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleService {
    ArticleRepository repository;
    UserRepository userRepository;
    ArticleMapper mapper;

    public ArticleResponse create(ArticleRequest request) {
        if (repository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }

        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        Article article = mapper.toEntity(request);
        article.setUser(user);

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            article.setPublishedAt(Instant.now());
        }

        article = repository.save(article);
        return mapper.toResponse(article);
    }

    public Page<ArticleResponse> list(Pageable pageable, String search, Boolean isPublished, String category) {
        Specification<Article> specification = buildSpecification(search, isPublished, category);
        return repository.findAll(specification, pageable)
                .map(mapper::toResponse);
    }

    public ArticleResponse get(Long id) {
        return mapper.toResponse(
                repository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public ArticleResponse getBySlug(String slug) {
        return mapper.toResponse(
                repository.findBySlug(slug)
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
    }

    public ArticleResponse update(Long id, ArticleRequest request) {
        Article article = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getSlug() != null && !request.getSlug().equals(article.getSlug())
                && repository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }

        mapper.update(article, request);

        if (Boolean.TRUE.equals(request.getIsPublished()) && article.getPublishedAt() == null) {
            article.setPublishedAt(Instant.now());
        }

        return mapper.toResponse(repository.save(article));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private Specification<Article> buildSpecification(String search, Boolean isPublished, String category) {
        Specification<Article> specification = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), keyword),
                    cb.like(cb.lower(root.get("slug")), keyword)));
        }

        if (isPublished != null) {
            specification = specification
                    .and((root, query, cb) -> cb.equal(root.get("isPublished"), isPublished));
        }

        if (StringUtils.hasText(category)) {
            specification = specification
                    .and((root, query, cb) -> cb.equal(root.get("category"), category.trim()));
        }

        return specification;
    }
}




