import axios from "axios";
import {
  ENVOY_CREATE_FAIL,
  ENVOY_CREATE_REQUEST,
  ENVOY_CREATE_SUCCESS,
  ENVOY_DELETE_FAIL,
  ENVOY_DELETE_REQUEST,
  ENVOY_DELETE_SUCCESS,
  ENVOY_LINKS_LIST_FAIL,
  ENVOY_LINKS_LIST_REQUEST,
  ENVOY_LINKS_LIST_SUCCESS,
  ENVOY_LIST_FAIL,
  ENVOY_LIST_REQUEST,
  ENVOY_LIST_SUCCESS,
  ENVOY_SOURCES_LIST_FAIL,
  ENVOY_SOURCES_LIST_REQUEST,
  ENVOY_SOURCES_LIST_SUCCESS,
} from "../constants/agentConstants";

export const listAgents =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: ENVOY_LIST_REQUEST });

      const { data } = await axios.get(`/connect/agent/Getall`);
      dispatch({
        type: ENVOY_LIST_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ENVOY_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
      return error;
    }
  };

export const deleteAgent = (id: $TSFixMe) => async (dispatch: $TSFixMe) => {
  try {
    dispatch({ type: ENVOY_DELETE_REQUEST });
    const { data } = await axios.delete(`/connect/agent/DeleteById/${id}`);
    dispatch({
      type: ENVOY_DELETE_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: ENVOY_DELETE_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });
  }
};

export const createAgent =
  (agent: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: ENVOY_CREATE_REQUEST });
      const { data } = await axios.post(
        `/connect/agent/create`,
        JSON.stringify(agent)
      );

      dispatch({
        type: ENVOY_CREATE_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ENVOY_CREATE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
export const listAgentSources =
  (agentId: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: ENVOY_SOURCES_LIST_REQUEST });
      const { data } = await axios.get(`/connect/agent/${agentId}/sources`);

      dispatch({
        type: ENVOY_SOURCES_LIST_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: ENVOY_SOURCES_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
export const listAgentLinks =
  (agentId: $TSFixMe) => async (dispatch: $TSFixMe) => {
    try {
      dispatch({ type: ENVOY_LINKS_LIST_REQUEST });
      const { data } = await axios.get(`/connect/agent/${agentId}/links`);

      dispatch({
        type: ENVOY_LINKS_LIST_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ENVOY_LINKS_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
