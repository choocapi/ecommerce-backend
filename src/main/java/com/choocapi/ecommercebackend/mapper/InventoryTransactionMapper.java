package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.choocapi.ecommercebackend.dto.response.InventoryTransactionResponse;
import com.choocapi.ecommercebackend.entity.InventoryTransaction;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, SupplierMapper.class})
public interface InventoryTransactionMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "type", expression = "java(transaction.getType() != null ? transaction.getType().name() : null)")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);
}

