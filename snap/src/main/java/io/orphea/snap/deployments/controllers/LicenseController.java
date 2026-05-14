package io.orphea.snap.deployments.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.orphea.snap.deployments.library.models.LicenseModel;
import io.orphea.snap.deployments.library.repository.LicenseRepository;
import io.orphea.snap.deployments.library.requests.LicenseRequestModel;
import io.orphea.snap.deployments.library.services.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deployments/license")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Deployments", description = "Manage License")
public class LicenseController {

    private final LicenseService licenseService;
    private final LicenseRepository licenseRepository;

    @Operation(summary = "Create license")
    @PostMapping(path = "/create/{deploymentId}")
    public ResponseEntity<Object> createLicense(Principal principal,
                                                @PathVariable("deploymentId") UUID deploymentId,
                                                @RequestBody LicenseRequestModel requestModel) {
        LicenseModel licenseModel = licenseService.createOrUpdateLicense(deploymentId, requestModel);
        return new ResponseEntity<>(licenseModel, HttpStatus.CREATED);
    }

    @Operation(summary = "Get license by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getLicenseById(Principal principal,
                                                 @PathVariable("Id") UUID Id) throws JsonProcessingException {
        if (!licenseRepository.existsById(Id)) {
            return new ResponseEntity<>("No license with the id exist", HttpStatus.NOT_FOUND);
        }

        LicenseModel licenseModel = licenseRepository.findById(Id).orElse(null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(licenseModel);

        return new ResponseEntity<>(respData, HttpStatus.OK);
    }

    @Operation(summary = "Delete license")
    @DeleteMapping("/{Id}")
    public ResponseEntity<Object> deleteLicense(Principal principal,
                                                @PathVariable("Id") UUID Id) {
        if (!licenseRepository.existsById(Id)) {
            return new ResponseEntity<>("No license with the id exist", HttpStatus.NOT_FOUND);
        }

        licenseRepository.deleteById(Id);
        return new ResponseEntity<>("License deleted successfully", HttpStatus.OK);
    }
}
