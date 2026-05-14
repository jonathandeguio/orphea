package io.orphea.kepler.controllers;

import io.orphea.bezier.library.models.PipelineModel;
import io.orphea.bezier.library.repository.PipelineRepository;
import io.orphea.build.BobEnums.BuildStatus;
import io.orphea.build.library.models.SocketMessage;
import io.orphea.dataset.library.DTOs.DatasetFiltersDTO;
import io.orphea.dataset.library.services.ChartDataService;
import io.orphea.dataset.library.services.DatasetMappingService;
import io.orphea.dataset.requests.ChartDataRequest;
import io.orphea.dataset.requests.ChartSeriesRequest;
import io.orphea.kepler.library.DTOs.ChangesDTO;
import io.orphea.kepler.library.models.*;
import io.orphea.kepler.library.repository.*;
import io.orphea.kepler.library.services.ChangesService;
import io.orphea.kepler.library.services.ChartsChangesDetectionResultDTO;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.repository.DatasetRepository;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.passport.exception.UnauthorizedException;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.platform.library.repository.CacheRepository;
import io.orphea.platform.library.services.PlatformConfigService;
import io.orphea.sharedutils.Response.ErrorDTO;
import io.orphea.sharedutils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.shaded.org.eclipse.jetty.util.ajax.JSON;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.*;

import static io.orphea.sharedutils.Redis.deleteCache;


@RestController
@RequestMapping("/api/kepler/charts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler charts management service endpoints")
public class ChartsController {
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ResourceVersionDetailsRepository resourceVersionDetailsRepository;
    private final UserService userService;
    private final ChartsRepository chartsRepository;
    private final TabsRepository tabsRepository;
    private final PipelineRepository pipelineRepository;
    private final DatasetRepository datasetRepository;
    private final CacheRepository cacheRepository;
    private final DatasetMappingService datasetMappingService;
    private final AuthzService authzService;
    private final ResourceService resourceService;
    private final ChartDataService chartDataService;
    private final ChangesService changesService;


    private final ChartSeriesRepository chartSeriesRepository;
    private final DatasetFilterModelRepository datasetFilterModelRepository;
    private final ChartConfigRepository chartConfigRepository;
    private final ChartCustomizeRepository chartCustomizeRepository;
    private final PlatformConfigService platformConfigService;

    @Autowired
    SimpMessagingTemplate template;

    @Inject
    FilterModelRepository filterModelRepository;

    @Autowired
    ModelMapper modelMapper;

    @Operation(summary = "Get latest version of charts for a tab")
    @GetMapping("/getCharts/{tabId}")
    public ResponseEntity<Object> getCharts(Principal principal, @PathVariable("tabId") UUID tabId) {

        UUID userId = userService.getUser(principal.getName()).getId();
        List<ChartsModel> allCharts = chartsRepository.findAll();
        Set<ChartsModel> tabCharts = tabsRepository.getReferenceById(tabId).getChartsForTabs();
        Set<UUID> alreadyExistedTabCharts = new HashSet<>();
        for (ChartsModel chart : tabCharts) {
            alreadyExistedTabCharts.add(chart.getId());
        }

        List<ChartsModel> charts = new ArrayList<>();
        for (ChartsModel chart : allCharts) {
            if (resourceService.existsById(chart.getId())) {
                if (!authzService.isViewer(userId, chart.getId())) {
                    continue;
                }
                if (resourceService.getResourceModel(chart.getId()).getStatus().equals("inTrash")) {
                    continue;
                }
                if (!alreadyExistedTabCharts.contains(chart.getId())) {
                    // Getting latest version of chart. As this is some old version.
                    ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chart.getId());
                    ChartKey chartKey = new ChartKey(chart.getId(), chartVersion.getLatestVersionId());
//                    chartsRepository.getReferenceById(chartKey);
                    charts.add(chartsRepository.getReferenceById(chartKey));
                    alreadyExistedTabCharts.add(chart.getId());
                }
            }
        }

        return ResponseEntity.accepted().body(charts);
    }

    @Operation(summary = "This can be used to search charts.")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getChartById(@NotNull Principal principal, @PathVariable("id") UUID id, @RequestParam(name = "v", required = false) Long versionId) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            throw new UnauthorizedException();
        }


        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        if (versionId == null) {
            versionId = chartVersion.getLatestVersionId();
        }
        ChartKey chartKey = new ChartKey(id, versionId);

        if (!authzService.isViewer(userId, id)) {
            throw new UnauthorizedException();
        }

        if (!chartsRepository.existsById(chartKey)) {
            return new ResponseEntity<>("No chart found for given Id or particular version", HttpStatus.NOT_FOUND);
        }

        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore chart to access it." + id, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(chartsRepository.findById(chartKey), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create charts.")
    @PostMapping("/new")
    public ResponseEntity<Object> newCharts(Principal principal, @RequestBody ChartRequest chartRequest) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();
        if (!authzService.isEditor(userId, chartRequest.getParent())) {
            throw new UnauthorizedException();
        }
        if (!resourceService.existsById(chartRequest.getChartConfig().getDatasetId())) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ErrorDTO(HttpStatus.EXPECTATION_FAILED.value(), "Dataset with Id " + chartRequest.getParent() + " does not exist"));
        }
        if(platformConfigService.isResourceCreationLimitReached(ResourceType.CHART)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorDTO(HttpStatus.TOO_MANY_REQUESTS.value(), "Maximum No. of Charts Limit Reached."));
        }
        try {
            datasetMappingService.getCurrentTransaction(chartRequest.getChartConfig().getDatasetId(), chartRequest.getChartConfig().getBranch());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ErrorDTO(HttpStatus.EXPECTATION_FAILED.value(), "No Data", "Dataset doesn't have any active transaction. Please upload some data!"));
        }

        // Create in kitab
        ResourceModel resourceModel = resourceService.newResource(chartRequest.getName(),
                chartRequest.getDescription(),
                ResourceType.CHART,
                ResourceSubtype.valueOf(chartRequest.getChartConfig().getChartType().toUpperCase()),
                userId,
                chartRequest.getParent());

        ChartConfigModel chartConfigModel = new ChartConfigModel();
        if (chartRequest.getChartConfig() != null) {
            chartConfigModel = constructChartConfigModelFromRequest(chartConfigModel, chartRequest.getChartConfig(), chartSeriesRepository, datasetFilterModelRepository);
        }

        chartConfigRepository.save(chartConfigModel);

        // As in starting no versions are present. Hence, creating entry in version table for v1
        ResourceVersionsModel resourceVersionsModel = new ResourceVersionsModel();
        resourceVersionsModel.setResourceId(resourceModel.getId());
        resourceVersionsModel.setLastVersionId(1L);
        resourceVersionsModel.setLatestVersionId(1L);

        List<ResourceVersionDetailsModel> currentVersions = resourceVersionsModel.getVersions();
        ResourceVersionDetailsModel newVersion = new ResourceVersionDetailsModel();
        newVersion.setName("Version1");
        newVersion.setCreatedAt(new Date());
        newVersion.setCreatedBy(userId);
        newVersion.setResourceVersionsModel(resourceVersionsModel);
        newVersion.setVersionId(1L);
        currentVersions.add(newVersion);
        resourceVersionDetailsRepository.save(newVersion);

        resourceVersionsModel.setVersions(currentVersions);

        resourceVersionsRepository.save(resourceVersionsModel);

        chartsRepository.save(ChartsModel
                .builder()
                .id(resourceModel.getId())
                .versionId(1L)
                .lastVersionedDate(new Date())
                .datasetId(chartRequest.getDatasetId())
                .branch(chartRequest.getBranch())
                .chartCustomize(JSON.toString(chartRequest.getChartCustomize()))
                .chartConfig(chartConfigModel)
                .build());

        // Create pipeline
        PipelineModel model = PipelineModel.builder()
                .sourceDataset(chartRequest.getDatasetId())
                .targetDataset(resourceModel.getId())
                .sourceBranch(chartRequest.getBranch())
                .targetBranch(chartRequest.getBranch())
                .status(BuildStatus.ACTIVE)
                .type("chart")
                .createdBy(userId)
                .updatedBy(userId)
                .createdBy(userId)
                .build();
        pipelineRepository.saveAndFlush(model);

        return new ResponseEntity<>(resourceModel, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to save latest version of chart.")
    @PutMapping("/update/{Id}")
    public ResponseEntity<Object> updateChart(Principal principal, @RequestBody ChartRequest chartRequest, @PathVariable("Id") UUID Id) throws Exception {
        if (chartsRepository.findById(Id).isEmpty()) {
            return new ResponseEntity<>("Chart with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (chartRequest.getDatasetId() != null) {
            if (!datasetRepository.existsById(chartRequest.getDatasetId())) {
                return new ResponseEntity<>("Dataset with Id " + chartRequest.getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);
            }
        }

        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(Id);
        ChartKey chartKey = new ChartKey(Id, chartVersion.getLatestVersionId());

        Optional<ChartsModel> optionalChartsModel = chartsRepository.findById(chartKey);
        ChartsModel chartsModelExisting = new ChartsModel();

        if(optionalChartsModel.isPresent()) {
            chartsModelExisting = optionalChartsModel.get();
        }

        // ------- Recording Changes for latest version
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(Id).getVersions().get(0);
        ChartsChangesDetectionResultDTO result = ChangesService.ChartsChangesDetection(chartsModelExisting, chartRequest);
        int totalChanges = result.getCount();
        List<String> headings = result.getHeading();
        List<List<ChangesDTO>> changes = result.getChanges();
        for (int index = 0; index < totalChanges; index++) {
            changesService.SaveChanges(changes.get(index), versionDetails, headings.get(index), userId);
        }
        // -------------------------

        if (chartRequest.getChartConfig() != null) {
            ChartConfigModel ccm = constructChartConfigModelFromRequest(chartsModelExisting.getChartConfig(), chartRequest.getChartConfig(), chartSeriesRepository, datasetFilterModelRepository);
            chartConfigRepository.save(ccm);
        }

        chartsModelExisting.setDatasetId(chartRequest.getDatasetId());
        chartsModelExisting.setBranch(chartRequest.getBranch());
        if(Objects.nonNull(chartRequest.getChartCustomize())) {
            chartsModelExisting.setChartCustomize(JSON.toString(chartRequest.getChartCustomize()));
        }

        ChartsModel chartsModelSaved = chartsRepository.save(chartsModelExisting);

        ResourceModel resourceModel = resourceService.getResourceModel(chartsModelExisting.getId());
        resourceModel.setUpdatedAt(new Date());
        resourceModel.setUpdatedBy(user.getId());

        if (chartRequest.getChartConfig() != null) {
            chartRequest.getChartConfig().setChartUUID(chartsModelSaved.getId());
            chartRequest.getChartConfig().setFetchCachedData(false);
            try {
                chartDataService.getDataFromSparkOrCache(chartRequest.getChartConfig(), chartRequest.getUserLocale());
            } catch (Exception ignored) {
            }
        }

        // FIXME
        if (Objects.equals(chartsModelSaved.getChartConfig().getChartType(), "VerticalAxisChart") && chartsModelSaved.getChartConfig().getSeries() != null && chartsModelSaved.getChartConfig().getSeries().size() == 1) {
            resourceModel.setSubType(ResourceSubtype.valueOf(chartsModelSaved.getChartConfig().getSeries().get(0).getSeriesType().toUpperCase()));
        } else {
            resourceModel.setSubType(ResourceSubtype.valueOf(chartRequest.getChartConfig().getChartType().toUpperCase()));
        }
        //send a socket call to all the dashboards it is attached.
        Set<DashboardsModel> dashboards = chartsModelSaved.getDashboard();
        for (DashboardsModel dashboard : dashboards) {
            for(TabsModel tab: dashboard.getTabs()) {
                if(tab.getChartsForTabs().contains(chartsModelSaved)) {
                    SocketMessage textMessage = new SocketMessage();
                    textMessage.setMessage(chartsModelSaved.getId().toString());
                    template.convertAndSend("/topic/dashboard/" + dashboard.getId() + "/" + tab.getId() , textMessage);
                }
            }
        }
        resourceService.save(resourceModel);

        return new ResponseEntity<Object>(chartsModelSaved, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to delete charts.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteChart(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (chartsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            throw new UnauthorizedException();
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        ChartKey chartKey = new ChartKey(id, chartVersion.getLatestVersionId());

        ChartsModel chartsModelExisting = chartsRepository.getReferenceById(chartKey);

        if (chartsModelExisting.getChartConfig() != null) {
            deleteCache("chartData" + chartsModelExisting.getDatasetId() + chartsModelExisting.getBranch() + id, cacheRepository);
        }

        pipelineRepository.deleteByTargetDatasetAndTargetBranch(chartsModelExisting.getId(), chartsModelExisting.getBranch());

        // delete chart
        chartsRepository.removeAllById(id);

        // delete from kitab also
        resourceService.deleteById(id);

        return new ResponseEntity<>("Chart deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to rename charts.")
    @GetMapping("/{Id}/{newName}/rename")
    public ResponseEntity<Object> renameChart(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (chartsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isEditor(userId, id)) {
            throw new UnauthorizedException();
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        ChartKey chartKey = new ChartKey(id, chartVersion.getLatestVersionId());

        ChartsModel chartsModel = chartsRepository.getReferenceById(chartKey);
        ResourceModel resourceModel = resourceService.getResourceModel(id);

        // ------- Recording Changes for latest version
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(id).getVersions().get(0);
        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("renamed", true));
        changes.add(new ChangesDTO("chart", true));
        changes.add(new ChangesDTO(resourceModel.getName(), false));
        changes.add(new ChangesDTO("to", true));
        changes.add(new ChangesDTO(newName, false));
        changesService.SaveChanges(changes, versionDetails, "rename", userId);
        // -------------------------


        chartsRepository.save(chartsModel);

        if (resourceService.existsById(id)) {
            ResourceModel resourceModel123 = resourceService.getResourceModel(id);
            resourceModel123.setName(newName);

            resourceService.save(resourceModel);
        }

        return new ResponseEntity<>("Chart renamed successfully", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to change description of charts.")
    @GetMapping("/{Id}/{newDescription}/renameChartDescription")
    public ResponseEntity<Object> renameChartDescription(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newDescription") String newDescription) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (chartsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            throw new UnauthorizedException();
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        ChartKey chartKey = new ChartKey(id, chartVersion.getLatestVersionId());

        ChartsModel chartsModel = chartsRepository.getReferenceById(chartKey);
        ResourceModel resourceModel = resourceService.getResourceModel(id);

        resourceModel.setDescription(newDescription);

        chartsRepository.save(chartsModel);

        if (resourceService.existsById(id)) {
            ResourceModel resourceModel123 = resourceService.getResourceModel(id);
            resourceModel123.setDescription(newDescription);

            resourceService.save(resourceModel);
        }

        return new ResponseEntity<>("Chart Description changed successfully", HttpStatus.OK);
    }

    private ChartConfigModel constructChartConfigModelFromRequest(ChartConfigModel chartConfigModel, ChartDataRequest queryConfigRequest, ChartSeriesRepository chartSeriesRepository, DatasetFilterModelRepository datasetFilterModelRepository) {
        List<DatasetFilterModel> DatasetFilterModelList = new ArrayList<>();

        if (queryConfigRequest.getFilter() != null) {
            for (DatasetFiltersDTO filter : queryConfigRequest.getFilter()) {
                DatasetFilterModel cFM = null;

                if (filter.getId() != null ) {
                    cFM = datasetFilterModelRepository.findById(filter.getId()).orElse(null);
                }

                if(cFM == null) {
                    cFM = Utils.convertDatasetFilterDTOToDatasetFilterModel(filter, filterModelRepository);
                }

                datasetFilterModelRepository.save(cFM);
                DatasetFilterModelList.add(cFM);
            }
        }

        List<SeriesModel> chartSeriesList = new ArrayList<>();
        if (queryConfigRequest.getSeries() != null) {
            for (ChartSeriesRequest seriesRequest: queryConfigRequest.getSeries()) {
                SeriesModel seriesModel = new SeriesModel();

                UUID id = seriesRequest.getSeriesId();
                if(Objects.nonNull(id)) {
                    seriesModel = chartSeriesRepository.findById(id).orElse(seriesModel);
                }

                seriesModel.copyNonNullProperties(seriesRequest);
                chartSeriesRepository.save(seriesModel);
                chartSeriesList.add(seriesModel);
            }
        }

        UUID origId = chartConfigModel.getId();
        chartConfigModel.copyNonNullProperties(queryConfigRequest);
        chartConfigModel.setId(origId);
        chartConfigModel.setFilter(DatasetFilterModelList);
        chartConfigModel.setSeries(chartSeriesList);
        chartConfigModel.setRowLimit(queryConfigRequest.getRowLimit());
        return chartConfigModel;
    }

    @Operation(summary = "Chart popover")
    @GetMapping("/popOverInfo/{id}")
    public ResponseEntity<Object> getPopOverInfo(Principal principal, @PathVariable("id") UUID chartId) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, chartId)) {
            throw new UnauthorizedException();
        }

        // TODO : add into views
        if (chartsRepository.findById(chartId).isEmpty()) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }

        if (!resourceService.existsById(chartId)) {
            return new ResponseEntity<>("Directory with Id " + chartId + " does not exist", HttpStatus.NOT_FOUND);
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chartId);
        ChartKey chartKey = new ChartKey(chartId, chartVersion.getLatestVersionId());
        ResourceModel resourceModel = resourceService.getResourceModel(chartId);
        ChartsModel chart = chartsRepository.getReferenceById(chartKey);

        List<ResourceModel> pathList = resourceService.getPathById(chartId, new ArrayList<>());
        Collections.reverse(pathList);
        String projectName = null;
        StringBuilder path = new StringBuilder("/Projects");

        for (ResourceModel resourceModel1 : pathList) {
            path.append("/").append(resourceModel1.getName());
            if (Objects.equals(resourceModel1.getParent(), null)) {
                projectName = resourceModel1.getName();
            }
        }

        ChartPopoverResponseModel responseModel = new ChartPopoverResponseModel();
        responseModel.setId(chartId);
        responseModel.setName(resourceModel.getName());
        responseModel.setPath(path.toString());
        responseModel.setProjectName(projectName);
        responseModel.setDatasetId(chart.getDatasetId());
        responseModel.setBranch(chart.getBranch());
        responseModel.setDescription(resourceModel.getDescription());

        return new ResponseEntity<>(responseModel, HttpStatus.OK);
    }

    @Operation(summary = "Chart version")
    @PostMapping("/createVersion/{id}/{name}")
    public ResponseEntity<Object> createVersion(Principal principal, @PathVariable("id") UUID id, @PathVariable("name") String versionName, @RequestParam(name = "v", required = false) Long versionId) {

        UUID userId = userService.getUser(principal.getName()).getId();


        if (!authzService.isEditor(userId, id)) {
            throw new UnauthorizedException();
        }

        ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(id);
        if (versionId == null) {
            versionId = chartVersion.getLatestVersionId();
        }
        ChartKey chartKey = new ChartKey(id, versionId);

        ChartsModel chart = chartsRepository.findById(chartKey).orElseThrow();
        Long futureVersionId = chartVersion.getLatestVersionId() + 1L;

        List<ResourceVersionDetailsModel> versions = chartVersion.getVersions();
        ResourceVersionDetailsModel newVersion = new ResourceVersionDetailsModel();
        if (versionName != null) {
            newVersion.setName(versionName);
        } else {
            newVersion.setName("Version" + futureVersionId);
        }

        newVersion.setCreatedAt(new Date());
        newVersion.setCreatedBy(userId);
        newVersion.setResourceVersionsModel(chartVersion);
        newVersion.setVersionId(futureVersionId);
        versions.add(newVersion);
        resourceVersionDetailsRepository.save(newVersion);
        chartVersion.setVersions(versions);
        chartVersion.setLatestVersionId(futureVersionId);
        resourceVersionsRepository.save(chartVersion);

        resourceService.getResourceModel(id);

        ChartsModel futureChart = new ChartsModel();
        futureChart.setId(chart.getId());
        futureChart.setVersionId(futureVersionId);
        futureChart.setLastVersionedDate(new Date());
        futureChart.setDatasetId(chart.getDatasetId());
        futureChart.setBranch(chart.getBranch());

        if (chart.getChartConfig() != null) {
            ChartConfigModel futureChartConfig = new ChartConfigModel(chart.getChartConfig());
            chartConfigRepository.save(futureChartConfig);
            futureChart.setChartConfig(futureChartConfig);
        }

        if (chart.getChartCustomize() != null) {
            String futureChartCustomize = chart.getChartCustomize();
//            chartCustomizeRepository.save(futureChartCustomize);
            futureChart.setChartCustomize(futureChartCustomize);
        }


        // Setting Dashboards
//        Set<DashboardsModel> dashboards = chart.getDashboard();
//        Set<DashboardsModel> futureDashboards = new HashSet<>();
//        for ()
        futureChart.setDashboard(chart.getDashboard());
        futureChart.setTabsForCharts(chart.getTabsForCharts());

        chartsRepository.save(futureChart);

        // Setting Tabs
        return new ResponseEntity<>(futureChart, HttpStatus.CREATED);
    }

    @Operation(summary = "This gives all the versioned models of the chart.")
    @GetMapping("/getVersions/{id}")
    public ResponseEntity<Object> getVersions(Principal principal, @PathVariable("id") UUID id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            throw new UnauthorizedException();
        }

        if (chartsRepository.findById(id).isEmpty()) {
            return new ResponseEntity<>("No chart found for given Id" + id, HttpStatus.NOT_FOUND);
        }
        if (resourceService.getResourceModel(id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>("Restore chart to access it." + id, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(chartsRepository.findById(id), HttpStatus.OK);

    }

}

