package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.ApiResponse;
import com.abarrote.abarroteapi.dto.VersionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/version")
public class VersionController {

    @GetMapping
    public ResponseEntity<ApiResponse<VersionResponse>> version() {
        VersionResponse version = new VersionResponse(
                "Abarrote Cloud",
                "1.0.0",
                "Abarrote Team"
        );
        return ResponseEntity.ok(ApiResponse.ok(version));
    }
}
