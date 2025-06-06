package io.bosler.bezier.controllers;

import io.bosler.bezier.library.models.PipelineModel;
import io.bosler.bezier.library.models.PipelineResponseModel;
import io.bosler.bezier.library.repository.PipelineRepository;
import io.bosler.bob.library.models.BuildSpecification;
import io.bosler.bob.library.models.BuildStageLog;
import io.bosler.bob.library.repository.BuildLogRepository;
import io.bosler.bob.library.repository.BuildSpecificationsRepository;
import io.bosler.bob.library.repository.BuildStageLogRepository;
import io.bosler.ignite.library.models.Sources;
import io.bosler.ignite.library.repository.LinkRepository;
import io.bosler.ignite.library.repository.SourcesRepository;
import io.bosler.kepler.library.models.ChartsModel;
import io.bosler.kepler.library.models.DashboardsModel;
import io.bosler.kepler.library.repository.ChartsRepository;
import io.bosler.kepler.library.repository.DashboardsRepository;
import io.bosler.kitab.library.models.BranchModel;
import io.bosler.kitab.library.models.DatasetStatsModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.repository.BranchRepository;
import io.bosler.kitab.library.repository.DatasetRepository;
import io.bosler.kitab.library.repository.DatasetStatsRepository;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.kitab.library.services.KitabService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedUtils.Response.OkResponse;
import io.bosler.synchro.library.repository.PostgresSyncRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.*;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/bezier")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bezier", description = "Pipeline management service endpoints")
public class BezierController {

    private final UserService userService;
    private final KitabService kitabService;
    private final PipelineRepository pipelineRepository;
    private final BranchRepository branchRepository;
    private final BuildLogRepository buildLogRepository;
    private final BuildStageLogRepository buildStageLogRepository;
    private final DatasetStatsRepository datasetStatsRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final DatasetRepository datasetRepository;
    private final FolderRepository folderRepository;
    private final ChartsRepository chartsRepository;
    private final SourcesRepository sourcesRepository;
    private final LinkRepository linkRepository;
    private final PostgresSyncRepository postgresSyncRepository;
    ;
    private final DashboardsRepository dashboardsRepository;
    private final OkResponse response = new OkResponse();

    private class Pair {
        UUID datasetId;
        String branch;

        Pair(UUID datasetId, String branch) {
            this.datasetId = datasetId;
            this.branch = branch;
        }
    }


//    @Operation(summary = "It provides pipeline to which dataset belongs.")
//    @GetMapping("/{datasetId}/{branch}/pipeline")
//    public ResponseEntity<Object> pipeline(
//            @PathVariable("datasetId") UUID datasetId,
//            @PathVariable("branch") String branch)
//            throws Exception {
//
//        // check if the dataset exists in catalog
//        if (!datasetRepository.existsById(datasetId)) {
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
//            DatasetModel model = datasetRepository.findById(visitedUUID).get();
////            model.setIdx(count++);
//            datasetRepository.save(model);
//            nodes.add(model);
//
//            for (UUID source : sources) {
//
//                link = new HashMap<>();
//                link.put("source", source);
//                link.put("target", visitedUUID);
//
//                if (false/*datasetRepository.findById(visitedUUID).get().isCollapsed()*/) {
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
//                if (false/*datasetRepository.findById(target).get().isCollapsed()*/) {
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
                if (folderRepository.existsById(cur_node)) { // no point going further if it does not exists in kitab
                    continue;
                }

                String repository = null;
                FolderModel folderModel = folderRepository.getReferenceById(cur_node);


                if (folderModel.getStatus().equals("inTrash")) {
                    continue;
                }

                List<UUID> nonActiveParents = new ArrayList<>();
                for (UUID parent : parents) {
                    if (!folderRepository.existsByIdAndStatus(parent, "active")) {
                        nonActiveParents.add(parent);
                    }
                }

                parents.removeAll(nonActiveParents);

                List<UUID> nonActiveChildren = new ArrayList<>();
                for (UUID child : children) {
                    if (!folderRepository.existsByIdAndStatus(child, "active")) {
                        nonActiveChildren.add(child);
                    }
                }

                children.removeAll(nonActiveChildren);


                if (Objects.equals(folderModel.getType(), "dataset")) {

                    boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(cur_node, branch);

                    String datasetType = null;
                    if (buildSpecificationExists) {
                        BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(cur_node, branch);
                        datasetType = buildSpecification.getLanguage();
                        repository = folderRepository.getReferenceById(buildSpecification.getRepository()).getName();
                    } else {

                        if (linkRepository.existsByDatasetIdAndBranch(cur_node, branch)) {
                            datasetType = "ignite";
                        } else {
                            datasetType = "uploaded";
                        }
                    }

                    boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(cur_node, branch);
                    List<BuildStageLog> buildStageLogs = buildStageLogRepository.findAllByDatasetIdAndBranchOrderByStartedAtDesc(cur_node, branch);

                    List<FolderModel> pathList = kitabService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (FolderModel folderModel1 : pathList) {
                        path.append("/").append(folderModel1.getName());
                        if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                            projectName = folderModel1.getName();
                        }
                    }

                    BranchModel model = branchRepository.findBranchModelByDatasetIdAndBranch(cur_node, branch);
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
                    res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                    if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(cur_node, branch)) {
                        DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(cur_node, branch);

                        res_model.setRows(datasetStatsModel.getRows());
                        res_model.setColumns(datasetStatsModel.getColumns());
                        res_model.setFiles(datasetStatsModel.getFiles());
                        res_model.setSize(datasetStatsModel.getSize());
                    }

                    if (!buildStageLogs.isEmpty()) {
                        res_model.setBuildStatus(buildStageLogs.get(0).getStatus());
                        if (buildStageLogs.get(0).getFinishedAt() != null) {
                            res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());
                        }
                    }

                    res_model.setSyncStatus(String.valueOf(postgresSyncStatus));

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);

                } else if (Objects.equals(folderModel.getType(), "chart")) {

                    List<FolderModel> pathList = kitabService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (FolderModel folderModel1 : pathList) {
                        path.append("/").append(folderModel1.getName());
                        if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                            projectName = folderModel1.getName();
                        }
                    }

                    ChartsModel model = chartsRepository.getReferenceById(cur_node);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getId());
                    res_model.setCreatedBy(model.getCreatedBy());
                    res_model.setCreatedAt(model.getCreatedAt());
                    res_model.setUpdatedBy(model.getUpdatedBy());
                    res_model.setUpdatedAt(model.getUpdatedAt());
                    res_model.setBranch(model.getChartConfig().getBranch());
                    res_model.setType("chart");
                    res_model.setSubType(model.getChartConfig().getChartId());
                    res_model.setPath(path.toString());
                    res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else if (Objects.equals(folderModel.getType(), "dashboard")) {

                    List<FolderModel> pathList = kitabService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (FolderModel folderModel1 : pathList) {
                        path.append("/").append(folderModel1.getName());
                        if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                            projectName = folderModel1.getName();
                        }
                    }

                    DashboardsModel model = dashboardsRepository.getReferenceById(cur_node);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getId());
                    res_model.setCreatedBy(model.getCreatedBy());
                    res_model.setCreatedAt(model.getCreatedAt());
                    res_model.setUpdatedBy(model.getUpdatedBy());
                    res_model.setUpdatedAt(model.getUpdatedAt());
                    res_model.setBranch(model.getBranch());
                    res_model.setType("dashboard");
                    res_model.setSubType("dashboard");
                    res_model.setPath(path.toString());
                    res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                    res_model.setTotalParents(parents.size());
                    res_model.setTotalChildren(children.size());

                    nodes.add(res_model);
                } else if (Objects.equals(folderModel.getType(), "source")) {

                    List<FolderModel> pathList = kitabService.getPathById(cur_node, new ArrayList<>());
                    Collections.reverse(pathList);
                    String projectName = null;
                    StringBuilder path = new StringBuilder("/Projects");
                    for (FolderModel folderModel1 : pathList) {
                        path.append("/").append(folderModel1.getName());
                        if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                            projectName = folderModel1.getName();
                        }
                    }

                    FolderModel parenFolder = folderRepository.getReferenceById(folderModel.getParent());

                    Sources model = sourcesRepository.getReferenceById(cur_node);
                    PipelineResponseModel res_model = new PipelineResponseModel();
                    res_model.setId(model.getId());
                    res_model.setCreatedBy(model.getCreatedBy());
                    res_model.setCreatedAt(model.getCreatedAt());
                    res_model.setUpdatedBy(model.getUpdatedBy());
                    res_model.setUpdatedAt(model.getUpdatedAt());
                    res_model.setBranch(null);

                    res_model.setType("source");
                    res_model.setPath(path.toString());
                    res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                    res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

//                    if (Objects.equals(model.getSourceConfig().get("type"), "jdbc")) {
//                        res_model.setSubType(model.getSourceConfig().get("jdbcType"));
//                    } else {
//                        res_model.setSubType(model.getSourceConfig().get("type"));
//                    }

                    res_model.setPath(path.toString());
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

            if (folderRepository.existsById(child)) { // no point going further if it does not exists in kitab
                continue;
            }

            assert datasetId != null;
            FolderModel folderModel = folderRepository.getReferenceById(child);

            Map<String, List<UUID>> sourcesAndTargetsChild = nodeExplorer(child);
            List<UUID> childrenChild = sourcesAndTargetsChild.get("targets");
            List<UUID> parentsChild = sourcesAndTargetsChild.get("sources");


            if (folderModel.getStatus().equals("inTrash")) {
                continue;
            }

            List<UUID> nonActiveParents = new ArrayList<>();
            for (UUID parent : parentsChild) {
                if (!folderRepository.existsByIdAndStatus(parent, "active")) {
                    nonActiveParents.add(parent);
                }
            }

            parentsChild.removeAll(nonActiveParents);

            List<UUID> nonActiveChildren = new ArrayList<>();
            for (UUID child1 : childrenChild) {
                if (!folderRepository.existsByIdAndStatus(child1, "active")) {
                    nonActiveChildren.add(child1);
                }
            }

            childrenChild.removeAll(nonActiveChildren);

            if (Objects.equals(folderModel.getType(), "dataset")) {

                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(child, branch);

                String datasetType;
                String repository = null;
                if (buildSpecificationExists) {
                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(child, branch);
                    datasetType = buildSpecification.getLanguage();

                    repository = folderRepository.getReferenceById(buildSpecification.getRepository()).getName();
                } else {
                    if (linkRepository.existsByDatasetIdAndBranch(child, branch)) {
                        datasetType = "ignite";
                    } else {
                        datasetType = "uploaded";
                    }
                }

                boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(child, branch);
                List<BuildStageLog> buildStageLogs = buildStageLogRepository.findAllByDatasetIdAndBranchOrderByStartedAtDesc(child, branch);

                List<FolderModel> pathList = kitabService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                BranchModel model = branchRepository.findBranchModelByDatasetIdAndBranch(child, branch);
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
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(child, branch)) {
                    DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(child, branch);

                    res_model.setRows(datasetStatsModel.getRows());
                    res_model.setColumns(datasetStatsModel.getColumns());
                    res_model.setFiles(datasetStatsModel.getFiles());
                    res_model.setSize(datasetStatsModel.getSize());
                }

                if (!buildStageLogs.isEmpty()) {
                    res_model.setBuildStatus(buildStageLogs.get(0).getStatus());
                    if (buildStageLogs.get(0).getFinishedAt() != null) {
                        res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());
                    }
                }

                res_model.setSyncStatus(String.valueOf(postgresSyncStatus));


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(folderModel.getType(), "chart")) {

                List<FolderModel> pathList = kitabService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                ChartsModel model = chartsRepository.getReferenceById(child);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(model.getChartConfig().getBranch());
                res_model.setType("chart");
                res_model.setSubType(model.getChartConfig().getChartId());
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(folderModel.getType(), "dashboard")) {

                List<FolderModel> pathList = kitabService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                DashboardsModel model = dashboardsRepository.getReferenceById(child);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch("master");
                res_model.setType("dashboard");
                res_model.setSubType("dashboard");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);
            } else if (Objects.equals(folderModel.getType(), "source")) {

                List<FolderModel> pathList = kitabService.getPathById(child, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                FolderModel parenFolder = folderRepository.getReferenceById(folderModel.getParent());

                Sources model = sourcesRepository.getReferenceById(child);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(null);
                res_model.setType("source");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

//                if (Objects.equals(model.getSourceConfig().get("type"), "jdbc")) {
//                    res_model.setSubType(model.getSourceConfig().get("jdbcType"));
//                } else {
//                    res_model.setSubType(model.getSourceConfig().get("type"));
//                }


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else {
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

            if (folderRepository.existsById(parent)) { // no point going further if it does not exists in kitab
                continue;
            }

            assert datasetId != null;
            FolderModel folderModel = folderRepository.getReferenceById(parent);

            if (folderModel.getStatus().equals("inTrash")) {
                continue;
            }

            List<UUID> nonActiveParents = new ArrayList<>();
            for (UUID parent1 : parentsChild) {
                if (!folderRepository.existsByIdAndStatus(parent1, "active")) {
                    nonActiveParents.add(parent1);
                }
            }

            parentsChild.removeAll(nonActiveParents);

            List<UUID> nonActiveChildren = new ArrayList<>();
            for (UUID child1 : childrenChild) {
                if (!folderRepository.existsByIdAndStatus(child1, "active")) {
                    nonActiveChildren.add(child1);
                }
            }

            childrenChild.removeAll(nonActiveChildren);


            if (Objects.equals(folderModel.getType(), "dataset")) {

                boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(parent, branch);

                String datasetType;
                String repository = null;
                if (buildSpecificationExists) {
                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(parent, branch);
                    datasetType = buildSpecification.getLanguage();

                    repository = folderRepository.getReferenceById(buildSpecification.getRepository()).getName();
                } else {
                    if (linkRepository.existsByDatasetIdAndBranch(parent, branch)) {
                        datasetType = "ignite";
                    } else {
                        datasetType = "uploaded";
                    }
                }

                boolean postgresSyncStatus = postgresSyncRepository.existsByDatasetIdAndBranch(parent, branch);
                List<BuildStageLog> buildStageLogs = buildStageLogRepository.findAllByDatasetIdAndBranchOrderByStartedAtDesc(parent, branch);

                List<FolderModel> pathList = kitabService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                BranchModel model = branchRepository.findBranchModelByDatasetIdAndBranch(parent, branch);
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
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

                if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(parent, branch)) {
                    DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(parent, branch);

                    res_model.setRows(datasetStatsModel.getRows());
                    res_model.setColumns(datasetStatsModel.getColumns());
                    res_model.setFiles(datasetStatsModel.getFiles());
                    res_model.setSize(datasetStatsModel.getSize());
                }

                if (!buildStageLogs.isEmpty()) {
                    res_model.setBuildStatus(buildStageLogs.get(0).getStatus());
                    if (buildStageLogs.get(0).getFinishedAt() != null) {
                        res_model.setBuildFinishedAt(buildStageLogs.get(0).getFinishedAt());
                    }
                }

                res_model.setSyncStatus(String.valueOf(postgresSyncStatus));


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(folderModel.getType(), "chart")) {

                List<FolderModel> pathList = kitabService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                ChartsModel model = chartsRepository.getReferenceById(parent);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(model.getChartConfig().getBranch());
                res_model.setType("chart");
                res_model.setSubType(model.getChartConfig().getChartId());
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

//                res_model.setTotalParents(parents.size());
//                res_model.setTotalChildren(children.size());
//
//                nodes.add(res_model);


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(folderModel.getType(), "dashboard")) {

                List<FolderModel> pathList = kitabService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                DashboardsModel model = dashboardsRepository.getReferenceById(parent);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch("master");
                res_model.setType("dashboard");
                res_model.setSubType("dashboard");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

//                res_model.setTotalParents(parents.size());
//                res_model.setTotalChildren(children.size());
//
//                nodes.add(res_model);


                res_model.setTotalParents(parentsChild.size());
                res_model.setTotalChildren(childrenChild.size());
                nodes.add(res_model);

            } else if (Objects.equals(folderModel.getType(), "source")) {

                List<FolderModel> pathList = kitabService.getPathById(parent, new ArrayList<>());
                Collections.reverse(pathList);
                String projectName = null;
                StringBuilder path = new StringBuilder("/Projects");
                for (FolderModel folderModel1 : pathList) {
                    path.append("/").append(folderModel1.getName());
                    if (Objects.equals(folderModel1.getParent(), new UUID(0, 0))) {
                        projectName = folderModel1.getName();
                    }
                }

                FolderModel parenFolder = folderRepository.getReferenceById(folderModel.getParent());

                Sources model = sourcesRepository.getReferenceById(parent);
                PipelineResponseModel res_model = new PipelineResponseModel();
                res_model.setId(model.getId());
                res_model.setCreatedBy(model.getCreatedBy());
                res_model.setCreatedAt(model.getCreatedAt());
                res_model.setUpdatedBy(model.getUpdatedBy());
                res_model.setUpdatedAt(model.getUpdatedAt());
                res_model.setBranch(null);
                res_model.setType("source");
                res_model.setPath(path.toString());
                res_model.setProjectName(projectName);
                res_model.setParentFolder(folderRepository.getReferenceById(folderModel.getParent()).getName());

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
//
//        /**git add .
//     *
//     * @param datasetId{UUID of the dataset}
//     * @return Map
//     * @throws Exception
//     * Helper method that provides list of sources and targets of given dataset.
//     */
//
//
//    private Map<String, List<Pair>> nodeExplorer2(Pair pair) throws Exception {
//
//        Map<String, List<Pair>> uuids = new HashMap<>();
//        List<Pair> sources = new ArrayList<>();
//        List<Pair> targets = new ArrayList<>();
//        List<PipelineModel> all = pipelineRepository.findAll();
//
//        for (PipelineModel pipelineModel : all) {
//            if (pipelineModel.sourceDataset.equals(pair.datasetId) && pipelineModel.sourceBranch.equals(pair.branch)) {
//                targets.add(new Pair(pipelineModel.targetDataset, pipelineModel.targetBranch));
//            }
//            if (pipelineModel.targetDataset.equals(pair.datasetId) && pipelineModel.targetBranch.equals(pair.branch)) {
//                sources.add(new Pair(pipelineModel.sourceDataset, pipelineModel.sourceBranch));
//            }
//        }
//        uuids.put("sources", sources);
//        uuids.put("targets", targets);
//
//        return uuids;
//    }

    @Operation(summary = "It deletes link between two datasets.")
    @PostMapping("/resolveBezierLinks")
    public ResponseEntity<Object> resolveLinks(Principal principal, @RequestBody PipelineModel resolveLinksBody) throws Exception {
        try {
            UUID userId = userService.getUser(principal.getName()).id;

            System.out.println("debug branches");
            System.out.println(resolveLinksBody.toString());
            System.out.println("debug branches");

            if (!branchRepository.existsByDatasetIdAndBranch(resolveLinksBody.getSourceDataset(), resolveLinksBody.getSourceBranch())) { // check if the dataset exists in catalog
                return new ResponseEntity<>("No source dataset found in catalog with id : " + resolveLinksBody.getSourceDataset(), HttpStatus.NOT_FOUND);
            }
            if (!branchRepository.existsByDatasetIdAndBranch(resolveLinksBody.getTargetDataset(), resolveLinksBody.getTargetBranch())) { // check if the dataset exists in catalog
                return new ResponseEntity<>("No target dataset found in catalog with id : " + resolveLinksBody.getTargetDataset(), HttpStatus.NOT_FOUND);
            }

            // TODO : remove below logic of delete links, its a big bang approach, needs to do it more intelligently
            List<PipelineModel> deleteLinks = pipelineRepository.findAllByRepositoryIdAndRepositoryBranchAndScriptPathAndBuildIdNot(
                    resolveLinksBody.getRepositoryId(),
                    resolveLinksBody.getRepositoryBranch(),
                    resolveLinksBody.getScriptPath(),
                    resolveLinksBody.getBuildId()
            );

            pipelineRepository.deleteAll(deleteLinks);


            // TODO: Try to shorten the JPA Query using some logic
            List<PipelineModel> modifyLinks = pipelineRepository.findAllByRepositoryIdAndRepositoryBranchAndScriptPathAndSourceDatasetAndSourceBranchAndTargetDatasetAndTargetBranch(
                    resolveLinksBody.getRepositoryId(),
                    resolveLinksBody.getRepositoryBranch(),
                    resolveLinksBody.getScriptPath(),
                    resolveLinksBody.getSourceDataset(),
                    resolveLinksBody.getSourceBranch(),
                    resolveLinksBody.getTargetDataset(),
                    resolveLinksBody.getTargetBranch()
            );

            if (!modifyLinks.isEmpty())
                for (PipelineModel modifyLink : modifyLinks) {
                    modifyLink.setUpdatedAt(new Date());
                    modifyLink.setUpdatedBy(userId);
                    pipelineRepository.saveAndFlush(modifyLink);
                }
            else {
                PipelineModel model = new PipelineModel();
                model.sourceDataset = resolveLinksBody.getSourceDataset();
                model.targetDataset = resolveLinksBody.getTargetDataset();
                model.sourceBranch = resolveLinksBody.getSourceBranch();
                model.targetBranch = resolveLinksBody.getTargetBranch();
                model.repositoryId = resolveLinksBody.getRepositoryId();
                model.repositoryBranch = resolveLinksBody.getRepositoryBranch();
                model.scriptPath = resolveLinksBody.getScriptPath();
                model.buildId = resolveLinksBody.getBuildId();
                model.status = "active";
                model.type = "dataset";
                model.setCreatedBy(userId);
                model.setUpdatedBy(userId);
                pipelineRepository.saveAndFlush(model);
            }
            return new ResponseEntity<>("Links resolved Successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.", HttpStatus.BAD_REQUEST);

        }
    }

    @Operation(summary = "get target dataset and branch by source")
    @GetMapping("/{sourceId}/{sourceBranch}/getTarget")
    ResponseEntity<Object> getTargetFromSource(Principal principal, @PathVariable("sourceId") UUID sourceId, @PathVariable("sourceBranch") String sourceBranch) {
        try {
            UUID userId = userService.getUser(principal.getName()).id;

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

