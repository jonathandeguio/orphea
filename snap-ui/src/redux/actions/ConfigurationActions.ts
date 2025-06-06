import axios from "axios";
import {
  ALL_CONFIGURATION_DETAILS_FAIL,
  ALL_CONFIGURATION_DETAILS_REQUEST,
  ALL_CONFIGURATION_DETAILS_SUCCESS,
} from "../constants/configurationConstants";

export const getAllConfigurationDetails =
  () => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: ALL_CONFIGURATION_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/deployments/configuration/all`);

      dispatch({
        type: ALL_CONFIGURATION_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ALL_CONFIGURATION_DETAILS_FAIL,
        payload:
          (error as any).response && (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };
