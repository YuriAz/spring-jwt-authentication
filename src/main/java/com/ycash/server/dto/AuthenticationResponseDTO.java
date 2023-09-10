package com.ycash.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AuthenticationResponseDTO(
        String accessToken,
        String refreshToken,
        boolean mfaEnabled,
        String secretImageUri
) {
}
