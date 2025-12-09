package com.choocapi.ecommercebackend.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.choocapi.ecommercebackend.dto.request.InventoryTransactionRequest;
import com.choocapi.ecommercebackend.dto.response.InventoryTransactionResponse;
import com.choocapi.ecommercebackend.entity.InventoryTransaction;
import com.choocapi.ecommercebackend.entity.Product;
import com.choocapi.ecommercebackend.entity.Supplier;
import com.choocapi.ecommercebackend.enums.InventoryTransactionType;
import com.choocapi.ecommercebackend.exception.AppException;
import com.choocapi.ecommercebackend.exception.ErrorCode;
import com.choocapi.ecommercebackend.mapper.InventoryTransactionMapper;
import com.choocapi.ecommercebackend.repository.InventoryTransactionRepository;
import com.choocapi.ecommercebackend.repository.ProductRepository;
import com.choocapi.ecommercebackend.repository.SupplierRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionService {

    InventoryTransactionRepository repository;
    ProductRepository productRepository;
    SupplierRepository supplierRepository;
    InventoryTransactionMapper mapper;

    public InventoryTransactionResponse create(InventoryTransactionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        InventoryTransactionType transactionType = parseType(request.getType());

        BigDecimal currentImportPrice = product.getImportPrice() != null ? product.getImportPrice() : BigDecimal.ZERO;
        BigDecimal newImportPrice = request.getPrice() != null ? request.getPrice() : currentImportPrice;
        Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
        Integer changeQuantity = request.getQuantity() != null ? request.getQuantity() : 0;
        Integer resultingQuantity = currentQuantity;

        switch (transactionType) {
            case IN:
                validateQuantity(changeQuantity);
                resultingQuantity = currentQuantity + changeQuantity;
                product.setImportPrice(newImportPrice);
                break;
            case OUT:
                validateQuantity(changeQuantity);
                if (currentQuantity < changeQuantity) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }
                resultingQuantity = currentQuantity - changeQuantity;
                break;
            case ADJUST:
                Integer targetQuantity = request.getTargetQuantity();
                if (targetQuantity == null || targetQuantity < 0) {
                    throw new AppException(ErrorCode.INVALID_REQUEST);
                }
                changeQuantity = Math.abs(targetQuantity - currentQuantity);
                resultingQuantity = targetQuantity;
                break;
            default:
                throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        product.setQuantity(resultingQuantity);
        productRepository.save(product);

        InventoryTransaction entity = InventoryTransaction.builder()
                .product(product)
                .supplier(null)
                .type(transactionType)
                .quantity(changeQuantity)
                .price(newImportPrice)
                .orderId(request.getOrderId())
                .note(request.getNote())
                .previousQuantity(currentQuantity)
                .resultingQuantity(resultingQuantity)
                .build();

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            entity.setSupplier(supplier);
        }

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    public Page<InventoryTransactionResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public Page<InventoryTransactionResponse> listByProduct(Long productId, Pageable pageable,
            InventoryTransactionType type) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        Page<InventoryTransaction> page = type != null
                ? repository.findByProductAndType(product, type, pageable)
                : repository.findByProduct(product, pageable);
        return page.map(mapper::toResponse);
    }

    public InventoryTransactionResponse get(Long id) {
        InventoryTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapper.toResponse(transaction);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private InventoryTransactionType parseType(String rawType) {
        if (!StringUtils.hasText(rawType)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        try {
            return InventoryTransactionType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
