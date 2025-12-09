package com.choocapi.ecommercebackend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Set<RoleResponse> roles;
    String address;
    String avatarUrl;
    Boolean isActive;
    Boolean emailVerified;
    Instant createdAt;
}
