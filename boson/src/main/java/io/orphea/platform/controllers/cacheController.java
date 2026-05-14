package io.orphea.platform.controllers;


import io.orphea.passport.library.Auth;
import io.orphea.platform.library.models.BackingFsConfigModel;
import io.orphea.platform.library.models.CacheConfigModel;
import io.orphea.platform.library.models.GitConfigModel;
import io.orphea.platform.library.repository.CacheRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cache", description = "This is a API is for cache config.")
public class cacheController {
    private final CacheRepository cacheRepository;

    @Operation(summary = "Update Cache Config for Platform.")
    @PostMapping("/updateCacheConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> updateBackingFsConfig(@RequestBody CacheConfigModel cacheConfigModel) {
        CacheConfigModel alreadyExistedModel = cacheRepository.findByConfig("platform");
        alreadyExistedModel.setCache(cacheConfigModel.isCache());
        alreadyExistedModel.setCacheExpiration(cacheConfigModel.getCacheExpiration());
        alreadyExistedModel.setUseRedis(cacheConfigModel.isUseRedis());
        alreadyExistedModel.setRedisUrl(cacheConfigModel.getRedisUrl());
        cacheRepository.save(alreadyExistedModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get Cache Config for Platform.")
    @GetMapping("/getCacheConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getBackingFsConfig() {
        CacheConfigModel alreadyExistedModel = cacheRepository.findByConfig("platform");
        return new ResponseEntity<>(alreadyExistedModel, HttpStatus.OK);
    }
}
