package io.orphea.platform.controllers;

import io.orphea.passport.library.Auth;
import io.orphea.platform.library.models.SparkConfigModel;
import io.orphea.platform.library.repository.SparkConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Platform", description = "This is a API is for platform config.")
public class sparkController {
    private final SparkConfigRepository sparkConfigRepository;

    @Operation(summary = "Update Spark Config for Platform.")
    @PostMapping("/updateSparkConfig")
    @PreAuthorize(Auth.PLATFORM_ADMIN)
    public ResponseEntity<Object> updateSparkConfig(@RequestBody SparkConfigModel sparkConfigModel) {
        SparkConfigModel alreadyExistedModel = sparkConfigRepository.findByConfig("platform");
        //Note:- Removing its update as its not needed for now.
        //alreadyExistedModel.setMaster(sparkConfigModel.getMaster());
        alreadyExistedModel.setDataset(sparkConfigModel.getDataset());
        alreadyExistedModel.setColumnStats(sparkConfigModel.getColumnStats());
        alreadyExistedModel.setPgSync(sparkConfigModel.getPgSync());
        alreadyExistedModel.setSqlBuild(sparkConfigModel.getSqlBuild());
        alreadyExistedModel.setSqlPreview(sparkConfigModel.getSqlPreview());
        alreadyExistedModel.setPythonBuild(sparkConfigModel.getPythonBuild());
        alreadyExistedModel.setPythonPreview(sparkConfigModel.getPythonPreview());
        sparkConfigRepository.save(alreadyExistedModel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get Spark Config for Platform.")
    @GetMapping("/getSparkConfig")
    public ResponseEntity<Object> getGitConfig() {
        SparkConfigModel alreadyExistedModel = sparkConfigRepository.findByConfig("platform");
        return new ResponseEntity<>(alreadyExistedModel, HttpStatus.OK);
    }
}
