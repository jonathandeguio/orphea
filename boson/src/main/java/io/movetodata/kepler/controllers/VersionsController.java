package io.movetodata.kepler.controllers;

import io.movetodata.kepler.library.DTOs.VersionRenameDTO;
import io.movetodata.kepler.library.models.ResourceVersionDetailsModel;
import io.movetodata.kepler.library.models.ResourceVersionsModel;
import io.movetodata.kepler.library.repository.ChartsRepository;
import io.movetodata.kepler.library.repository.ResourceVersionDetailsRepository;
import io.movetodata.kepler.library.repository.ResourceVersionsRepository;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.UUID;


@RestController
@RequestMapping("/api/kepler/")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler versions management service endpoints")
public class VersionsController {
    private final ResourceVersionDetailsRepository resourceVersionDetailsRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final UserService userService;
    private final ChartsRepository chartsRepository;
    private final ResourceService resourceService;
    private final ResourceViewsRepository resourceViewsRepository;
    private final AuthzService authzService;

    @Operation(summary = "This gives all the versions of a resource.")
    @GetMapping("/history/{id}")
    public ResponseEntity<Object> getVersionsHistory(Principal principal, @PathVariable("id") UUID id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            throw new UnauthorizedException();
        }

        if (!resourceService.existsById(id)) {
            return new ResponseEntity<>("No resource found for given Id" + id, HttpStatus.NOT_FOUND);
        }
        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore chart to access it." + id, HttpStatus.NOT_FOUND);
        }

        ResourceVersionsModel history = resourceVersionsRepository.findById(id).orElseThrow();

        return new ResponseEntity<>(history, HttpStatus.OK);

    }

    @Operation(summary = "Rename saved version name")
    @PutMapping("/rename")
    public ResponseEntity<Object> renameVersionName(Principal principal, @RequestBody VersionRenameDTO version) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, version.getResourceId())) {
            throw new UnauthorizedException();
        }

        if (!resourceVersionDetailsRepository.existsById(version.getVersionId())) {
            return new ResponseEntity<>("No version found for given Id" + version.getVersionId(), HttpStatus.NOT_FOUND);
        }

        ResourceVersionDetailsModel versionDetailsModel = resourceVersionDetailsRepository.getReferenceById(version.getVersionId());
        versionDetailsModel.setName(version.getName());
        resourceVersionDetailsRepository.save(versionDetailsModel);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

