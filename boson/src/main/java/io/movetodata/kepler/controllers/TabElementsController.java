package io.movetodata.kepler.controllers;

import io.movetodata.kepler.library.models.ManageElementUpdateRequest;
import io.movetodata.kepler.library.models.TabElementsModel;
import io.movetodata.kepler.library.repository.TabElementsRepository;
import io.movetodata.kepler.library.repository.TabsRepository;
import io.movetodata.kepler.library.services.TabElementsService;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/kepler/tabElements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler dashboards management service endpoints")
public class TabElementsController {

    private final TabElementsRepository tabElementsRepository;
    private final TabsRepository tabsRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final TabElementsService tabElementsService;

    @Operation(summary = "Get all elements")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllElements(Principal principal) {
        List<TabElementsModel> elements = tabElementsRepository.findAll();

        return ResponseEntity.accepted().body(elements);
    }

    @Operation(summary = "This can be used to get tab elements.")
    @GetMapping("/getElements/{dashboardId}/{tabId}")
    public ResponseEntity<Object> getElements(Principal principal, @PathVariable("dashboardId") UUID dashboardId, @PathVariable("tabId") UUID tabId) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, dashboardId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(tabId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<TabElementsModel> tabElementsModels = tabElementsService.getTabElements(tabId);
        return ResponseEntity.accepted().body(tabElementsModels);
    }

    @Operation(summary = "This can be used to create tab elements.")
    @PostMapping("/create")
    public ResponseEntity<Object> createElement(@Valid @RequestBody ManageElementUpdateRequest manageElementUpdateRequest, Principal principal) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, manageElementUpdateRequest.getDashboardId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(manageElementUpdateRequest.getTabId())) {
            throw new Exception("Tab does not exist");
        }

        TabElementsModel newElement = tabElementsService.createTabElement(manageElementUpdateRequest, userId);

        return ResponseEntity.accepted().body(newElement);
    }

    @Operation(summary = "Update Element.")
    @PostMapping("/update/{dashboardId}/{tabId}")
    public ResponseEntity<Object> updateElement(@PathVariable("dashboardId") UUID dashboardId, @PathVariable("tabId") UUID tabId, @Valid @RequestBody List<ManageElementUpdateRequest> manageElementUpdateRequestList, Principal principal) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, dashboardId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        tabElementsService.updateTabElements(manageElementUpdateRequestList, dashboardId, tabId, userId);

        return ResponseEntity.accepted().body("Updated Success");
    }
}

