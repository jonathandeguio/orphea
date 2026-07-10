package io.movetodata.connect.controllers;

import io.movetodata.connect.library.models.AgentStats;
import io.movetodata.connect.library.models.Agents;
import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.models.Source;
import io.movetodata.connect.library.requests.AgentRequestDTO;
import io.movetodata.connect.library.responses.AgentResponse;
import io.movetodata.connect.library.services.AgentService;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@EnableWebMvc
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/connect/agent")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Connect", description = "Code Repository management service endpoints")
public class AgentController {
    private final AgentService agentService;

    @Operation(summary = "Get all agents.")
    @GetMapping("/Getall")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getAll(@AuthenticationPrincipal AuthUser authUser) {
        UUID userId = authUser.getId();
        return new ResponseEntity<>(agentService.getAllAgents(userId), HttpStatus.OK);
    }

    @Operation(summary = "It provides agent by Id")
    @GetMapping("/GetById/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(agentService.findById(id), HttpStatus.OK);
    }

    @Operation(summary = "It delete agents by Id")
    @DeleteMapping("/DeleteById/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> deleteById(@PathVariable("id") UUID id) {
        agentService.deleteById(id);

        return new ResponseEntity<>(" Delete successfully ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create agents.")
    @PostMapping("/create")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> newAgent(@AuthenticationPrincipal AuthUser authUser, @RequestBody AgentRequestDTO agents) {
        UUID userId = authUser.getId();
        AgentResponse response = agentService.createNewAgent(agents, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "This can be used to create agents.")
    @PostMapping("/update")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> update(@AuthenticationPrincipal AuthUser authUser, @RequestBody Agents agent) {
        UUID userId = authUser.getId();
        Agents agents1 = agentService.updateAgent(agent, userId);
        return new ResponseEntity<>(agents1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get agents stats.")
    @GetMapping("/{agentId}/stats")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> agentStats(@PathVariable("agentId") UUID agentId) {
        AgentStats agentStats = agentService.getAgentStats(agentId);

        return new ResponseEntity<>(agentStats, HttpStatus.OK);
    }


    @Operation(summary = "This can be used to get agents sources.")
    @GetMapping("/{agentId}/sources")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> agentSources(@PathVariable("agentId") UUID agentId) {
        List<Source> sourceList = agentService.getSources(agentId);

        return new ResponseEntity<>(sourceList, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get agents links.")
    @GetMapping("/{agentId}/links")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> agentLinks(@PathVariable("agentId") UUID agentId) {
        List<Link> linkList = agentService.getLinks(agentId);

        return new ResponseEntity<>(linkList, HttpStatus.OK);
    }
    @Operation(summary = "This can be used to regenerate agents OTC.")
    @GetMapping("/{agentId}/regenerate")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> regenerateAgent(@AuthenticationPrincipal AuthUser authUser, @PathVariable("agentId") UUID agentId) {
        UUID userId = authUser.getId();
        AgentResponse response = agentService.getAgentResponse(userId, agentId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to install agent.")
    @GetMapping("/install/{code}")
    public ResponseEntity<Object> installAgent(HttpServletRequest request, @PathVariable("code") String code) throws IOException {
        // Usage : bash <(curl -s http://localhost:8080/api/ignite/agent/install/dsadada)
        Resource resource = agentService.getResource(request, code);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @Operation(summary = "This can be used to download code.")
    @PostMapping("/download/{code}")
    public ResponseEntity<Object> downloadAgent(HttpServletRequest request, @PathVariable("code") String code, @RequestBody HashMap<String, String> client) throws GitAPIException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        Resource resource = agentService.getObjectResponseEntity(request, code, client);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }
}
