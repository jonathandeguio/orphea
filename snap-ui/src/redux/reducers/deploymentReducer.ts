import {
  ALL_DEPLOYMENT_DETAILS_FAIL,
  ALL_DEPLOYMENT_DETAILS_REQUEST,
  ALL_DEPLOYMENT_DETAILS_RESET,
  ALL_DEPLOYMENT_DETAILS_SUCCESS,
} from "../constants/deploymentConstants";

export const allDeploymentDetailsReducer = (
  state = { allDeployments: undefined, loading: true, error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case ALL_DEPLOYMENT_DETAILS_REQUEST:
      return { allDeployments: state.allDeployments, loading: true, error: false };

    case ALL_DEPLOYMENT_DETAILS_SUCCESS:
      return {
        allDeployments: action.payload,
        loading: false,
        error: false,
      };

    case ALL_DEPLOYMENT_DETAILS_FAIL:
      return { allDeployments: undefined, loading: false, error: action.payload };

    case ALL_DEPLOYMENT_DETAILS_RESET:
      return { allDeployments: undefined, loading: false, error: false };

    default:
      return state;
  }
};
