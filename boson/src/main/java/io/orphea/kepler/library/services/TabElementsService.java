package io.orphea.kepler.library.services;

import io.orphea.kepler.enums.TabElementOperationEnum;
import io.orphea.kepler.library.DTOs.ChangesDTO;
import io.orphea.kepler.library.models.*;
import io.orphea.kepler.library.repository.*;
import io.orphea.kitab.library.services.ResourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TabElementsService {
    private final TabsRepository tabsRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ChartsRepository chartsRepository;
    private final ChangesService changesService;
    private final TabElementsRepository tabElementsRepository;
    private final ResourceService resourceService;
    private final DashboardsRepository dashboardsRepository;
    private final TabsService tabsService;
    private final DashboardsService dashboardsService;

    @Transactional
    public TabElementsModel createTabElement(ManageElementUpdateRequest manageElementUpdateRequest, UUID userId) throws Exception {
        String elementType = manageElementUpdateRequest.getType();
        if (!((Objects.equals(elementType, "text")) || (Objects.equals(elementType, "chart")) || (Objects.equals(elementType, "header")) || (Objects.equals(elementType, "divider")) || (Objects.equals(elementType, "file")) || (Objects.equals(elementType, "markdown") || (Objects.equals(elementType, "editor"))))) {
            throw new Exception("Invalid element type");
        }

        TabsModel tabsModel = tabsRepository.findById(manageElementUpdateRequest.getTabId()).get();
        List<TabElementsModel> tabElements = tabsModel.getTabElements();

        for (TabElementsModel model : tabElements) {
            if (Objects.equals(elementType, "chart") && manageElementUpdateRequest.getData().equals(model.getData())) {
                removeTabElement(model.getId(), manageElementUpdateRequest.getDashboardId(), userId);
                log.error("Element was already attached to the tab, removing and re-adding it");
            }
        }

        TabElementsModel newElement = new TabElementsModel();
        if (Objects.equals(elementType, "chart")) {
            UUID chartID = UUID.fromString(manageElementUpdateRequest.getData());
            ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chartID);
            ChartKey chartKey = new ChartKey(chartID, chartVersion.getLatestVersionId());
            ChartsModel chart = chartsRepository.getReferenceById(chartKey);
            newElement.setDatasetId(String.valueOf(chart.getDatasetId()));
            dashboardsService.manageChartsOnDashboard(new ManageDashboardRequest(manageElementUpdateRequest.getDashboardId(), Collections.singletonList(chartID), "add"), userId);
            tabsService.manageCharts(new ManageTabRequest(manageElementUpdateRequest.getDashboardId(), manageElementUpdateRequest.getTabId(), Collections.singletonList(chartID), "add"), userId);
        } else {
            // ------- Recording Changes for latest version
            ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(manageElementUpdateRequest.getDashboardId()).getVersions().get(0);

            List<ChangesDTO> changes = new ArrayList<>();
            changes.add(new ChangesDTO("added", true));
            changes.add(new ChangesDTO(elementType, true));
            changes.add(new ChangesDTO("to", true));
            changes.add(new ChangesDTO(tabsModel.getName(), false));
            changesService.SaveChanges(changes, versionDetails, "add", userId);
            // -------------------------
        }

        newElement.setId(manageElementUpdateRequest.getElementId());
        newElement.setType(elementType);
        newElement.setData(manageElementUpdateRequest.getData());
        newElement.setPosition(manageElementUpdateRequest.getPosition());
        newElement.setTabsModel(tabsModel);
        tabElementsRepository.save(newElement);

        tabElements.add(newElement);
        tabsModel.setTabElements(tabElements);
        tabsModel.setUpdatedBy(userId);
        tabsModel.setUpdatedAt(new Date());
        tabsRepository.save(tabsModel);

        return newElement;
    }

    public List<TabElementsModel> getTabElements(UUID tabId) {
        TabsModel tabsModel = tabsRepository.getReferenceById(tabId);
        return tabsModel.getTabElements();
    }

    @Transactional
    public void updateTabElement(ManageElementUpdateRequest manageElementUpdateRequest, UUID dashboardId, UUID userId) throws Exception {
        UUID elementId = manageElementUpdateRequest.getElementId();

        if (!tabElementsRepository.existsById(elementId)) {
            throw new Exception("Element does not exist");
        }
        TabElementsModel tabElementsModel = tabElementsRepository.findById(elementId).get();
        if (!(manageElementUpdateRequest.getPosition() == null)) {
            tabElementsModel.setPosition(manageElementUpdateRequest.getPosition());
        }

        if (!manageElementUpdateRequest.getData().equals("")) {
            tabElementsModel.setData(manageElementUpdateRequest.getData());
        }

        // ------- Recording Changes for latest version
        TabsModel tabsModel = tabsRepository.getReferenceById(manageElementUpdateRequest.getTabId());
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("updated", true));
        changes.add(new ChangesDTO(manageElementUpdateRequest.getType(), true));
        changes.add(new ChangesDTO("at", true));
        changes.add(new ChangesDTO(tabsModel.getName(), false));
        changesService.SaveChanges(changes, versionDetails, "update", userId);
        // -------------------------
        tabElementsRepository.save(tabElementsModel);
    }

    @Transactional
    public void updateTabElements(List<ManageElementUpdateRequest> manageElementUpdateRequestList, UUID dashboardId, UUID tabId, UUID userId) throws Exception {
        for (ManageElementUpdateRequest manageElementUpdateRequest : manageElementUpdateRequestList) {
            if (manageElementUpdateRequest.getOperation().equals(TabElementOperationEnum.CREATE)) {
                createTabElement(manageElementUpdateRequest, userId);
            } else if (manageElementUpdateRequest.getOperation().equals(TabElementOperationEnum.DELETE)) {
                removeTabElement(manageElementUpdateRequest.getElementId(), dashboardId, userId);
            } else {
                updateTabElement(manageElementUpdateRequest, dashboardId, userId);
            }
        }
    }

    @Transactional
    public void removeTabElement(UUID elementId, UUID dashboardId, UUID userId) {
        TabElementsModel tabElementsModel = tabElementsRepository.getReferenceById(elementId);
        TabsModel tab = tabElementsModel.getTabsModel();
        if (tabElementsModel.getType().equals("chart")) {
            try {
                UUID chartID = UUID.fromString(tabElementsModel.getData());
                ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chartID);
                ChartKey chartKey = new ChartKey(chartID, chartVersion.getLatestVersionId());
                ChartsModel chart = chartsRepository.getReferenceById(chartKey);

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
                if (particularDashboardTabs.isEmpty()) {
                    Set<ChartsModel> dashboardCharts = dashboard.getCharts();
                    dashboardCharts.remove(chart);
                    dashboard.setCharts(dashboardCharts);

                    Set<DashboardsModel> chartDashboards = chart.getDashboard();
                    chartDashboards.remove(dashboard);
                    chart.setDashboard(chartDashboards);

                    chartsRepository.save(chart);
                    dashboardsRepository.save(dashboard);
                }
            } catch (Exception e) {
                // In case of logical error While removing chart then at least let the tabElement to be removed\
                log.error(e.getMessage());
            }

        } else {
            // ------- Recording Changes for latest version
            ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

            List<ChangesDTO> changes = new ArrayList<>();
            changes.add(new ChangesDTO("removed", true));
            changes.add(new ChangesDTO(tabElementsModel.getType(), true));
            changes.add(new ChangesDTO("from", true));
            changes.add(new ChangesDTO(tab.getName(), false));
            changesService.SaveChanges(changes, versionDetails, "remove", userId);
            // -------------------------
        }

        // Other tab elements will be deleted normally
        tabElementsRepository.deleteById(elementId);
    }

}
