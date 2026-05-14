package io.orphea.platform.controllers;

import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.platform.library.models.*;
import io.orphea.platform.library.repository.DataMartConfigRepository;
import io.orphea.platform.library.repository.PlatformConfigRepository;
import io.orphea.platform.library.repository.SMTPConfigRepository;
import io.orphea.platform.library.services.PlatformConfigService;
import io.orphea.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Platform", description = "This is a API is for platform config.")
public class platformController {
    private final UserService userService;
    private final PlatformConfigRepository platformConfigRepository;
    private final DataMartConfigRepository datamartConfigRepository;
    private final SMTPConfigRepository smtpConfigRepository;
    private final AuthzService authzService;
    private final PlatformConfigService platformConfigService;

    @Operation(summary = "Update SMTP Config for Platform.")
    @PostMapping("/updateSMTPConfig")
    public ResponseEntity<Object> updateSMTPConfig(Principal principal, @RequestBody SMTPConfigModel smtpConfigModel) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to configure mailer", HttpStatus.FORBIDDEN);

        SMTPConfigModel alreadyExistedModel = smtpConfigRepository.findByConfig("platform");
        alreadyExistedModel.setSmtpEmail(smtpConfigModel.getSmtpEmail());
        alreadyExistedModel.setSmtpPassword(smtpConfigModel.getSmtpPassword());
        alreadyExistedModel.setHost(smtpConfigModel.getHost());
        alreadyExistedModel.setPort(smtpConfigModel.getPort());
        alreadyExistedModel.setAuth(smtpConfigModel.getAuth());
        alreadyExistedModel.setTtls(smtpConfigModel.getTtls());

        smtpConfigRepository.save(alreadyExistedModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get SMTP Config for Platform.")
    @GetMapping("/getSMTPConfig")
    public ResponseEntity<Object> getSMTPConfig(Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to configure mailer", HttpStatus.FORBIDDEN);

        SMTPConfigModel alreadyExistedModel = smtpConfigRepository.findByConfig("platform");

        return new ResponseEntity<>(alreadyExistedModel, HttpStatus.OK);
    }


    @Operation(summary = "Checks the Running Environment Config")
    @GetMapping(path = "/config")
    ResponseEntity<Object> getPlatformConfig(Principal principal) {
        PlatformConfigResponse platformConfigResponse = new PlatformConfigResponse();
        platformConfigResponse.setPlatformConfig(platformConfigRepository.findByName("platformConfig").orElseThrow());

        Versions versions = new Versions();
        Map<String, String> nonNullVersions = versions.getNonNullVersions();

        platformConfigResponse.setVersions(nonNullVersions);

        if (System.getenv("LAST_UPDATED_ON") != null) {
            platformConfigResponse.setLastUpdatedOn(System.getenv("LAST_UPDATED_ON"));
        }

        return new ResponseEntity<>(platformConfigResponse, HttpStatus.OK);
    }

    @Operation(summary = "Updates platform config")
    @PostMapping(path = "/config")
    ResponseEntity<Object> updatePlatformConfig(Principal principal, @RequestBody PlatformConfigUpdateModel platformConfigUpdateModel) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to change platform config", HttpStatus.FORBIDDEN);

        PlatformConfig platformConfig = new PlatformConfig();

        if (platformConfigRepository.existsByName("platformConfig")) {
            platformConfig = platformConfigRepository.findByName("platformConfig").orElseThrow();
            platformConfig.setUpdatedBy(userId);
            platformConfig.setUpdatedAt(new Date());
        } else {
            platformConfig.setName("platformConfig");
            platformConfig.setCreatedBy(userId);
            platformConfig.setCreatedAt(new Date());
        }
        platformConfig.setDefaultBranch(platformConfigUpdateModel.getDefaultBranch());
        platformConfig.setLicenseKey(platformConfigUpdateModel.getLicenseKey());
        platformConfig.setPlatformName(platformConfigUpdateModel.getPlatformName());
        platformConfig.setDownload(platformConfigUpdateModel.isDownload());
        platformConfig.setCustomTheme(platformConfigUpdateModel.getCustomTheme());
        platformConfig.setSizeLimit(platformConfigUpdateModel.getSizeLimit());
        platformConfig.setRowLimit(platformConfigUpdateModel.getRowLimit());
        platformConfig.setUpload(platformConfigUpdateModel.isUpload());
        platformConfig.setTimezone(platformConfigUpdateModel.getTimezone());
        platformConfig.setCache(platformConfigUpdateModel.isCache());
        platformConfig.setCacheExpiration(platformConfigUpdateModel.getCacheExpiration());
        platformConfig.setLogo(platformConfigUpdateModel.getLogo());
        platformConfig.setDatasetHistory(platformConfigUpdateModel.getDatasetHistory());
        platformConfig.setArtifactory(platformConfigUpdateModel.isArtifactory());
        platformConfig.setArtifactoryUrl(platformConfigUpdateModel.getArtifactoryUrl());
        platformConfig.setHttpProxy(platformConfigUpdateModel.isHttpProxy());
        platformConfig.setHttpProxyUrl(platformConfigUpdateModel.getHttpProxyUrl());
        platformConfig.setMfaEnabled(platformConfigUpdateModel.getMfaEnabled());
        platformConfig.setMfaEnforced(platformConfigUpdateModel.getMfaEnforced());
        platformConfig.setDataMartEnabled(platformConfigUpdateModel.getDataMartEnabled());
        return new ResponseEntity<>(platformConfigRepository.save(platformConfig), HttpStatus.OK);
    }

    @Operation(summary = "Generates a license Key")
    @PostMapping(path = "/decryptLicenseKey")
    ResponseEntity<Object> decryptLicenseKey(@RequestBody String licenseKey) {
        try {
            LicenseInfoModel licenseInfoModel = PlatformConfigService.decrypt(licenseKey);
            return new ResponseEntity<>(licenseInfoModel, HttpStatus.OK);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO.builder().status(HttpStatus.BAD_REQUEST.value()).error("Invalid License Key").description("Please Contact Orphea Team").build());
        }
    }
}



