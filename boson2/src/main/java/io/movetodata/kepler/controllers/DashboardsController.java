package io.movetodata.kepler.controllers;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.bob.library.models.SocketMessage;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.ChartsRepository;
import io.movetodata.kepler.library.repository.DashboardFilterRepository;
import io.movetodata.kepler.library.repository.DashboardsRepository;
import io.movetodata.kepler.library.repository.TabsRepository;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.ActiveDisplay;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import scala.Char;

import java.security.Principal;
import java.util.*;


@RestController
@RequestMapping("/api/kepler/dashboards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler dashboards management service endpoints")
public class DashboardsController {

    private final DatasetRepository datasetRepository;
    private final TabsRepository tabsRepository;
    private final UserService userService;
    private final DashboardsRepository dashboardsRepository;
    private final ChartsRepository chartsRepository;
    private final FolderRepository folderRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final ActiveDisplay activeDisplay;
    private final AuthzService authzService;
    private final PipelineRepository pipelineRepository;
    private final DashboardFilterRepository dashboardFilterRepository;
    private final OkResponse response = new OkResponse();

    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Get all dashboards")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllDashboards(Principal principal) {

//        UUID userId = userService.getUser(principal.getName()).id;
//        User user = userRepository.findByUsername(principal.getName())
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("User not found with userId : ")
//                );
//
//        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();
//
//        resourceViewsModel.setResourceId(id);
//        resourceViewsModel.setAction("view");
//        resourceViewsModel.setViewedBy(userId);

//        resourceViewsRepository.save(resourceViewsModel);
        List<DashboardsModel> dashboards = dashboardsRepository.findAll();

        return ResponseEntity.accepted().body(dashboards);
    }

    @Operation(summary = "This can be used to get dashboards by chartId.")
    @GetMapping("/getDashboardByChartId/{id}")
    public ResponseEntity<Set<DashboardsModel>> getDashboardByChartId(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ChartsModel chartsModel = chartsRepository.getById(id);

//        UUID dashboardId = chartsModel.getDashboard().getId();
        Set<DashboardsModel> dashboards = chartsModel.getDashboard();
        List<DashboardsModel> dashboard_list = new ArrayList<>();
        List<UUID> dashbaords_uuids = new ArrayList<>();
        for (DashboardsModel dashboard : dashboards) {
            dashbaords_uuids.add(dashboard.getId());
            dashboard_list.add(dashboard);
        }

        return ResponseEntity.accepted().body(dashboards);
    }

    @Operation(summary = "This can be used to get dashboards by Id.")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getDashboardsById(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }


        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }


        // Sending to socket to tell that someone opened dashboard
//        SocketMessage textMessage = new SocketMessage();
//        textMessage.setMessage(userId.toString());
//
//        System.out.println("sending dashboard socket on :: " + id);
//
//        template.convertAndSend("/topic/dashboard/" + id, textMessage);

        return new ResponseEntity<>(dashboardsRepository.findById(id), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create dashboards.")
    @PostMapping("/new")
    public ResponseEntity<Object> newDashboards(Principal principal, @RequestBody DashboardRequest dashboardRequest) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!folderRepository.existsById(dashboardRequest.getParent())) {
            return new ResponseEntity<>("Parent with Id " + dashboardRequest.getParent() + " does not exist", HttpStatus.NOT_FOUND);

        }
        if (!authzService.isOwner(userId, dashboardRequest.getParent())) {
            return new ResponseEntity<>("Access Denied to " + dashboardRequest.getParent(), HttpStatus.FORBIDDEN);
        }

        // TODO : add validation for chart id

        // Create in kitab
        FolderModel folderModel = new FolderModel();
        folderModel.setName(dashboardRequest.getName());
        folderModel.setDescription(dashboardRequest.getDescription());
        folderModel.setParent(dashboardRequest.getParent());
        folderModel.setStatus("active");
        folderModel.setType("dashboard");
        folderModel.setCreatedBy(userId);
        folderModel.setCreatedAt(new Date());

        FolderModel folderModel1 = folderRepository.save(folderModel);

        DashboardsModel dashboardsModel = new DashboardsModel();


        dashboardsModel.setId(folderModel1.getId());

        dashboardsModel.setName(dashboardRequest.getName());
        dashboardsModel.setDescription(dashboardRequest.getDescription());
        dashboardsModel.setParent(dashboardRequest.getParent());

        // A dashboard by default will always have atleast 1 tab
        TabsModel newTab = new TabsModel();
        List<TabsModel> tabs = new ArrayList<>();
        newTab.setName("Tab 1");

        dashboardsModel.setTabs(tabs);
        dashboardsModel.setCreatedAt(new Date());
        dashboardsModel.setCreatedBy(userId);
        tabs.add(newTab);
        newTab.setDashboardsModel(dashboardsModel);
        tabsRepository.save(newTab);

        return new ResponseEntity<>(dashboardsRepository.save(dashboardsModel), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to update dashboards by existing Id.")
    @PutMapping("/update/{Id}")
    public ResponseEntity<Object> updateDashboards(Principal principal, @RequestBody DashboardRequest dashboardRequest, @PathVariable("Id") UUID Id) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (dashboardRequest.getParent() != null) {
            if (!authzService.isOwner(userId, dashboardRequest.getParent())) {
                return new ResponseEntity<>("Access Denied to " + dashboardRequest.getParent(), HttpStatus.FORBIDDEN);
            }
        }


        if (!dashboardsRepository.existsById(Id)) {
            return new ResponseEntity<>("Dashboard with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);

        }

        if (dashboardRequest.getParent() != null) {
            if (!folderRepository.existsById(dashboardRequest.getParent())) {
                return new ResponseEntity<>("Parent with Id " + dashboardRequest.getParent() + " does not exist", HttpStatus.NOT_FOUND);
            }
        }

        if (!dashboardsRepository.existsById(Id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }


        DashboardsModel dashboardsModelExisting = dashboardsRepository.getById(Id);

        if (!authzService.isOwner(userId, dashboardsModelExisting.getParent())) {
            return new ResponseEntity<>("Access Denied to " + dashboardsModelExisting.getParent(), HttpStatus.FORBIDDEN);
        }

        dashboardsModelExisting.setName(dashboardRequest.getName());
        dashboardsModelExisting.setDescription(dashboardRequest.getDescription());
        dashboardsModelExisting.setParent(dashboardRequest.getParent());
        dashboardsModelExisting.setUpdatedAt(new Date());
        dashboardsModelExisting.setUpdatedBy(userId);

        DashboardsModel dashboardsModelSaved = dashboardsRepository.save(dashboardsModelExisting);

        FolderModel folderModel = folderRepository.getById(dashboardsModelExisting.getId());

        folderModel.setName(dashboardRequest.getName());
        folderModel.setDescription(dashboardRequest.getDescription());
        folderModel.setParent(dashboardRequest.getParent());
        folderModel.setUpdatedAt(new Date());
        folderModel.setUpdatedBy(userId);

        folderRepository.save(folderModel);


        return new ResponseEntity<Object>(dashboardsModelSaved, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to delete dashboards.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteDashboards(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        if (!dashboardsRepository.existsById(id)) {

            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }

        dashboardsRepository.deleteById(id);

        // delete from kitab also

        folderRepository.deleteById(id);

        return new ResponseEntity<>("Dashboards deleted successfully", HttpStatus.OK);
    }


    @Operation(summary = "Add or Remove charts from dashboard")
    @PostMapping("/manage")
    public ResponseEntity<Object> addChartsToDashboard(@Valid @RequestBody ManageDashboardRequest manageDashboardRequest, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(manageDashboardRequest.getDashboardId())) {
            return new ResponseEntity<>("Id " + manageDashboardRequest.getDashboardId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if(!dashboardsRepository.existsById(manageDashboardRequest.getDashboardId())){
            return new ResponseEntity<>("Id " + manageDashboardRequest.getDashboardId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isEditor(userId, manageDashboardRequest.getDashboardId())) {
            return new ResponseEntity<>("Access Denied to " + manageDashboardRequest.getDashboardId(), HttpStatus.FORBIDDEN);
        }

        if (manageDashboardRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageDashboardRequest.getAction(), "add")) && (!Objects.equals(manageDashboardRequest.getAction(), "remove"))) {
            return new ResponseEntity<>("Not a valid action type, use add or remove", HttpStatus.BAD_REQUEST);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.findById(manageDashboardRequest.getDashboardId()).orElseThrow();

        Set<ChartsModel> chartsModels = dashboardsModel.getCharts();

        for (UUID chartId : manageDashboardRequest.getChartIds()) {

            if (!chartsRepository.existsById(chartId)) {
                return new ResponseEntity<>("Chart not found " + chartId, HttpStatus.BAD_REQUEST);
            }

            ChartsModel chartsModel = chartsRepository.getById(chartId);

            if (chartsModels.contains(chartsModel)) {
                if (Objects.equals(manageDashboardRequest.getAction(), "remove")) {
                    chartsModel.setDashboard(null);
                    chartsModel.setUpdatedAt(new Date());
                    chartsModel.setUpdatedBy(userId);

                    PipelineModel model = pipelineRepository.findBySourceDatasetAndSourceBranchAndTargetDatasetAndTargetBranch(chartId, chartsModel.getBranch(),  dashboardsModel.getId(), dashboardsModel.getBranch());
                    pipelineRepository.delete(model);
                }
            } else {
                if (Objects.equals(manageDashboardRequest.getAction(), "add")) {
                    /*
                        dashboardModel -> Requested dashboard
                        chartsModels -> R D charts
                        chartsModel -> candidate chart
                        dashboard -> candidate dashboard set

                        add dM to dashboard and save to candidate chart
                     */
                    Set<DashboardsModel> dashboard = chartsModel.getDashboard();
//                    dashboardsRepository.findById(manageDashboardRequest.getDashboardId());
                    if (!dashboard.contains(dashboardsModel)) {
                        dashboard.add(dashboardsModel);

                        // Making pipeline link
                        PipelineModel model = new PipelineModel();
                        model.sourceDataset = chartId;
                        model.targetDataset = dashboardsModel.getId();
                        model.sourceBranch = chartsModel.getBranch();
                        model.targetBranch = dashboardsModel.getBranch();
                        model.repositoryId = null;
                        model.repositoryBranch = null;
                        model.scriptPath = null;
                        model.buildId = null;
                        model.status = "active";
                        model.type = "dashboard";
                        model.setCreatedBy(userId);
                        model.setUpdatedBy(userId);
                        pipelineRepository.saveAndFlush(model);
                    }

                    chartsModel.setDashboard(dashboard);

                    chartsModel.setUpdatedAt(new Date());
                    chartsModel.setUpdatedBy(userId);

                    chartsRepository.save(chartsModel);

                    /*
                        add candidate chart to charts of dashboard

                     */
                    Set <ChartsModel> charts = dashboardsModel.getCharts();
                    charts.add(chartsRepository.getById(chartsModel.getId()));
                    dashboardsModel.setCharts(charts);
                    dashboardsRepository.save(dashboardsModel);
                }
            }
        }

        dashboardsModel.setUpdatedAt(new Date());
        dashboardsModel.setUpdatedBy(userId);


        DashboardsModel dashboardsModelSaved = dashboardsRepository.save(dashboardsModel);

        // Updated by
        FolderModel folderModel = folderRepository.getById(dashboardsModel.getId());
        folderModel.setUpdatedAt(new Date());
        folderModel.setUpdatedBy(userId);
        folderRepository.save(folderModel);


        return new ResponseEntity<>("Dashboard updated", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to rename dashboard.")
    @GetMapping("/{Id}/{newName}/rename")
    public ResponseEntity<Object> renameDashboard(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);

        dashboardsModel.setName(newName);

        dashboardsRepository.save(dashboardsModel);

        if (folderRepository.existsById(id)) {
            FolderModel folderModel = folderRepository.getById(id);
            folderModel.setName(newName);

            folderRepository.save(folderModel);
        }

        return new ResponseEntity<>("Chart renamed successfully", HttpStatus.OK);

    }

    @Operation(summary = "This can be used to change description of dashboards.")
    @GetMapping("/{Id}/{newDescription}/renameDashboardDescription")
    public ResponseEntity<Object> renameDashboardDescription(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newDescription") String newDescription) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);

        dashboardsModel.setDescription(newDescription);

        dashboardsRepository.save(dashboardsModel);

        if (folderRepository.existsById(id)) {
            FolderModel folderModel = folderRepository.getById(id);
            folderModel.setDescription(newDescription);

            folderRepository.save(folderModel);
        }

        return new ResponseEntity<>("Chart Description changed successfully", HttpStatus.OK);
    }

    @Operation(summary = "Get Dashboard Datasets")
    @GetMapping("/getDatasets/{id}")
    public ResponseEntity<Object> getDatasets(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
//        if (!authzService.isOwner(userId, id)) {
//            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
//        }

        HashMap<String, String> datasets = new HashMap<>();

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        Set<ChartsModel> charts = dashboardsModel.getCharts();

        for (ChartsModel chart : charts) {
            FolderModel model = folderRepository.getById(chart.getDatasetId());
            datasets.put(model.getId().toString(), model.getName());
        }

        return new ResponseEntity<>(datasets, HttpStatus.OK);
    }

    @Operation(summary = "Get Dashboard Filters")
    @GetMapping("/getFilters/{id}")
    public ResponseEntity<Object> getFilters(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
//        if (!authzService.isOwner(userId, id)) {
//            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
//        }
        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        List<DashboardFilterModel> filters = dashboardsModel.getFilters();

        return new ResponseEntity<>(filters, HttpStatus.OK);
    }

    @Operation(summary = "Save Dashboard Filters")
    @PostMapping("/saveFilters/{id}")
    public ResponseEntity<Object> saveFilters(Principal principal, @PathVariable("id") UUID id, @RequestBody DashboardFilterModel filter) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
//        if (!authzService.isOwner(userId, id)) {
//            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
//        }
        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        List<DashboardFilterModel> dashboardFilters = dashboardsModel.getFilters();
        System.out.println(filter);
        if (filter.getId() == null) {
            System.out.println("CASE1");
            DashboardFilterModel newFilter = new DashboardFilterModel();
            newFilter.setFilterType(filter.getFilterType());
            newFilter.setColumnName(filter.getColumnName());
            newFilter.setDatasetId(filter.getDatasetId());
            newFilter.setName(filter.getName());
            newFilter.setCreatedAt(new Date());
            newFilter.setUpdatedAt(new Date());
            dashboardFilterRepository.save(newFilter);
            dashboardFilters.add(newFilter);
            dashboardsModel.setFilters(dashboardFilters);
            dashboardsRepository.save(dashboardsModel);
        }
        else {
            System.out.println("CASE2");
            DashboardFilterModel oldFilter = dashboardFilterRepository.getById(filter.getId());
            oldFilter.setFilterType(filter.getFilterType());
            oldFilter.setColumnName(filter.getColumnName());
            oldFilter.setDatasetId(filter.getDatasetId());
            oldFilter.setName(filter.getName());
            oldFilter.setUpdatedAt(new Date());
            dashboardFilterRepository.saveAndFlush(oldFilter);
            for (DashboardFilterModel dFilter : dashboardFilters) {
                if (dFilter.getId() == oldFilter.getId()) {
                    dFilter.setFilterType(oldFilter.getFilterType());
                    dFilter.setName(oldFilter.getName());
                    dFilter.setColumnName(oldFilter.getColumnName());
                    dFilter.setUpdatedAt(new Date());
                    break;
                }
            }
            dashboardsModel.setFilters(dashboardFilters);
            dashboardsRepository.saveAndFlush(dashboardsModel);
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        }
//        System.out.println("CASE3");
//        List<DashboardFilterModel> newFilters = dashboardsRepository.getById(id).getFilters();
        return new ResponseEntity<>(dashboardFilters, HttpStatus.OK);
    }

    @Operation(summary = "Get Dashboard Filters")
    @DeleteMapping("/deleteFilter/{dashId}/{filterId}")
    public ResponseEntity<Object> deleteFilter(Principal principal, @PathVariable("dashId") UUID dashId, @PathVariable("filterId") UUID filterId) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!dashboardsRepository.existsById(dashId)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }

        if (!dashboardFilterRepository.existsById(filterId)) {
            return new ResponseEntity<>("No filter found for given Id", HttpStatus.NOT_FOUND);
        }
//        if (!authzService.isOwner(userId, id)) {
//            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
//        }

        DashboardsModel dash = dashboardsRepository.getById(dashId);
        DashboardFilterModel filter = dashboardFilterRepository.getById(filterId);
        List<DashboardFilterModel> dashFilters = dash.getFilters();
        dashFilters.remove(filter);
        dash.setFilters(dashFilters);
        dashboardsRepository.save(dash);
        dashboardFilterRepository.delete(filter);

        return new ResponseEntity<>("Deletion success", HttpStatus.OK);
    }
}

