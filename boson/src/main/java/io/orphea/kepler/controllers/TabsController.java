package io.orphea.kepler.controllers;

import io.orphea.bezier.library.repository.PipelineRepository;
import io.orphea.kepler.library.DTOs.ChangesDTO;
import io.orphea.kepler.library.DTOs.TabCustomizeDTO;
import io.orphea.kepler.library.models.*;
import io.orphea.kepler.library.repository.*;
import io.orphea.kepler.library.services.ChangesService;
import io.orphea.kepler.library.services.TabsService;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/api/kepler/tabs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler dashboards management service endpoints")
public class TabsController {

    private final TabElementsRepository tabElementsRepository;
    private final TabsRepository tabsRepository;
    private final UserService userService;
    private final DashboardsRepository dashboardsRepository;
    private final ChartsRepository chartsRepository;
    private final AuthzService authzService;
    private final PipelineRepository pipelineRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ChangesService changesService;
    private final ResourceService resourceService;
    private final TabsService tabsService;

    @Operation(summary = "Get all tabs")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllTabs(Principal principal) {
        List<TabsModel> tabs = tabsRepository.findAll();

        return ResponseEntity.accepted().body(tabs);
    }

    @Operation(summary = "This can be used to get charts for particular tab")
    @GetMapping("/getTabCharts/{id}")
    public ResponseEntity<Object> getTabCharts(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<ChartsModel> charts = tabsRepository.getReferenceById(id).getChartsForTabs();
        // Ignore compiler warning here
        Set<ChartsModel> chartsNotInTrash = charts;

        for (ChartsModel chart : charts) {
            if (resourceService.getResourceModel(chart.getId()).getStatus().equals("inTrash")) {
                continue;
            }
            chartsNotInTrash.add(chart);
        }

        return ResponseEntity.accepted().body(chartsNotInTrash);
    }

    @Operation(summary = "Update Tab name")
    @PostMapping("/updateTab/{id}/{newName}")
    public ResponseEntity<Object> renameTab(Principal principal, @PathVariable("id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TabsModel tab = tabsRepository.getReferenceById(id);

        // ------- Recording Changes for latest version
        UUID dashboardId = tab.getDashboardsModel().getId();
        Long versionId = resourceVersionsRepository.getReferenceById(dashboardId).getLatestVersionId();
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("renamed", true));
        changes.add(new ChangesDTO("tab", true));
        changes.add(new ChangesDTO(tab.getName(), false));
        changes.add(new ChangesDTO("to", true));
        changes.add(new ChangesDTO(newName, false));
        changesService.SaveChanges(changes, versionDetails, "rename", userId);
        // -------------------------

        tab.setName(newName);
        tabsRepository.save(tab);

        return ResponseEntity.accepted().body("DONE");
    }

    @Operation(summary = "Get Tab Config")
    @GetMapping("/getTabConfig/{id}")
    public ResponseEntity<Object> getTabConfig(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TabsModel tab = tabsRepository.getReferenceById(id);

        TabCustomizeDTO tabCustomizeDTO = new TabCustomizeDTO();
        tabCustomizeDTO.setChartHeadingTextColor(tab.getChartHeadingTextColor());
        tabCustomizeDTO.setChartHeadingBg(tab.getChartHeadingBg());
        tabCustomizeDTO.setChartBodyBg(tab.getChartBodyBg());
        tabCustomizeDTO.setCanvasBg(tab.getCanvasBg());
        tabCustomizeDTO.setPageBg(tab.getPageBg());

        tabCustomizeDTO.setTopPadding(tab.getTopPadding());
        tabCustomizeDTO.setRightPadding(tab.getRightPadding());
        tabCustomizeDTO.setBottomPadding(tab.getBottomPadding());
        tabCustomizeDTO.setLeftPadding(tab.getLeftPadding());

        tabCustomizeDTO.setPreventCollision(tab.isPreventCollision());
        tabCustomizeDTO.setAllowOverlap(tab.isAllowOverlap());

        return new ResponseEntity<>(tabCustomizeDTO, HttpStatus.OK);
    }

    @Operation(summary = "Update Tab Config")
    @PostMapping("/updateTabConfig/{dashboardId}/{id}")
    public ResponseEntity<Object> updateTabConfig(Principal principal, @PathVariable("dashboardId") UUID dashboardId, @PathVariable("id") UUID id, @Valid @RequestBody TabCustomizeDTO tabCustomizeDTO) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, dashboardId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        tabsService.updateTabConfig(id, userId, tabCustomizeDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "This can be used to get tabs by dashboard Id for particular version or latest version.")
    @GetMapping("/getDashboardTabs/{id}")
    public ResponseEntity<Object> getDashboardTabs(Principal principal, @PathVariable("id") UUID id, @RequestParam(name = "v", required = false) Long versionId) {

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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);
        List<TabsModel> tabs = dashboardsModel.getTabs();

        return ResponseEntity.accepted().body(tabs);
    }

    @Operation(summary = "Add tab to dashbaord latest version only.")
    @PostMapping("/addTabToDashboard/{id}")
    public ResponseEntity<Object> addTabToDashboard(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);
        Long versionId = dashboardVersion.getLatestVersionId();

        DashboardKey dashboardId = new DashboardKey(id, versionId);

        if (!dashboardsRepository.existsById(dashboardId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);
        List<TabsModel> tabs = dashboardsModel.getTabs();
        TabsModel newTab = new TabsModel();
        int nextTabNumber = tabs.size() + 1;
        newTab.setName("Tab" + nextTabNumber);
        tabs.add(newTab);
        // Handling tab ordering
        tabsService.updateOrderInTabsList(tabs);
        dashboardsModel.setTabs(tabs);

        // ------- Recording Changes for latest version
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(id).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("added", true));
        changes.add(new ChangesDTO("tab", true));
        changes.add(new ChangesDTO("Tab" + nextTabNumber, false));
        changesService.SaveChanges(changes, versionDetails, "add", userId);
        // -------------------------

        dashboardsRepository.save(dashboardsModel);
        newTab.setDashboardsModel(dashboardsModel);
        tabsRepository.save(newTab);

        return ResponseEntity.accepted().body(newTab);
    }

    @Operation(summary = "Delete tab from dashboard latest version only.")
    @DeleteMapping("/removeTabFromDashboard/{dashboardId}/{tabId}")
    public ResponseEntity<Object> removeTabFromDashboard(Principal principal, @PathVariable("dashboardId") UUID id, @PathVariable("tabId") UUID tabId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        tabsService.deleteTab(id, tabId, userId);

        return ResponseEntity.accepted().body("Success Deleted");
    }

    @Operation(summary = "Add or Remove charts from tab")
    @PostMapping("/manageCharts")
    public ResponseEntity<Object> manageCharts(@Valid @RequestBody ManageTabRequest manageTabRequest, Principal principal) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, manageTabRequest.getDashboardId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!tabsRepository.existsById(manageTabRequest.getTabId())) {
            return new ResponseEntity<>("Id " + manageTabRequest.getTabId() + " does not exist.", HttpStatus.NOT_FOUND);
        }

        if (manageTabRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageTabRequest.getAction(), "add")) && (!Objects.equals(manageTabRequest.getAction(), "remove"))) {
            throw new Exception("Not a valid action type, use add or remove");
        }

        tabsService.manageCharts(manageTabRequest, userId);

        return new ResponseEntity<>("Tab updated", HttpStatus.OK);
    }

    @Operation(summary = "Add or Remove elements from tab")
    @PostMapping("/manageElements")
    public ResponseEntity<Object> manageElements(@Valid @RequestBody ManageElementRequest manageElementRequest, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isEditor(userId, manageElementRequest.getDashboardId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!tabsRepository.existsById(manageElementRequest.getTabId())) {
            return new ResponseEntity<>("Id " + manageElementRequest.getTabId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!tabElementsRepository.existsById(manageElementRequest.getElementId())) {
            return new ResponseEntity<>("Id " + manageElementRequest.getElementId() + " does not exist.", HttpStatus.NOT_FOUND);
        }

        if (manageElementRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageElementRequest.getAction(), "add")) && (!Objects.equals(manageElementRequest.getAction(), "remove"))) {
            return new ResponseEntity<>("Not a valid action type, use add or remove", HttpStatus.BAD_REQUEST);
        }

        TabsModel tabModel = tabsRepository.getReferenceById(manageElementRequest.getTabId());
        List<TabElementsModel> elementsModels = tabModel.getTabElements();
        TabElementsModel element = tabElementsRepository.getReferenceById(manageElementRequest.getElementId());
        boolean deleteRequest = false;

        // ------- Recording Changes for latest version
        UUID dashboardId = tabModel.getDashboardsModel().getId();
        Long versionId = resourceVersionsRepository.getReferenceById(dashboardId).getLatestVersionId();
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO(manageElementRequest.getAction(), true));
        changes.add(new ChangesDTO("element", true));
        changes.add(new ChangesDTO(tabModel.getName(), false));
        changesService.SaveChanges(changes, versionDetails, manageElementRequest.getAction(), userId);
        // -------------------------

        if (elementsModels.contains(element)) {
            if (Objects.equals(manageElementRequest.getAction(), "add")) {
                return new ResponseEntity<>("Element already present", HttpStatus.BAD_REQUEST);
            } else {
                // remove element and delete it
                elementsModels.remove(element);
                deleteRequest = true;
            }
        } else {
            if (Objects.equals(manageElementRequest.getAction(), "add")) {
                elementsModels.add(element);
            } else {
                return new ResponseEntity<>("Element not present", HttpStatus.BAD_REQUEST);
            }
        }

        tabModel.setUpdatedAt(new Date());
        tabModel.setUpdatedBy(userId);
        tabsRepository.save(tabModel);

        if (deleteRequest)
            tabElementsRepository.delete(element);

        return new ResponseEntity<>("Tab elements updated", HttpStatus.OK);
    }

    @Operation(summary = "Get Tab Datasets")
    @GetMapping("/getDatasets/{id}")
    public ResponseEntity<Object> getDatasets(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>("No tab found for given Id", HttpStatus.NOT_FOUND);
        }

        HashMap<String, String> datasets = new HashMap<>();

        TabsModel tabsModel = tabsRepository.getReferenceById(id);
        Set<ChartsModel> charts = tabsModel.getChartsForTabs();

        for (ChartsModel chart : charts) {
            ResourceModel model = resourceService.getResourceModel(chart.getDatasetId());
            datasets.put(model.getId().toString(), model.getName());
        }

        return new ResponseEntity<>(datasets, HttpStatus.OK);
    }

    @PutMapping("/move/{tabId}/{pos}")
    public ResponseEntity<String> moveTab(@PathVariable("tabId") UUID tabId, @PathVariable("pos") int newPosition) {
        tabsService.updateTabOrder(tabId, newPosition);

        return ResponseEntity.ok("Tab order updated successfully.");
    }

}

