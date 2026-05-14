package io.orphea.zoro.controllers;


import io.orphea.fractal.library.services.GitService;
import io.orphea.kitab.library.repository.DatasetRepository;
import io.orphea.zoro.library.repository.ZoroRepository;
import io.orphea.zoro.library.services.ZoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/zoro")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Zoro", description = "This is a data management service.")
public class branch1Controller {

    private final ZoroRepository zoroRepository;
    private final ZoroService zoroService;
    private final GitService gitServiceZoro;
    private final DatasetRepository datasetRepository;

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
}
