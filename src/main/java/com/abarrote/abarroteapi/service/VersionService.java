package com.abarrote.abarroteapi.service;

import org.springframework.stereotype.Service;
import com.abarrote.abarroteapi.dto.VersionResponse;

@Service
public class VersionService {

    public VersionResponse getVersion() {
        return new VersionResponse(
                "Abarrote Cloud",
                "1.0.0",
                "Tony Montaña"
        );
    }
}