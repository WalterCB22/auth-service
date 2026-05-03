package com.booking.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    private String username;
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("business_id")
    private UUID businessId;

    private Set<String> roles;
}
