package io.movetodata.kepler.controllers;

import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.*;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import io.movetodata.kitab.library.services.KitabService;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.zoro.controllers.dataset1Controller;
import io.movetodata.zoro.library.models.QueryConfigRequest;
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
import java.util.*;

import static io.movetodata.sharedUtils.Redis.deleteCache;


@RestController
@RequestMapping("/api/kepler/charts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler charts management service endpoints")
public class ChartsController {

    private final UserService userService;
    private final ChartsRepository chartsRepository;
    private final TabsRepository tabsRepository;
    private final DashboardsRepository dashboardsRepository;
    private final FolderRepository folderRepository;
    private final PipelineRepository pipelineRepository;
    private final DatasetRepository datasetRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final AuthzService authzService;
    private final KitabService kitabService;


    private final ChartMetricRepository chartMetricRepository;
    private final ChartFilterRepository chartFilterRepository;
    private final ChartConfigRepository chartConfigRepository;

    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Get charts")
    @GetMapping("/getCharts/{tabId}")
    public ResponseEntity<Object> getCharts(Principal principal, @PathVariable("tabId") UUID tabId) {

        UUID userId = userService.getUser(principal.getName()).id;
        List<ChartsModel> allCharts = chartsRepository.findAll();
        Set<ChartsModel> tabCharts = tabsRepository.getById(tabId).getChartsForTabs();

        List<ChartsModel> charts = new ArrayList<>();
        for (ChartsModel chart : allCharts) {
            if(folderRepository.existsById(chart.getId())) {
                if (!authzService.isViewer(userId, chart.getId())) {
                    continue;
                }
                if (!tabCharts.contains(chart)) {
                    charts.add(chart);
                }
            }
        }

        return ResponseEntity.accepted().body(charts);
    }

    @Operation(summary = "Get all charts")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllCharts(Principal principal) {

        List<ChartsModel> charts = chartsRepository.findAll();

        return ResponseEntity.accepted().body(charts);
    }

    @Operation(summary = "This can be used to search charts based in dashboardId.")
    @GetMapping("/getChartsByDashboardId/{id}")
    public ResponseEntity<Object> getChartsByDashboardId(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        // TODO : add into views

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>("No dashboard found for given Id", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(chartsRepository.findAllByDashboardId(id), HttpStatus.OK);

    }

    @Operation(summary = "This can be used to search charts.")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getChartById(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        // TODO : add into views

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(chartsRepository.findById(id), HttpStatus.OK);
    }

    @Operation(summary = "It provides chart by Dataset Id and branch.")
    @GetMapping("/getByDatasetIdAndBranch/{datasetId}/{branch}")
    public ResponseEntity<Object> getDatasetIdAndBranch(Principal principal,
                                                        @PathVariable("datasetId") UUID datasetId,
                                                        @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(chartsRepository.findByDatasetIdAndBranch(datasetId, branch), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create charts.")
    @PostMapping("/new")
    public ResponseEntity<Object> newCharts(Principal principal, @RequestBody ChartRequest chartRequest) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!folderRepository.existsById(chartRequest.getParent())) {
            return new ResponseEntity<>("Parent with Id " + chartRequest.getParent() + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (!folderRepository.existsById(chartRequest.getChartConfig().getDatasetId())) {
            return new ResponseEntity<>("Dataset with Id " + chartRequest.getChartConfig().getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (!authzService.isOwner(userId, chartRequest.getParent())) {
            return new ResponseEntity<>("Access Denied to " + chartRequest.getParent(), HttpStatus.FORBIDDEN);
        }

        // Create in kitab
        FolderModel folderModel = new FolderModel();
        folderModel.setName(chartRequest.getName());
        folderModel.setDescription(chartRequest.getDescription());
        folderModel.setParent(chartRequest.getParent());
        folderModel.setStatus("active");
        folderModel.setType("chart");
        folderModel.setCreatedBy(userId);
        folderModel.setCreatedAt(new Date());

        FolderModel folderModel1 = folderRepository.save(folderModel);


        ChartCustomizeModel chartCustomizeModel = new ChartCustomizeModel();
        if(chartRequest.getChartCustomize() != null) {
            chartCustomizeModel = constructChartCustomizeModelFromRequest(chartRequest.getChartCustomize());
        }

        ChartConfigModel chartConfigModel = new ChartConfigModel();

        if(chartRequest.getChartConfig() != null) {
            chartConfigModel = constructChartConfigModelFromRequest(chartRequest.getChartConfig(), chartMetricRepository, chartFilterRepository);
        }

        chartConfigRepository.save(chartConfigModel);

        ChartsModel chartsModel = new ChartsModel();
        chartsModel.setId(folderModel1.getId());
        chartsModel.setName(chartRequest.getName());
        chartsModel.setDescription(chartRequest.getDescription());
        chartsModel.setParent(chartRequest.getParent());
        chartsModel.setName(chartRequest.getName());
        chartsModel.setChartConfig(chartConfigModel);
        chartsModel.setChartCustomize(chartCustomizeModel);
        chartsModel.setDatasetId(chartRequest.getDatasetId());
        chartsModel.setBranch(chartRequest.getBranch());
        chartsModel.setUpdatedAt(new Date());
        chartsModel.setUpdatedBy(userId);
        chartsModel.setCreatedAt(new Date());
        chartsModel.setCreatedBy(userId);

        ChartsModel chartsModelSaved = chartsRepository.save(chartsModel);

        // Create pipeline
        PipelineModel model = new PipelineModel();
        model.sourceDataset = chartsModelSaved.getChartConfig().getDatasetId();
        model.targetDataset = chartsModelSaved.getId();
        model.sourceBranch = chartsModelSaved.getChartConfig().getBranch();
        model.targetBranch = chartsModelSaved.getChartConfig().getBranch();
        model.repositoryId = null;
        model.repositoryBranch = null;
        model.scriptPath = null;
        model.buildId = null;
        model.status = "active";
        model.type = "chart";
        model.setCreatedBy(userId);
        model.setUpdatedBy(userId);
        pipelineRepository.saveAndFlush(model);

        // Removed because it is not needed after kepler moved to Spark : Add Synchro postgres sync if chart created
//        if (!postgresSyncRepository.existsByDatasetIdAndBranch(chartRequest.getChartConfig().getDatasetId(), chartRequest.getChartConfig().getBranch())) {
//
//            PostgresSyncProperties postgresSyncProperties = new PostgresSyncProperties();
//
//            postgresSyncProperties.setDatasetId(chartRequest.getChartConfig().getDatasetId());
//            postgresSyncProperties.setBranch(chartRequest.getChartConfig().getBranch());
//            postgresSyncProperties.setTableName(chartRequest.getName());
//            postgresSyncProperties.setEnabled(true);
//            postgresSyncProperties.setIndexNames(new ArrayList<>());
//
//            synchroService.createSync(postgresSyncProperties, userId);
//
//            PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(chartRequest.getChartConfig().getDatasetId(), chartRequest.getChartConfig().getBranch());
//            if(postgresSyncSpecification.getId() != null) {
//                postgresSyncSpecification.setStartedAt(new Date());
//                postgresSyncSpecification.setSyncStatus("active");
//
//                postgresSyncRepository.save(postgresSyncSpecification);
//
//
//                // Sending to socket
//                SocketMessage textMessage = new SocketMessage();
//                textMessage.setMessage("active");
//
//                template.convertAndSend("/topic/postgresSync/" + chartRequest.getChartConfig().getDatasetId() + "/" + chartRequest.getChartConfig().getBranch(), textMessage);
//
//                synchroService.performSync(userId, chartRequest.getChartConfig().getDatasetId(), chartRequest.getChartConfig().getBranch());
//            }
//        }
        // PG Sync End


        return new ResponseEntity<>(chartsModelSaved, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to save charts.")
    @PutMapping("/update/{Id}")
    public ResponseEntity<Object> updateChart(Principal principal, @RequestBody ChartRequest chartRequest, @PathVariable("Id") UUID Id) throws Exception {
        if (!chartsRepository.existsById(Id)) {
            return new ResponseEntity<>("Chart with Id " + Id + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (chartRequest.getChartConfig().getDatasetId() != null) {
            if (!datasetRepository.existsById(chartRequest.getChartConfig().getDatasetId())) {
                return new ResponseEntity<>("Dataset with Id " + chartRequest.getChartConfig().getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);
            }
        }

        if (chartRequest.getChartConfig().getBranch() == null) {
            return new ResponseEntity<>("Branch is not defined.", HttpStatus.BAD_REQUEST);
        }

        if (chartRequest.getParent() != null) {
            if (!folderRepository.existsById(chartRequest.getParent())) {
                return new ResponseEntity<>("Parent with Id " + chartRequest.getParent() + " does not exist", HttpStatus.NOT_FOUND);
            }
        }

        UUID userId = userService.getUser(principal.getName()).id;

        if (chartRequest.getParent() != null) {
            if (!authzService.isOwner(userId, chartRequest.getParent())) {
                return new ResponseEntity<>("Access Denied to " + chartRequest.getParent(), HttpStatus.FORBIDDEN);
            }
        }

        if (!chartsRepository.existsById(Id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }

        ChartsModel chartsModelExisting = chartsRepository.getById(Id);

        if (!authzService.isOwner(userId, chartsModelExisting.getParent())) {
            return new ResponseEntity<>("Access Denied to " + chartsModelExisting.getParent(), HttpStatus.FORBIDDEN);
        }

        ChartCustomizeModel chartCustomizeModel = new ChartCustomizeModel();
        if(chartRequest.getChartCustomize() != null) {
            chartCustomizeModel = constructChartCustomizeModelFromRequest(chartRequest.getChartCustomize());
        }

        if(chartRequest.getChartConfig() != null) {
            ChartConfigModel ccm = constructChartConfigModelFromRequest(chartRequest.getChartConfig(), chartMetricRepository, chartFilterRepository);
            chartConfigRepository.save(ccm);

            if(chartRequest.getChartConfig() != null) {
                dataset1Controller.setSQLConfigDataCache(Id,"chartData" + chartRequest.getDatasetId() + chartRequest.getBranch() + Id);
                System.out.println("setting Chart Cache :: " + "chartData" + chartRequest.getDatasetId() + chartRequest.getBranch() + Id);
            }
            chartsModelExisting.setChartConfig(ccm);
        }

        chartsModelExisting.setName(chartRequest.getName());
        chartsModelExisting.setDescription(chartRequest.getDescription());
        chartsModelExisting.setParent(chartRequest.getParent());
        chartsModelExisting.setDatasetId(chartRequest.getDatasetId());
        chartsModelExisting.setBranch(chartRequest.getBranch());
        chartsModelExisting.setChartCustomize(chartCustomizeModel);
        chartsModelExisting.setUpdatedAt(new Date());
        chartsModelExisting.setUpdatedBy(userId);

        ChartsModel chartsModelSaved = chartsRepository.save(chartsModelExisting);

        FolderModel folderModel = folderRepository.getById(chartsModelExisting.getId());
        folderModel.setName(chartRequest.getName());
        folderModel.setDescription(chartRequest.getDescription());
        folderModel.setParent(chartRequest.getParent());
        folderModel.setUpdatedAt(new Date());
        folderModel.setUpdatedBy(userId);

        folderRepository.save(folderModel);

        return new ResponseEntity<Object>(chartsModelSaved, HttpStatus.OK);
    }

    @Operation(summary = "This can be used to save charts/filters.")
    @PostMapping("/{id}/filters/save")
    public ResponseEntity<Object> saveFilter(Principal principal, @PathVariable("id") UUID id, @RequestBody HashMap<String, Object> filters) throws Exception {

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("Chart with Id " + id + " does not exist", HttpStatus.NOT_FOUND);

        }

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        ChartsModel chartsModelExisting = chartsRepository.getById(id);

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }

        chartsModelExisting.setUpdatedAt(new Date());
        chartsModelExisting.setUpdatedBy(userId);

//        chartsModelExisting.setFilters(filters);

        return new ResponseEntity<Object>(chartsRepository.save(chartsModelExisting), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to delete charts.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteChart(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        ChartsModel chartsModelExisting = chartsRepository.getById(id);

        if(chartsModelExisting.getChartConfig() != null) {
            deleteCache("chartData" + chartsModelExisting.getDatasetId() + chartsModelExisting.getBranch() + id);
            System.out.println("deleting Chart Cache :: " + "chartData" + chartsModelExisting.getDatasetId() + chartsModelExisting.getBranch() + id);
        }


        // delete from pipeline
        ChartsModel chartsModel = chartsRepository.getById(id);

        pipelineRepository.deleteByTargetDatasetAndTargetBranch(chartsModel.getId(), chartsModel.getBranch());

        // delete chart
        chartsRepository.deleteById(id);

        // delete from kitab also
        folderRepository.deleteById(id);



        return new ResponseEntity<>("Chart deleted successfully", HttpStatus.OK);
    }

    @Operation(summary = "This can be used to rename charts.")
    @GetMapping("/{Id}/{newName}/rename")
    public ResponseEntity<Object> renameChart(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        ChartsModel chartsModel = chartsRepository.getById(id);

        chartsModel.setName(newName);

        chartsRepository.save(chartsModel);

        if (folderRepository.existsById(id)) {
            FolderModel folderModel = folderRepository.getById(id);
            folderModel.setName(newName);

            folderRepository.save(folderModel);
        }

        return new ResponseEntity<>("Chart renamed successfully", HttpStatus.OK);

    }

    @Operation(summary = "This can be used to change description of charts.")
    @GetMapping("/{Id}/{newDescription}/renameChartDescription")
    public ResponseEntity<Object> renameChartDescription(Principal principal, @PathVariable("Id") UUID id, @PathVariable("newDescription") String newDescription) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!chartsRepository.existsById(id)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, id)) {
            return new ResponseEntity<>("Access Denied to " + id, HttpStatus.FORBIDDEN);
        }

        ChartsModel chartsModel = chartsRepository.getById(id);

        chartsModel.setDescription(newDescription);

        chartsRepository.save(chartsModel);

        if (folderRepository.existsById(id)) {
            FolderModel folderModel = folderRepository.getById(id);
            folderModel.setDescription(newDescription);

            folderRepository.save(folderModel);
        }

        return new ResponseEntity<>("Chart Description changed successfully", HttpStatus.OK);
    }

    private static ChartCustomizeModel constructChartCustomizeModelFromRequest(ChartCustomizeRequest chartCustomizeRequest) {
        ChartCustomizeModel chartCustomizeModel = new ChartCustomizeModel();

        chartCustomizeModel.setColorScheme(chartCustomizeRequest.getColorScheme());
        chartCustomizeModel.setSubHeader(chartCustomizeRequest.getSubHeader());
        chartCustomizeModel.setBigNumberFontSize(chartCustomizeRequest.getBigNumberFontSize());
        chartCustomizeModel.setSubHeaderFontSize(chartCustomizeRequest.getSubHeaderFontSize());
        chartCustomizeModel.setXAxis(chartCustomizeRequest.getXAxis());
        chartCustomizeModel.setXAxisTitlePosition(chartCustomizeRequest.getXAxisTitlePosition());
        chartCustomizeModel.setXAxisTitleMargin(chartCustomizeRequest.getXAxisTitleMargin());
        chartCustomizeModel.setYAxis(chartCustomizeRequest.getYAxis());
        chartCustomizeModel.setYAxisTitlePosition(chartCustomizeRequest.getYAxisTitlePosition());
        chartCustomizeModel.setYAxisTitleMargin(chartCustomizeRequest.getYAxisTitleMargin());
        chartCustomizeModel.setDataZoom(chartCustomizeRequest.getDataZoom());
        chartCustomizeModel.setStackedBars(chartCustomizeRequest.getStackedBars());
        chartCustomizeModel.setShowLabel(chartCustomizeRequest.getShowLabel());
        chartCustomizeModel.setSortBars(chartCustomizeRequest.getSortBars());
        chartCustomizeModel.setLegend(chartCustomizeRequest.getLegend());
        chartCustomizeModel.setDonut(chartCustomizeRequest.getDonut());
        chartCustomizeModel.setLegendType(chartCustomizeRequest.getLegendType());
        chartCustomizeModel.setInnerRadius(chartCustomizeRequest.getInnerRadius());
        chartCustomizeModel.setOuterRadius(chartCustomizeRequest.getOuterRadius());
        chartCustomizeModel.setLineChartStyle(chartCustomizeRequest.getLineChartStyle());
        chartCustomizeModel.setGridMarginTop(chartCustomizeRequest.getGridMarginTop());
        chartCustomizeModel.setGridMarginRight(chartCustomizeRequest.getGridMarginRight());
        chartCustomizeModel.setGridMarginBottom(chartCustomizeRequest.getGridMarginBottom());
        chartCustomizeModel.setGridMarginLeft(chartCustomizeRequest.getGridMarginLeft());

        return chartCustomizeModel;
    }

    private static ChartConfigModel constructChartConfigModelFromRequest(QueryConfigRequest queryConfigRequest, ChartMetricRepository chartMetricRepository, ChartFilterRepository chartFilterRepository) {
        List<ChartMetricModel> chartMetricModelList = new ArrayList<>();
        if(queryConfigRequest.getMetric() != null)
            for(int i = 0; i < queryConfigRequest.getMetric().size(); i++) {
                ChartMetricModel cMM = new ChartMetricModel();
                cMM.setColumnName(queryConfigRequest.getMetric().get(i).getColumnName());
                cMM.setAggregate(queryConfigRequest.getMetric().get(i).getAggregate());
                chartMetricRepository.save(cMM);
                chartMetricModelList.add(cMM);
            }

        List<ChartFilterModel> chartFilterModelList = new ArrayList<>();
        if(queryConfigRequest.getFilter() != null) {
            for(int i = 0; i < queryConfigRequest.getFilter().size(); i++) {
                ChartFilterModel cFM = new ChartFilterModel();
                cFM.setColumnName(queryConfigRequest.getFilter().get(i).getColumnName());
                cFM.setOperator(queryConfigRequest.getFilter().get(i).getOperator());
                cFM.setFilterValue(queryConfigRequest.getFilter().get(i).getFilterValue());
                chartFilterRepository.save(cFM);
                chartFilterModelList.add(cFM);
            }
        }

        List<String> dimensions = new ArrayList<>();
        if(queryConfigRequest.getDimension() != null)
            dimensions.addAll(queryConfigRequest.getDimension());

        List<ChartMetricModel> chartMetricModelList_sort = new ArrayList<>();
        if(queryConfigRequest.getSortBy() != null)
            for(int i = 0; i < queryConfigRequest.getSortBy().size(); i++) {
                ChartMetricModel cMM = new ChartMetricModel();
                cMM.setColumnName(queryConfigRequest.getMetric().get(i).getColumnName());
                cMM.setAggregate(queryConfigRequest.getMetric().get(i).getAggregate());
                chartMetricRepository.save(cMM);
                chartMetricModelList_sort.add(cMM);
            }

        TimeModel timeModel = new TimeModel();
        if( queryConfigRequest.getTime() != null) {
            timeModel.setTimeColumn(queryConfigRequest.getTime().getTimeColumn());
            timeModel.setTimeGrain(queryConfigRequest.getTime().getTimeGrain());
            timeModel.setTimeRange(queryConfigRequest.getTime().getTimeRange());
        }

        ChartConfigModel chartConfigModel = new ChartConfigModel();
        chartConfigModel.setMetric(chartMetricModelList);
        chartConfigModel.setFilter(chartFilterModelList);
        chartConfigModel.setSortBy(chartMetricModelList_sort);
        chartConfigModel.setChartId(queryConfigRequest.getChartId());
        chartConfigModel.setDimension(dimensions);
        chartConfigModel.setDatasetId(queryConfigRequest.getDatasetId());
        chartConfigModel.setBranch(queryConfigRequest.getBranch());
        chartConfigModel.setRowSelect(queryConfigRequest.getRowSelect());
        chartConfigModel.setTime(timeModel);

        return chartConfigModel;
    }

    @Operation(summary = "Chart popover")
    @GetMapping("/popOverInfo/{id}")
    public ResponseEntity<Object> getPopOverInfo(Principal principal, @PathVariable("id") UUID chartId) {

        UUID userId = userService.getUser(principal.getName()).id;

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(chartId);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!authzService.isViewer(userId, chartId)) {
            return new ResponseEntity<>("Access Denied to " + chartId, HttpStatus.FORBIDDEN);
        }

        // TODO : add into views

        if (!chartsRepository.existsById(chartId)) {
            return new ResponseEntity<>("No chart found for given Id", HttpStatus.NOT_FOUND);
        }

        if (!folderRepository.existsById(chartId)) {
            return new ResponseEntity<>("Directory with Id " + chartId + " does not exist", HttpStatus.NOT_FOUND);
        }

        ChartsModel chart = chartsRepository.getById(chartId);

        List<FolderModel> pathList = kitabService.getPathById(chartId, new ArrayList<>());
        Collections.reverse(pathList);
        String projectName = null;
        StringBuilder path = new StringBuilder("/Projects");
        for(FolderModel folderModel1 : pathList) {
            path.append("/").append(folderModel1.getName());
            if(Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                projectName = folderModel1.getName();
            }
        }

        ChartPopoverResponseModel responseModel = new ChartPopoverResponseModel();
        responseModel.setId(chartId);
        responseModel.setName(chart.getName());
        responseModel.setPath(path.toString());
        responseModel.setProjectName(projectName);
        responseModel.setDatasetId(chart.getDatasetId());
        responseModel.setBranch(chart.getBranch());
        responseModel.setDescription(chart.getDescription());
        responseModel.setCreatedAt(chart.getCreatedAt());
        responseModel.setCreatedBy(chart.getCreatedBy());
        responseModel.setUpdatedAt(chart.getUpdatedAt());
        responseModel.setUpdatedBy(chart.getUpdatedBy());

        return new ResponseEntity<>(responseModel, HttpStatus.OK);
    }
}

