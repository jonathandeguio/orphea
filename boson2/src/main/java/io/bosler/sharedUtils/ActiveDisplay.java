package io.bosler.sharedUtils;

import io.bosler.kepler.library.models.ChartsModel;
import io.bosler.kepler.library.models.DashboardsModel;
import io.bosler.kitab.library.models.BranchModel;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.models.TransactionModel;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ActiveDisplay {
    private final UserService userService;
    private final AuthzService authzService;

    public List<FolderModel> statusDisplay(UUID userId, List<FolderModel> folderModels, String status) {

        List<FolderModel> activeFolderModel = new ArrayList<>();
        for (FolderModel folderModel : folderModels) {
            if (folderModel.getStatus().equals(status) && authzService.isViewer(userId, folderModel.getId())) {
                activeFolderModel.add(folderModel);
            }
        }
        activeFolderModel.sort(Comparator.comparing(FolderModel::getName));
        return activeFolderModel;
    }

    // TODO : not working
    public List<TransactionModel> activeDisplayTransactionModel(UUID userId, List<TransactionModel> transactionModels) {

        List<TransactionModel> activeTransactionModel = new ArrayList<>();
        for (TransactionModel transactionModel : transactionModels) {
            if (transactionModel.getStatus().equals("active") && authzService.isViewer(userId, transactionModel.getId())) {
                activeTransactionModel.add(transactionModel);
            }
        }
        activeTransactionModel.sort(Comparator.comparing(TransactionModel::getBuildId));
        return activeTransactionModel;
    }


    // TODO : not working
    public List<BranchModel> activeDisplayBranchModel(UUID userId, List<BranchModel> branchModels) {

        List<BranchModel> activeBranchModel = new ArrayList<>();
        for (BranchModel branchModel : branchModels) {
            if (authzService.isViewer(userId, branchModel.getId())) {
                activeBranchModel.add(branchModel);
            }
        }
        activeBranchModel.sort(Comparator.comparing(BranchModel::getBranch));
        return activeBranchModel;
    }

    public List<ChartsModel> activeDisplayChartsModel(UUID userId, List<ChartsModel> chartsModels) {

        List<ChartsModel> activeChartsModel = new ArrayList<>();
        for (ChartsModel chartsModel : chartsModels) {
            if (authzService.isViewer(userId, chartsModel.getId())) {
                activeChartsModel.add(chartsModel);
            }
        }
        activeChartsModel.sort(Comparator.comparing(ChartsModel::getParent));
        return activeChartsModel;
    }

    public List<DashboardsModel> activeDisplayDashboardsModel(UUID userId, List<DashboardsModel> dashboardsModels) {

        List<DashboardsModel> activeDashboardsModel = new ArrayList<>();
        for (DashboardsModel dashboardsModel : dashboardsModels) {
            if (authzService.isViewer(userId, dashboardsModel.getId())) {
                activeDashboardsModel.add(dashboardsModel);
            }
        }
        activeDashboardsModel.sort(Comparator.comparing(DashboardsModel::getName));
        return activeDashboardsModel;
    }


}
