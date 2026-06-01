package com.shopsphere.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class LoginRequest {

    @NotBlank(message ="Email is required")
    @Email(message = "Envalid email Format")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

}
