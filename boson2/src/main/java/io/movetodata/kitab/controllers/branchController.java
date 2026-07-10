package io.movetodata.kitab.controllers;

import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/branch")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class branchController {

    private final UserService userService;
    private final BranchRepository branchRepository;
    private final FolderRepository folderRepository;
    private final DatasetRepository datasetRepository;
    private final AuthzService authzService;


    @Operation(summary = "It return type for dataset and branch")
    @GetMapping("/{datasetId}/{branch}/getType")
    public ResponseEntity<Object> branchType(Principal principal, @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById((datasetId))) {
            return new ResponseEntity<>("Dataset with id " + datasetId + " not found", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(branchRepository.findBranchModelByDatasetIdAndBranch(datasetId, branch).getType(), HttpStatus.OK);
    }

    @Operation(summary = "Get all branches by datasetId.")
    @GetMapping("/branches/{datasetId}")
    ResponseEntity<Object> branches(@PathVariable("datasetId") UUID datasetId) throws Exception {


        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        //        TODO : this needs to come from Kitab Branch repository
        List<String> branches = new ArrayList<>();
        branches.add("main");

        return new ResponseEntity<>(branches, HttpStatus.OK);
    }

    @Operation(summary = "resolve branches")
    @PostMapping("/resolveBranch")
    ResponseEntity<Object> resolveBranches(Principal principal, @RequestBody BranchModel resolveBranchBody) {
        // TODO : auth

        try {
            UUID userId = userService.getUser(principal.getName()).id;
//            Boolean existByRepoAndBranchAndDataset = branchRepository.existsByDatasetIdAndBranchAndRepositoryId(
//                    resolveBranchBody.getDatasetId(),
//                    resolveBranchBody.getBranch(),
//                    resolveBranchBody.getRepositoryId());

            Boolean existByBranchAndDataset = branchRepository.existsByDatasetIdAndBranch(
                    resolveBranchBody.getDatasetId(),
                    resolveBranchBody.getBranch());

            if (!existByBranchAndDataset) {
                BranchModel newBranch = new BranchModel();
                newBranch.setBranch(resolveBranchBody.getBranch());
                newBranch.setCreatedBy(userId);
                newBranch.setUpdatedBy(userId);
                newBranch.setDatasetId(resolveBranchBody.getDatasetId());
                newBranch.setRepositoryId(resolveBranchBody.getRepositoryId());
                newBranch.setType("parquet");

                branchRepository.saveAndFlush(newBranch);
            } else {

                BranchModel existingBranchModel = branchRepository.findBranchModelByDatasetIdAndBranch(resolveBranchBody.getDatasetId(), resolveBranchBody.getBranch());
                existingBranchModel.setUpdatedAt(new Date());
                existingBranchModel.setUpdatedBy(userId);
                existingBranchModel.setDatasetId(resolveBranchBody.getDatasetId());
                existingBranchModel.setRepositoryId(resolveBranchBody.getRepositoryId());

                branchRepository.save(existingBranchModel);
            }

//            else {
//                Boolean existByRepoAndBranch = branchRepository.existsByRepositoryIdAndBranch(
//                        resolveBranchBody.getRepositoryId(),
//                        resolveBranchBody.getBranch());
//
//                if (!existByRepoAndBranch) {
//                    BranchModel newBranch = new BranchModel();
//                    newBranch.setBranch(resolveBranchBody.getBranch());
//                    newBranch.setCreatedBy(user);
//                    newBranch.setUpdatedBy(user);
//                    newBranch.setDatasetId(resolveBranchBody.getDatasetId());
//                    newBranch.setRepositoryId(resolveBranchBody.getRepositoryId());
//                    newBranch.setType("parquet");
//
//                    branchRepository.saveAndFlush(newBranch);
//                } else {
//                    BranchModel existingBranchModel = branchRepository.findBranchModelByRepositoryIdAndBranch(
//                            resolveBranchBody.getRepositoryId(),
//                            resolveBranchBody.getBranch());
//                    existingBranchModel.setUpdatedAt(new Date());
//                    existingBranchModel.setUpdatedBy(user);
//                    existingBranchModel.setDatasetId(resolveBranchBody.getDatasetId());
//                }
//            }


            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.", HttpStatus.BAD_REQUEST);
        }
    }
}
