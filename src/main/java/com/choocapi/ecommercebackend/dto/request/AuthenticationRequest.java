package com.choocapi.ecommercebackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @Email(message = "REQUIRED_FIELD_MISSING")
    String email;

    @NotEmpty(message = "REQUIRED_FIELD_MISSING")
    String password;
}
