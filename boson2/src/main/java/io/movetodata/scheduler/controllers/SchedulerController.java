package io.movetodata.scheduler.controllers;

import io.movetodata.bob.library.repository.BuildSpecificationsRepository;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.UserPrincipal;
import io.movetodata.scheduler.library.models.Message;
import io.movetodata.scheduler.library.models.SchedulerJobInfo;
import io.movetodata.scheduler.library.repository.SchedulerRepository;
import io.movetodata.scheduler.library.services.SchedulerService;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Scheduler", description = "Schedule management service endpoints")
public class SchedulerController {

    private final UserService userService;
    private final FolderRepository folderRepository;
    private final SchedulerRepository schedulerRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final OkResponse response = new OkResponse();
    private final SchedulerService schedulerService;


    @Operation(summary = "Get  by datasetId and branch.")
    @GetMapping("/{datasetId}/{branch}")
    public Object getByDatasetIdAndBranch(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch)
            throws Exception {

        return schedulerService.getByDatasetIdAndBranch(datasetId, branch);

    }

    @Operation(summary = "Get jobId from datasetId and branch and trigger type")
    @GetMapping("/{datasetId}/{branch}/{triggerType}")
    ResponseEntity<Object> getJobIdFromDatasetIdBranchAndTrigger(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch,
            @PathVariable("triggerType") String triggerType)
            throws Exception {
        SchedulerJobInfo schedulerJobInfo =  schedulerRepository.findJobIdByDatasetIdAndBranchAndTriggerType(datasetId, branch, triggerType);

        if(schedulerJobInfo.getJobId() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            return new ResponseEntity<>(schedulerJobInfo.getJobId(), HttpStatus.OK);
        }

    }

    @Hidden
    @Operation(summary = "Get  meta data.")
    @GetMapping("/metaData")
    public Object metaData() throws SchedulerException {
        SchedulerMetaData metaData = SchedulerService.getMetaData();
        return metaData;
    }

    @GetMapping("/getAll")
    public Object getAllJobs() throws SchedulerException {
        List<SchedulerJobInfo> List = schedulerService.getAllJobList();
        return List;
    }

    @Hidden
    @Deprecated
    @Operation(summary = "it start the schedulejob.")
    @PostMapping(value = "/startJob")
    Message runJob(@RequestBody SchedulerJobInfo job) {
        log.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            schedulerService.startJobNow(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("runJob ex:", e);
        }
        return message;
    }

    @Hidden
    @Deprecated
    @Operation(summary = "It is pause schedulejob by Id.")
    @GetMapping(value = "/{id}/pause")
    public Object pause(@PathVariable("id") UUID id) throws Exception {
        log.info(" id = {}", id);
        Message message = Message.failure();
        try {
            schedulerService.pause(id);
            message = Message.success();
            message.setMsg("Schedule paused successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("pause ex:", e);
            throw new Exception("Not able to pause schedule : " + message);
        }
    }

    @Hidden
    @Deprecated
    @Operation(summary = "It is resume schedule job by Id.")
    @GetMapping(value = "/{id}/resume")
    public Object resume(@PathVariable("id") UUID id) throws Exception {
        log.info(" id = {}", id);
        Message message = Message.failure();
        try {
            schedulerService.resume(id);
            message = Message.success();
            message.setMsg("Schedule resumed successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("resume ex:", e);
            throw new Exception("Not able to resume schedule : " + message);
        }
    }

    @Operation(summary = "Delete schedulejob by datasetId and branch.")
    @DeleteMapping(value = "/{datasetId}/{branch}/delete")
    public Object deleteBydatasetIdandBranch(@PathVariable UUID datasetId, @PathVariable String branch) throws Exception {
        log.info(" datasetId = {}", " branch = {} ", datasetId, branch);
        Message message = Message.failure();
        try {
            schedulerService.deleteBydatasetIdandBranch(datasetId, branch);
            message = Message.success();
            message.setMsg("Schedule deleted successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("delete ex:", e);
            throw new Exception("Not able to delete schedule : " + message);
        }
    }

    @Operation(summary = "It is pause schedulejob by Id.")
    @GetMapping(value = "/{datasetId}/{branch}/pause")
    public Object pauseBydatasetIdandBranch(@PathVariable UUID datasetId, @PathVariable String branch) throws Exception {
        log.info(" datasetId = {}", " branch = {} ", datasetId, branch);
        Message message = Message.failure();
        try {
            schedulerService.pauseBydatasetIdandBranch(datasetId, branch);
            message = Message.success();
            message.setMsg("Schedule pause successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("delete ex:", e);
            throw new Exception("Not able to pause schedule : " + message);
        }
    }

    @Operation(summary = "It is resume schedulejob by Id.")
    @GetMapping(value = "/{datasetId}/{branch}/resume")
    public Object resumeBydatasetIdandBranch(@PathVariable UUID datasetId, @PathVariable String branch) throws Exception {
        log.info(" datasetId = {}", " branch = {} ", datasetId, branch);
        Message message = Message.failure();
        try {
            schedulerService.resumeBydatasetIdandBranch(datasetId, branch);
            message = Message.success();
            message.setMsg("Schedule resume successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("delete ex:", e);
            throw new Exception("Not able to resume schedule : " + message);
        }
    }

    //@GetMapping(value = "/{datasetId}/{branch}/update")
    //public Object updateBydatasetIdandBranch(@RequestBody SchedulerJobInfo schedulerJobInfo) throws Exception {
    //    log.info(" datasetId = {}", schedulerJobInfo);
    //    Message message = Message.failure();
    //    try {
    //        schedulerService.updateByDatasetIdAndBranch(schedulerJobInfo);
    //        message = Message.success();
    //        message.setMsg("Schedule update successfully");
    //        return message;
    //    } catch (Exception e) {
    //        message.setMsg(e.getMessage());
    //        log.error("delete ex:", e);
    //        throw new Exception("Not able to update schedule : " + message);
    //    }
    //}

    @Hidden
    @Deprecated
    @Operation(summary = "Delete schedulejob by Id.")
    @DeleteMapping(value = "/{id}/delete")
    public Object delete(@PathVariable("id") UUID id) throws Exception {
        log.info(" id = {}", id);
        Message message = Message.failure();
        try {
            schedulerService.delete(id);
            message = Message.success();
            message.setMsg("Schedule deleted successfully");
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("delete ex:", e);
            throw new Exception("Not able to delete schedule : " + message);
        }
    }

    @Hidden
    @Deprecated
    @Operation(summary = "Get schedule job by Id.")
    @GetMapping(value = "/{id}")
    public Object get(@PathVariable("id") UUID id) throws Exception {
        log.info(" id = {}", id);
        Message message = Message.failure();
        try {
            return schedulerService.get(id);

        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("get ex:", e);
            throw new Exception("Not able to get schedule : " + message);
        }
    }

    @Operation(summary = "It is add schedule job.")
    @PostMapping(value = "/add")
    public Object add(Principal principal, @RequestBody SchedulerJobInfo schedulerJobInfo) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;

        /*
         example payload
                 {
                    "jobName": "cronJob2",
                    "cronExpression": "* * * * * ?"
                  }
        */


        if (!buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(schedulerJobInfo.getDatasetId(), schedulerJobInfo.getBranch())) {
            throw new Exception("Dataset does not have build specification, This dataset has never been built via code editor.");
        }


        schedulerJobInfo.setUpdatedAt(new Date());
        schedulerJobInfo.setCreatedBy(userId);
        schedulerJobInfo.setUpdatedBy(userId);
        log.info("params, job = {}", schedulerJobInfo);
        Message message = Message.failure();

        // check of datasetId exists in kitab
        if (!folderRepository.existsById(schedulerJobInfo.getDatasetId())) {
            message.setMsg("Error: Not able to find dataset in the catalog");
            message.setValid(false);
            return message;
        }


        try {
            String newJobId = schedulerService.add(schedulerJobInfo);
            message = Message.success();
            message.setData(newJobId);
            message.setMsg("Schedule added successfully : " + newJobId);
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("add ex:", e);
            throw new Exception("Not able to add schedule : " + message);
        }

    }

    @Operation(summary = "It is update schedulejob.")
    @PostMapping(value = "/update")
    public Object update(Principal principal, @RequestBody SchedulerJobInfo schedulerJobInfo) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;

        /*
         example payload
                 {
                    "jobName": "cronJob2",
                    "cronExpression": "* * * * * ?"
                  }
        */

        schedulerJobInfo.setUpdatedAt(new Date());
        schedulerJobInfo.setCreatedBy(userId);
        schedulerJobInfo.setUpdatedBy(userId);
        log.info("params, job = {}", schedulerJobInfo);
        Message message = Message.failure();

        // check of datasetId exists in kitab
        if (!folderRepository.existsById(schedulerJobInfo.getDatasetId())) {
            message.setMsg("Error: Not able to find dataset in the catalog");
            message.setValid(false);
            return message;
        }

        try {
            String newJobId = schedulerService.update(schedulerJobInfo);
            message = Message.success();
            message.setData(newJobId);
            message.setMsg("Schedule update successfully : " + newJobId);
            return message;
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error("add ex:", e);
            throw new Exception("Not able to update schedule : " + message);
        }

    }


    // removed by Rakesh
//    @Scheduled(cron="*/5 * * * * ?")
//    public void demoServiceMethod()    {
//        System.out.println("Method executed at every 5 seconds. Current time is :: "+ new Date());
//
//    }


}
