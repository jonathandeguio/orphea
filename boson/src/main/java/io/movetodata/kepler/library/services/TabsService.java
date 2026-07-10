package io.movetodata.kepler.library.services;

import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.kepler.library.DTOs.ChangesDTO;
import io.movetodata.kepler.library.DTOs.TabCustomizeDTO;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.*;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.sharedutils.Exceptions.BadRequestException;
import io.movetodata.sharedutils.Exceptions.UnsupportedOperationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TabsService {
    private final DashboardsRepository dashboardsRepository;
    private final TabsRepository tabsRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final ChartsRepository chartsRepository;
    private final ChangesService changesService;
    private final TabElementsRepository tabElementsRepository;
    private final ResourceService resourceService;
    private final PipelineRepository pipelineRepository;

    public void updateTabOrder(UUID tabId, int newPosition) {
        TabsModel tab = tabsRepository.getReferenceById(tabId);

        DashboardsModel dashboard = tab.getDashboardsModel();
        List<TabsModel> tabs = dashboard.getTabs();

        // Handle deletion
        if (newPosition < 0) {
            tabs.remove(tab);
        } else {
            // Handle addition
            if (newPosition >= tabs.size()) {
                tabs.add(tab);
            } else {
                // Handle reordering
                tabs.remove(tab);
                tabs.add(newPosition, tab);
            }
        }

        updateOrderInTabsList(tabs);
        dashboard.setTabs(tabs);
        dashboard.getTabs().addAll(tabs);

        dashboardsRepository.save(dashboard);
    }

    public void updateOrderInTabsList(List<TabsModel> tabs) {
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setTabOrder(i);
        }
    }

    @Transactional
    public void deleteTab(UUID id, UUID tabId, UUID userId) throws Exception {
        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(id);
        Long versionId = dashboardVersion.getLatestVersionId();
        DashboardKey dashboardId = new DashboardKey(id, versionId);

        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);

        if (dashboardsModel.getTabs().size() == 1) {
            throw new UnsupportedOperationException("Can't delete all tabs");
        }

        // Handling tab deletion from list
        updateTabOrder(tabId, -1);

        Set<ChartsModel> charts = tabsRepository.getReferenceById(tabId).getChartsForTabs();
        List<DashboardsModel> dashboardsToRemove = new ArrayList<>();
        List<ChartsModel> chartsToRemove = new ArrayList<>();
        for (ChartsModel chart : charts) {
            // Chart tabs can be from multiple dashboards, filter only current dash tabs
            Set<TabsModel> beforechartTabs = chart.getTabsForCharts();
            Set<TabsModel> chartTabs = new HashSet<>();
            beforechartTabs.forEach(tabsModel -> {
                if (tabsModel.getDashboardsModel().getId() == id) {
                    log.info("FOUND");
                    chartTabs.add(tabsModel);
                }
            });

            if (chartTabs.size() == 1 && chartTabs.contains(tabsRepository.getReferenceById(tabId))) {
                dashboardsToRemove.add(dashboardsRepository.getReferenceById(new DashboardKey(id, versionId)));
                chartsToRemove.add(chart);
            }
        }

        tabsRepository.deleteById(tabId);

        for (int _i = 0; _i < dashboardsToRemove.size(); _i++) {
            ChartsModel chart = chartsToRemove.get(_i);
            DashboardsModel dashboard = dashboardsToRemove.get(_i);

            dashboard.getCharts().remove(chart);
            chart.getDashboard().remove(dashboard);
            dashboardsRepository.save(dashboard);
            chartsRepository.save(chart);
        }

        // ------- Recording Changes for latest version
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(id).getVersions().get(0);
        List<ChangesDTO> changes = new ArrayList<>();

        changes.add(new ChangesDTO("removed", true));
        changes.add(new ChangesDTO("tab", true));
        changes.add(new ChangesDTO("and", true));
        changes.add(new ChangesDTO("its", true));
        changes.add(new ChangesDTO("elements", true));
        changesService.SaveChanges(changes, versionDetails, "remove", userId);
        // -------------------------


    }

    @Transactional
    public void updateTabConfig(UUID tabId, UUID userId, TabCustomizeDTO tabCustomizeDTO) {
        TabsModel tab = tabsRepository.getReferenceById(tabId);

        // ------- Recording Changes for latest version
        UUID dashboardId = tab.getDashboardsModel().getId();
        Long versionId = resourceVersionsRepository.getReferenceById(dashboardId).getLatestVersionId();
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO("Tab", true));
        changes.add(new ChangesDTO("customize", true));
        changes.add(new ChangesDTO("options", true));
        changes.add(new ChangesDTO("updated", true));
        changesService.SaveChanges(changes, versionDetails, "Customize", userId);
        // -------------------------

        tab.setChartHeadingTextColor(tabCustomizeDTO.getChartHeadingTextColor());
        tab.setChartHeadingBg(tabCustomizeDTO.getChartHeadingBg());
        tab.setChartBodyBg(tabCustomizeDTO.getChartBodyBg());
        tab.setCanvasBg(tabCustomizeDTO.getCanvasBg());
        tab.setPageBg(tabCustomizeDTO.getPageBg());

        tab.setTopPadding(tabCustomizeDTO.getTopPadding());
        tab.setRightPadding(tabCustomizeDTO.getRightPadding());
        tab.setBottomPadding(tabCustomizeDTO.getBottomPadding());
        tab.setLeftPadding(tabCustomizeDTO.getLeftPadding());

        tab.setPreventCollision(tabCustomizeDTO.isPreventCollision());
        tab.setAllowOverlap(tabCustomizeDTO.isAllowOverlap());

        tab.setUpdatedAt(new Date());
        tab.setUpdatedBy(userId);

        tabsRepository.save(tab);
    }

    @Transactional
    public void manageCharts(ManageTabRequest manageTabRequest, UUID userId) throws Exception {
        TabsModel tabsModel = tabsRepository.findById(manageTabRequest.getTabId()).orElseThrow();

        Set<ChartsModel> chartsModels = tabsModel.getChartsForTabs();

        // ------- Recording Changes for latest version
        UUID dashboardId = tabsModel.getDashboardsModel().getId();
        Long versionId = resourceVersionsRepository.getReferenceById(dashboardId).getLatestVersionId();
        ResourceVersionDetailsModel versionDetails = resourceVersionsRepository.getReferenceById(dashboardId).getVersions().get(0);

        List<ChangesDTO> changes = new ArrayList<>();
        changes.add(new ChangesDTO(manageTabRequest.getAction(), true));
        changes.add(new ChangesDTO("chart", true));
        changes.add(new ChangesDTO(resourceService.getResourceModel(manageTabRequest.getChartIds().get(0)).getName(), false));
        changes.add(new ChangesDTO("from", true));
        changes.add(new ChangesDTO(tabsModel.getName(), false));
        changesService.SaveChanges(changes, versionDetails, manageTabRequest.getAction(), userId);
        // -------------------------

        for (UUID chartId : manageTabRequest.getChartIds()) {

            ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chartId);
            ChartKey chartKey = new ChartKey(chartId, chartVersion.getLatestVersionId());

            if (!chartsRepository.existsById(chartKey)) {
                throw new Exception("Chart not found " + chartId);
            }

            ChartsModel chartsModel = chartsRepository.getReferenceById(chartKey);

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

                    Set<TabsModel> allTabsOfChart = chartsModel.getTabsForCharts();
                    allTabsOfChart.remove(tabsModel);
                    chartsModel.setTabsForCharts(allTabsOfChart);

                    // If allTabsForChart got empty, hence the chart need to be removed from dashboardModel as well
                    if (allTabsOfChart.isEmpty()) {
                        DashboardsModel dashboard = tabsModel.getDashboardsModel();
                        dashboard.getCharts().remove(chartsModel);
                        chartsModel.getDashboard().remove(dashboard);

                        PipelineModel model = pipelineRepository.findBySourceDatasetAndSourceBranchAndTargetDatasetAndTargetBranch(chartId, chartsModel.getBranch(), dashboard.getId(), dashboard.getBranch());
                        pipelineRepository.delete(model);

                        dashboardsRepository.save(dashboard);
                    }


                    chartsRepository.save(chartsModel);
                } else {
                    throw new BadRequestException("Chart already added " + chartId);
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
                    tab.add(tabsModel);
                    chartsModel.setTabsForCharts(tab);

                    chartsRepository.save(chartsModel);

                    /*
                        add candidate chart to charts of dashboard

                     */
                    Set<ChartsModel> charts = tabsModel.getChartsForTabs();
                    charts.add(chartsRepository.getReferenceById(chartKey));
                    tabsModel.setChartsForTabs(charts);
                    tabsRepository.save(tabsModel);
                } else {
                    throw new Exception("Chart not there " + chartId);
                }
            }
        }

        tabsModel.setUpdatedAt(new Date());
        tabsModel.setUpdatedBy(userId);


        tabsRepository.save(tabsModel);
    }

}
