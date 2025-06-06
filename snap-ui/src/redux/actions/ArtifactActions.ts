import axios from "axios";
import {
  ALL_ARTIFACT_DETAILS_FAIL,
  ALL_ARTIFACT_DETAILS_REQUEST,
  ALL_ARTIFACT_DETAILS_SUCCESS,
} from "../constants/artifactConstants";

export const getAllArtifactsDetails =
  (id: string) => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: ALL_ARTIFACT_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/artifact/byTrigger/${id}`);

      dispatch({
        type: ALL_ARTIFACT_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ALL_ARTIFACT_DETAILS_FAIL,
        payload:
          (error as any).response && (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };
