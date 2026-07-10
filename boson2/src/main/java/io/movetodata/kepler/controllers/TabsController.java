package io.movetodata.kepler.controllers;

import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.ChartsRepository;
import io.movetodata.kepler.library.repository.DashboardsRepository;
import io.movetodata.kepler.library.repository.TabElementsRepository;
import io.movetodata.kepler.library.repository.TabsRepository;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.ActiveDisplay;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final FolderRepository folderRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final ActiveDisplay activeDisplay;
    private final AuthzService authzService;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "Get all tabs")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllTabs(Principal principal) {
        List<TabsModel> tabs = tabsRepository.findAll();

        return ResponseEntity.accepted().body(tabs);
    }

    @Operation(summary = "This can be used to get charts for particular tab")
    @GetMapping("/getTabCharts/{id}")
    public ResponseEntity<Object> getTabCharts(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;
//        if (!authzService.isViewer(userId, id)) {
//            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<ChartsModel> charts = tabsRepository.getById(id).getChartsForTabs();

        return ResponseEntity.accepted().body(charts);
    }

    @Operation(summary = "Update Tab")
    @PostMapping("/updateTab/{id}/{newName}")
    public ResponseEntity<Object> updateTab(Principal principal, @PathVariable("id") UUID id, @PathVariable("newName") String newName) {

        UUID userId = userService.getUser(principal.getName()).id;
//        if (!authzService.isViewer(userId, id)) {
//            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//        }

        if (!tabsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TabsModel tab = tabsRepository.getById(id);
        tab.setName(newName);
        tabsRepository.save(tab);

        return ResponseEntity.accepted().body("DONE");
    }

    @Operation(summary = "This can be used to get tabs by dashboard Id.")
    @GetMapping("/getDashboardTabs/{id}")
    public ResponseEntity<Object> getDashboardTabs(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        List<TabsModel> tabs = dashboardsModel.getTabs();
        tabs.sort(new Comparator<TabsModel>() {
            @Override
            public int compare(TabsModel m1, TabsModel m2) {
                return m1.getCreatedAt().compareTo(m2.getCreatedAt());
            }});
        return ResponseEntity.accepted().body(tabs);
    }

    @Operation(summary = "Add tab to dashbaord.")
    @PostMapping("/addTabToDashboard/{id}")
    public ResponseEntity<Object> addTabToDashboard(Principal principal, @PathVariable("id") UUID id) {

        UUID userId = userService.getUser(principal.getName()).id;
//        if (!authzService.isViewer(userId, id)) {
//            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//        }
        System.out.println("HERE");
        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!dashboardsRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        List<TabsModel> tabs = dashboardsModel.getTabs();
        TabsModel newTab = new TabsModel();
        Integer nextTabNumber = tabs.size() + 1;
        newTab.setName("Tab" + nextTabNumber);
        tabs.add(newTab);

        dashboardsModel.setTabs(tabs);

        dashboardsRepository.save(dashboardsModel);
        newTab.setDashboardsModel(dashboardsModel);
        tabsRepository.save(newTab);

        return ResponseEntity.accepted().body(newTab);
    }

    @Operation(summary = "Delete tab from dashboard.")
    @DeleteMapping("/removeTabFromDashboard/{dashboardId}/{tabId}")
    public ResponseEntity<Object> removeTabFromDashboard(Principal principal, @PathVariable("dashboardId") UUID id, @PathVariable("tabId") UUID tabId) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!dashboardsRepository.existsById(id) || !tabsRepository.existsById(tabId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getById(id);
        List<TabsModel> tabs = dashboardsModel.getTabs();
        System.out.println(tabs.size());
        if (tabs.size() == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Set<ChartsModel> charts = tabsRepository.getById(tabId).getChartsForTabs();
        List<DashboardsModel> dashboardsToRemove = new ArrayList<>();
        List<ChartsModel> chartsToRemove = new ArrayList<>();
        for (ChartsModel chart : charts) {
            // Chart tabs can be from multiple dashs, filter only current dash tabs
            Set<TabsModel> beforechartTabs = chart.getTabsForCharts();
            Set<TabsModel> chartTabs = new HashSet<>();
            beforechartTabs.forEach(tabsModel -> {
                if (tabsModel.getDashboardsModel().getId() == id) {
                    log.info("FOUND");
                    chartTabs.add(tabsModel);
                }
            });
//            chartTabs.stream().filter(tabsModel -> tabsModel.getDashboardsModel().equals(dashboardsRepository.getById(id)));
            log.info("CHART : ");
            log.info(chart.getId().toString());
            System.out.println("SIZE : " + chartTabs.size());
            // Need to iterate over all dashboard tabs it is added to;
//            Set<DashboardsModel> chartDash = chart.getDashboard();
//            for (DashboardsModel dash : chartDash) {
//                List<TabsModel> dashTabs = dash.getTabs();
//                for (TabsModel tab : dashTabs) {
//                    if (tab.getChartsForTabs().contains(chart))
//                }
//
//            }
            if (chartTabs.size() == 1 && chartTabs.contains(tabsRepository.getById(tabId))) {
                // It will always contain just one dashboard
                log.info("GOT A DASH");
//                Set<DashboardsModel> dashboard = chart.getDashboard();
//                log.info(dashboard.stream().findAny().get().getId().toString());
                dashboardsToRemove.add(dashboardsRepository.getById(id));
                chartsToRemove.add(chart);
            }
        }

        try {
            TabsModel currentTab = tabsRepository.getById(tabId);
            List<TabElementsModel> tabElements = currentTab.getTabElements();
            System.out.print("ON ELEMENTS");
            for(TabElementsModel element : tabElements) {
                tabElementsRepository.delete(element);
            }
            tabElements = new ArrayList<>();
            currentTab.setTabElements(tabElements);
            tabsRepository.save(currentTab);
            System.out.println("DELETED TAB ELEMENTS");
            tabsRepository.deleteById(tabId);
//            TabsModel tab = tabsRepository.findById(tabId).orElseThrow(() -> new IllegalArgumentException("Invalid tab Id:" + tabId));;
//            tabsRepository.delete(tab);
        }
        catch (Exception e) {
            log.error("ERROR PRESENT BELOW");
            log.error(e.getMessage());
        }

        if (tabsRepository.existsById(tabId)) {
            log.info("TAB ID EXIST", tabId);
            return ResponseEntity.internalServerError().body("Failed to Delete");
        }

        for (Integer _i = 0; _i < dashboardsToRemove.size(); _i++) {
            ChartsModel chart = chartsToRemove.get(_i);
            DashboardsModel dashboard = dashboardsToRemove.get(_i);

            dashboard.getCharts().remove(chart);
            chart.getDashboard().remove(dashboard);
            dashboardsRepository.save(dashboard);
            chartsRepository.save(chart);
        }

//        for (DashboardsModel dashboard : dashboardsToRemove) {
//            Set<ChartsModel> dashboardCharts = dashboard.getCharts();
//            for (ChartsModel chart : charts) {
//                if (dashboardCharts.contains(chart)) {
//                    dashboardCharts.remove(chart);
//                    chart.getDashboard().remove(dashboard);
//
//                    log.info(chart.getId().toString());
//                    log.info(dashboard.getId().toString());
//                    log.info("--------------------------");
//                }
//                chartsRepository.save(chart);
//            }
//            dashboardsRepository.save(dashboard);
//        }

        return ResponseEntity.accepted().body("Success Deleted");
    }

    @Operation(summary = "Add or Remove charts from tab")
    @PostMapping("/manageCharts")
    public ResponseEntity<Object> manageCharts(@Valid @RequestBody ManageTabRequest manageTabRequest, Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;
        if(!tabsRepository.existsById(manageTabRequest.getTabId())){
            return new ResponseEntity<>("Id " + manageTabRequest.getTabId() + " does not exist.", HttpStatus.NOT_FOUND);
        }

        if (manageTabRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageTabRequest.getAction(), "add")) && (!Objects.equals(manageTabRequest.getAction(), "remove"))) {
            return new ResponseEntity<>("Not a valid action type, use add or remove", HttpStatus.BAD_REQUEST);
        }

        TabsModel tabsModel = tabsRepository.findById(manageTabRequest.getTabId()).orElseThrow();

        Set<ChartsModel> chartsModels = tabsModel.getChartsForTabs();

        for (UUID chartId : manageTabRequest.getChartIds()) {

            if (!chartsRepository.existsById(chartId)) {
                return new ResponseEntity<>("Chart not found " + chartId, HttpStatus.BAD_REQUEST);
            }

            ChartsModel chartsModel = chartsRepository.getById(chartId);

            if (chartsModels.contains(chartsModel)) {
                if (Objects.equals(manageTabRequest.getAction(), "remove")) {
                    /*
                        Remove chartsModel from chartsModels
                        Remove tab from chart
                     */

                    chartsModels.remove(chartsModel);
                    tabsModel.setChartsForTabs(chartsModels);
                    tabsModel.setUpdatedAt(new Date());
                    tabsModel.setUpdatedBy(userId);
                    tabsRepository.save(tabsModel);

                    Set<TabsModel> allTabsOfChart =  chartsModel.getTabsForCharts();
                    allTabsOfChart.remove(tabsModel);
                    chartsModel.setTabsForCharts(allTabsOfChart);
                    chartsModel.setUpdatedAt(new Date());
                    chartsModel.setUpdatedBy(userId);
                    chartsRepository.save(chartsModel);
                }
                else {
                    return new ResponseEntity<>("Chart already added " + chartId, HttpStatus.BAD_REQUEST);
                }
            } else {
                if (Objects.equals(manageTabRequest.getAction(), "add")) {
                    /*
                        dashboardModel -> Requested dashboard
                        chartsModels -> R D charts
                        chartsModel -> candidate chart
                        dashboard -> candidate dashboard set

                        add dM to dashboard and save to candidate chart
                     */
                    Set<TabsModel> tab = chartsModel.getTabsForCharts();
//                    dashboardsRepository.findById(manageDashboardRequest.getDashboardId());
                    if (!tab.contains(tabsModel))
                        tab.add(tabsModel);
                    chartsModel.setTabsForCharts(tab);

                    chartsModel.setUpdatedAt(new Date());
                    chartsModel.setUpdatedBy(userId);

                    chartsRepository.save(chartsModel);

                    /*
                        add candidate chart to charts of dashboard

                     */
                    Set <ChartsModel> charts = tabsModel.getChartsForTabs();
                    charts.add(chartsRepository.getById(chartsModel.getId()));
                    tabsModel.setChartsForTabs(charts);
                    tabsRepository.save(tabsModel);
                }
                else {
                    return new ResponseEntity<>("Chart not there " + chartId, HttpStatus.BAD_REQUEST);
                }
            }
        }

        tabsModel.setUpdatedAt(new Date());
        tabsModel.setUpdatedBy(userId);


        TabsModel tabsModelSaved = tabsRepository.save(tabsModel);

        return new ResponseEntity<>("Tab updated", HttpStatus.OK);
    }

    @Operation(summary = "Add or Remove elements from tab")
    @PostMapping("/manageElements")
    public ResponseEntity<Object> manageElements(@Valid @RequestBody ManageElementRequest manageElementRequest, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if(!tabsRepository.existsById(manageElementRequest.getTabId())){
            return new ResponseEntity<>("Id " + manageElementRequest.getTabId() + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if(!tabElementsRepository.existsById(manageElementRequest.getElementId())){
            return new ResponseEntity<>("Id " + manageElementRequest.getElementId() + " does not exist.", HttpStatus.NOT_FOUND);
        }

        if (manageElementRequest.getAction() == null) {
            return new ResponseEntity<>("Action is required, either add or remove.", HttpStatus.BAD_REQUEST);
        }

        if ((!Objects.equals(manageElementRequest.getAction(), "add")) && (!Objects.equals(manageElementRequest.getAction(), "remove"))) {
            return new ResponseEntity<>("Not a valid action type, use add or remove", HttpStatus.BAD_REQUEST);
        }

        TabsModel tabModel = tabsRepository.findById(manageElementRequest.getTabId()).orElseThrow();
        List<TabElementsModel> elementsModels = tabModel.getTabElements();
        TabElementsModel element = tabElementsRepository.getById(manageElementRequest.getElementId());
        Boolean deleteRequest = false;

        if (elementsModels.contains(element)) {
            if (manageElementRequest.getAction() == "add") {
                return new ResponseEntity<>("Element already present", HttpStatus.BAD_REQUEST);
            }
            else {
                // remove element and delete it
                elementsModels.remove(element);
                tabModel.setTabElements(elementsModels);
                deleteRequest = true;
            }
        }
        else {
            if (manageElementRequest.getAction() == "add") {
                elementsModels.add(element);
                tabModel.setTabElements(elementsModels);
            }
            else {
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
}

