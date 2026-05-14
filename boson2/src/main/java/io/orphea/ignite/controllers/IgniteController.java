package io.orphea.ignite.controllers;

import io.orphea.ignite.library.models.*;
import io.orphea.ignite.library.repository.*;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/ignite")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ignite", description = "Code Repository management service endpoints")
public class IgniteController {

    private final SourcesRepository sourcesRepository;
    private final IgniteConfigRepository igniteConfigRepository;
    private final AgentRepository agentRepository;
    private final AgentStatsRepository agentStatsRepository;
    private final UserService userService;
    private final AuthzService authzService;
    private final LinkRepository linkRepository;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "Get ignite config status.")
    @GetMapping("/config/{agentId}/status")
    public ResponseEntity<Object> getConfigStatus(Principal principal,
                                                  @PathVariable("agentId") UUID agentId,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse servletResponse

    ) throws Exception {

        // first check if it is valid Agent
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        // Update lastStatus
        Agents agents = agentRepository.getById(agentId);
        agents.setLastStatus(new Date());
        agentRepository.save(agents);

        IgniteConfig igniteConfigFromDB = igniteConfigRepository.findFirstByAgentIdOrderByUpdatedAtDesc(agentId);

        if (igniteConfigFromDB == null) {

            // if not config found then create an initial one.

            IgniteConfig igniteConfig = new IgniteConfig();
            igniteConfig.setAgentId(agentId);
            igniteConfig.setVersion(UUID.randomUUID());

            return new ResponseEntity<>(igniteConfigRepository.save(igniteConfig), HttpStatus.OK);
        }

        return new ResponseEntity<>(igniteConfigFromDB, HttpStatus.OK);
    }

    @Operation(summary = "Pull ignite config.")
    @GetMapping("/config/{agentId}/pull")
    public ResponseEntity<Object> configPull(Principal principal,
                                             @PathVariable("agentId") UUID agentId,
                                             HttpServletRequest httpRequest,
                                             HttpServletResponse servletResponse

    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        // first check if it is valid Agent
        if (!agentRepository.existsById(agentId)) {
            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        HashMap<String, Object> newConfig = new HashMap<>();

        List<Sources> sourcesList = sourcesRepository.findAllByAgentId(agentId);
        System.out.println(sourcesList);

        newConfig.put("sources", sourcesList);

        List<Object> linksList = new ArrayList<>();

        for (Sources sources : sourcesList) {
            linksList.addAll(linkRepository.findBySourceId(sources.getId()));
        }

        newConfig.put("links", linksList);

        List<Object> buildNowList = new ArrayList<>();
        for (Sources sources : sourcesList) {

            for (Links links : linkRepository.findBySourceId(sources.getId())) {
                if (links.getBuild() == null) {
                    continue;
                }

                Date timeNow = new Date();
                final long millis = timeNow.getTime() - links.getBuild().getTime();  // TODO : need to think of a better logic here... its not the best using time

                if (millis < 45000) { // 45 seconds or less otherwise don't put in the buildNow
                    buildNowList.add(links);
                }
            }

        }

        newConfig.put("buildNow", buildNowList);

        return new ResponseEntity<>(newConfig, HttpStatus.OK);
    }


    @Operation(summary = "Update ignite config status.")
    @PostMapping("/config/{agentId}/agentStats")
    public ResponseEntity<Object> agentStatus(Principal principal,
                                              @PathVariable("agentId") UUID agentId,
                                              @RequestBody AgentStats agentStats,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse servletResponse

    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.checkSystemPermissions(userId, new UUID(3, 3), "ignite-administrators")) {
            List<String> EmptyList = Collections.<String>emptyList();
            return new ResponseEntity<>(EmptyList, HttpStatus.OK);
        }
        // first check if it is valid Agnet
        if (!agentRepository.existsById(agentId)) {

            return new ResponseEntity<>("agent with Id " + agentId + " does not exist", HttpStatus.NOT_FOUND);

        }

        agentStats.setCreatedAt(new Date());

        AgentStats agentStats1 = agentStatsRepository.save(agentStats);

        return new ResponseEntity<>(agentStats1, HttpStatus.OK);
    }

}
