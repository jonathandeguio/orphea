import axios from "axios";
import { DATASET } from "components/Builds/Builds.constants";
import { getSchedulesAPI } from "components/bottomBar/Schedules/api";
import {
  PIPELINE_DETAILS_FAIL,
  PIPELINE_DETAILS_REQUEST,
  PIPELINE_DETAILS_SUCCESS,
  PIPELINE_SCHEMA_REQUEST,
  PIPELINE_SCHEMA_SUCCESS,
  PIPELINE_SELECTED_NODE,
  PIPELINE_SOURCE_FAIL,
  PIPELINE_UPDATE_FAIL,
  PIPELINE_UPDATE_REQUEST,
  PIPELINE_UPDATE_SUCCESS,
  SCHEDULE_DETAILS_FAIL,
  SCHEDULE_DETAILS_REQUEST,
  SCHEDULE_DETAILS_SUCCESS,
} from "../constants/pipelineConstants";

export const pipelineDetails =
  (id: $TSFixMe, branch: $TSFixMe) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: PIPELINE_DETAILS_REQUEST });

      const { data } = await axios.get(`/bezier/${id}/${branch}/pipeline`);
      // @ts-expect-error TS(7034): Variable 'datasetIds' implicitly has type 'any[]' ... Remove this comment to see the full error message
      const datasetIds = [];
      data.nodes.map((node: $TSFixMe) => datasetIds.push(node.id));
      const res = await axios.post(
        `/kitab/dataset/byIds`,
        // @ts-expect-error TS(7005): Variable 'datasetIds' implicitly has an 'any[]' ty... Remove this comment to see the full error message
        datasetIds
      );
      let ith = 0;
      data.nodes.map((node: $TSFixMe) => {
        node.name = res.data[ith].name;
        ith++;
        return 0;
      });

      dispatch({
        type: PIPELINE_DETAILS_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: PIPELINE_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const pipelineUpdate =
  (id: $TSFixMe, branch: $TSFixMe, pipeline: $TSFixMe) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: PIPELINE_UPDATE_REQUEST });

      pipeline.nodes.map((node: $TSFixMe) => {
        axios.post(`/bezier/${node.id}/${branch}/Update`, {
          branch: node.branch,
          buildStatus: node.buildStatus,
          collapsed: node.collapsed,
          vx: node.vx + 0.0000001, // == 0 ? 0.1 : node.vx,
          vy: node.vy + 0.0000001, // == 0 ? 0.1 : node.vy,
          x: node.x + 0.0000001, // == 0 ? 0.1 : node.x,
          y: node.y + 0.0000001, // == 0 ? 0.1 : node.y,
        });
        return 0;
      });
      // const { data } = await axios.post(
      // 	`/bezier/${id}/${branch}/Update`,
      // 	pipeline,
      // 	config,
      // );

      dispatch({
        type: PIPELINE_UPDATE_SUCCESS,
        payload: pipeline,
      });
    } catch (error) {
      dispatch({
        type: PIPELINE_UPDATE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const selectedNode = (data: $TSFixMe) => async (dispatch: $TSFixMe) => {
  dispatch({
    type: PIPELINE_SELECTED_NODE,
    payload: data,
  });
};

export const scheduleDetails =
  (id: string, branch: string) => async (dispatch: any) => {
    try {
      dispatch({ type: SCHEDULE_DETAILS_REQUEST });

      const data = getSchedulesAPI(id, branch, DATASET);

      dispatch({
        type: SCHEDULE_DETAILS_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: SCHEDULE_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const fetchSchema =
  (id: $TSFixMe, branch: $TSFixMe, transactionId: string) =>
  async (dispatch: $TSFixMe) => {
    try {
      dispatch({ type: PIPELINE_SCHEMA_REQUEST });

      const { data } = await axios.get(
        `/dataset/schema/${id}/${branch}/${transactionId}`
      );

      dispatch({
        type: PIPELINE_SCHEMA_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: PIPELINE_SOURCE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
