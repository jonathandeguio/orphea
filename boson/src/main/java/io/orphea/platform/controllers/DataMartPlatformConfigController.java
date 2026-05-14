package io.orphea.platform.controllers;

import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.platform.library.models.DataMartConfigModel;
import io.orphea.platform.library.services.DataMartPlatformConfigService;
import io.orphea.synchro.library.services.DataMartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "PlatformDataMart", description = "Manage all data mart configurations from here.")
public class DataMartPlatformConfigController {
    private final UserService userService;
    private final DataMartPlatformConfigService dataMartPlatformConfigService;
    private final DataMartService dataMartService;
    private final AuthzService authzService;

    @Operation(summary = "Get DataMart Config for Platform.")
    @GetMapping("/getDataMartConfig")
    public ResponseEntity<Object> getDataMartConfig(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied to configure mailer", HttpStatus.FORBIDDEN);

        return new ResponseEntity<>(dataMartPlatformConfigService.getDataMartPlatformConfig(), HttpStatus.OK);
    }

    @Operation(summary = "Get DataMart Config for Platform.")
    @GetMapping("/getDataMartModel/{id}")
    public ResponseEntity<Object> getDataMartModel(Principal principal, @PathVariable UUID id) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);

        return new ResponseEntity<>(dataMartPlatformConfigService.getDataMartModel(id), HttpStatus.OK);
    }

    @Operation(summary = "Get DataMart Database Tree data  for Platform.")
    @GetMapping("/getDataMartDatabaseTreeData/{id}")
    public ResponseEntity<Object> getDataMartDatabaseTreeData(Principal principal, @PathVariable UUID id) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isPlatformAdmin(userId))
            return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);

        return new ResponseEntity<>(dataMartService.getDataMartSourceContentMetaData(id), HttpStatus.OK);
    }

    @Operation(summary = "Update DataMart Config for Platform.")
    @PostMapping("/updateDataMartConfig")
    public ResponseEntity<Object> updateDataMartConfig(Principal principal, @RequestBody DataMartConfigModel datamartConfigModel) {
        UUID userId = userService.getUser(principal.getName()).id;
        return new ResponseEntity<>(dataMartPlatformConfigService.putDataMartPlatformConfig(datamartConfigModel, userId), HttpStatus.OK);
    }
}

