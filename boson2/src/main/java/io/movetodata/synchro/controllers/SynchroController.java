package io.movetodata.synchro.controllers;

import io.movetodata.bob.library.models.SocketMessage;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.movetodata.synchro.library.models.PostgresSyncProperties;
import io.movetodata.synchro.library.models.PostgresSyncSpecification;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import io.movetodata.synchro.library.services.SynchroService;
import io.movetodata.zoro.library.services.ZoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;

import static io.movetodata.sharedUtils.Utils.isPostgresTableNameValid;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/synchro")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Synchro", description = "This is to synchronise spark dataframes to databases.")
public class SynchroController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthzService authzService;

    private final FolderRepository folderRepository;
    private final PostgresSyncRepository postgresSyncRepository;
    private final ZoroService zoroService;
    private final SynchroService synchroService;
    private final OkResponse response = new OkResponse();

    @Autowired
    SimpMessagingTemplate template;


    @Operation(summary = "Create new postgres synchronisation.")
    @PostMapping("/PostgresSync")
    public ResponseEntity<Object> status(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @RequestBody PostgresSyncProperties postgresSyncProperties
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!isPostgresTableNameValid(postgresSyncProperties.getTableName())) {
            return new ResponseEntity<>("Postgres table name can not contain spaces or start with numbers.", HttpStatus.BAD_REQUEST);
        }

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/postgresSync/" + postgresSyncProperties.getDatasetId() + "/" + postgresSyncProperties.getBranch(), textMessage);

        if (!folderRepository.existsById(postgresSyncProperties.getDatasetId())) {
            return new ResponseEntity<>("Dataset with Id " + postgresSyncProperties.getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);

        }

        synchroService.createSync(postgresSyncProperties, userId);

        // TODO : Permissions overwrite also.

        PostgresSyncSpecification postgresSyncSpecificationSaved = postgresSyncRepository.findByDatasetIdAndBranch(postgresSyncProperties.getDatasetId(), postgresSyncProperties.getBranch());

        textMessage.setMessage("success");
        template.convertAndSend("/topic/postgresSync/" + postgresSyncProperties.getDatasetId() + "/" + postgresSyncProperties.getBranch(), textMessage);

        textMessage.setMessage("active");
        template.convertAndSend("/topic/postgresSync/" + postgresSyncProperties.getDatasetId() + "/" + postgresSyncProperties.getBranch(), textMessage);

        postgresSyncSpecificationSaved.setStartedAt(new Date());
        postgresSyncSpecificationSaved.setSyncStatus("active");

        postgresSyncRepository.save(postgresSyncSpecificationSaved);

        synchroService.performSync(userId, postgresSyncProperties.getDatasetId(), postgresSyncProperties.getBranch());


        return new ResponseEntity<>("Successful new Postgres Synchronisation created.", HttpStatus.OK);
    }

    @Operation(summary = "Perform postgres synchronisation on existing sync.")
    @GetMapping("/PostgresSync/{datasetId}/{branch}/perform")
    public ResponseEntity<Object> performPostgresSync(Principal principal,
                                                      @PathVariable("datasetId") UUID datasetId,
                                                      @PathVariable("branch") String branch

    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " does not exist", HttpStatus.BAD_REQUEST);
        }

        if (!postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " and branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
        }

        PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch);

        postgresSyncSpecification.setStartedAt(new Date());
        postgresSyncSpecification.setSyncStatus("active");

        postgresSyncRepository.save(postgresSyncSpecification);


        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/postgresSync/" + datasetId + "/" + branch, textMessage);

        synchroService.performSync(userId, datasetId, branch);

        return new ResponseEntity<>("Successful Postgres Synchronisation.", HttpStatus.OK);
    }

    @Operation(summary = "Perform postgres synchronisation on existing sync via postgres.")
    @GetMapping("/PostgresSync/{datasetId}/{branch}/performed")
    public ResponseEntity<Object> performedPostgresSync(Principal principal,
                                                        @PathVariable("datasetId") UUID datasetId,
                                                        @PathVariable("branch") String branch

    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch);

        postgresSyncSpecification.setSyncedBy(userId);

        postgresSyncRepository.save(postgresSyncSpecification);

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("success");

        template.convertAndSend("/topic/postgresSync/" + datasetId + "/" + branch, textMessage);

        postgresSyncSpecification.setFinishedAt(new Date());
        postgresSyncSpecification.setSyncStatus("completed");

        postgresSyncRepository.save(postgresSyncSpecification);

        return new ResponseEntity<>("Successful Postgres Synchronisation.", HttpStatus.OK);
    }

    @Operation(summary = "Get Postgres Syncs based on datasetId and branch.")
    @GetMapping("/PostgresSync/{datasetId}/{branch}")
    public ResponseEntity<Object> PostgresSyncDatasetIdBranch(Principal principal,
                                                              HttpServletRequest httpRequest,
                                                              HttpServletResponse servletResponse,
                                                              @PathVariable("datasetId") UUID datasetId,
                                                              @PathVariable("branch") String branch


    ) throws Exception {

        if (!folderRepository.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (!postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            return new ResponseEntity<>("Postgres sync does not exists for the datasetId " + datasetId + " and branch." + branch, HttpStatus.NOT_FOUND);

        }

        return new ResponseEntity<>(postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch), HttpStatus.OK);

    }

    @Operation(summary = "Get Postgres Syncs based on datasetId and branch.")
    @GetMapping("/PostgresSync/existsTable/{tableName}")
    public ResponseEntity<Object> existsTable(Principal principal,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse servletResponse,
                                              @PathVariable("tableName") String tableName
    ) throws Exception {

        return new ResponseEntity<>(postgresSyncRepository.existsByTableName(tableName), HttpStatus.OK);
    }

    @Operation(summary = "Deletes Postgres Syncs based on datasetId and branch.")
    @DeleteMapping("/PostgresSync/{datasetId}/{branch}")
    public ResponseEntity<Object> PostgresSyncDatasetIdBranchDelete(Principal principal,
                                                                    HttpServletRequest httpRequest,
                                                                    HttpServletResponse servletResponse,
                                                                    @PathVariable("datasetId") UUID datasetId,
                                                                    @PathVariable("branch") String branch

    ) throws Exception {

        if (!folderRepository.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (!postgresSyncRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
            return new ResponseEntity<>("Postgres sync does not exists for the datasetId " + datasetId + " and branch." + branch, HttpStatus.NOT_FOUND);
        }

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");
        template.convertAndSend("/topic/postgresSync/" + datasetId + "/" + branch, textMessage);

        // TODO : delete tables also cascade properly.

        PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(datasetId, branch);
        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + System.getenv("DB_HOST") + ":5432/kepler",
                            System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));

            System.out.println("Opened database successfully");

            stmt = connection.createStatement();
            String sql = "DROP TABLE IF EXISTS public." + postgresSyncSpecification.getTableName();
            stmt.executeUpdate(sql);
            stmt.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        // TODO : end here

        postgresSyncRepository.deleteByDatasetIdAndBranch(datasetId, branch);

        // Sending to socket
        textMessage.setMessage("success");
        template.convertAndSend("/topic/postgresSync/" + datasetId + "/" + branch, textMessage);

        return new ResponseEntity<>("Postgres Sync deleted successfully.", HttpStatus.OK);

    }
}
