package io.bosler.platform.controllers;

import io.bosler.passport.library.Auth;
import io.bosler.platform.library.models.GitConfigModel;
import io.bosler.platform.library.repository.GitConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Platform", description = "This is a API is for platform config.")
public class gitController {
    private final GitConfigRepository gitConfigRepository;

    @Operation(summary = "Update Git Config for Platform.")
    @PostMapping("/updateGitConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> updateGitConfig(@RequestBody GitConfigModel gitConfigModel) {
        GitConfigModel alreadyExistedModel = gitConfigRepository.findByConfig("platform");
        alreadyExistedModel.setHost(gitConfigModel.getHost());
        alreadyExistedModel.setApiPort(gitConfigModel.getApiPort());
        alreadyExistedModel.setPort(gitConfigModel.getPort());
        gitConfigRepository.save(alreadyExistedModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get Git Config for Platform.")
    @GetMapping("/getGitConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getGitConfig() {
        GitConfigModel alreadyExistedModel = gitConfigRepository.findByConfig("platform");
        return new ResponseEntity<>(alreadyExistedModel, HttpStatus.OK);
    }
}
