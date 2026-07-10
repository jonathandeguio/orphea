package io.movetodata.kitab.controllers;


import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.TokenProvider;
import io.movetodata.passport.util.AuthUtils;
import io.movetodata.platform.library.models.Versions;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.CommonService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Tag(name = "Index", description = "This is a only for health checks stuff, never delete this. All hell will break loose.")
public class indexController {

    private final UserService userService;
    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private ResourceService resourceService;

    @Hidden
    @RequestMapping("/")
    public String indexRedirect() {
        return "<html><h2>OK</h2></html>";
    }

    @Hidden
    @PostMapping("/api/ping")
    public ResponseEntity<Object> ping(Principal principal, HttpServletRequest request, @RequestBody HashMap<String, String> pingRequest) {
        UUID userId = userService.getUser(principal.getName()).getId();
        String jwt = AuthUtils.getAccessTokenFromRequest(request);

        String path = pingRequest.get("path");
        String regex = "\\b[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            String firstUUID = matcher.group();

            // Sending to socket to tell that someone opened chart
            SocketMessage textMessage = new SocketMessage();
            textMessage.setMessage(userId.toString());

            template.convertAndSend("/topic/" + firstUUID, textMessage);
        }

        Map<String, Object> response = new HashMap<>();

        if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {

            Versions versions = new Versions();
            Map<String, String> nonNullVersions = versions.getNonNullVersions();

            response.put("versions", nonNullVersions);

            if (System.getenv("LAST_UPDATED_ON") != null) {
                response.put("lastUpdatedOn", System.getenv("LAST_UPDATED_ON"));
            }

            Date ttl = AuthUtils.getTTLFromToken(jwt);
            response.put("message", "pong");
            response.put("ttl", (ttl.getTime() - (new Date()).getTime()) / 1000);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

    }

    @Hidden
    @Operation(summary = "Get files to a folder")
    @GetMapping(value = "/api/file/{fileId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Object> getFile(
            @PathVariable("fileId") UUID fileId
    ) throws Exception {

//        UUID userId = userService.getUser(principal.getName()).getId();

//        if (!authzService.isViewer(userId, fileId)) {
//            return new ResponseEntity<>("Access Denied to " + fileId, HttpStatus.FORBIDDEN);
//        }

        ResourceModel folderModel = resourceService.getResourceModel(fileId);

        InputStream inputStream = null;
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Object> responseEntity = null;

        String backingFS = System.getenv("BACKING_FS");

        switch (backingFS) {
            case "s3":
                responseEntity = ResponseEntity.ok()
                        .headers(headers)
                        .body("Not working yet.");
                break;
            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "movetodata/file/" + fileId + "/master/" + folderModel.getId());
                Blob blob = storage.get(blobId);
                inputStream = new ByteArrayInputStream(blob.getContent());
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", folderModel.getName());
                responseEntity = ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
                break;
            case "hdfs":
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                FileSystem fs = FileSystem.get(conf);
                FSDataInputStream fileOpen = fs.open(new Path("/movetodata/file/" + fileId + "/master/" + folderModel.getId()));
                inputStream = fileOpen;
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", folderModel.getName());
                responseEntity = ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
                break;
            case "localfs":
                String branch = platformConfigService.getPlatformConfig().getDefaultBranch();
                FileSystemResource resource = new FileSystemResource(getResourcePath("file", fileId, branch) + "/" + fileId);
                inputStream = resource.getInputStream();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDispositionFormData("attachment", folderModel.getName());
                responseEntity = ResponseEntity.ok()
//                        .headers(headers)
                        .contentType(MediaType.IMAGE_PNG)
                        .body(new InputStreamResource(inputStream));
                break;
            default:
                throw new Exception("Unknown backing FS ");
        }

        return responseEntity;
    }

//    @RequestMapping("/error")
//    public String errorMessage() {
//        return "Something has seriously gone wrong";
//    }

    // Not working yet
    @Hidden
    @RequestMapping("/swagger")
    public String swaggerRedirect() {
        return "redirect:/swagger-ui.html";
    }

//    @MessageMapping("/sendMessage")
//    public void receiveMessage(@Payload SocketMessage textMessage) {
//        // receive message from client
//
//        
//    }
//
//
//    @SendTo("/topic/message")
//    public SocketMessage broadcastMessage(@Payload SocketMessage textMessage) {
//        return textMessage;
//    }

}
