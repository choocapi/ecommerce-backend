package com.choocapi.ecommercebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    String token;

    @NotBlank
    String newPassword;
}


