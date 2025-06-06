package io.bosler.kepler.controllers;

import io.bosler.kepler.library.models.*;
import io.bosler.kepler.library.repository.ChartsRepository;
import io.bosler.kepler.library.repository.DashboardsRepository;
import io.bosler.kepler.library.repository.TabElementsRepository;
import io.bosler.kepler.library.repository.TabsRepository;
import io.bosler.kitab.library.models.ResourceViewsModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.kitab.library.repository.ResourceViewsRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.sharedUtils.ActiveDisplay;
import io.bosler.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;


@RestController
@RequestMapping("/api/kepler/tabElements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kepler", description = "Kepler dashboards management service endpoints")
public class TabElementsController {

    private final TabElementsRepository tabElementsRepository;
    private final TabsRepository tabsRepository;
    private final UserService userService;
    private final DashboardsRepository dashboardsRepository;
    private final ChartsRepository chartsRepository;
    private final FolderRepository folderRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final ActiveDisplay activeDisplay;
    private final AuthzService authzService;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "Get all elements")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllElements(Principal principal) {
        List<TabElementsModel> elements = tabElementsRepository.findAll();

        return ResponseEntity.accepted().body(elements);
    }

    @Operation(summary = "This can be used to create tab elements.")
    @GetMapping("/getElements/{tabId}")
    public ResponseEntity<Object> getElements(Principal principal, @PathVariable("tabId") UUID tabId) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!tabsRepository.existsById(tabId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        TabsModel tabsModel = tabsRepository.getById(tabId);
        List<TabElementsModel> tabElements = tabsModel.getTabElements();

        return ResponseEntity.accepted().body(tabElements);
    }
    @Operation(summary = "This can be used to create tab elements.")
    @PostMapping("/create")
    public ResponseEntity<Object> createElement(@Valid @RequestBody ManageElementUpdateRequest manageElementUpdateRequest, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;
        UUID tabId = manageElementUpdateRequest.getTabId();
        String elementType = manageElementUpdateRequest.getType();
        String elementData = manageElementUpdateRequest.getData();
        String elementPos = manageElementUpdateRequest.getPosition();

        if (!tabsRepository.existsById(tabId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!((Objects.equals(elementType, "chart")) || (Objects.equals(elementType, "header")) || (Objects.equals(elementType, "divider")) || (Objects.equals(elementType, "markdown")))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        TabsModel tabsModel = tabsRepository.getById(tabId);
        List<TabElementsModel> tabElements = tabsModel.getTabElements();
        TabElementsModel newElement = new TabElementsModel();
        if (Objects.equals(elementType, "chart")) {
            ChartsModel chart = chartsRepository.getById(UUID.fromString(elementData));
            newElement.setDatasetId(String.valueOf(chart.getDatasetId()));
        }

        newElement.setType(elementType);
        newElement.setData(elementData);
        newElement.setPosition(elementPos);
        newElement.setTabsModel(tabsModel);
        tabElementsRepository.save(newElement);

        tabElements.add(newElement);
        tabsModel.setTabElements(tabElements);
        tabsModel.setUpdatedBy(userId);
        tabsModel.setUpdatedAt(new Date());
        tabsRepository.save(tabsModel);

        return ResponseEntity.accepted().body(newElement);
    }

    @Operation(summary = "Update Element.")
    @PostMapping("/update")
    public ResponseEntity<Object> updateElement(@Valid @RequestBody List<ManageElementUpdateRequest> manageElementUpdateRequestList,Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        for (ManageElementUpdateRequest manageElementUpdateRequest : manageElementUpdateRequestList) {
            UUID elementId = manageElementUpdateRequest.getElementId();

            if (!tabElementsRepository.existsById(elementId)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            TabElementsModel tabElementsModel = tabElementsRepository.getById(elementId);
            if (!(manageElementUpdateRequest.getPosition() == null)) {
                tabElementsModel.setPosition(manageElementUpdateRequest.getPosition());
            }

            if (!manageElementUpdateRequest.getData().equals("")) {
                tabElementsModel.setData(manageElementUpdateRequest.getData());
            }

//            if (!manageElementUpdateRequest.getType().equals("chart") && !manageElementUpdateRequest.getType().equals("divider")) {
//                if (manageElementUpdateRequest.getType().equals("header") && !manageElementUpdateRequest.getData().equals(""))
//                    tabElementsModel.setData(manageElementUpdateRequest.getData());
//                else if (!manageElementUpdateRequest.getType().equals("header")) {
//                    tabElementsModel.setData(manageElementUpdateRequest.getData());
//                }
//            }
            tabElementsRepository.save(tabElementsModel);
        }

        return ResponseEntity.accepted().body("Updated Success");
    }

    @Operation(summary = "Remove Element.")
    @DeleteMapping("/remove/{elementId}")
    public ResponseEntity<Object> removeElement(Principal principal, @PathVariable("elementId") UUID elementId) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!tabElementsRepository.existsById(elementId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        TabElementsModel tabElementsModel = tabElementsRepository.getById(elementId);

        if (tabElementsModel.getType().equals("chart")) {
            ChartsModel chart = chartsRepository.getById(UUID.fromString(tabElementsModel.getData()));
            TabsModel tab = tabElementsModel.getTabsModel();
            Set<TabsModel> tabs = chart.getTabsForCharts();
            Set<ChartsModel> charts = tab.getChartsForTabs();

            // Removing the current tab in which chart to be deleted is added
            tabs.remove(tab);
            charts.remove(chart);

            // Set back the changed attributes and saved the repository
            tab.setChartsForTabs(charts);
            tabsRepository.save(tab);
            chart.setTabsForCharts(tabs);
            chartsRepository.save(chart);
            // Left : Delete the dashboard if that dashboard tabs are not present.
            DashboardsModel dashboard = tab.getDashboardsModel();
            Set<TabsModel> particularDashboardTabs = new HashSet<>();
            for (TabsModel _tab : tabs) {
                if (_tab.getDashboardsModel().getId() == dashboard.getId()) {
                    particularDashboardTabs.add(_tab);
                }
            }
            // If chart is not present in any other tab then delete it from the dashboard too
            if (particularDashboardTabs.size() == 0) {
                Set<ChartsModel> dashboardCharts = dashboard.getCharts();
                dashboardCharts.remove(chart);
                dashboard.setCharts(dashboardCharts);

                Set<DashboardsModel> chartDashboards = chart.getDashboard();
                chartDashboards.remove(dashboard);
                chart.setDashboard(chartDashboards);

                chartsRepository.save(chart);
                dashboardsRepository.save(dashboard);
            }
        }

        // Other tab elements will be deleted normally
        tabElementsRepository.deleteById(elementId);

        return ResponseEntity.accepted().body("Success");
    }
}

