package io.orphea.accessManager.controllers;

import io.orphea.accessManager.library.enums.AccessRequestStatus;
import io.orphea.accessManager.library.models.AccessManagerFilters;
import io.orphea.accessManager.library.models.CloseRequest;
import io.orphea.accessManager.library.models.RequestAccessModel;
import io.orphea.accessManager.library.services.AccessManagerService;
import io.orphea.passport.security.AuthUser;
import io.orphea.sharedutils.DTO.PageToPageDTOMapper;
import io.orphea.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accessManager")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AccessManager", description = "This is an Access Management Service.")
public class AccessManagerController {
    private final PageToPageDTOMapper pageToPageDTOMapper;
    private final AccessManagerService accessManagerService;

    @Operation(summary = "Get All Access Request Belonging to you")
    @PostMapping("/all")
    public ResponseEntity<Object> getAllAccessRequests(@AuthenticationPrincipal AuthUser authUser,
                                                       PageSettings pageSettings,
                                                       @RequestBody AccessManagerFilters accessManagerFilters) {
        try {
            log.info("Request for log page received with data: {}", pageSettings);
            return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                    accessManagerService.getAccessRequestPage(pageSettings, accessManagerFilters, authUser.getId())));
        } catch (Exception e) {
            log.error("Error while getting all access requests: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting all access requests");
        }
    }

    @Operation(summary = "Create a new access request")
    @PostMapping("/createAccessRequest")
    public ResponseEntity<Object> createAccessRequest(@AuthenticationPrincipal AuthUser authUser,
                                                      @RequestBody RequestAccessModel requestAccessModel) {
        try {
            log.info("Making a request access with details: {}", requestAccessModel);
            return ResponseEntity.ok().body(accessManagerService.createAccessRequest(requestAccessModel, authUser.getId()));
        } catch (Exception e) {
            log.error("Error while creating access request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating access request");
        }
    }

    @Operation(summary = "Get Access Request details")
    @GetMapping("/getAccessRequest/{id}")
    public ResponseEntity<Object> getAccessRequest(@PathVariable("id") UUID requestId) {
        try {
            log.info("Get Access Request with id: {}", requestId);
            return ResponseEntity.ok().body(accessManagerService.getAccessRequest(requestId));
        } catch (Exception e) {
            log.error("Error while getting access request details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting access request details");
        }
    }

    @Operation(summary = "Update or Reject access Request")
    @PostMapping("/closeAccessRequest")
    public ResponseEntity<Object> closeAccessRequest(@AuthenticationPrincipal AuthUser authUser,
                                                     @RequestBody CloseRequest closeRequest) {
        try {
            log.info("Get Access Request to close with id: {}", closeRequest);
            return ResponseEntity.ok().body(accessManagerService.closeAccessRequest(authUser.getId(), closeRequest));
        } catch (Exception e) {
            log.error("Error while closing access request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while closing access request");
        }
    }
}
