package io.movetodata.kitab.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.bob.library.models.BuildSpecification;
import io.movetodata.bob.library.repository.BuildLogRepository;
import io.movetodata.bob.library.repository.BuildSpecificationsRepository;
import io.movetodata.bob.library.repository.BuildStageLogRepository;
import io.movetodata.docket.library.models.Tags;
import io.movetodata.docket.library.models.TagsCategory;
import io.movetodata.docket.library.repository.TagCategoryRepository;
import io.movetodata.docket.library.repository.TagRepository;
import io.movetodata.fractal.library.models.GitCommit;
import io.movetodata.ignite.library.models.Agents;
import io.movetodata.ignite.library.models.Links;
import io.movetodata.ignite.library.models.Sources;
import io.movetodata.ignite.library.repository.AgentRepository;
import io.movetodata.ignite.library.repository.LinkRepository;
import io.movetodata.ignite.library.repository.SourcesRepository;
import io.movetodata.kepler.library.models.ChartsModel;
import io.movetodata.kepler.library.repository.ChartsRepository;
import io.movetodata.kepler.library.repository.DashboardsRepository;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.DatasetModel;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.*;
import io.movetodata.kitab.library.services.KitabService;
import io.movetodata.passport.library.models.*;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.PermissionMappingRepository;
import io.movetodata.passport.library.repository.RoleRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.scheduler.library.repository.SchedulerRepository;
import io.movetodata.sharedUtils.ActiveDisplay;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import io.movetodata.zoro.library.repository.SchemaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static io.movetodata.sharedUtils.BackingFS.deleteDatasetFiles;
import static io.movetodata.sharedUtils.Utils.isValidUUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class kitabController {

    private final UserService userService;
    private final GroupService groupService;
    private final KitabService kitabService;
    private final GroupsRepository groupsRepository;
    private final LinkRepository linkRepository;
    private final PipelineRepository pipelineRepository;
    private final AgentRepository agentRepository;
    private final SourcesRepository sourcesRepository;
    private final FolderRepository folderRepository;
    private final DashboardsRepository dashboardsRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final DatasetRepository datasetRepository;
    private final BranchRepository branchRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;
    private final PermissionMappingRepository permissionMappingRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ChartsRepository chartsRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final SchemaRepository schemaRepository;
    private final TransactionRepository transactionRepository;
    public final PostgresSyncRepository postgresSyncRepository;
    public final SchedulerRepository schedulerRepository;
    public final BuildLogRepository buildLogRepository;
    public final BuildStageLogRepository buildStageLogRepository;
    public final TagCategoryRepository tagCategoryRepository;
    public final TagRepository tagRepository;
    public final PasswordEncoder passwordEncoder;


    private final OkResponse response = new OkResponse();


    @Operation(summary = "It provides list of all kitab.")
    @GetMapping("/all")
    public ResponseEntity<Object> getAll(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> all = folderRepository.findAll();
        List<FolderModel> activeFolderModel = activeDisplay.statusDisplay(userId, all, "active");  // note authz is checked in activeDisplay

        return ResponseEntity.ok().body(activeFolderModel);
    }


    @Operation(summary = "It provides list of all projects")
    @GetMapping("/project/all")
    public ResponseEntity<Object> getProjects(Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with userId : ")
                );

        List<FolderModel> allProjects = folderRepository.getByType("project");
        List<FolderModel> activeProjects = activeDisplay.statusDisplay(user.getId(), allProjects, "active");  // note authz is checked in activeDisplay


        return ResponseEntity.ok().body(activeProjects);
    }

    @Operation(summary = "This endpoint can be used to create / update projects")
    @PostMapping("/project/create")
    public ResponseEntity<?> newProject(Principal principal, @Valid @RequestBody FolderModel newProject) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isOwner(userId, new UUID(0, 0))) {
            return new ResponseEntity<>("Access Denied to " + newProject.getParent(), HttpStatus.FORBIDDEN);
        }

        List<FolderModel> children = folderRepository.findAllByName(newProject.name);


        if (children.stream().anyMatch(project -> newProject.name.equals(project.name))) {
            return new ResponseEntity<>("Project with same name " + newProject.name + " already exists", HttpStatus.NOT_FOUND);
        }

        newProject.parent = new UUID(0, 0);
        newProject.type = "project";
        newProject.status = "active";

        FolderModel project = folderRepository.save(newProject);

        FolderModel folder = new FolderModel();

        FolderModel dataFolder = folderRepository.save(new FolderModel(null, "Data", "Data folder", "folder", project.id, "active", new Date(), new Date(), userId, userId));
        FolderModel documentationFolder = folderRepository.save(new FolderModel(null, "Documentation", "Documentation folder", "folder", project.id, "active", new Date(), new Date(), userId, userId));
        FolderModel logicFolder = folderRepository.save(new FolderModel(null, "Logic", "Logic folder", "folder", project.id, "active", new Date(), new Date(), userId, userId));
//        FolderModel igniteFolder = folderRepository.save(new FolderModel(null, "Ignite", "Ignite folder", "folder", project.id, "active", new Date(), new Date(), userId, userId));
        FolderModel viewFolder = folderRepository.save(new FolderModel(null, "View", "View folder", "folder", project.id, "active", new Date(), new Date(), userId, userId));

        User usermodel = userService.getUser(principal.getName());

        List<User> projectOwners = List.of(usermodel);
        List<User> projectMembers = List.of(usermodel);

        Groups projectGroupOwners = groupService.saveGroup(new Groups(null, "TEAM-" + newProject.name + "-OWNER", "Created automatically while project creation.", "active", projectOwners, new ArrayList<>(), projectMembers, new Date(), new Date(), userId, null));
        Groups projectGroupEditors = groupService.saveGroup(new Groups(null, "TEAM-" + newProject.name + "-EDITOR", "Created automatically while project creation.", "active", projectOwners, new ArrayList<>(), projectMembers, new Date(), new Date(), userId, null));
        Groups projectGroupViewers = groupService.saveGroup(new Groups(null, "TEAM-" + newProject.name + "-VIEWER", "Created automatically while project creation.", "active", projectOwners, new ArrayList<>(), projectMembers, new Date(), new Date(), userId, null));

        // create permission mapping for the user who creates it
        Role ownerRole = roleRepository.findByName("Owner");
        Role editorRole = roleRepository.findByName("Editor");
        Role viewerRole = roleRepository.findByName("Viewer");

        PermissionsMapping creatorRole = permissionMappingRepository.save(new PermissionsMapping(null, userId, project.getId(), ownerRole, "active", new Date(), null, userId, null));

        PermissionsMapping ownersRole = permissionMappingRepository.save(new PermissionsMapping(null, projectGroupOwners.getId(), project.getId(), ownerRole, "active", new Date(), null, userId, null));
        PermissionsMapping editorsRole = permissionMappingRepository.save(new PermissionsMapping(null, projectGroupEditors.getId(), project.getId(), editorRole, "active", new Date(), null, userId, null));
        PermissionsMapping viewersRole = permissionMappingRepository.save(new PermissionsMapping(null, projectGroupViewers.getId(), project.getId(), viewerRole, "active", new Date(), null, userId, null));


        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @Operation(summary = "It provides project by Name")
    @GetMapping("/project/{name}")
    public ResponseEntity<Object> getProjectByName(Principal principal, @PathVariable("name") String name) {

        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allProjects = folderRepository.getByType("project");
        List<FolderModel> activeProjects = activeDisplay.statusDisplay(userId, allProjects, "active");  // note authz is checked in activeDisplay

        for (FolderModel project : activeProjects) {
            if (project.getName().equals(name)) {
                if (project.getStatus().equals("active")) {
                    return new ResponseEntity<>(project, HttpStatus.OK);
                } else {

                    return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>("Project with name " + name + " not found.", HttpStatus.NOT_FOUND);
    }

//    @Operation(summary = "It provides project by Id")
//    @GetMapping("/{Id}")
//    public ResponseEntity<?> getProjectById(@PathVariable("Id") UUID Id, Principal principal) {
//
//        UUID userId = userService.getUser(principal.getName()).id;
//
//        if (!authzService.isViewer(userId, Id)) {
//            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
//        }
//
//        System.out.println("access given");
//
//        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();
//
//        resourceViewsModel.setResourceId(Id);
//        resourceViewsModel.setAction("view");
//        resourceViewsModel.setViewedBy(userId);
//
//        resourceViewsRepository.save(resourceViewsModel);
//        System.out.println("added to views");
//
//        if (!folderRepository.existsById(Id)) {
//            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
//
//        } else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
//            return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
//        }
//        System.out.println("validation given");
//        FolderModel folderModel = folderRepository.findById(Id).get();
//
//        System.out.println("saerched given");
//
//        return new ResponseEntity<>(folderModel, HttpStatus.OK);
//    }

    @Operation(summary = "It provides project by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getProjectById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId;
        if(isValidUUID(principal.getName()))
        {
            userId = userRepository.findById(UUID.fromString(principal.getName())).get().getId();
        } else {
            userId = userService.getUser(principal.getName()).getId();
        }

        // TODO : this got some issue, not working with authz on
//        if (!authzService.isViewer(userId, Id)) {
//            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
//        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);

        } else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "It provides Kitab details based on Ids list")
    @PostMapping("/byIds")
    public ResponseEntity<Object> getByIds(@RequestBody List<UUID> Ids) {


        Set<UUID> set = new HashSet<>(Ids);
        Ids.clear();
        Ids.addAll(set);

        Map<UUID, Object> responseIds = new HashMap<>();

        for (UUID Id : Ids) {

            if (folderRepository.existsById(Id)) {
                responseIds.put(Id, folderRepository.findById(Id).get());
//                return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);

    }

    @Operation(summary = "It provides Kitab list of top viewed resources.")
    @GetMapping("/topViews")
    public ResponseEntity<Object> topViews(Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        List<ResourceViewsModel> resourceViewsModel = resourceViewsRepository.findFirst10DistinctByViewedByOrderByViewedAtDesc(userId);

        List<UUID> alreadyResource = new ArrayList<>();
        List<String> alreadyType = new ArrayList<>();

        List<Object> folderModels = new ArrayList<>();

        for (ResourceViewsModel resourceView : resourceViewsModel) {

            if(folderRepository.existsById(resourceView.getResourceId())) {
                FolderModel folderModel = folderRepository.findById(resourceView.getResourceId()).get();
                HashMap<Object, Object> Views = new HashMap<>();

//            if(!alreadyType.contains(folderModel.getType()) && !alreadyResource.contains(folderModel.getId())) {
                if (!alreadyResource.contains(folderModel.getId())) {

                    Views.put("resource", folderModel);
                    Views.put("view", resourceView);

                    folderModels.add(Views);

                    alreadyResource.add(folderModel.getId());
//                alreadyType.add(folderModel.getType());
                }
            }

        }

        return new ResponseEntity<>(folderModels, HttpStatus.OK);

    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Map<String, String> handleValidationExceptions(
//            MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//        return errors;
//    }

    @Operation(summary = "It rename folder by Id")
    @GetMapping(path = "/{Id}/{newName}/rename")
    public ResponseEntity<Object> rename(Principal principal, @PathVariable("Id") UUID Id, @PathVariable("newName") String newName) {


        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isEditor(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        } else if (folderRepository.getById(Id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The folder has been deleted."), HttpStatus.OK);
        }

        FolderModel folder = folderRepository.getById(Id);
        folder.setName(newName);

        folder.setUpdatedAt(new Date());
        folder.setCreatedBy(userId);
        folder.setUpdatedBy(userId);

        folderRepository.save(folder);

        return new ResponseEntity<>(response.okResponse("Resource renamed successfully"), HttpStatus.OK);

    }

    @Operation(summary = "It rename description of resource by Id")
    @GetMapping(path = "/{Id}/{newDescription}/renameDescription")
    public ResponseEntity<Object> renameDescription(Principal principal, @PathVariable("Id") UUID Id, @PathVariable("newDescription") String newDescription) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isEditor(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        } else if (folderRepository.getById(Id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The resource has been deleted."), HttpStatus.OK);
        }
        FolderModel folder = folderRepository.getById(Id);
        folder.setDescription(newDescription);

        folderRepository.save(folder);

        return new ResponseEntity<>(response.okResponse("Description changed successfully"), HttpStatus.OK);

    }

    @Operation(summary = "It deletes by ID")
    @DeleteMapping(path = "/{Id}/moveToTrash")
    public ResponseEntity<Object> moveToTrash(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isOwner(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        }

        // TODO : this should detect type and based on that it should delete the physical stuff also.
        //  may be initially, just mark inTrash, then cleaning service deletes it.

        FolderModel folderModel = folderRepository.getById(Id);
        moveToTrashRecursive(folderModel);

        return new ResponseEntity<>(response.okResponse("Id " + Id + " deleted successfully!"), HttpStatus.OK);
    }

    private void moveToTrashRecursive(FolderModel folderModel) {

        if (folderModel.getType().equals("repository") || folderModel.getType().equals("dataset") || folderModel.getType().equals("dashboard") || folderModel.getType().equals("chart")) {
            folderModel.setStatus("inTrash");
            folderRepository.save(folderModel);
            return;
        }
        folderModel.setStatus("inTrash");
        folderRepository.save(folderModel);
        List<FolderModel> children = folderRepository.getByParent(folderModel.getId());

        if (children.isEmpty()) return;

        for (FolderModel child : children) {
            moveToTrashRecursive(child);
        }
    }
    @Operation(summary = "It restore from trash by ID")
    @GetMapping(path = "/{Id}/restoreFromTrash")
    public ResponseEntity<Object> restoreFromTrash(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isOwner(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        }

        // TODO : this should detect type and based on that it should delete the
        // physical stuff also.
        // may be initially, just mark inTrash, then cleaning service deletes it.

        FolderModel folderModel = folderRepository.getById(Id);
        List<FolderModel> children = folderRepository.getByParent(folderModel.getParent());
        children.removeIf(child -> child.getStatus().equals("inTrash"));

        if (children.stream().anyMatch(folder -> folderModel.getName().equals(folder.name))) {
            folderModel.setName(folderModel.getName() + " - Recovered");
        }

        restoreFromTrashRecursive(folderModel);

        return new ResponseEntity<>(response.okResponse("Id " + Id + " restored successfully!"), HttpStatus.OK);
    }

    private void restoreFromTrashRecursive(FolderModel folderModel) {

        if (folderModel.getType().equals("repository") || folderModel.getType().equals("dataset") || folderModel.getType().equals("dashboard") || folderModel.getType().equals("chart")) {
            folderModel.setStatus("active");
            folderRepository.save(folderModel);
            return;
        }
        folderModel.setStatus("active");
        folderRepository.save(folderModel);
        List<FolderModel> children = folderRepository.getByParent(folderModel.getId());

        if (children.isEmpty())
            return;

        for (FolderModel child : children) {
            restoreFromTrashRecursive(child);
        }
    }

    @Operation(summary = "It restore from trash by ID")
    @DeleteMapping(path = "/{Id}/permanentDelete")
    public ResponseEntity<Object> permanentDelete(Principal principal, @PathVariable("Id") UUID Id) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isOwner(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        }

        // TODO : this should detect type and based on that it should delete the
        // physical stuff also.
        // may be initially, just mark inTrash, then cleaning service deletes it.

        FolderModel folderModel = folderRepository.getById(Id);

        permanentDeleteRecursive(folderModel);

        return new ResponseEntity<>(response.okResponse("Id " + Id + " permanently deleted!"), HttpStatus.OK);
    }

    private void permanentDeleteRecursive(FolderModel folderModel) throws Exception {
        UUID id = folderModel.getId();
        String type = folderModel.getType();
        folderRepository.delete(folderModel);
        resourceViewsRepository.deleteAllInBatch(resourceViewsRepository.findAllByResourceId(id));
        switch (type) {
            case "dataset":
                // TODO: bezier pipeline

                List<BranchModel> branchModels = branchRepository.findAllBranchModelByDatasetId(id);
                for (BranchModel branch : branchModels) {
                    // deleting build specifications
                    List<BuildSpecification>  datasetBuilds = buildSpecificationsRepository.findAllByDatasetIdAndBranch(id, branch.getBranch());
                    buildSpecificationsRepository.deleteAll(datasetBuilds);

                    // deleting charts associated with that dataset
                    List<ChartsModel> datasetCharts = chartsRepository.findAllByDatasetIdAndBranch(id, branch.getBranch());
                    for (ChartsModel charts : datasetCharts) {
                        permanentDeleteRecursive(folderRepository.getById(charts.getId()));
                    }

                    // deleting schemas
                    schemaRepository.deleteAll(schemaRepository.findByDatasetIdAndBranch(id, branch.getBranch()));

                    // deleting transactions
                    transactionRepository.deleteAll(transactionRepository.findAllByDatasetIdAndBranch(id, branch.getBranch()));

                    // deleting postgres synchro
                    postgresSyncRepository.deleteAll(postgresSyncRepository.findAllByDatasetIdAndBranch(id, branch.getBranch()));

                    // deleting schedules
                    schedulerRepository.deleteAll(schedulerRepository.findAllByDatasetIdAndBranch(id, branch.getBranch()));

                    // deleting files from cloud
                    deleteDatasetFiles(id, branch.getBranch());
                }
                branchRepository.deleteAll(branchModels);

                // deleting build log reference to dataset :below needs changing,its not a correct way
//                List <BuildLog> buildLogs = buildLogRepository.findAllByDatasetId(id);
//                for (BuildLog buildLog : buildLogs) {
//
//                    BuildStageLog buildStageLog = buildStageLogRepository.findByBuildIdAndDatasetId(buildLog.getId(), id);
//
//                    buildStageLogRepository.setDatasetId(null);
//                    buildLogRepository.save(buildLog);
//                }

                datasetRepository.delete(datasetRepository.findDatasetModelById(id));
                return;
            case "repository":
                return;
            case "dashboard":
                dashboardsRepository.deleteById(id);
                break;
            case "chart":
                chartsRepository.deleteById(id);
                // TODO : Delete charts from kepler_dashboards
//                chartsRepository.deleteById(id);
//                List <DashboardsModel> dashboards = dashboardsRepository.findAll();
//                for (DashboardsModel dashboard : dashboards) {
//                    Collection<HashMap<String, Object >> charts = dashboard.getCharts();
//                    for (HashMap<String,Object> h : charts)
//                        System.out.println(h.get(0));
//                }
                break;
        }
        List<FolderModel> children = folderRepository.getByParent(id);

        if (children.isEmpty()) {
            return;
        }

        for (FolderModel child : children) {
            permanentDeleteRecursive(child);
        }
    }

    @Operation(summary = "Provides logical path based on Id")
    @GetMapping(path = "/{Id}/getPath", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> getPath(Principal principal, @PathVariable("Id") UUID Id) {

        if (Objects.equals(Id.toString(), "undefined")) {
            return new ResponseEntity<>("Not valid Id", HttpStatus.BAD_REQUEST);
        }

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }


        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        } else if (folderRepository.getById(Id).getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The project has been deleted."), HttpStatus.OK);
        }

        List<FolderModel> path = kitabService.getPathById(Id, new ArrayList<>());
        List<FolderModel> activeProjects = activeDisplay.statusDisplay(userId, path, "active");  // note authz is checked in activeDisplay


        Collections.reverse(path);

        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);
        Gson gson = b.create();

        // converts to json
        String json = gson.toJson(path);

        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @Operation(summary = "Creates Sample Data")
    @GetMapping(path = "/sampleData", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> sampleData() {

        // don't load data if below variable is null or not true
        if (System.getenv("LOAD_SAMPLE_DATA") == null) {
            new ResponseEntity<>("Load Sample Data variable is not set to true", HttpStatus.OK);

        } else {
            if (!System.getenv("LOAD_SAMPLE_DATA").equals("true")) {
                new ResponseEntity<>("Load Sample Data variable is not set to true", HttpStatus.OK);
            }
        }


        // If sample data exists then do not add again
        if (folderRepository.existsByName("Test")) {
            return new ResponseEntity<>("Sample data already exists", HttpStatus.ALREADY_REPORTED);
        }

        User movetodataInternal = userService.getUser("movetodata-internal");

        Groups sampleOwnGroup = groupService.saveGroup(new Groups(null, "TEAM-SAMPLE-OWN", "This is sample own group", null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, null, null, null));
        Groups sampleEditGroup = groupService.saveGroup(new Groups(null, "TEAM-SAMPLE-EDIT", "This is sample own group", null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, null, null, null));
        Groups sampleViewGroup = groupService.saveGroup(new Groups(null, "TEAM-SAMPLE-VIEW", "This is sample own group", null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, null, null, null));


//        userService.saveUser(new User(null, "Ashif Mohd", "ashif", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Ashif", "Mohd", "Aix", "https://ca.slack-edge.com/T0291A2684X-U02QYG1V692-be8bc7a76379-512", "ashif@movetodata.io", "auto",AuthProvider.local, null, null, null,null,null,null,null));
//        userService.saveUser(new User(null, "Harsh Yadav", "harsh", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Harsh", "Yadav", "India", "https://avatars.githubusercontent.com/u/62005923?v=4", "harsh@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null));

        userService.saveUser(new User(null, "Bhagesh Dhankhar", "bhagesh", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Bhagesh", "Dhankhar", "India", "", "bhagesh@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER,null));

        userService.saveUser(new User(null, "Amine Aouini", "amine", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Amine", "Aouini", "Aix", "", "auto", "auto", "amine@movetodata.io",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER,null));

        userService.saveUser(new User(null, "Anshul Dhankhar", "anshul", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Anshul", "Dhankhar", "Delhi", "", "auto", "auto", "anshul@movetodata.io",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER,null));

        userService.saveUser(new User(null, "Lakshay Dabas", "lakshay", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Lakshay", "Dabas", "Delhi", "", "auto", "auto", "lakshay@movetodata.io",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "Manav Rathi", "manav", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Manav", "Rathi", "Delhi", "", "auto", "auto", "manav@movetodata.io",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "Ankit Kumar", "ankit", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Ankit", "Kumar", "Delhi", "", "auto", "auto", "ankit@movetodata.io",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "Vincent De-Guio", "vincent", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Vincent", "De-Guio", "Aix", "", "vincent@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "Jonathan De-Guio", "jonathan", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Jonathan", "De-Guio", "Aix", "https://ca.slack-edge.com/T0291A2684X-U0292AZES5D-aea210fd8068-192", "jonathan@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "James Morrish", "james", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "James", "Morrish", "Aix", "https://ca.slack-edge.com/T0291A2684X-U029Q9B53EX-e6b31ee5d73c-192", "james@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));

        userService.saveUser(new User(null, "Rakesh Malik", "rakesh", passwordEncoder.encode(System.getenv("DEFAULT_PASSWORD")), "Rakesh", "Malik", "Aix", "https://avatars.githubusercontent.com/u/38586615?s=400&u=b071181ada05d564a60d8f1631ecc5f79b9a518d&v=4", "rakesh@movetodata.io", "auto", "auto",AuthProvider.local, null, null, null,null,null,null,null,RoleS3.USER, null));


//        FolderModel projectQuality = folderRepository.save(new FolderModel(null, "Quality", "This is a test Quality Project", "project", new UUID(0, 0), "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel dataFolder = folderRepository.save(new FolderModel(null, "Data", "Data folder Quality Project", "folder", projectQuality.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel igniteFolder = folderRepository.save(new FolderModel(null, "Ignite", "Ignite folder Quality Project", "folder", projectQuality.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel logicFolder = folderRepository.save(new FolderModel(null, "Logic", "Logic folder Quality Project", "folder", projectQuality.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));

//        folderRepository.save(new FolderModel(null, "View", "View folder Quality Project", "folder", projectQuality.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//        FolderModel sampleDataset0 = folderRepository.save(new FolderModel(null, "Dataset0", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel sampleDataset1 = folderRepository.save(new FolderModel(null, "Dataset1", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel sampleDataset2 = folderRepository.save(new FolderModel(null, "Dataset2", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel sampleDataset3 = folderRepository.save(new FolderModel(null, "Dataset3", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel sampleDataset4 = folderRepository.save(new FolderModel(null, "Dataset4", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel sampleDataset5 = folderRepository.save(new FolderModel(null, "Dataset5", "This is a dataset for testing purposes only.", "dataset", dataFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));

//        addSampleData(sampleDataset0, movetodataInternal.getId());
//        addSampleData(sampleDataset1, movetodataInternal.getId());
//        addSampleData(sampleDataset2, movetodataInternal.getId());
//        addSampleData(sampleDataset3, movetodataInternal.getId());
//        addSampleData(sampleDataset4, movetodataInternal.getId());
//        addSampleData(sampleDataset5, movetodataInternal.getId());

//        pipelineRepository.save(new PipelineModel(null, sampleDataset0.id, "main", sampleDataset1.id, "main", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset0.id, "main", sampleDataset1.id, "master", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset1.id, "master", sampleDataset2.id, "main", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset1.id, "main", sampleDataset3.id, "main", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset2.id, "main", sampleDataset4.id, "master", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset3.id, "main", sampleDataset4.id, "main", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        pipelineRepository.save(new PipelineModel(null, sampleDataset4.id, "main", sampleDataset5.id, "main", "none", "main", "none", "none", "active", "pipeline", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));

        GitCommit commitModel = new GitCommit();

        commitModel.setUsername(movetodataInternal.getId().toString());
        commitModel.setEmail("rakesh@movetodata.io");
        commitModel.setMessage("Sample Repository : Automatically created repository");


        FolderModel test = folderRepository.save(new FolderModel(null, "Test", "This is a test project for build testing", "project", new UUID(0, 0), "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
        FolderModel dataFolderTest = folderRepository.save(new FolderModel(null, "Data", "Data folder Test Project", "folder", test.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
        FolderModel logicFolderTest = folderRepository.save(new FolderModel(null, "Logic", "Logic folder Test Project", "folder", test.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//        FolderModel salesRecords = folderRepository.save(new FolderModel(null, "SalesRecords", "This is a dataset for testing purposes only.", "dataset", dataFolderTest.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));

//        addSampleData(salesRecords, movetodataInternal.getId());


        // sample permissions
        Role projectOwner = roleRepository.findByName("Owner");

//        PermissionsMapping permissionsMappingProjectQuality = permissionMappingRepository.save(new PermissionsMapping(null, sampleOwnGroup.id, projectQuality.id, projectOwner, "active", null, null, null, null));
        PermissionsMapping permissionsMappingProjectTest = permissionMappingRepository.save(new PermissionsMapping(null, sampleOwnGroup.id, test.id, projectOwner, "active", new Date(), null, movetodataInternal.getId(), null));

        List<UUID> sampleGroupOwners = new ArrayList<>();
        List<UUID> sampleGroupMembers = new ArrayList<>();

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            System.out.println(user.getName());
            sampleGroupOwners.add(user.getId());
            sampleGroupMembers.add(user.getId());




        }

        if (!groupsRepository.existsByName("platform-administrators")) {
            Groups platformAdminGroup = groupsRepository.findByName("platform-administrators");

            groupService.addUsersToGroupMembers(movetodataInternal.getId(), sampleGroupMembers, platformAdminGroup.getId());
        }

        groupService.addUsersToGroupMembers(movetodataInternal.getId(), sampleGroupMembers, sampleOwnGroup.getId());
        groupService.addUsersToGroupOwners(movetodataInternal.getId(), sampleGroupOwners, sampleOwnGroup.getId());

        // Tags

        TagsCategory businessTagsCategory = tagCategoryRepository.save(new TagsCategory(null,"Business", "This is a business category", true,null,new Date(),movetodataInternal.getId(),null,null ));
        TagsCategory governanceTagsCategory = tagCategoryRepository.save(new TagsCategory(null,"Governance", "This is a governance category", true,null,new Date(),movetodataInternal.getId(),null,null ));

        tagRepository.save(new Tags(null, "Internal", "This is an internal tag", "yellow", businessTagsCategory, new Date(),movetodataInternal.getId(),null,null));
        tagRepository.save(new Tags(null, "Confidential", "This is an confidential tag", "red", businessTagsCategory, new Date(),movetodataInternal.getId(),null,null));
        tagRepository.save(new Tags(null, "Restricted", "This is an restricted tag", "blue", businessTagsCategory, new Date(),movetodataInternal.getId(),null,null));
        tagRepository.save(new Tags(null, "Non-Confidential", "This is an Non-Confidential", "green", businessTagsCategory, new Date(),movetodataInternal.getId(),null,null));

        tagRepository.save(new Tags(null, "Governance", "This is an Governance tag", "orange", governanceTagsCategory, new Date(),movetodataInternal.getId(),null,null));
        tagRepository.save(new Tags(null, "Non-Confidential", "This is an Non-Confidential tag", "cyan", governanceTagsCategory, new Date(),movetodataInternal.getId(),null,null));

        boolean LOAD_IGNITE_SAMPLE_DATA = false;

//        if (LOAD_IGNITE_SAMPLE_DATA) {
//
//            FolderModel agentKitab = folderRepository.save(new FolderModel(null, "TestAgent - ServerP", "This agent is for testing.", "agent", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Agents agentsMacbook = agentRepository.save(new Agents(agentKitab.getId(), agentKitab.getName(), agentKitab.getDescription(), dataFolder.getId(), false, null, null, null, new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            HashMap<String, String> sourceConfig = new HashMap<>();
//            sourceConfig.put("type", "folder");
//            sourceConfig.put("path", "/User/commonaccount/Downloads");
//            sourceConfig.put("timeout", "2000");
//
//            List<UUID> agentListIds = new ArrayList<>();
//            agentListIds.add(agentsMacbook.getId());
//
//
//            FolderModel sourcesKitab = folderRepository.save(new FolderModel(null, "test_folder_serverp", "This is a test description", "source", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Sources sources = sourcesRepository.save(new Sources(sourcesKitab.getId(), sourcesKitab.getName(), sourceConfig, sourcesKitab.getDescription(), dataFolder.getId(), false, agentListIds, new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            HashMap<String, String> sourceJDBCConfig = new HashMap<>();
//            sourceJDBCConfig.put("type", "jdbc");
//            sourceJDBCConfig.put("jdbcType", "postgres");
//            sourceJDBCConfig.put("username", "postgres");
//            sourceJDBCConfig.put("password", "solaris");
//            sourceJDBCConfig.put("server", "localhost");
//            sourceJDBCConfig.put("databaseName", "kepler");
//            sourceJDBCConfig.put("port", "5432");
//
//            FolderModel sourcesJDBCKitab = folderRepository.save(new FolderModel(null, "test_jdbc_serverp", "This is a test description", "source", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Sources sourcesJDBC = sourcesRepository.save(new Sources(sourcesJDBCKitab.getId(), sourcesJDBCKitab.getName(), sourceJDBCConfig, sourcesJDBCKitab.getDescription(), dataFolder.getId(), false, agentListIds, new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            HashMap<String, String> sourceDatasetConfig = new HashMap<>();
//            sourceDatasetConfig.put("subFolder", "/Sales");
//            sourceDatasetConfig.put("deleteFilesAfterUpload", "false");
//
//            FolderModel link1MacKitab = folderRepository.save(new FolderModel(null, "dataset0-folder", "This is a test link", "link", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Links links = linkRepository.save(new Links(link1MacKitab.getId(), link1MacKitab.getName(), link1MacKitab.getDescription(), sourceDatasetConfig, sampleDataset0.getId(), "master", sources.getId(), dataFolder.getId(), "overwrite", "None", null, "", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//
//            HashMap<String, String> sourceJDBCDatasetConfig = new HashMap<>();
//            sourceJDBCDatasetConfig.put("dbTable", "public.salesrecords"); // TODO : add SQL -- - - test branch
//
//            FolderModel linkMacKitab = folderRepository.save(new FolderModel(null, "dataset5-jdbc-postgres", "This is a test link", "link", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Links linksJDBC = linkRepository.save(new Links(linkMacKitab.getId(), linkMacKitab.getName(), linkMacKitab.getDescription(), sourceJDBCDatasetConfig, sampleDataset5.getId(), "master", sourcesJDBC.getId(), dataFolder.getId(), "overwrite", "None", null, "", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//
//            FolderModel agentMacKitab = folderRepository.save(new FolderModel(null, "TestAgent - ServerD", "This agent is for testing", "agent", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Agents agentsiMac = agentRepository.save(new Agents(agentMacKitab.getId(), agentMacKitab.getName(), agentMacKitab.getDescription(), dataFolder.getId(), false, null, null, null, new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            HashMap<String, String> sourceConfig1 = new HashMap<>();
//            sourceConfig1.put("type", "folder");
//            sourceConfig1.put("path", "/User/rakeshmalik/Downloads");
//            sourceConfig1.put("timeout", "2000");
//
//            List<UUID> agentListIds1 = new ArrayList<>();
//            agentListIds1.add(agentsiMac.getId());
//
//            FolderModel sources1Kitab = folderRepository.save(new FolderModel(null, "test_folder_serverD", "This is a test description", "source", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Sources sources1 = sourcesRepository.save(new Sources(sources1Kitab.getId(), sources1Kitab.getName(), sourceConfig1, sources1Kitab.getDescription(), dataFolder.getId(), false, agentListIds1, new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            HashMap<String, String> sourceDatasetConfig1 = new HashMap<>();
//            sourceDatasetConfig1.put("subFolder", "/Sales");
//            sourceDatasetConfig1.put("deleteFilesAfterUpload", "false");
//
//            FolderModel links1Kitab = folderRepository.save(new FolderModel(null, "dataset2-folder", "This is a test link", "link", igniteFolder.id, "active", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//            Links links1 = linkRepository.save(new Links(links1Kitab.getId(), links1Kitab.getName(), links1Kitab.getDescription(), sourceDatasetConfig1, sampleDataset2.getId(), "master", sources1.getId(), dataFolder.getId(), "overwrite", "None", null, "", new Date(), new Date(), movetodataInternal.getId(), movetodataInternal.getId()));
//
//
//
//        }

        return new ResponseEntity<>(response.okResponse("Sample Data has been created."), HttpStatus.OK);

    }

    private void addSampleData(FolderModel sampleDataset0, UUID movetodataInternalUserId) {

        branchRepository.save(new BranchModel(null, "master", sampleDataset0.getId(), UUID.randomUUID(), "raw", new Date(), new Date(), movetodataInternalUserId, movetodataInternalUserId));

        datasetRepository.save(new DatasetModel(sampleDataset0.id, sampleDataset0.name, null,
                new Date(), new Date(), movetodataInternalUserId, movetodataInternalUserId));
    }


    /**
     * This TypeAdapter unproxies Hibernate proxied objects, and serializes them
     * through the registered (or default) TypeAdapter of the base class.
     */
    public static class HibernateProxyTypeAdapter extends TypeAdapter<HibernateProxy> {

        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return (HibernateProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateProxyTypeAdapter(gson) : null);
            }
        };
        private final Gson context;

        private HibernateProxyTypeAdapter(Gson context) {
            this.context = context;
        }

        @Override
        public HibernateProxy read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void write(JsonWriter out, HibernateProxy value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            // Retrieve the original (not proxy) class
            Class<?> baseType = Hibernate.getClass(value);
            // Get the TypeAdapter of the original class, to delegate the serialization
            TypeAdapter delegate = context.getAdapter(TypeToken.get(baseType));
            // Get a filled instance of the original class
            Object unproxiedValue = value.getHibernateLazyInitializer().getImplementation();
            // Serialize the value
            delegate.write(out, unproxiedValue);
        }
    }

    @Operation(summary = "Provides id based on path based on given path")
    @PostMapping(path = "/getIdByPath")
    ResponseEntity<Object> getIdByPath(Principal principal, @RequestBody String path) {
        try {
            UUID id, resourceId = new UUID(0, 0);

            String resorceType = null;

            String[] individualPath = path.split("/");

            FolderModel tempModel;

            for (int i = 2; i < individualPath.length; i++) {
                tempModel = folderRepository.findByNameAndParentAndStatus(individualPath[i], resourceId, "active");
                if (tempModel == null) {
                    Map<String,Object> response = Map.of("Status", false, "Message", individualPath[i] + " Not Found in " + path.substring(0,path.indexOf(individualPath[i])));
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
                resourceId = tempModel.getId();
                resorceType = tempModel.getType();
            }

            UUID userId = userService.getUser(principal.getName()).id;
            if (!authzService.isViewer(userId, resourceId)) {
                Map<String,Object> response = Map.of("Status", false, "Message", "Access Denied to " + resourceId);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            assert resorceType != null;
            Map<String,Object> response = Map.of("Status", true, "Message", resourceId, "Type", resorceType);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.: " + e.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Provides id based on path based on given path")
    @PostMapping(path = "/getParentIdByPath")
    ResponseEntity<Object> getParent(Principal principal, @RequestBody String path) {
        try {
            UUID id, resourceId = new UUID(0, 0);

            String[] individualPath = path.split("/");

            FolderModel tempModel;

            for (int i = 2; i < individualPath.length-1; i++) {
                tempModel = folderRepository.findByNameAndParentAndStatus(individualPath[i], resourceId, "active");
                if (tempModel == null) {
                    Map<String,Object> response = Map.of("Status", false, "Message", individualPath[i] + " Not Found in " + path.substring(0,path.indexOf(individualPath[i])));
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
                resourceId = tempModel.getId();
            }

            UUID userId = userService.getUser(principal.getName()).id;
            if (!authzService.isViewer(userId, resourceId)) {
                Map<String,Object> response = Map.of("Status", false, "Message", "Access Denied to " + resourceId);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            Map<String,Object> response = Map.of("Status", true, "Message", resourceId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.: " + e.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Update dataset time based on id")
    @GetMapping("/{Id}/updateTime")
    ResponseEntity <Object> updateTime (Principal principal, @PathVariable("Id") UUID Id) {
        FolderModel datasetFolderModel = folderRepository.findById(Id).get();
        DatasetModel datasetDatasetModel = datasetRepository.findDatasetModelById(Id);

        datasetFolderModel.setUpdatedBy(userService.getUser(principal.getName()).getId());
        datasetDatasetModel.setUpdatedBy(userService.getUser(principal.getName()).getId());
        datasetFolderModel.setUpdatedAt(new Date());
        datasetDatasetModel.setUpdatedAt(new Date());

        datasetRepository.saveAndFlush(datasetDatasetModel);
        folderRepository.saveAndFlush(datasetFolderModel);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
