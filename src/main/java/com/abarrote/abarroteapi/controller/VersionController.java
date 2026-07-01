package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.dto.VersionResponse;
import com.abarrote.abarroteapi.service.VersionService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/version")
    public VersionResponse version() {
        return versionService.getVersion();
    }
}