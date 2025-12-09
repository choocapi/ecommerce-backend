package com.choocapi.ecommercebackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotEmpty(message = "REQUIRED_FIELD_MISSING")
    String oldPassword;

    @NotEmpty(message = "REQUIRED_FIELD_MISSING")
    String newPassword;
}
