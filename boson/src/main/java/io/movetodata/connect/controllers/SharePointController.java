package io.movetodata.connect.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.movetodata.connect.library.models.SharePointSourceConfig;
import io.movetodata.connect.library.models.Source;
import io.movetodata.connect.library.services.SharePointConnectorService;
import io.movetodata.connect.library.services.SharepointConfigService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.passport.library.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.ws.rs.PathParam;
import java.io.IOException;
import java.util.UUID;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/connect/source/{source_id}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Connect", description = "SharePoint data endpoints")
public class SharePointController {
    @Autowired
    private SharepointConfigService sharepointConfigService;

    @Autowired
    private SourceService sourceService;
//    @Operation(summary = "Get all connectors.")
//    @GetMapping("/token/{source_id}")
//    public ResponseEntity<Object> getAccessToken(@PathVariable("source_id") String sourceId) throws IOException {
//        // get these details from source
//        String TENANT_ID = "6aa76d38-1d16-4a92-9e60-b13be28d5f2a";  // Replace with your Azure AD Tenant ID
//        String CLIENT_ID = "48c46a4c-4613-4ebc-8841-42f4846ae2aa";  // Replace with your Client ID (App ID)
//        String CLIENT_SECRET = "Q7c8Q~_fz6CroMUjU3gaOfs3eKQKmu~5VxZprbBW";  // Replace with your Client Secret
//
//        String accessToken = SharePointConnectorService.getAccessToken(CLIENT_ID, CLIENT_SECRET, TENANT_ID);
//
//        sharePointConnectorService.getSiteIdAndDriveId(sourceId, accessToken, "https://movetodataio.sharepoint.com/sites/MOVETODATASmartDP/Shared%20Documents/Forms/AllItems.aspx");
//
//        return new ResponseEntity<>(accessToken, HttpStatus.OK);
//    }


    @Operation(summary = "Get all connectors.")
    @GetMapping("/children/{folder_id}")
    @PreAuthorize(Auth.CONNECT_ADMIN)
    public ResponseEntity<Object> getAll(@PathVariable("source_id") UUID sourceId, @PathVariable("folder_id") String folderId) throws IOException {
        Source source = sourceService.findById(sourceId);
        SharePointSourceConfig sharePointSourceConfig = sharepointConfigService.findById(source.getSourceConfig());

        JsonNode tree = SharePointConnectorService.fetchDriveFolderChildren(sharePointSourceConfig.getToken(), sharePointSourceConfig.getSiteId(), sharePointSourceConfig.getDriveId(), folderId);

        return new ResponseEntity<>(tree, HttpStatus.OK);
    }

}
