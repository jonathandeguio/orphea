import axios from "axios";
import {
  ALL_TRIGGER_DETAILS_FAIL,
  ALL_TRIGGER_DETAILS_REQUEST,
  ALL_TRIGGER_DETAILS_SUCCESS,
} from "../constants/triggerConstants";

export const getAllTriggerDetails =
  () => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: ALL_TRIGGER_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/build/trigger/all`);

      dispatch({
        type: ALL_TRIGGER_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ALL_TRIGGER_DETAILS_FAIL,
        payload:
          (error as any).response && (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };
