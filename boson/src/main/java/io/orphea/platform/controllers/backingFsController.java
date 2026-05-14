package io.orphea.platform.controllers;

import io.orphea.passport.library.Auth;
import io.orphea.platform.library.models.BackingFsConfigModel;
import io.orphea.platform.library.repository.BackingFsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.transaction.Transactional;

@Slf4j
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Platform", description = "This is a API is for platform config.")
public class backingFsController {
    private final BackingFsRepository backingFsRepository;

    @Operation(summary = "Update backingFs Config for Platform.")
    @PostMapping("/updateBackingFsConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> updateBackingFsConfig(@RequestBody BackingFsConfigModel backingFsConfigModel) {
        BackingFsConfigModel alreadyExistedModel = backingFsRepository.findByConfig("platform");
        if(backingFsConfigModel.getFsType().equals("localfs")){
            alreadyExistedModel.setLocalFs(backingFsConfigModel.getLocalFs());
        }
        else if(backingFsConfigModel.getFsType().equals("gs")){
            alreadyExistedModel.setGsBucket(backingFsConfigModel.getGsBucket());
            alreadyExistedModel.setGsCredentials(backingFsConfigModel.getGsCredentials());
        }
        else if(backingFsConfigModel.getFsType().equals("hdfs")){
            alreadyExistedModel.setHdfs(backingFsConfigModel.getHdfs());
        }
        else if(backingFsConfigModel.getFsType().equals("s3")){
            alreadyExistedModel.setS3AccessKey(backingFsConfigModel.getS3AccessKey());
            alreadyExistedModel.setS3SecretKey(backingFsConfigModel.getS3SecretKey());
        }
        alreadyExistedModel.setFsType(backingFsConfigModel.getFsType());
        backingFsRepository.save(alreadyExistedModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get BackingFs Config for Platform.")
    @GetMapping("/getBackingFsConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> getBackingFsConfig() {
        BackingFsConfigModel alreadyExistedModel = backingFsRepository.findByConfig("platform");
        return new ResponseEntity<>(alreadyExistedModel, HttpStatus.OK);
    }
}
