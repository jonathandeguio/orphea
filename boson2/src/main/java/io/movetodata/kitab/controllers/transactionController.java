package io.movetodata.kitab.controllers;

import io.movetodata.bob.library.models.SocketMessage;
import io.movetodata.kitab.library.models.TransactionModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.TransactionRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.Response.BadRequestException;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/transaction")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a dataset transactions management service.")
public class transactionController {

    private final UserService userService;
    private final AuthzService authzService;

    private final DatasetRepository datasetRepository;
    private final TransactionRepository transactionRepository;

    private final OkResponse response = new OkResponse();

    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Start transaction")
    @GetMapping("/{datasetId}/{branch}/start")
    public ResponseEntity<Object> startTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @RequestParam(name = "buildId", required = false) String buildId) {
        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        List<TransactionModel> transactionModels = transactionRepository
                .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
        for (TransactionModel model : transactionModels) {
            if (model.getStatus().equals("active")) {
                return new ResponseEntity<>("There is a transaction already active", HttpStatus.ALREADY_REPORTED);
            }
        }

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setBranch(branch);
        transactionModel.setDatasetId(datasetId);
        transactionModel.setTrigger("ignite");  // TODO : this needs correcting, it can't be hardcoded
        transactionModel.setStatus("active");
        transactionModel.setCreatedBy(userId);
        transactionModel.setCreatedAt(new Date());
        transactionModel.setBuildId(buildId);

        TransactionModel transactionModel1 = transactionRepository.save(transactionModel);

        HashMap<String, Object> response = new HashMap<>();
        response.put("message", "Transaction started");
        response.put("transactionId", transactionModel1.getId());


        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");

        template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, textMessage);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "End transaction")
    @GetMapping("/{datasetId}/{branch}/end")
    public ResponseEntity<Object> endTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) {


        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        List<TransactionModel> transactionModels = transactionRepository
                .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
        for (TransactionModel model : transactionModels) {
            if (model.getStatus().equals("active")) {

                model.setStatus("completed");
                model.setFinishedBy(userId);
                model.setFinishedAt(new Date());

                TransactionModel transactionModel1 = transactionRepository.save(model);

                // Sending to socket
                SocketMessage socketMessage = new SocketMessage();
                socketMessage.setMessage("completed");

                template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, socketMessage);

                return new ResponseEntity<>("Transaction ended", HttpStatus.OK);
            }
        }


        return new ResponseEntity<>("No active transactions found", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Abort transaction")
    @GetMapping("/{datasetId}/{branch}/abort")
    public ResponseEntity<Object> abortTransaction(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) {

        if (!datasetRepository.existsById(datasetId)) { // check if the dataset exists in catalog
            return new ResponseEntity<>("No dataset found in catalog for " + datasetId, HttpStatus.NOT_FOUND);
        }

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isEditor(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        List<TransactionModel> transactionModels = transactionRepository
                .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
        for (TransactionModel model : transactionModels) {
            if (model.getStatus().equals("active")) {

                model.setStatus("aborted");
                model.setFinishedBy(userId);
                model.setFinishedAt(new Date());

                TransactionModel transactionModel1 = transactionRepository.save(model);

                // Sending to socket
                SocketMessage socketMessage = new SocketMessage();
                socketMessage.setMessage("aborted");

                template.convertAndSend("/topic/transactions/" + datasetId + "/" + branch, socketMessage);

                return new ResponseEntity<>("Transaction aborted", HttpStatus.OK);
            }
        }

        return new ResponseEntity<>("No active transactions found", HttpStatus.NOT_FOUND);
    }
}
