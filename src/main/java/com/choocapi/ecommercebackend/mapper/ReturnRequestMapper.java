package com.choocapi.ecommercebackend.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.choocapi.ecommercebackend.dto.request.ReturnRequestRequest;
import com.choocapi.ecommercebackend.dto.response.ReturnRequestResponse;
import com.choocapi.ecommercebackend.entity.ReturnRequest;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReturnRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    ReturnRequest toEntity(ReturnRequestRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void update(@MappingTarget ReturnRequest entity, ReturnRequestRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user", target = "user")
    ReturnRequestResponse toResponse(ReturnRequest returnRequest);
}
