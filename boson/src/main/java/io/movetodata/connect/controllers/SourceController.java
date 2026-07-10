package io.movetodata.connect.controllers;

import io.movetodata.connect.library.DTOs.LinkPreviewDTO;
import io.movetodata.connect.library.DTOs.SourceSQLTableExplanation;
import io.movetodata.connect.library.models.*;
import io.movetodata.connect.library.repository.LinkRepository;
import io.movetodata.connect.library.requests.SourceRequestDTO;
import io.movetodata.connect.library.services.LinkService;
import io.movetodata.connect.library.services.SharePointConnectorService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/connect/source")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Connect", description = "Code Repository management service endpoints")
public class SourceController {
    private final LinkRepository linkRepository;
    private final SourceService sourceService;
    private final LinkService linkService;

    @Operation(summary = "Get all connectors.")
    @GetMapping("/Getall")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getAll(@AuthenticationPrincipal AuthUser authUser) {
        UUID userId = authUser.getId();
        List<Source> sourcesList = sourceService.getSources(userId);
        sourcesList = sourcesList.stream()
                .filter(source -> Objects.nonNull(source.getSourceConfig()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(sourcesList, HttpStatus.OK);
    }

    @Operation(summary = "It provides connectors by Id")
    @GetMapping("/GetById/{id}")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getById(@PathVariable("id") UUID id) {
        Source source = sourceService.getSource(id);
        return new ResponseEntity<>(source, HttpStatus.OK);
    }

    @Operation(summary = "It delete connectors by Id")
    @DeleteMapping("/DeleteById/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> deleteById(@PathVariable("id") UUID id) {
        sourceService.deleteSourceById(id);

        return new ResponseEntity<>(" Deleted Successfully. ", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create sources.")
    @PostMapping("/create")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> newSource(@AuthenticationPrincipal AuthUser authUser, @RequestBody SourceRequestDTO sourceRequest) throws IOException {
        UUID userId = authUser.getId();
        Source source1 = sourceService.createNewSource(sourceRequest, userId);

        return new ResponseEntity<>(source1, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to update sources.")
    @PostMapping("/update")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> update(@AuthenticationPrincipal AuthUser authUser, @RequestBody SourceRequestDTO source) throws IOException {
        UUID userId = authUser.getId();

        Source source1 = sourceService.updateSource(source, userId);
        return new ResponseEntity<>(source1, HttpStatus.OK);

    }

    @Operation(summary = "This can be used to get source links.")
    @GetMapping("/{sourceId}/links")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> agentLinks(@PathVariable("sourceId") UUID sourceId) {
        Source source = sourceService
                .findById(sourceId);

        List<Link> linkList = new ArrayList<>(linkRepository.findBySourceId(source.getId()));

        return new ResponseEntity<>(linkList, HttpStatus.OK);
    }


    @Operation(summary = "This can be used to update sources.")
    @PostMapping("/checkFolderPath")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> checkFolderPath(@RequestBody TestConnection testConnection) {
        Map<String, Object> message = sourceService.checkFolderPathExists(testConnection);
        return new ResponseEntity<>(message, HttpStatus.OK);

    }

    @Operation(summary = "This can be used to update sources.")
    @PostMapping("/testConnection")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> testConnection(@RequestBody DatabaseSourceConfig testConnection) throws Exception {
        Map<String, String> message = sourceService.testJdbcConnection(testConnection);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to update sources.")
    @PostMapping("/testSharepointConnection")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> testSharepointConnection(@RequestBody SharePointSourceConfig testConnection) throws Exception {
        Map<String, Object> message = SharePointConnectorService.testConnection(testConnection);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get source databaseSourceConfig.")
    @GetMapping("/{sourceId}/databaseSourceConfig")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getDatabaseSourceConfig(@PathVariable("sourceId") UUID sourceId) {
        DatabaseSourceConfig databaseSourceConfig = sourceService
                .getSourceDatabaseSourceConfig(sourceId);
        return new ResponseEntity<>(databaseSourceConfig, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get SQL table details.")
    @GetMapping("/{sourceId}/{tableName}/explain")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> explainSQLTable(@PathVariable("sourceId") UUID sourceId, @PathVariable("tableName") String tableName) throws Exception {
        SourceSQLTableExplanation explanation = sourceService.explainSQLTable(sourceId, tableName);
        return new ResponseEntity<>(explanation, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get SQL table details.")
    @GetMapping("/{sourceId}/{tableName}/{tableSchema}/definition")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getSQLTableDefinition(@PathVariable("sourceId") UUID sourceId, @PathVariable("tableName") String tableName) throws Exception {
        SourceSQLTableExplanation explanation = sourceService.explainSQLTable(sourceId, tableName);
        return new ResponseEntity<>(explanation, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get SQL table details.")
    @GetMapping("/{sourceId}/{tableName}/columns")
//    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getSQLTableCols(@PathVariable("sourceId") UUID sourceId, @PathVariable("tableName") String tableName) throws Exception {
        List<List<String>> columns = sourceService.getSqlColumns(sourceId, tableName);
        return new ResponseEntity<>(columns, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get source content.")
    @GetMapping("/{sourceId}/getSourceContentMetaData")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getSourceContentMetaData(@AuthenticationPrincipal AuthUser authUser, @PathVariable("sourceId") UUID sourceId) throws Exception {
        UUID userId = authUser.getId();
        return new ResponseEntity<>(sourceService.getSourceContentMetaData(sourceId, userId), HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to Preview a source.")
    @PostMapping("/preview/{id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> preview(@AuthenticationPrincipal AuthUser authUser, @PathVariable("id") UUID id, @RequestBody LinkPreviewDTO linkPreviewDTO) {
        UUID userId = authUser.getId();
        Map<String, Object> message = linkService.getPreview(id, linkPreviewDTO.getQuery(), linkPreviewDTO.getRequests(), linkPreviewDTO.getResponseParam(), linkPreviewDTO.getCsvPreprocessing(), true, userId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}

