import { configureStore } from "@reduxjs/toolkit";

import {
  allUserDetailsReducer,
  isConnectAdminReducer,
  isGroupAdminReducer,
  isPlatformAdminReducer,
  isProjectAdminReducer,
  isUserAdminReducer,
  userDetailsReducer,
  loginMethod,
  userLoginReducer,
} from "./reducers/userReducers";

import { tokenStatus } from "./reducers/tokenReducer";

import { getAllGroupsReducer } from "./reducers/authReducer";

import { postApi } from "./FileQuery";
import { allArtifactDetailsReducer } from "./reducers/artifactReducers";
import { platformConfigReducer } from "./reducers/platformSettingsReducer";
import { allTriggerDetailsReducer } from "./reducers/triggerReducers";
import { allDeploymentDetailsReducer } from "./reducers/deploymentReducer";
import { allConfigurationDetailsReducer } from "./reducers/configurationReducer";
import { setLoginMethod } from "./actions/userActions";

const store = configureStore({
  reducer: {
    [postApi.reducerPath]: postApi.reducer,
    tokenStatus: tokenStatus,
    userLogin: userLoginReducer,
    userDetails: userDetailsReducer,
    userAdmin: isUserAdminReducer,
    projectAdmin: isProjectAdminReducer,
    groupAdmin: isGroupAdminReducer,
    platformAdmin: isPlatformAdminReducer,
    connectAdmin: isConnectAdminReducer,
    allGroups: getAllGroupsReducer,
    platformConfig: platformConfigReducer,
    allUserDetails: allUserDetailsReducer,
    allTriggerDetails: allTriggerDetailsReducer,
    allArtifactDetails: allArtifactDetailsReducer,
    allDeploymentDetails: allDeploymentDetailsReducer,
    allConfigurationDetails: allConfigurationDetailsReducer,
    loginMethod: loginMethod,
  },
  middleware: (getDefaultMiddleware: any) =>
    getDefaultMiddleware({
      serializableCheck: false,
    })
      .concat(postApi.middleware)
      .concat((store: any) => (next: any) => (action: any) => {
        next(action);
      }),
});
export default store;
