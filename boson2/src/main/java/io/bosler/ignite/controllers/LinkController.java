package io.bosler.ignite.controllers;

import io.bosler.bezier.library.models.PipelineModel;
import io.bosler.bezier.library.repository.PipelineRepository;
import io.bosler.ignite.library.models.IgniteConfig;
import io.bosler.ignite.library.models.Links;
import io.bosler.ignite.library.models.Sources;
import io.bosler.ignite.library.repository.IgniteConfigRepository;
import io.bosler.ignite.library.repository.LinkRepository;
import io.bosler.ignite.library.repository.SourcesRepository;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;

import static io.bosler.sharedUtils.Utils.copyNonNullProperties;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/ignite/link")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ignite", description = "Code Repository management service endpoints")
public class LinkController {

    private final LinkRepository linkRepository;
    private final SourcesRepository sourcesRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final IgniteConfigRepository igniteConfigRepository;
    private final DatasetRepository datasetRepository;
    private final FolderRepository folderRepository;
    private final PipelineRepository pipelineRepository;


    @Operation(summary = "Get all links.")
    @GetMapping("/Getall")
    public ResponseEntity<Object> getAll(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse

    ) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        List<Links> Links = linkRepository.findAll();
        return new ResponseEntity<>(Links, HttpStatus.OK);
    }

    @Operation(summary = "It provides link by Id")
    @GetMapping("/GetById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!linkRepository.existsById(Id)) {
            return new ResponseEntity<>("link by id does not exist.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(linkRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It delete link by Id")
    @DeleteMapping("/DeleteById/{Id}")
    public ResponseEntity<Object> deleteById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }

        if (!linkRepository.existsById(Id)) {
            return new ResponseEntity<>("link by id does not exist.", HttpStatus.NOT_FOUND);
        }
        linkRepository.deleteById(Id);

        // delete from kitab also

        folderRepository.deleteById(Id);

        pipelineRepository.deleteByTargetDatasetAndTargetBranch(Id, null);

        // TODO : update config Status
//        IgniteConfig igniteConfig = new IgniteConfig();
//        igniteConfig.setAgentId(link1.getAgentId());
//        igniteConfig.setVersion(UUID.randomUUID());
//        igniteConfigRepository.save(igniteConfig);

        return new ResponseEntity<>(" Deleted Successfully. ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create link.")
    @PostMapping("/create")
    public ResponseEntity<Object> newLink(Principal principal, @RequestBody Links links) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!datasetRepository.existsById(links.getDatasetId())) {
            return new ResponseEntity<>("dataset by id does not exist.", HttpStatus.NOT_FOUND);
        }
        if (linkRepository.existsByDatasetId(links.getDatasetId())) {
            return new ResponseEntity<>("dataset already linked.", HttpStatus.NOT_FOUND);
        }
        if (!sourcesRepository.existsById(links.getSourceId())) {
            return new ResponseEntity<>("source by id does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!folderRepository.existsById(links.getParent())) {
            return new ResponseEntity<>("parent by id does not exist.", HttpStatus.NOT_FOUND);
        }

        //UUID userId = userService.getUser(principal.getName()).id;


        // Create in kitab
        FolderModel folderModel = new FolderModel();
        folderModel.setName(links.getName());
        folderModel.setDescription(links.getDescription());
        folderModel.setParent(links.getParent());
        folderModel.setStatus("active");
        folderModel.setType("link");
        folderModel.setCreatedBy(userId);
        folderModel.setCreatedAt(new Date());

        FolderModel folderModel1 = folderRepository.save(folderModel);

        links.setId(folderModel1.getId());
        links.setCreatedAt(new Date());
        links.setCreatedBy(userId);

        Links links1 = linkRepository.save(links);


        Sources sources = sourcesRepository.getById(links1.getSourceId());

        // Create pipeline
        PipelineModel model = new PipelineModel();
        model.sourceDataset = links1.getSourceId();
        model.targetDataset = links1.getDatasetId();
        model.sourceBranch = links1.getBranch();
        model.targetBranch = links1.getBranch();
        model.repositoryId = null;
        model.repositoryBranch = null;
        model.scriptPath = null;
        model.buildId = null;
        model.status = "active";
//        model.type = sources.getSourceConfig().get("type");
        model.setCreatedBy(userId);
        model.setUpdatedBy(userId);
        pipelineRepository.saveAndFlush(model);


        // update config Status
        updateConfig(sources.getAgentId());

        return new ResponseEntity<>(links1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to update link.")
    @PostMapping("/update")
    public ResponseEntity<Object> update(Principal principal, @RequestBody Links links) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access denied", HttpStatus.OK);
        }
        if (!linkRepository.existsById(links.getId())) {
            return new ResponseEntity<>("Link with Id " + links.id + " does not exist", HttpStatus.NOT_FOUND);

        }

        if (links.getDatasetId() != null) {

            if (!datasetRepository.existsById(links.getDatasetId())) {
                return new ResponseEntity<>("Dataset with Id " + links.datasetId + " does not exist", HttpStatus.NOT_FOUND);

            }
        }

        if (links.getSourceId() != null) {
            if (!sourcesRepository.existsById(links.getSourceId())) {
                return new ResponseEntity<>("Source with Id " + links.sourceId + " does not exist", HttpStatus.NOT_FOUND);

            }
        }
        if (links.getParent() != null) {
            if (!folderRepository.existsById(links.getParent())) {
                return new ResponseEntity<>("Parent with Id " + links.parent + " does not exist", HttpStatus.NOT_FOUND);
            }
        }

        //UUID userId = userService.getUser(principal.getName()).id;

        Links linksExisting = linkRepository.getById(links.getId());

        links.setUpdatedAt(new Date());
        links.setUpdatedBy(userId);

        copyNonNullProperties(links, linksExisting);

        Links links1 = linkRepository.save(linksExisting);

        Sources sources = sourcesRepository.getById(links1.getSourceId());

        // update config Status
        updateConfig(sources.getAgentId());

        return new ResponseEntity<>(links1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to check if dataset already linked.")
    @GetMapping("/{datasetId}/{branch}/existsDatasetLink")
    public ResponseEntity<Object> existsDatasetLink(Principal principal,
                                                    @PathVariable("datasetId") UUID datasetId,
                                                    @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).id;

        HashMap<String, Object> response = new HashMap<>();

//        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
//            List<String> EmptyList = Collections.<String>emptyList();
////            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
//            response.put("status", false);
//            response.put("link", EmptyList);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }

        if (Objects.equals(datasetId.toString(), "undefined") || Objects.equals(branch, "undefined")) {
            return new ResponseEntity<>("Not valid datasetId or branch", HttpStatus.BAD_REQUEST);
        }

        if (linkRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            response.put("status", true);
        } else {
            response.put("status", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        response.put("link", linkRepository.findByDatasetIdAndBranch(datasetId, branch));
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @Operation(summary = "This endpoint can be used to Build the link.")
    @GetMapping("/Build/{Id}")
    public ResponseEntity<Object> Build(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!linkRepository.existsById(Id)) {
            return new ResponseEntity<>("Link with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }

        Links links = linkRepository.getById(Id);
        links.setBuild(new Date());
        linkRepository.save(links);

        Sources sources = sourcesRepository.findById(links.getSourceId()).get();

        // update config Status
        updateConfig(sources.getAgentId());

        return new ResponseEntity<>("Link has been added to build immediately.", HttpStatus.OK);
    }

    public void updateConfig(List<UUID> agentIdList) {

        for (UUID agentId : agentIdList) {
            IgniteConfig igniteConfig = new IgniteConfig();
            igniteConfig.setAgentId(agentId);
            igniteConfig.setVersion(UUID.randomUUID());
            igniteConfig.setUpdatedAt(new Date());
            igniteConfigRepository.save(igniteConfig);
        }
    }
}

