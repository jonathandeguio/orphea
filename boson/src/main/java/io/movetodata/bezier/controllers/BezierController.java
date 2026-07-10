package io.movetodata.bezier.controllers;

import io.movetodata.bezier.library.dto.ResolveBezierLinksDTO;
import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.models.PipelineResponseModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.bezier.library.services.BezierService;
import io.movetodata.build.library.models.BuildSpecification;
import io.movetodata.build.library.repository.BuildSpecificationsRepository;
import io.movetodata.connect.library.models.DatabaseSourceConfig;
import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.models.Source;
import io.movetodata.connect.library.repository.LinkRepository;
import io.movetodata.connect.library.repository.SourcesRepository;
import io.movetodata.connect.library.services.FolderSourceConfigService;
import io.movetodata.connect.library.services.SourceService;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.ChartsRepository;
import io.movetodata.kepler.library.repository.DashboardsRepository;
import io.movetodata.kepler.library.repository.ResourceVersionsRepository;
import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.DatasetStatsRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.*;

@Slf4j
@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/bezier")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bezier", description = "Pipeline management service endpoints")
public class BezierController {

    private final UserService userService;
    private final ResourceService resourceService;
    private final PipelineRepository pipelineRepository;
    private final io.movetodata.connect.library.services.DatabaseSourceConfigService databaseSourceConfigService;
    private final FolderSourceConfigService folderSourceConfigService;
    private final DatasetStatsRepository datasetStatsRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final DatasetRepository datasetRepository;
    private final ChartsRepository chartsRepository;
    private final SourcesRepository sourcesRepository;
    private final LinkRepository linkRepository;
    private final PostgresSyncRepository postgresSyncRepository;
    private final DashboardsRepository dashboardsRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final DatasetMappingRepository datasetMappingRepository;
    private final BezierService bezierService;
    private final SourceService sourceService;

    @Operation(summary = "Loads initial Layout of Bezier")
    @GetMapping("/{datasetId}/{branch}/getInitial")
    public ResponseEntity<Object> getInitial(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch)
            throws Exception {

        HashSet<UUID> visitedNodesSet = new HashSet<>();
        List<Object> nodes = new ArrayList<>();
        HashSet<Object> links = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        visitedNodesSet.add(datasetId);
        queue.add(datasetId);

        while (!queue.isEmpty()) {
            int count_at_cur_level = queue.size();

            while (count_at_cur_level > 0) {
                count_at_cur_level -= 1;
                UUID cur_node = queue.poll();

                Map<String, List<UUID>> sourcesAndTargets = nodeExplorer(cur_node);
                List<UUID> parents = sourcesAndTargets.get("sources");
                List<UUID> children = sourcesAndTargets.get("targets");

                assert cur_node != null;
                if (!resourceService.existsById(cur_node)) { // no point going further if it does not exists in kitab
                    continue;
                }

                String repository = null;
                ResourceModel resourceModel = resourceService.getResourceModel(cur_node);


                if (resourceModel.getStatus().equals("inTrash")) {
                    continue;
                }

                List<UUID> nonActiveParents = new ArrayList<>();
                for (UUID parent : parents) {
                    if (!resourceService.existsByIdAndStatus(parent, ResourceStatus.ACTIVE)) {
                        nonActiveParents.add(parent);
                    }
                }

                parents.removeAll(nonActiveParents);

                List<UUID> nonActiveChildren = new ArrayList<>();
                for (UUID child : children) {
                    if (!resourceService.existsByIdAndStatus(child, ResourceStatus.ACTIVE)) {
                        nonActiveChildren.add(child);
                    }
                }

                children.removeAll(nonActiveChildren);


                if (Objects.equals(resourceModel.getType(), ResourceType.DATASET)) {
                    UUID transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(cur_node, branch)).getCurrentTransaction();
                    boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId);

                    String datasetType = null;
                    if (buildSpecificationExists) {
                        BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);
                        datasetType = String.valueOf(buildSpecification.getLanguage());
                        repository = resourceService.getResourceModel(buildSpecification.getRepository()).getName();
                    } else {

                        if (linkRepository.existsByDatasetIdAndBranch(cur_node, branch)) {
                            datasetType = "connect";
                        } else {
                            datasetType = "uploaded";
                        }
                    }

                    boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(cur_node, branch);

                    List<ResourceModel> pathList = resourceService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (ResourceModel resourceModel1 : pathList) {
                        path.append("/").append(resourceModel1.getName());
                        if (Objects.equals(resourceModel1.getParent(), null)) {
                            projectName = resourceModel1.getName();
                        }
                    }

                    DatasetModel datasetModel = datasetRepository.getById(cur_node);

                    Set<BranchModel> branches = datasetModel.getBranches();
                    // Here finding the branch model, as set takes O(n) to find, but the branches of a dataset will be very limited so no problem
                    // Didn't use hashmap because maybe in SB3.0 we face problem.
                    BranchModel model = null;
                    for (BranchModel datasetBranch : branches) {
                        if (datasetBranch.getBranch().equals(branch)) {
                            model = datasetBranch;
                            break;
                        }
                    }

                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getDatasetId());
                    res_model.setCreatedBy(model.getCreatedBy());
                    res_model.setCreatedAt(model.getCreatedAt());
                    res_model.setUpdatedBy(model.getUpdatedBy());
                    res_model.setUpdatedAt(model.getUpdatedAt());
                    res_model.setBranch(model.getBranch());
                    res_model.setType("dataset");
                    res_model.setSubType(datasetType);
                    res_model.setRepository(repository);
                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());
                    res_model.setBranches(branches);

                    if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(cur_node, branch)) {
                        DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(cur_node, branch);

                        res_model.setRows(datasetStatsModel.getRows());
                        res_model.setColumns(datasetStatsModel.getColumns());
                        res_model.setFiles(datasetStatsModel.getFiles());
                        res_model.setSize(datasetStatsModel.getSize());
                    }
                    // TODO : BUILD
//                    res_model.setBuildStatus(buildStageLogs.get(0).getStageStatus());
//                    res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());


                    res_model.setSyncStatus(String.valueOf(postgresSyncStatus));

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);

                } else if (Objects.equals(resourceModel.getType(), ResourceType.CHART)) {

                    List<ResourceModel> pathList = resourceService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (ResourceModel resourceModel1 : pathList) {
                        path.append("/").append(resourceModel1.getName());
                        if (Objects.equals(resourceModel1.getParent(), null)) {
                            projectName = resourceModel1.getName();
                        }
                    }

                    ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(cur_node);
                    ChartKey chartKey = new ChartKey(cur_node, chartVersion.getLatestVersionId());

                    ChartsModel model = chartsRepository.getReferenceById(chartKey);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getId());
                    res_model.setCreatedBy(resourceModel.getCreatedBy());
                    res_model.setCreatedAt(resourceModel.getCreatedAt());
                    res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                    res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                    res_model.setBranch(model.getChartConfig().getBranch());
                    res_model.setType("chart");
                    res_model.setSubType(model.getChartConfig().getChartType());
                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else if (Objects.equals(resourceModel.getType(), ResourceType.DASHBOARD)) {

                    List<ResourceModel> pathList = resourceService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (ResourceModel resourceModel1 : pathList) {
                        path.append("/").append(resourceModel1.getName());
                        if (Objects.equals(resourceModel1.getParent(), null)) {
                            projectName = resourceModel1.getName();
                        }
                    }

                    ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(cur_node);
                    Long versionId = dashboardVersion.getLatestVersionId();
                    DashboardKey dashboardId = new DashboardKey(cur_node, versionId);

                    DashboardsModel model = dashboardsRepository.getReferenceById(dashboardId);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getId());
                    res_model.setCreatedBy(resourceModel.getCreatedBy());
                    res_model.setCreatedAt(resourceModel.getCreatedAt());
                    res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                    res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                    res_model.setBranch(model.getBranch());
                    res_model.setType("dashboard");
                    res_model.setSubType("dashboard");
                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else if (Objects.equals(resourceModel.getType(), ResourceType.SOURCE)) {

                    List<ResourceModel> pathList = resourceService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (ResourceModel resourceModel1 : pathList) {
                        path.append("/").append(resourceModel1.getName());
                        if (Objects.equals(resourceModel1.getParent(), null)) {
                            projectName = resourceModel1.getName();
                        }
                    }

                    ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());

                    Source sourceModel = sourcesRepository.getReferenceById(cur_node);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(sourceModel.getId());
                    res_model.setCreatedBy(resourceModel.getCreatedBy());
                    res_model.setCreatedAt(resourceModel.getCreatedAt());
                    res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                    res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                    res_model.setBranch(null);

                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                    if (Objects.equals(sourceModel.getType(), "jdbc")) {
                        DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                        res_model.setType("source");
                        res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                    } else {
                        res_model.setType("source");
                        res_model.setSubType(sourceModel.getType()); // FIXME TODO
                    }

                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setProjectName(projectName);

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else if (Objects.equals(resourceModel.getType(), ResourceType.LINK)) {

                    List<ResourceModel> pathList = resourceService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (ResourceModel resourceModel1 : pathList) {
                        path.append("/").append(resourceModel1.getName());
                        if (Objects.equals(resourceModel1.getParent(), null)) {
                            projectName = resourceModel1.getName();
                        }
                    }

                    ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());

                    Link linkModel = linkRepository.getReferenceById(cur_node);
                    Source sourceModel = sourcesRepository.getReferenceById(linkModel.getSourceId());
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(linkModel.getId());
                    res_model.setCreatedBy(resourceModel.getCreatedBy());
                    res_model.setCreatedAt(resourceModel.getCreatedAt());
                    res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                    res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                    res_model.setBranch(null);

                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                    if (Objects.equals(sourceModel.getType(), "jdbc")) {
                        DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                        res_model.setType("link");
                        res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                    } else {
                        res_model.setType("link");
                        res_model.setSubType(sourceModel.getType()); // FIXME TODO
                    }

                    res_model.setPath(path.toString());
                    res_model.setProjectName(projectName);
                    res_model.setProjectName(projectName);

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else {
                    return new ResponseEntity<>("invalid type", HttpStatus.BAD_REQUEST);
                }


                for (UUID parent : parents) {
                    if (!visitedNodesSet.contains(parent)) {
                        visitedNodesSet.add(parent);
                        queue.add(parent);
                    }

                    HashMap<String, Object> link = new HashMap<>();
                    link.put("source", parent);
                    link.put("target", cur_node);
                    links.add(link);
                }

            }
        }

        Map<String, Object> initialLayout = new HashMap<>();
        initialLayout.put("nodes", nodes);
        initialLayout.put("links", links);
        return new ResponseEntity<>(initialLayout, HttpStatus.OK);
    }


//    @Operation(summary = "It provides pipeline to which dataset belongs.")
//    @GetMapping("/{datasetId}/{branch}/pipeline")
//    public ResponseEntity<Object> pipeline(
//            @PathVariable("datasetId") UUID datasetId,
//            @PathVariable("branch") String branch)
//            throws Exception {
//
//        // check if the dataset exists in catalog
//        if (!kitabDatasetRepository.existsById(datasetId)) {
//            return new ResponseEntity<>("No dataset found in catalog with id : " + datasetId, HttpStatus.NOT_FOUND);
//        }
//
//        List<DatasetModel> nodes = new ArrayList<>();
//        HashSet<Object> links = new HashSet<>();
//
//        Queue<UUID> queue = new LinkedList<>();
//        HashSet<UUID> visitedNodesSet = new HashSet<>();
//
//        visitedNodesSet.add(datasetId);
//        queue.add(datasetId);
//        HashMap<String, Object> link;
//        int count = 0;
//
//        while (!queue.isEmpty()) {
//
//            UUID visitedUUID = queue.poll();
//
//            Map<String, List<UUID>> sourcesAndTargets = nodeExplorer(visitedUUID);
//            List<UUID> sources = sourcesAndTargets.get("sources");
//            List<UUID> targets = sourcesAndTargets.get("targets");
//
//            DatasetModel model = kitabDatasetRepository.findById(visitedUUID).get();
////            model.setIdx(count++);
//            kitabDatasetRepository.save(model);
//            nodes.add(model);
//
//            for (UUID source : sources) {
//
//                link = new HashMap<>();
//                link.put("source", source);
//                link.put("target", visitedUUID);
//
//                if (false/*kitabDatasetRepository.findById(visitedUUID).get().isCollapsed()*/) {
//                    link.put("collapsedLink", true);
//                } else {
//                    link.put("collapsedLink", false);
//                }
//                links.add(link);
//
//                if (!visitedNodesSet.contains(source)) {
//                    visitedNodesSet.add(source);
//                    queue.add(source);
//                }
//            }
//
//            for (UUID target : targets) {
//
//                link = new HashMap<>();
//                link.put("source", visitedUUID);
//                link.put("target", target);
//                if (false/*kitabDatasetRepository.findById(target).get().isCollapsed()*/) {
//                    link.put("collapsedLink", true);
//                } else {
//                    link.put("collapsedLink", false);
//                }
//
//                links.add(link);
//
//                if (!visitedNodesSet.contains(target)) {
//                    visitedNodesSet.add(target);
//                    queue.add(target);
//                }
//            }
//        }
//
//        Map<String, Object> root = new HashMap<>();
//        root.put("nodes", nodes);
//        root.put("links", links);
//        return new ResponseEntity<>(root, HttpStatus.OK);
//    }

    @Operation(summary = "Expand the children of Bezier Node")
    @GetMapping("/{datasetId}/{branch}/getChildren")
    public ResponseEntity<Object> getChildren(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch)
            throws Exception {


        List<Object> nodes = new ArrayList<>();
        HashSet<Object> links = new HashSet<>();

        Map<String, List<UUID>> sourcesAndTargets = nodeExplorer(datasetId);
        List<UUID> children = sourcesAndTargets.get("targets");
        List<UUID> parents = sourcesAndTargets.get("sources");


        for (UUID child : children) {

            if (!resourceService.existsById(child)) { // no point going further if it does not exists in kitab
                continue;
            }

            assert datasetId != null;
            ResourceModel resourceModel = resourceService.getResourceModel(child);

            Map<String, List<UUID>> sourcesAndTargetsChild = nodeExplorer(child);
            List<UUID> childrenChild = sourcesAndTargetsChild.get("targets");
            List<UUID> parentsChild = sourcesAndTargetsChild.get("sources");


            if (resourceModel.getStatus().equals("inTrash")) {
                continue;
            }

            List<UUID> nonActiveParents = new ArrayList<>();
            for (UUID parent : parentsChild) {
                if (!resourceService.existsByIdAndStatus(parent, ResourceStatus.ACTIVE)) {
                    nonActiveParents.add(parent);
                }
            }

            parentsChild.removeAll(nonActiveParents);

            List<UUID> nonActiveChildren = new ArrayList<>();
            for (UUID child1 : childrenChild) {
                if (!resourceService.existsByIdAndStatus(child1, ResourceStatus.ACTIVE)) {
                    nonActiveChildren.add(child1);
                }
            }

            childrenChild.removeAll(nonActiveChildren);

            if (Objects.equals(resourceModel.getType(), ResourceType.DATASET)) {
                UUID transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(child, branch)).getCurrentTransaction();
                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId);

                String datasetType;
                String repository = null;
                if (buildSpecificationExists) {
                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);
                    datasetType = String.valueOf(buildSpecification.getLanguage());

                    repository = resourceService.getResourceModel(buildSpecification.getRepository()).getName();
                } else {
                    if (linkRepository.existsByDatasetIdAndBranch(child, branch)) {
                        datasetType = "connect";
                    } else {
                        datasetType = "uploaded";
                    }
                }

                boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(child, branch);

                List<ResourceModel> pathList = resourceService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                DatasetModel datasetModel = datasetRepository.getById(child);

                Set<BranchModel> branches = datasetModel.getBranches();
                // Here finding the branch model, as set takes O(n) to find, but the branches of a dataset will be very limited so no problem
                // Didn't use hashmap because maybe in SB3.0 we face problem.
                BranchModel model = null;
                for (BranchModel datasetBranch : branches) {
                    if (datasetBranch.getBranch().equals(branch)) {
                        model = datasetBranch;
                        break;
                    }
                }
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getDatasetId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(model.getBranch());
                res_model.setType("dataset");
                res_model.setSubType(datasetType);
                res_model.setRepository(repository);
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());
                res_model.setBranches(branches);

                if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(child, branch)) {
                    DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(child, branch);

                    res_model.setRows(datasetStatsModel.getRows());
                    res_model.setColumns(datasetStatsModel.getColumns());
                    res_model.setFiles(datasetStatsModel.getFiles());
                    res_model.setSize(datasetStatsModel.getSize());
                }
                // TODO : BUILD
//                res_model.setBuildStatus(buildStageLogs.get(0).getStageStatus());
//                res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());
                res_model.setSyncStatus(String.valueOf(postgresSyncStatus));


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.CHART)) {

                List<ResourceModel> pathList = resourceService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(child);
                ChartKey chartKey = new ChartKey(child, chartVersion.getLatestVersionId());

                ChartsModel model = chartsRepository.getReferenceById(chartKey);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(model.getChartConfig().getBranch());
                res_model.setType("chart");
                res_model.setSubType(model.getChartConfig().getChartType());
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.DASHBOARD)) {

                List<ResourceModel> pathList = resourceService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(child);
                Long versionId = dashboardVersion.getLatestVersionId();
                DashboardKey dashboardId = new DashboardKey(child, versionId);

                DashboardsModel model = dashboardsRepository.getReferenceById(dashboardId);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);
                res_model.setType("dashboard");
                res_model.setSubType("dashboard");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);
            } else if (Objects.equals(resourceModel.getType(), ResourceType.SOURCE)) {

                List<ResourceModel> pathList = resourceService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());

                Source sourceModel = sourcesRepository.getReferenceById(child);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(sourceModel.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);

                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                if (Objects.equals(sourceModel.getType(), "jdbc")) {
                    DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                    res_model.setType("source");
                    res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                } else {
                    res_model.setType("source");
                    res_model.setSubType(sourceModel.getType()); // FIXME TODO
                }
//                if (Objects.equals(sourceModel.getSourceConfig().get("type"), "jdbc")) {
//                    res_model.setSubType(sourceModel.getSourceConfig().get("jdbcType"));
//                } else {
//                    res_model.setSubType(sourceModel.getSourceConfig().get("type"));
//                }


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.LINK)) {

                List<ResourceModel> pathList = resourceService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());
                Link linkModel = linkRepository.getReferenceById(child);
                Source sourceModel = sourcesRepository.getReferenceById(linkModel.getSourceId());
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(sourceModel.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);

                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                if (Objects.equals(sourceModel.getType(), "jdbc")) {
                    DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                    res_model.setType("link");
                    res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                } else {
                    res_model.setType("link");
                    res_model.setSubType(sourceModel.getType()); // FIXME TODO
                }
//                if (Objects.equals(sourceModel.getSourceConfig().get("type"), "jdbc")) {
//                    res_model.setSubType(sourceModel.getSourceConfig().get("jdbcType"));
//                } else {
//                    res_model.setSubType(sourceModel.getSourceConfig().get("type"));
//                }


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else {
                System.out.println(resourceModel.getType());
                throw new Exception("invalid type");
            }


            HashMap<String, Object> link = new HashMap<>();
            link.put("source", datasetId);
            link.put("target", child);
            links.add(link);
        }

        Map<String, Object> childrenLayout = new HashMap<>();
        childrenLayout.put("nodes", nodes);
        childrenLayout.put("links", links);
        return new ResponseEntity<>(childrenLayout, HttpStatus.OK);
    }

    @Operation(summary = "Expand the parents of Bezier Node")
    @GetMapping("/{datasetId}/{branch}/getParents")
    public ResponseEntity<Object> getParents(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("branch") String branch)
            throws Exception {


        List<Object> nodes = new ArrayList<>();
        HashSet<Object> links = new HashSet<>();

        Map<String, List<UUID>> sourcesAndTargets = nodeExplorer(datasetId);
        List<UUID> parents = sourcesAndTargets.get("sources");
        List<UUID> children = sourcesAndTargets.get("targets");


        for (UUID parent : parents) {

            Map<String, List<UUID>> sourcesAndTargetsChild = nodeExplorer(parent);
            List<UUID> childrenChild = sourcesAndTargetsChild.get("targets");
            List<UUID> parentsChild = sourcesAndTargetsChild.get("sources");

            if (!resourceService.existsById(parent)) { // no point going further if it does not exists in kitab
                continue;
            }

            assert datasetId != null;
            ResourceModel resourceModel = resourceService.getResourceModel(parent);

            if (resourceModel.getStatus().equals("inTrash")) {
                continue;
            }

            List<UUID> nonActiveParents = new ArrayList<>();
            for (UUID parent1 : parentsChild) {
                if (!resourceService.existsByIdAndStatus(parent1, ResourceStatus.ACTIVE)) {
                    nonActiveParents.add(parent1);
                }
            }

            parentsChild.removeAll(nonActiveParents);

            List<UUID> nonActiveChildren = new ArrayList<>();
            for (UUID child1 : childrenChild) {
                if (!resourceService.existsByIdAndStatus(child1, ResourceStatus.ACTIVE)) {
                    nonActiveChildren.add(child1);
                }
            }

            childrenChild.removeAll(nonActiveChildren);


            if (Objects.equals(resourceModel.getType(), ResourceType.DATASET)) {

                UUID transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(parent, branch)).getCurrentTransaction();
                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId);

                String datasetType;
                String repository = null;
                if (buildSpecificationExists) {
                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);
                    datasetType = String.valueOf(buildSpecification.getLanguage());

                    repository = resourceService.getResourceModel(buildSpecification.getRepository()).getName();
                } else {
                    if (linkRepository.existsByDatasetIdAndBranch(parent, branch)) {
                        datasetType = "connect";
                    } else {
                        datasetType = "uploaded";
                    }
                }

                boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(parent, branch);

                List<ResourceModel> pathList = resourceService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                DatasetModel datasetModel = datasetRepository.getById(parent);

                Set<BranchModel> branches = datasetModel.getBranches();
                // Here finding the branch model, as set takes O(n) to find, but the branches of a dataset will be very limited so no problem
                // Didn't use hashmap because maybe in SB3.0 we face problem.
                BranchModel model = null;
                for (BranchModel datasetBranch : branches) {
                    if (datasetBranch.getBranch().equals(branch)) {
                        model = datasetBranch;
                        break;
                    }
                }
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getDatasetId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(model.getBranch());
                res_model.setType("dataset");
                res_model.setSubType(datasetType);
                res_model.setRepository(repository);
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());
                res_model.setBranches(branches);

                if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(parent, branch)) {
                    DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(parent, branch);

                    res_model.setRows(datasetStatsModel.getRows());
                    res_model.setColumns(datasetStatsModel.getColumns());
                    res_model.setFiles(datasetStatsModel.getFiles());
                    res_model.setSize(datasetStatsModel.getSize());
                }
                // TODO : Build
//                res_model.setBuildStatus(buildStageLogs.get(0).getStageStatus());
//                res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());

                res_model.setSyncStatus(String.valueOf(postgresSyncStatus));
                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.CHART)) {

                List<ResourceModel> pathList = resourceService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(parent);
                ChartKey chartKey = new ChartKey(parent, chartVersion.getLatestVersionId());

                ChartsModel model = chartsRepository.getReferenceById(chartKey);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(model.getChartConfig().getBranch());
                res_model.setType("chart");
                res_model.setSubType(model.getChartConfig().getChartType());
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

//                res_model.setTotalParents(parents.size());
//                res_model.setTotalChildren(children.size());
//
//                nodes.add(res_model);


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.DASHBOARD)) {

                List<ResourceModel> pathList = resourceService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(parent);
                Long versionId = dashboardVersion.getLatestVersionId();
                DashboardKey dashboardId = new DashboardKey(parent, versionId);

                DashboardsModel model = dashboardsRepository.getReferenceById(dashboardId);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);
                res_model.setType("dashboard");
                res_model.setSubType("dashboard");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

//                res_model.setTotalParents(parents.size());
//                res_model.setTotalChildren(children.size());
//
//                nodes.add(res_model);


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.SOURCE)) {

                List<ResourceModel> pathList = resourceService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());

                Source sourceModel = sourcesRepository.getReferenceById(parent);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(sourceModel.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);

                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                if (Objects.equals(sourceModel.getType(), "jdbc")) {
                    DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                    res_model.setType("source");
                    res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                } else {
                    res_model.setType("source");
                    res_model.setSubType(sourceModel.getType()); // FIXME TODO
                }
//                if (Objects.equals(model.getSourceConfig().get("type"), "jdbc")) {
//                    res_model.setSubType(model.getSourceConfig().get("jdbcType"));
//                } else {
//                    res_model.setSubType(model.getSourceConfig().get("type"));
//                }


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(resourceModel.getType(), ResourceType.LINK)) {

                List<ResourceModel> pathList = resourceService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (ResourceModel resourceModel1 : pathList) {
                    path.append("/").append(resourceModel1.getName());
                    if (Objects.equals(resourceModel1.getParent(), null)) {
                        projectName = resourceModel1.getName();
                    }
                }

                ResourceModel parenFolder = resourceService.getResourceModel(resourceModel.getParent());
                Link linkModel = linkRepository.getReferenceById(parent);
                Source sourceModel = sourcesRepository.getReferenceById(linkModel.getSourceId());

                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(sourceModel.getId());
                res_model.setCreatedBy(resourceModel.getCreatedBy());
                res_model.setCreatedAt(resourceModel.getCreatedAt());
                res_model.setUpdatedBy(resourceModel.getUpdatedBy());
                res_model.setUpdatedAt(resourceModel.getUpdatedAt());
                res_model.setBranch(null);

                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(resourceService.getResourceModel(resourceModel.getParent()).getName());

                if (Objects.equals(sourceModel.getType(), "jdbc")) {
                    DatabaseSourceConfig databaseSourceConfig = databaseSourceConfigService.findById(sourceModel.getSourceConfig());
                    res_model.setType("link");
                    res_model.setSubType(String.valueOf(sourceService.getResourceType(databaseSourceConfig.getDbmsType())));
                } else {
                    res_model.setType("link");
                    res_model.setSubType(sourceModel.getType()); // FIXME TODO
                }
//                if (Objects.equals(model.getSourceConfig().get("type"), "jdbc")) {
//                    res_model.setSubType(model.getSourceConfig().get("jdbcType"));
//                } else {
//                    res_model.setSubType(model.getSourceConfig().get("type"));
//                }


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else {
                return new ResponseEntity<>("invalid type", HttpStatus.BAD_REQUEST);
            }


            HashMap<String, Object> link = new HashMap<>();
            link.put("source", parent);
            link.put("target", datasetId);
            links.add(link);

        }

        Map<String, Object> parentsLayout = new HashMap<>();
        parentsLayout.put("nodes", nodes);
        parentsLayout.put("links", links);
        return new ResponseEntity<>(parentsLayout, HttpStatus.OK);
    }

    /**
     * git add .
     *
     * @param datasetId{UUID of the dataset}
     * @return Map
     * @throws Exception Helper method that provides list of sources and targets of given dataset.
     */
    private Map<String, List<UUID>> nodeExplorer(UUID datasetId) throws Exception {

        Map<String, List<UUID>> uuids = new HashMap<>();
        List<UUID> sources = new ArrayList<>();
        List<UUID> targets = new ArrayList<>();
        List<PipelineModel> all = pipelineRepository.findAll();

        for (PipelineModel pipelineModel : all) {
            if (pipelineModel.sourceDataset.equals(datasetId)) {
                targets.add(pipelineModel.targetDataset);
            }
            if (pipelineModel.targetDataset.equals(datasetId)) {
                sources.add(pipelineModel.sourceDataset);
            }
        }
        uuids.put("sources", sources);
        uuids.put("targets", targets);

        return uuids;
    }

    @Operation(summary = "Resolves Bezier links for funnel / shyne")
    @PostMapping("/resolveBezierLinks")
    public ResponseEntity<Object> resolveLinks(Principal principal, @RequestBody ResolveBezierLinksDTO resolveLinksBody) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        bezierService.resolveBezierLinks(resolveLinksBody.getSourceDatasets(),
                resolveLinksBody.getTargetDataset(),
                resolveLinksBody.getRepositoryBranch(),
                UUID.fromString(resolveLinksBody.getRepositoryId()),
                resolveLinksBody.getScriptPath(),
                UUID.fromString(resolveLinksBody.getBuildId())
                , userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "get target dataset and branch by source")
    @GetMapping("/{sourceId}/{sourceBranch}/getTarget")
    ResponseEntity<Object> getTargetFromSource(Principal principal, @PathVariable("sourceId") UUID sourceId, @PathVariable("sourceBranch") String sourceBranch) {
        try {
            UUID user = userService.getUser(principal.getName()).getId();

            if (!datasetRepository.existsById(sourceId)) {
                return new ResponseEntity<>("Dataset with Id " + sourceId + " does not exist", HttpStatus.NOT_FOUND);
            }

            List<PipelineModel.TargetDatasetAndTargetBranch> targetDatasets = pipelineRepository.findAllBySourceDatasetAndSourceBranch(sourceId, sourceBranch);

            if (targetDatasets.isEmpty()) {
                return new ResponseEntity<>("No downstream dataset found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(targetDatasets, HttpStatus.OK);
            }

            // TODO: Authentication
//            if (!authzService.isEditor(user, sourceId)) {
//                return new ResponseEntity<>("Access Denied to " + sourceId, HttpStatus.FORBIDDEN);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.", HttpStatus.BAD_REQUEST);
        }
    }
}

