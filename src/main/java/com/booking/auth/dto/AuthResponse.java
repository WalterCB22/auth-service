package com.booking.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
}
