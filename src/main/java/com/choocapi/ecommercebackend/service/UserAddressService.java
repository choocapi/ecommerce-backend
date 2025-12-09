package com.choocapi.ecommercebackend.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.request.UserAddressRequest;
import com.choocapi.ecommercebackend.dto.response.UserAddressResponse;
import com.choocapi.ecommercebackend.entity.User;
import com.choocapi.ecommercebackend.entity.UserAddress;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.UserAddressMapper;
import com.choocapi.ecommercebackend.repository.UserAddressRepository;
import com.choocapi.ecommercebackend.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressService {
    UserRepository userRepository;
    UserAddressRepository userAddressRepository;
    UserAddressMapper userAddressMapper;

    private User getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }

    public List<UserAddressResponse> listMyAddresses() {
        User user = getCurrentUser();
        return userAddressRepository.findByUser(user).stream().map(userAddressMapper::toResponse).toList();
    }

    public UserAddressResponse createAddress(UserAddressRequest request) {
        User user = getCurrentUser();
        UserAddress address = userAddressMapper.toEntity(request);
        address.setUser(user);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            userAddressRepository.findByUserAndIsDefaultTrue(user).ifPresent(existing -> {
                existing.setIsDefault(false);
                userAddressRepository.save(existing);
            });
        }

        return userAddressMapper.toResponse(userAddressRepository.save(address));
    }

    public UserAddressResponse updateAddress(Long addressId, UserAddressRequest request) {
        User user = getCurrentUser();
        UserAddress address = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        userAddressMapper.update(address, request);

        if (request.getIsDefault() != null && request.getIsDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user).ifPresent(existing -> {
                if (!existing.getId().equals(address.getId())) {
                    existing.setIsDefault(false);
                    userAddressRepository.save(existing);
                }
            });
            address.setIsDefault(true);
        }

        return userAddressMapper.toResponse(userAddressRepository.save(address));
    }

    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        UserAddress address = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        userAddressRepository.delete(address);
    }
}


