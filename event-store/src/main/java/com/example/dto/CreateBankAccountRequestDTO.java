package com.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBankAccountRequestDTO(
    @Email @NotBlank @Size(min = 10, max = 250) String email,
    @NotBlank @Size(min = 10, max = 250) String address,
    @NotBlank @Size(min = 10, max = 250) String userName) {
}
