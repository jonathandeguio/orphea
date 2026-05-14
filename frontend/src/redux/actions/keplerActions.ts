import {
  fetchChart,
  fetchChartData,
  putChart,
} from "Apps/Kepler/chart/charts.utils";
import store from "../../redux/store";
import { RootState, ThunkAppDispatch } from "../../redux/types/store";

export const initialLoad =
  (payload: any, transactionId: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: "INITIAL_LOAD",
      payload: payload,
    });
    if (payload.fetchChart) {
      fetchChartData(
        undefined,
        payload.chart.id,
        payload.query,
        dispatch,
        undefined,
        transactionId
      );
    }
  };

export const changeVersion =
  (payload: any, transactionId: any) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    const state = store.getState();
    fetchChart(state.kepler.chart.id, payload.versionId).then((data) => {
      payload.query = data.chartConfig;
      payload.customize = data.chartCustomize;
      // fetchChartData(
      //   false,
      //   state.kepler.chart.id,
      //   data.chartConfig,
      //   dispatch,
      //   undefined,
      //   transactionId
      // ).then(() => {
      // });
      dispatch({
        type: "CHANGE_VERSION",
        payload: payload,
      });
    });
  };

export const updateQuery =
  (payload: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: "UPDATE_QUERY",
      payload: payload,
    });
  };

export const fetchDataTrigger =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: "FORM_SUBMIT",
    });
  };
export const keplerCleanup = (payload: any) => {
  return {
    type: "CLEANUP",
    payload: payload,
  };
};

export const setColumns = (payload: any) => {
  return {
    type: "SET_COLUMNS",
    payload: payload,
  };
};
export const updateCustomize = (payload: any) => {
  return {
    type: "UPDATE_CUSTOMIZE",
    payload: payload,
  };
};
export const silentUpdateQuery = (payload: any) => {
  return {
    type: "SILENT_UPDATE_QUERY",
    payload: payload,
  };
};
export const updateChart = (payload: any) => {
  return {
    type: "UPDATE_CHART",
    payload: payload,
  };
};
export const fetchDataRequest = () => {
  return {
    type: "FETCH_DATA_REQUEST",
  };
};
export const fetchDataSuccess =
  (payload: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: "FETCH_DATA_SUCCESS",
      payload: payload,
    });
  };
// export const postFetchDataSuccess =
//   () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
//     console.log("POST FETCH DATA CALL", getState.kepler);
//     if (isDefined(getState.kepler)) {

//     }
//   };
export const fetchDataError = (payload: any) => {
  return {
    type: "FETCH_DATA_ERROR",
    payload: payload,
  };
};
