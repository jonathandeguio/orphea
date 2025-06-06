package io.bosler.snap.git.controllers;

import io.bosler.snap.git.library.models.GitRepositoryModel;
import io.bosler.snap.git.library.services.GitRepositoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Github", description = "Sync With Github")
public class GitSyncController {

    @Autowired
    private GitRepositoryService gitRepositoryService;

    @GetMapping("/sync/{org}")
    public ResponseEntity<?> syncRepositories(@PathVariable("org") String organization) {
        try {
             List<GitRepositoryModel> repositories = gitRepositoryService.syncRepositories(organization);
            return new ResponseEntity<>(repositories, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}