package com.choocapi.ecommercebackend.dto.request;

import java.time.LocalDate;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String phoneNumber;
    String firstName;
    String lastName;

//    @DobConstraint(min = 16, message = "REQUIRED_FIELD_MISSING")
    LocalDate dateOfBirth;

    String address;
    String avatarUrl;
    Boolean isActive;
    Boolean emailVerified;

    Set<String> roles;
}
