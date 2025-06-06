package io.bosler.kitab.controllers;

import io.bosler.kitab.library.models.BranchModel;
import io.bosler.kitab.library.repository.BranchRepository;
import io.bosler.kitab.library.services.BranchService;
import io.bosler.kitab.library.services.ResourceService;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/branch")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class branchController {

    private final UserService userService;
    private final BranchRepository branchRepository;
    private final AuthzService authzService;
    private final BranchService branchService;
    private final ResourceService resourceService;

    // Clean this using getTYpe in branch service
    @Operation(summary = "It return type for dataset and branch")
    @GetMapping("/{datasetId}/{branch}/getType")
    public ResponseEntity<Object> branchType(Principal principal, @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!resourceService.existsById((datasetId))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(HttpStatus.EXPECTATION_FAILED.value(), "Dataset with id " + datasetId + " not found"));
        }
        if (!authzService.isViewer(userId, datasetId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDTO(HttpStatus.FORBIDDEN.value(), "Unauthorized to perform this action"));
        }

        HashMap<String, Object> response = new HashMap<>();

        response.put("branchType", branchService.getBranchModel(datasetId, branch).getType());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get all branches by dataset id.")
    @GetMapping("/datasetBranches/{id}")
    ResponseEntity<Object> getDatasetBranches(@PathVariable("id") UUID datasetId) throws Exception {
        return new ResponseEntity<>(branchService.findAllBranchModelByDatasetId(datasetId), HttpStatus.OK);
    }

    @Operation(summary = "Get all branch by ID.")
    @GetMapping("/branch/{id}")
    ResponseEntity<Object> getBranch(@PathVariable("id") String branchId) throws Exception {
        return new ResponseEntity<>(branchRepository.getReferenceById(branchId), HttpStatus.OK);
    }

    @Operation(summary = "Check if a branch exist for a dataset.")
    @GetMapping("/branches/{datasetId}/{branch}")
    ResponseEntity<Object> checkForBranch(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) throws Exception {
        boolean isPresent = branchService.isBranchPresent(datasetId, branch);

        if (!isPresent) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }


    @Operation(summary = "resolve branches")
    @PostMapping("/resolveBranch")
    ResponseEntity<Object> resolveBranches(Principal principal, @RequestBody BranchModel resolveBranchBody) {
        UUID user = userService.getUser(principal.getName()).getId();

        branchService.resolveBranch(resolveBranchBody.getDatasetId(),
                resolveBranchBody.getBranch(),
                resolveBranchBody.getRepositoryId(),
                UUID.fromString(resolveBranchBody.getBuildId()),
                user);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
