package com.choocapi.ecommercebackend.service;

import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.ChangePasswordRequest;
import com.choocapi.ecommercebackend.dto.request.UserCreationRequest;
import com.choocapi.ecommercebackend.dto.request.UserUpdateRequest;
import com.choocapi.ecommercebackend.dto.response.UserResponse;
import com.choocapi.ecommercebackend.entity.Role;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.UserMapper;
import com.choocapi.ecommercebackend.repository.RoleRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository repository;
    RoleRepository roleRepository;
    UserMapper mapper;
    PasswordEncoder passwordEncoder;

    public UserResponse create(UserCreationRequest request) {
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = mapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = new HashSet<Role>();
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName)
                    .ifPresent(roles::add);
            }
            user.setRoles(roles);
        }
        
        // Set default values if not provided
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        
        user = repository.save(user);
        return mapper.toResponse(user);
    }

    public Page<UserResponse> list(Pageable pageable, String search, String role, String excludeRole, Boolean status, Boolean verified) {
        Specification<User> specification = buildSpecification(search, role, excludeRole, status, verified);
        return repository.findAll(specification, pageable).map(mapper::toResponse);
    }

    public UserResponse get(String userId) {
        return mapper.toResponse(
                repository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST)));
    }

    public UserResponse update(String userId, UserUpdateRequest request) {
        User user = repository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        mapper.updateUser(user, request);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = new HashSet<Role>();
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName)
                    .ifPresent(roles::add);
            }
            user.setRoles(roles);
        }

        return mapper.toResponse(repository.save(user));
    }

    public UserResponse changePassword(ChangePasswordRequest request) {
        var oldPassword = request.getOldPassword();
        var newPassword = request.getNewPassword();

        if (oldPassword.equals(newPassword)) {
            throw new AppException(ErrorCode.DUPLICATE_ENTRY);
        }

        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = repository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        boolean isPasswordMatched = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!isPasswordMatched) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = repository.save(user);

        return mapper.toResponse(user);
    }

    public void delete(String userId) {
        repository.deleteById(userId);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = repository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return mapper.toResponse(user);
    }

    public UserResponse updateMyProfile(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        User user = repository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        mapper.updateSelf(user, request);

        user = repository.save(user);
        return mapper.toResponse(user);
    }

    private Specification<User> buildSpecification(String search, String role, String excludeRole, Boolean status, Boolean verified) {
        Specification<User> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (StringUtils.hasText(search)) {
            String keyword = "%" + search.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), keyword),
                    cb.like(cb.lower(root.get("firstName")), keyword),
                    cb.like(cb.lower(root.get("lastName")), keyword),
                    cb.like(cb.lower(root.get("phoneNumber")), keyword)
            ));
        }

        if (StringUtils.hasText(role)) {
            String normalizedRole = role.trim().toUpperCase();
            specification = specification.and((root, query, cb) -> {
                var join = root.join("roles", JoinType.LEFT);
                query.distinct(true);
                return cb.equal(cb.upper(join.get("name")), normalizedRole);
            });
        }

        if (StringUtils.hasText(excludeRole)) {
            String normalizedExcludeRole = excludeRole.trim().toUpperCase();
            specification = specification.and((root, query, cb) -> {
                var join = root.join("roles", JoinType.LEFT);
                query.distinct(true);
                return cb.notEqual(cb.upper(join.get("name")), normalizedExcludeRole);
            });
        }

        if (status != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("isActive"), status));
        }

        if (verified != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("emailVerified"), verified));
        }

        return specification;
    }
}
