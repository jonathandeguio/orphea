import axios from "axios";
import {
  ALL_DEPLOYMENT_DETAILS_FAIL,
  ALL_DEPLOYMENT_DETAILS_REQUEST,
  ALL_DEPLOYMENT_DETAILS_SUCCESS,
} from "../constants/deploymentConstants";

export const getAllDeploymentDetails =
  () => async (dispatch: any, getState: any) => {
    try {
      const { allDeploymentDetails } = getState();

      // Avoid fetching if data already exists
      if (allDeploymentDetails && allDeploymentDetails.allDeployments?.length > 0) {
        return allDeploymentDetails.allDeployments;
      }

      dispatch({
        type: ALL_DEPLOYMENT_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/deployments/all`);

      dispatch({
        type: ALL_DEPLOYMENT_DETAILS_SUCCESS,
        payload: data,
      });

      return data;
    } catch (error) {
      dispatch({
        type: ALL_DEPLOYMENT_DETAILS_FAIL,
        payload:
          (error as any).response && (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };
