package io.movetodata.kepler.controllers;

import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.kepler.library.DTOs.ChangesDTO;
import io.movetodata.kepler.library.DTOs.ChartSuggestedFilterDTO;
import io.movetodata.kepler.library.DTOs.DashboardResponseDTO;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.*;
import io.movetodata.kepler.library.services.ChangesService;
import io.movetodata.kepler.library.services.DashboardsService;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.DTO.PageToPageDTOMapper;
import io.movetodata.sharedutils.Response.ErrorDTO;
import io.movetodata.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/kepler/dashboards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler dashboards management service endpoints")
public class DashboardsController {

    private final UserService userService;
    private final ResourceService resourceService;
    private final DashboardsRepository dashboardsRepository;
    private final ChartsRepository chartsRepository;
    private final AuthzService authzService;
    private final PipelineRepository pipelineRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ResourceVersionDetailsRepository resourceVersionDetailsRepository;
    private final ChangesService changesService;
    private final DashboardsService dashboardsService;
    private final PlatformConfigService platformConfigService;
    private final PageToPageDTOMapper pageToPageDTOMapper;
    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private TabElementsRepository tabElementsRepository;

    @Operation(summary = "Get all dashboards")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllDashboards(Principal principal) {

        List<DashboardsModel> dashboards = dashboardsRepository.findAll();

        return ResponseEntity.accepted().body(dashboards);
    }

    @Operation(summary = "This can be used to get dashboards by chartId.")
    @GetMapping("/getDashboardByChartId/{id}")
    public ResponseEntity<Set<DashboardsModel>> getDashboardByChartId(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (chartsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        ChartKey chartKey = new ChartKey(id, chartVersion.getLatestVersionId());

        ChartsModel chartsModel = chartsRepository.getReferenceById(chartKey);

//        UUID dashboardId = chartsModel.getDashboard().getId();
        Set<DashboardsModel> dashboards = chartsModel.getDashboard();
        Set<DashboardsModel> dashboardsNotInTrash = new HashSet<>();
        List<UUID> dashbaords_uuids = new ArrayList<>();
        for (DashboardsModel dashboard : dashboards) {
            if (resourceService.getResourceModel(dashboard.getId()).getStatus().equals("inTrash")) {
                continue;
            }
            dashbaords_uuids.add(dashboard.getId());
            dashboardsNotInTrash.add(dashboard);
        }

        return ResponseEntity.accepted().body(dashboardsNotInTrash);
    }

    @Operation(summary = "This can be used to get latest dashboard version or particular version of dash by Id.")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getDashboardsById(Principal principal, @PathVariable("id") UUID id, @RequestParam(name = "v", required = false) Long versionId) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        log.info(">>>>>>> RES VIEWED");

        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);

        if (versionId == null) {
            versionId = dashboardVersion.getLatestVersionId();
        }
        log.info(">>>>>>> VID : " + versionId);
        DashboardKey dashboardId = new DashboardKey(id, versionId);
        if (!dashboardsRepository.existsById(dashboardId)) {
            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }
        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore Dashboard to access it." + id, HttpStatus.NOT_FOUND);
        }


        ResourceModel dashboardsModel = resourceService.findById(id).orElseThrow();
        DashboardsModel dashboard = dashboardsRepository.getReferenceById(dashboardId);
        log.info(">>>>>>> DASH : " + dashboard);
        log.info(">>>>>>> DASH NAME : " + dashboardsModel.getName());
        return new ResponseEntity<>(new DashboardResponseDTO(dashboard), HttpStatus.OK);
    }

    @Operation(summary = "This method will return smartly suggest chart")
    @PostMapping("/getSuggestedCharts/{id}")
    public ResponseEntity<Object> getSuggestedCharts(@NotNull Principal principal, PageSettings pageSettings, @PathVariable("id") UUID dashboardId, @RequestBody ChartSuggestedFilterDTO chartSuggestedFilter) {
        log.info(String.valueOf(chartSuggestedFilter));
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, dashboardId)) {
            throw new UnauthorizedException();
        }
        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(dashboardsService.getAllSuggestedCharts(pageSettings, chartSuggestedFilter)));
    }

    @Operation(summary = "This can be used to create dashboards.")
    @PostMapping("/new")
    public ResponseEntity<Object> newDashboards(Principal principal, @RequestBody DashboardRequest dashboardRequest) {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (platformConfigService.isResourceCreationLimitReached(ResourceType.DASHBOARD)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorDTO(HttpStatus.TOO_MANY_REQUESTS.value(), "Maximum No. of Dashboard Limit Reached."));
        }
        ResourceModel resourceModel = dashboardsService.createDashboard(userId, dashboardRequest);

        return new ResponseEntity<>(resourceModel, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to delete dashboards and all its versions or particular version.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteDashboards(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isOwner(userId, id)) {
            throw new UnauthorizedException();
        }

        if (dashboardsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }

        dashboardsRepository.removeAllById(id);

        // delete from kitab also
        resourceService.deleteById(id);

        return new ResponseEntity<>("Dashboards deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "Add or Remove charts from dashboard")
    @PostMapping("/manage")
    public ResponseEntity<Object> addChartsToDashboard(@Valid @RequestBody ManageDashboardRequest manageDashboardRequest, Principal principal) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        // Only editors can add the only charts which they have access to.
        // Dashboard editor access
        if (!authzService.isEditor(userId, manageDashboardRequest.getDashboardId())) {
            throw new UnauthorizedException();
        }

        dashboardsService.manageChartsOnDashboard(manageDashboardRequest, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "This can be used to rename dashboard.")
    @PostMapping("/{Id}/{newName}/rename")
    public ResponseEntity<Object> renameDashboard(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newName") String newName) {
        UUID userId = userService.getUser(principal.getName()).getId();
        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);
        Long versionId = dashboardVersion.getLatestVersionId();
        DashboardKey dashboardId = new DashboardKey(id, versionId);

        if (!dashboardsRepository.existsById(dashboardId)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            throw new UnauthorizedException();
        }

        ResourceModel dashResourceModel = resourceService.findById(id).orElseThrow();
        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);

        // ------- Recording Changes for latest version
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(id).getVersions().get(0);
        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("renamed", true));
        changes.add(new ChangesDTO("dashboard", true));
        changes.add(new ChangesDTO(dashResourceModel.getName(), false));
        changes.add(new ChangesDTO("to", true));
        changes.add(new ChangesDTO(newName, false));

        changesService.SaveChanges(changes, versionDetails, "rename", userId);
        // -------------------------


        dashboardsRepository.save(dashboardsModel);

        if (resourceService.existsById(id)) {
            ResourceModel resourceModel = resourceService.getResourceModel(id);
            resourceModel.setName(newName);
            resourceService.save(resourceModel);
        }
        return new ResponseEntity<>("Dashboard renamed successfully", HttpStatus.OK);
    }

    @Operation(summary = "Get Dashboard Datasets")
    @GetMapping("/getDatasets/{id}")
    public ResponseEntity<Object> getDatasets(Principal principal, @PathVariable("id") UUID id, @RequestParam(name = "v", required = false) Long versionId) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);
        if (versionId == null) {
            versionId = dashboardVersion.getLatestVersionId();
        }
        DashboardKey dashboardId = new DashboardKey(id, versionId);

        if (!dashboardsRepository.existsById(dashboardId)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }

        HashMap<String, String> datasets = new HashMap<>();
        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);
        Set<ChartsModel> charts = dashboardsModel.getCharts();

        for (ChartsModel chart : charts) {
            ResourceModel model = resourceService.getResourceModel(chart.getDatasetId());
            datasets.put(model.getId().toString(), model.getName());
        }

        return new ResponseEntity<>(datasets, HttpStatus.OK);
    }

    @Operation(summary = "This gives all the versions of dashboard.")
    @GetMapping("/getVersions/{id}")
    public ResponseEntity<Object> getVersions(Principal principal, @PathVariable("id") UUID id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            throw new UnauthorizedException();
        }

        if (dashboardsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }
        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore Dashboard to access it." + id, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dashboardsRepository.findById(id), HttpStatus.OK);

    }

    @Operation(summary = "This creates a new version.")
    @PostMapping("/createVersion/{id}/{name}")
    public ResponseEntity<Object> createVersion(Principal principal, @PathVariable("id") UUID id, @PathVariable("name") String versionName, @RequestParam(name = "v", required = false) Long versionId) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, id)) {
            throw new UnauthorizedException();
        }

        if (dashboardsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No dashboard found for given Id" + id, HttpStatus.NOT_FOUND);
        }
        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore Dashboard to access it." + id, HttpStatus.NOT_FOUND);
        }

        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);
        if (versionId == null) {
            versionId = dashboardVersion.getLatestVersionId();
        }
        Long futureVersionId = dashboardVersion.getLatestVersionId() + 1;
        DashboardKey dashboardId = new DashboardKey(id, versionId);
        DashboardKey futureDashboardId = new DashboardKey(id, futureVersionId);

        DashboardsModel dashboard = dashboardsRepository.getReferenceById(dashboardId);
        DashboardsModel futureVersionDashboard = new DashboardsModel();

        // Taking care of static fields
        futureVersionDashboard.setId(dashboard.getId());
        futureVersionDashboard.setVersionId(futureVersionId);
        futureVersionDashboard.setLastVersionedDate(new Date());
        futureVersionDashboard.setBranch(dashboard.getBranch());

        futureVersionDashboard.setCharts(new HashSet<>(dashboard.getCharts()));

        // Tabs
        List<TabsModel> tabs = dashboard.getTabs();
        List<TabsModel> futureTabs = new ArrayList<>();

        for (TabsModel tab : tabs) {
            TabsModel futureTab = new TabsModel();
            futureTab.setName(tab.getName());
            futureTab.setCreatedAt(tab.getCreatedAt());
            futureTab.setCreatedBy(tab.getCreatedBy());
            futureTab.setUpdatedAt(new Date());
            futureTab.setUpdatedBy(userId);

            // Charts
            Set<ChartsModel> charts = tab.getChartsForTabs();
            Set<ChartsModel> futureCharts = new HashSet<>();
            for (ChartsModel chart : charts) {
                Set<TabsModel> chartTab = chart.getTabsForCharts();
                chartTab.add(tab);
                chart.setTabsForCharts(chartTab);

                chartsRepository.save(chart);
                futureCharts.add(chart);
            }

            futureTab.setChartsForTabs(futureCharts);

            // Dashboard
            futureTab.setDashboardsModel(futureVersionDashboard);

            // Tab Elements
            List<TabElementsModel> tabElements = tab.getTabElements();
            List<TabElementsModel> futureTabElements = new ArrayList<>();

            for (TabElementsModel tabElement : tabElements) {
                TabElementsModel futureTabElement = new TabElementsModel();
                futureTabElement.setId(UUID.randomUUID());
                futureTabElement.setData(tabElement.getData());
                futureTabElement.setType(tabElement.getType());
                futureTabElement.setPosition(tabElement.getPosition());
                futureTabElement.setDatasetId(tabElement.getDatasetId());
                futureTabElement.setTabsModel(futureTab);

//                tabElementsRepository.save(futureTabElement);
                futureTabElements.add(futureTabElement);
            }
            futureTab.setTabElements(futureTabElements);
//            tabsRepository.save(futureTab);

            futureTabs.add(futureTab);
        }

        futureVersionDashboard.setTabs(futureTabs);

        dashboardsRepository.save(futureVersionDashboard);

        // Updating in Resource Versions
        dashboardVersion.setLatestVersionId(futureVersionId);
        List<ResourceVersionDetailsModel> currentVersions = dashboardVersion.getVersions();
        ResourceVersionDetailsModel newVersion = new ResourceVersionDetailsModel();
        if (versionName != null) {
            newVersion.setName(versionName);
        } else {
            newVersion.setName("Version" + futureVersionId);
        }
        newVersion.setCreatedAt(new Date());
        newVersion.setCreatedBy(userId);
        newVersion.setResourceVersionsModel(dashboardVersion);
        newVersion.setVersionId(futureVersionId);
        resourceVersionDetailsRepository.save(newVersion);

        currentVersions.add(newVersion);
        dashboardVersion.setVersions(currentVersions);
        resourceVersionsRepository.save(dashboardVersion);

        return new ResponseEntity<>(new DashboardResponseDTO(futureVersionDashboard), HttpStatus.CREATED);

    }


}

