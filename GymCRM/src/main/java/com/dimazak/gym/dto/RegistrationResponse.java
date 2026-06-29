package com.dimazak.gym.dto;

public record RegistrationResponse(
        String username,
        String password,
        String token
) {}