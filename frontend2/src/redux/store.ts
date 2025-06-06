import { configureStore } from "@reduxjs/toolkit";

import {
  userLoginReducer,
  userDetailsReducer,
  allUserDetailsReducer,
  isUserAdminReducer,
  isProjectAdminReducer,
  isGroupAdminReducer,
  userByIdReducer,
} from "./reducers/userReducers";

import {
  projectListReducer,
  projectCreateReducer,
  folderDetailsReducer,
  folderCreateReducer,
} from "./reducers/projectReducers";

import { tokenCreateReducer, tokenListReducer, tokenStatus } from "./reducers/tokenReducer";

import {
  repoTreeReducer,
  repoCreateReducer,
  repoDetailsReducer,
  repoNodeDataReducer,
} from "./reducers/repoReducers";

import {
  pinelineDetailsReducer,
  schemeDetailsReducer,
} from "./reducers/pipelineReducers";
import { contextMenuReducer } from "./reducers/contextMenuReducer";

// import {
//   datasetCreateReducer,
//   datasetBranchReducer,
//   datasetTableReducer,
//   datasetTransactionsReducer,
//   datasetBuildHistoryReducer,
//   datasetBuildReducer,
//   datasetSyncReducer,
//   datasetBuildSpecReducer,
//   datasetBuildLogReducer,
//   datasetDetailsReducer,
//   igniteDatasetLinkReducer,
//   datasetChartExistReducer,
//   datasetSchemaChangeReducer,
//   importDatasetReducer,
//   columnStatsPaneReducer,
//   datasetTableRowReducer,
//   existDatasetTransactionsReducer,
// } from "./reducers/datasetReducers";

import { editorPaneReducer, editorVariables } from "./reducers/editorReducer";

import {
  agentListReducer,
  agentDeleteReducer,
  agentCreateReducer,
  agentSourcesListReducer,
  agentLinksListReducer,
} from "./reducers/agentReducers";

import {
  sourceListReducer,
  sourceDeleteReducer,
  sourceCreateReducer,
  sourceLinksListReducer,
} from "./reducers/sourceReducers";

import {
  linkListReducer,
  linkDeleteReducer,
  linkCreateReducer,
} from "./reducers/linkReducers";
import {
  userRolesOfProjectReducer,
  getAllGroupsReducer,
  getAllPermissionMappingReducer,
  getAllRolesReducer,
  getGroupByIdReducer,
  getPermissionsMappingByResourceIdReducer,
  groupCreateReducer,
  groupDeleteReducer,
  manageGroupReducer,
  userDeleteReducer,
  userResourcePermissionsReducer,
  deletePermissionsMappingReducer,
  networkErrorReducer,
} from "./reducers/authReducer";

// import { getChartReducer } from "./reducers/chartReducers";
import { getQueryReducer } from "./reducers/queryReducer";
// import { keplerChartDataReducer } from "./reducers/keplerChartDataReducer";

const store = configureStore({
  reducer: {
    tokenStatus: tokenStatus,
    userLogin: userLoginReducer,
    networkError: networkErrorReducer,
    userDetails: userDetailsReducer,
    user: isUserAdminReducer,
    projectAdmin: isProjectAdminReducer,
    groupAdmin: isGroupAdminReducer,
    groupCreate: groupCreateReducer,
    allGroups: getAllGroupsReducer,
    groupDelete: groupDeleteReducer,
    groupDetails: getGroupByIdReducer,
    userDetailById: userByIdReducer,
    userDelete: userDeleteReducer,
    manageGroupDetails: manageGroupReducer,
    allRoles: getAllRolesReducer,
    allPermissionMapping: getAllPermissionMappingReducer,
    permissionsMappingByResourceId: getPermissionsMappingByResourceIdReducer,
    // dataset: importDatasetReducer,
    userRoles: userRolesOfProjectReducer,
    userResourcePermissions: userResourcePermissionsReducer,
    deleteMapping: deletePermissionsMappingReducer,

    allUserDetails: allUserDetailsReducer,

    schemaDetails: schemeDetailsReducer,

    projectList: projectListReducer,
    projectCreate: projectCreateReducer,

    folderDetails: folderDetailsReducer,
    folderCreate: folderCreateReducer,

    repoDetails: repoDetailsReducer,
    repoCreate: repoCreateReducer,
    repoTree: repoTreeReducer,
    repoNodeData: repoNodeDataReducer,

    pipelineDetails: pinelineDetailsReducer,

    // datasetCreate: datasetCreateReducer,
    // datasetTable: datasetTableReducer,
    // datasetTableRow: datasetTableRowReducer,
    // headerLink: headerLinkReducer,
    // datasetBranch: datasetBranchReducer,
    // datasetTransactions: datasetTransactionsReducer,
    // datasetBuildHistory: datasetBuildHistoryReducer,
    // datasetBuildSpec: datasetBuildSpecReducer,
    // datasetBuild: datasetBuildReducer,
    // datasetSync: datasetSyncReducer,
    // datasetBuildLog: datasetBuildLogReducer,
    // datasetDetails: datasetDetailsReducer,
    // columnStatsPane: columnStatsPaneReducer,
    // transactionExist: existDatasetTransactionsReducer,

    // igniteDatasetLink: igniteDatasetLinkReducer,
    // datasetChartExist: datasetChartExistReducer,
    // // chartCreate : chartCreateReducer,
    // datasetSchemaChange: datasetSchemaChangeReducer,
    editorPane: editorPaneReducer,
    editorVariables: editorVariables,

    tokenCreate: tokenCreateReducer,
    tokenList: tokenListReducer,

    agentList: agentListReducer,
    agentDelete: agentDeleteReducer,
    agentCreate: agentCreateReducer,
    agentSourcesList: agentSourcesListReducer,
    agentLinksList: agentLinksListReducer,

    sourceList: sourceListReducer,
    sourceDelete: sourceDeleteReducer,
    sourceCreate: sourceCreateReducer,
    sourceLinksList: sourceLinksListReducer,

    linkList: linkListReducer,
    linkDelete: linkDeleteReducer,
    linkCreate: linkCreateReducer,

    // chart: getChartReducer,
    query: getQueryReducer,
    // keplerChartData: keplerChartDataReducer,

    contextMenu: contextMenuReducer
  },
  middleware: (getDefaultMiddleware: any) => getDefaultMiddleware({
    serializableCheck: false
  })
})
export default store;