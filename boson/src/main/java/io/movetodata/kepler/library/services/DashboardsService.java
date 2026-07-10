package io.movetodata.kepler.library.services;

import io.movetodata.bezier.library.models.PipelineModel;
import io.movetodata.bezier.library.repository.PipelineRepository;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.kepler.library.DTOs.ChartSuggestedFilterDTO;
import io.movetodata.kepler.library.models.*;
import io.movetodata.kepler.library.repository.*;
import io.movetodata.kepler.library.specifications.ChartsSpecifications;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.ResourceRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.sharedutils.models.PageSettings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class DashboardsService {
    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final ResourceVersionDetailsRepository resourceVersionDetailsRepository;
    private final ResourceVersionsRepository resourceVersionsRepository;
    private final TabsRepository tabsRepository;
    private final DashboardsRepository dashboardsRepository;
    private final ChartsRepository chartsRepository;
    private final AuthzService authzService;
    private final PipelineRepository pipelineRepository;

    @Transactional
    public ResourceModel createDashboard(UUID userId, DashboardRequest dashboardRequest) {
        // TODO : add validation for chart id
        // Create in kitab
        ResourceModel resourceModel = resourceService.newResource(
                dashboardRequest.getName(),
                dashboardRequest.getDescription(),
                ResourceType.DASHBOARD,
                ResourceSubtype.DASHBOARD,
                userId,
                dashboardRequest.getParent()
        );

        List<TabsModel> tabs = new ArrayList<>();

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

        DashboardsModel dashboardsModel = new DashboardsModel();
        dashboardsModel.setId(resourceModel.getId());
        dashboardsModel.setVersionId(1L);
        dashboardsModel.setLastVersionedDate(new Date());
        dashboardsModel.setTabs(tabs);

        // A dashboard by default will always have at least 1 tab
        TabsModel newTab = new TabsModel();
        newTab.setName("Tab 1");
        tabs.add(newTab);
        newTab.setDashboardsModel(dashboardsModel);
        tabsRepository.save(newTab);
        dashboardsRepository.save(dashboardsModel);
        return resourceModel;
    }

    @Transactional
    public void manageChartsOnDashboard(ManageDashboardRequest manageDashboardRequest, UUID userId) throws Exception {
        ResourceVersionsModel dashboardVersion = resourceVersionsRepository.getReferenceById(manageDashboardRequest.getDashboardId());
        Long versionId = dashboardVersion.getLatestVersionId();
        DashboardKey dashboardId = new DashboardKey(manageDashboardRequest.getDashboardId(), versionId);

        if (!resourceService.existsById(manageDashboardRequest.getDashboardId())) {
            throw new Exception("Id " + manageDashboardRequest.getDashboardId() + " does not exist.");
        }
        if (!dashboardsRepository.existsById(dashboardId)) {
            throw new Exception("Id " + manageDashboardRequest.getDashboardId() + " does not exist.");
        }

        if (manageDashboardRequest.getAction() == null) {
            throw new Exception("Action is required, either add or remove.");
        }

        if ((!Objects.equals(manageDashboardRequest.getAction(), "add")) && (!Objects.equals(manageDashboardRequest.getAction(), "remove"))) {
            throw new Exception("Not a valid action type, use add or remove");
        }

        DashboardsModel dashboardsModel = dashboardsRepository.getReferenceById(dashboardId);
        Set<ChartsModel> chartsModels = dashboardsModel.getCharts();

        for (UUID chartId : manageDashboardRequest.getChartIds()) {

            ResourceVersionsModel chartVersion = resourceVersionsRepository.getReferenceById(chartId);
            ChartKey chartKey = new ChartKey(chartId, chartVersion.getLatestVersionId());

            if (!chartsRepository.existsById(chartKey)) {
                throw new Exception("Chart not found " + chartId);
            }

            // Charts editor access
            if (!authzService.isEditor(userId, chartId)) {
                throw new UnauthorizedException();
            }

            ChartsModel chartsModel = chartsRepository.getReferenceById(chartKey);

            if (chartsModels.contains(chartsModel)) {
                if (Objects.equals(manageDashboardRequest.getAction(), "remove")) {
                    chartsModel.setDashboard(null);

                    PipelineModel model = pipelineRepository.findBySourceDatasetAndSourceBranchAndTargetDatasetAndTargetBranch(chartId, chartsModel.getBranch(), dashboardsModel.getId(), dashboardsModel.getBranch());
                    pipelineRepository.delete(model);
                }
            } else {
                if (Objects.equals(manageDashboardRequest.getAction(), "add")) {
                    /*
                        dashboardModel -> Requested dashboard
                        chartsModels -> R D charts
                        chartsModel -> candidate chart
                        dashboard -> candidate dashboard set

                        add dM to dashboard and save to candidate chart
                     */
                    Set<DashboardsModel> dashboard = chartsModel.getDashboard();
//                    dashboardsRepository.findById(manageDashboardRequest.getDashboardId());
                    if (!dashboard.contains(dashboardsModel)) {
                        dashboard.add(dashboardsModel);

                        // Making pipeline link
                        PipelineModel model = new PipelineModel();
                        model.sourceDataset = chartId;
                        model.targetDataset = dashboardsModel.getId();
                        model.sourceBranch = chartsModel.getBranch();
                        model.targetBranch = dashboardsModel.getBranch();
                        model.repositoryId = null;
                        model.repositoryBranch = null;
                        model.scriptPath = null;
                        model.buildId = null;
                        model.status = BuildStatus.ACTIVE;
                        model.type = "dashboard";
                        model.setCreatedBy(userId);
                        model.setUpdatedBy(userId);
                        pipelineRepository.saveAndFlush(model);
                    }

                    chartsModel.setDashboard(dashboard);


                    chartsRepository.save(chartsModel);

                    /*
                        add candidate chart to charts of dashboard
                     */

                    Set<ChartsModel> charts = dashboardsModel.getCharts();
                    charts.add(chartsRepository.getReferenceById(chartKey));
                    dashboardsModel.setCharts(charts);
                    dashboardsRepository.save(dashboardsModel);
                }
            }
        }

        dashboardsRepository.save(dashboardsModel);

        // Updated by
        ResourceModel resourceModel = resourceService.getResourceModel(dashboardsModel.getId());
        resourceModel.setUpdatedAt(new Date());
        resourceModel.setUpdatedBy(userId);
        resourceService.save(resourceModel);

    }

    public Page<ResourceModel> getAllSuggestedCharts(@NotNull PageSettings pageSettings, ChartSuggestedFilterDTO filterCriteria) {
        Sort chatsSort = pageSettings.buildSort();
        Pageable suggestedChart = PageRequest.of(pageSettings.getPage(), pageSettings.getElementPerPage(), chatsSort);
        Specification<ResourceModel> spec = Specification.where(ChartsSpecifications.hasTypeChart());

        // Add conditions based on the filter criteria
        if (filterCriteria.getSearchText() != null && !filterCriteria.getSearchText().isEmpty()) {
            spec = spec.and(ChartsSpecifications.partialSearchOnId(filterCriteria.getSearchText()));
        }

        if (filterCriteria.getCreatedAtFrom() != null) {
            spec = spec.and(ChartsSpecifications.isCreatedFrom(filterCriteria.getCreatedAtFrom()));
        }

        if (filterCriteria.getCreatedAtTo() != null) {
            spec = spec.and(ChartsSpecifications.isCreatedTo(filterCriteria.getUpdatedAtTo()));
        }

        if (filterCriteria.getUpdatedAtFrom() != null) {
            spec = spec.and(ChartsSpecifications.isUpdatedFrom(filterCriteria.getUpdatedAtFrom()));
        }

        if (filterCriteria.getUpdatedAtTo() != null) {
            spec = spec.and(ChartsSpecifications.isUpdatedTo(filterCriteria.getUpdatedAtTo()));
        }

        if (!filterCriteria.getCreatedBy().isEmpty()) {
            spec = spec.and(ChartsSpecifications.createdBy(filterCriteria.getCreatedBy()));
        }
        return resourceRepository.findAll(spec, suggestedChart);
    }

}
