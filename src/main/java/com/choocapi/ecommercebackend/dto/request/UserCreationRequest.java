package com.choocapi.ecommercebackend.dto.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class UserCreationRequest {

    @Email(message = "REQUIRED_FIELD_MISSING")
    String email;

    @Size(min = 6, message = "REQUIRED_FIELD_MISSING")
    String password;

    String firstName;
    String lastName;
    String phoneNumber;
    String address;
    LocalDate dateOfBirth;
    Boolean isActive;
    Boolean emailVerified;
    Set<String> roles;
}
