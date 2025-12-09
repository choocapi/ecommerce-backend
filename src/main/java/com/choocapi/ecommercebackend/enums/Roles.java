package com.choocapi.ecommercebackend.enums;

import com.choocapi.ecommercebackend.entity.Role;

public enum Roles {
    ADMIN,
    STAFF,
    CUSTOMER;

    public Role getRoleEntity() {
        return Role.builder()
                .name(this.name())
                .build();
    }
}
