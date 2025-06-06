package io.bosler.ignite.controllers;

import io.bosler.ignite.library.models.IgniteConfig;
import io.bosler.ignite.library.models.Links;
import io.bosler.ignite.library.models.Sources;
import io.bosler.ignite.library.repository.AgentRepository;
import io.bosler.ignite.library.repository.IgniteConfigRepository;
import io.bosler.ignite.library.repository.LinkRepository;
import io.bosler.ignite.library.repository.SourcesRepository;
import io.bosler.kitab.library.models.FolderModel;
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
@RequestMapping("/api/ignite/sources")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ignite", description = "Code Repository management service endpoints")
public class SourcesController {

    private final LinkRepository linkRepository;
    private final SourcesRepository sourcesRepository;
    private final AgentRepository agentRepository;
    private final IgniteConfigRepository igniteConfigRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final FolderRepository folderRepository;


    @Operation(summary = "Get all connectors.")
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
        List<Sources> sourcesList = sourcesRepository.findAll();
        return new ResponseEntity<>(sourcesList, HttpStatus.OK);
    }

    @Operation(summary = "It provides connectors by Id")
    @GetMapping("/GetById/{Id}")
    public ResponseEntity<Object> getById(@PathVariable("Id") UUID Id, HttpServletRequest httpRequest) {
        String user = httpRequest.getUserPrincipal().getName();
        UUID userId = userService.getUser(user).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!sourcesRepository.existsById(Id)) {
            return new ResponseEntity<>("connectors with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(sourcesRepository.findById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It delete connectors by Id")
    @DeleteMapping("/DeleteById/{Id}")
    public ResponseEntity<Object> deleteById(@PathVariable("Id") UUID Id, HttpServletRequest httpRequest) {
        String user = httpRequest.getUserPrincipal().getName();
        UUID userId = userService.getUser(user).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            //List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>("Access Denied", HttpStatus.OK);
        }
        if (!sourcesRepository.existsById(Id)) {
            return new ResponseEntity<>("connectors with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }


        // TODO : update config also on delete

        sourcesRepository.deleteById(Id);
        folderRepository.deleteById(Id);

        return new ResponseEntity<>(" Deleted Successfully. ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create sources.")
    @PostMapping("/create")
    public ResponseEntity<Object> newSource(Principal principal, @RequestBody Sources sources) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!folderRepository.existsById(sources.getParent())) {
            return new ResponseEntity<>("connectors with Id " + sources.getId() + " does not exist", HttpStatus.NOT_FOUND);

        }

        for (UUID agentId : sources.getAgentId()) {
            if (!agentRepository.existsById(agentId)) {
                return new ResponseEntity<>("Agent with Id " + sources.getAgentId() + " does not exist", HttpStatus.NOT_FOUND);

            }
        }

        // Create in kitab
        FolderModel folderModel = new FolderModel();
        folderModel.setName(sources.getName());
        folderModel.setDescription(sources.getDescription());
        folderModel.setParent(sources.getParent());
        folderModel.setStatus("active");
        folderModel.setType("source");
        folderModel.setCreatedBy(userId);
        folderModel.setCreatedAt(new Date());

        FolderModel folderModel1 = folderRepository.save(folderModel);

        sources.setId(folderModel.getId());
        sources.setCreatedAt(new Date());
        sources.setCreatedBy(userId);

        Sources sources1 = sourcesRepository.save(sources);


        // update config Status
        updateConfig(sources1.getAgentId());

        return new ResponseEntity<>(sources1, HttpStatus.OK);

    }

    @Operation(summary = "This can be used to update sources.")
    @PostMapping("/update")
    public ResponseEntity<Object> update(Principal principal, @RequestBody Sources sources) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }

        if (!sourcesRepository.existsById(sources.getId())) {
            return new ResponseEntity<>("source with Id " + sources.getId() + " does not exist", HttpStatus.NOT_FOUND);

        }

        if (sources.getParent() != null) {
            if (!folderRepository.existsById(sources.getParent())) {
                return new ResponseEntity<>("parent with Id " + sources.getParent() + " does not exist", HttpStatus.NOT_FOUND);

            }
        }
        if (sources.getParent() != null) {
            for (UUID agentId : sources.getAgentId()) {
                if (!agentRepository.existsById(agentId)) {
                    return new ResponseEntity<>("agent with Id " + sources.getAgentId() + " does not exist", HttpStatus.NOT_FOUND);

                }
            }
        }

        sources.setUpdatedAt(new Date());
        sources.setUpdatedBy(userId);

        Sources sourcesExisting = sourcesRepository.getById(sources.getId());
        copyNonNullProperties(sources, sourcesExisting);


        Sources sources1 = sourcesRepository.save(sourcesExisting);

        // update config Status
        updateConfig(sources1.getAgentId());

        return new ResponseEntity<>(sources1, HttpStatus.OK);

    }

    @Operation(summary = "This can be used to get source links.")
    @GetMapping("/{sourceId}/links")
    public ResponseEntity<Object> agentLinks(Principal principal, @PathVariable("sourceId") UUID sourceId) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!sourcesRepository.existsById(sourceId)) {
            return new ResponseEntity<>("source with Id " + sourceId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Sources sources = sourcesRepository.getById(sourceId);

        List<Links> linksList = new ArrayList<>(linkRepository.findBySourceId(sources.getId()));

        return new ResponseEntity<>(linksList, HttpStatus.OK);
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

