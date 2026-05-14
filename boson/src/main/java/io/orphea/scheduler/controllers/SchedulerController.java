package io.orphea.scheduler.controllers;

import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.passport.library.Auth;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.scheduler.enums.JobStatus;
import io.orphea.scheduler.library.models.Message;
import io.orphea.scheduler.library.models.ScheduleFilters;
import io.orphea.scheduler.library.models.SchedulerJobInfo;
import io.orphea.scheduler.library.repository.SchedulerRepository;
import io.orphea.scheduler.library.services.SchedulerService;
import io.orphea.sharedutils.DTO.PageToPageDTOMapper;
import io.orphea.sharedutils.Exceptions.BadRequestException;
import io.orphea.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SchedulerRepository schedulerRepository;
    private final SchedulerService schedulerService;
    private final AuthzService authzService;
    private final PageToPageDTOMapper pageToPageDTOMapper;
    private final ResourceService resourceService;

    /*
        Only 1 job for each resource. Exception being subscription on dashboard.
        For that different api present on subscription controller
     */
    @Operation(summary = "Get by datasetId and branch.")
    @GetMapping("/getJob/{resourceId}/{branch}/{resourceType}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getByDatasetIdAndBranch(
            Principal principal,
            @PathVariable("resourceId") @Param("id") UUID resourceId,
            @PathVariable("branch") String branch,
            @PathVariable("resourceType") ResourceType resourceType) {
        SchedulerJobInfo job = schedulerService.getByResourceIdAndBranch(resourceId, branch, resourceType);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAllJobs(Principal principal, PageSettings pageSettings, @RequestBody ScheduleFilters scheduleFilters) {
        log.info("Request for schedules page received with data : " + pageSettings);
        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                schedulerService.getSchedulesPage(pageSettings, scheduleFilters)));
    }

    @Operation(summary = "Take action on scheduled job via Id.")
    @PostMapping(value = "/{jobId}/{action}")
    public ResponseEntity<Object> action(Principal principal, @PathVariable("jobId") UUID jobId, @PathVariable("action") JobStatus action) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!schedulerRepository.existsById(jobId)) {
            return new ResponseEntity<>("No such Job Exist", HttpStatus.BAD_REQUEST);
        }

        UUID resourceId = schedulerRepository.getReferenceById(jobId).getResourceId();

        if (!authzService.isEditor(userId, resourceId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        switch (action) {
            case PAUSED:
                schedulerService.pause(jobId);
                break;
            case RUNNING:
                schedulerService.resume(jobId);
                break;
            case DELETED:
                schedulerService.delete(jobId);
                break;
            default:
                throw new UnsupportedOperationException("No such action!");
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Just for developer use, remove later
    @Operation(summary = "Delete all schedules by resource id")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> delete(Principal principal, @PathVariable("id") UUID resourceId) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();
        if (!authzService.isPlatformAdmin(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        log.info(" Deleting resource id = {}", resourceId);
        Message message = Message.failure();

        try {
            List<SchedulerJobInfo> schedules = schedulerRepository.findAllByResourceId(resourceId);
            for (SchedulerJobInfo schedule : schedules) {
                schedulerService.delete(schedule.getJobId());
            }

            message = Message.success();
            message.setMsg("Schedule successfully");
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            log.error(" ex:", e);
            throw new Exception("Not able to delete schedules : " + message);
        }
    }

    @Operation(summary = "Get schedule job by Id.")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getSchedule(Principal principal, @PathVariable("id") UUID id) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();


        if (!schedulerRepository.existsById(id)) {
            return new ResponseEntity<>("No such Job Exist", HttpStatus.NOT_FOUND);
        }

        SchedulerJobInfo job = schedulerRepository.findById(id).orElseThrow();
        UUID resourceId = job.getResourceId();
        if (!authzService.isViewer(userId, resourceId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(schedulerRepository.findById(id), HttpStatus.OK);
    }

    @Operation(summary = "Get schedule job logs by jobId.")
    @GetMapping(value = "/logs/{id}")
    public ResponseEntity<Object> getScheduleLogs(Principal principal, PageSettings pageSettings, @PathVariable("id") UUID jobId) throws Exception {
        User user = userService.getUser(principal.getName());

        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                schedulerService.getScheduleLogs(pageSettings, jobId)));
    }

    @Operation(summary = "Add / Update schedule job.")
    @PutMapping("/schedule")
    public ResponseEntity<Object> addOrUpdateSchedule(Principal principal, @RequestBody SchedulerJobInfo schedulerJobInfo) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        String actionType = "add";
        if (schedulerJobInfo.getJobId() != null) {
            actionType = "update";

            if (!schedulerRepository.existsById(schedulerJobInfo.getJobId())) {
                return new ResponseEntity<>("No such Job Exist", HttpStatus.BAD_REQUEST);
            }

            if (!authzService.isEditor(userId, schedulerJobInfo.getResourceId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        schedulerJobInfo.setUpdatedAt(new Date());
        schedulerJobInfo.setUpdatedBy(userId);

        if (actionType.equals("add")) {
            schedulerJobInfo.setCreatedAt(new Date());
            schedulerJobInfo.setCreatedBy(userId);
        }

        if (schedulerJobInfo.getResourceType().equals(ResourceType.DATASET)) {
            if (!resourceService.existsById(schedulerJobInfo.getResourceId())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (!schedulerJobInfo.getResourceType().equals(ResourceType.DASHBOARD) && schedulerRepository.existsByResourceIdAndBranchAndResourceType(schedulerJobInfo.getResourceId(), schedulerJobInfo.getBranch(), schedulerJobInfo.getResourceType())) {
            schedulerJobInfo.setJobId(schedulerRepository.findByResourceIdAndBranchAndResourceType(schedulerJobInfo.getResourceId(), schedulerJobInfo.getBranch(), schedulerJobInfo.getResourceType()).getJobId());
            actionType = "update";
        }

        // Resolve Builder
        schedulerService.resolveBuilder(schedulerJobInfo);
        log.info(">>>>>>>>>>>>> ACTION TYPE : " + actionType);
        UUID newJobId = schedulerService.manageJob(schedulerJobInfo, schedulerJobInfo.getTriggers().get(0), actionType);
        log.info(" >>>>>>>>>> NEW JOB ID " + newJobId);
        if (!schedulerRepository.existsById(newJobId)) {
            throw new BadRequestException("New job wasn't created completely.");
        }

        return new ResponseEntity<>(schedulerRepository.findById(newJobId), HttpStatus.OK);


    }
}
