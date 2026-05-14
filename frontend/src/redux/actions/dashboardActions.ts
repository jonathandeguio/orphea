import { ITabConfig } from "Apps/Kepler/dashboard/Dashboard";
import {
  DASHBOARD_APPLIED_FILTER_FAIL,
  DASHBOARD_APPLIED_FILTER_REQUEST,
  DASHBOARD_APPLIED_FILTER_SUCCESS,
  DASHBOARD_CHANGE_STATUS,
  DASHBOARD_CHART_ACTION_FAIL,
  DASHBOARD_CHART_ACTION_REQUEST,
  DASHBOARD_CHART_ACTION_SUCCESS,
  DASHBOARD_CHART_ADDED_SUCCESS,
  DASHBOARD_CHART_RELOAD_SUCCESS,
  DASHBOARD_EDIT_FAIL,
  DASHBOARD_EDIT_REQUEST,
  DASHBOARD_EDIT_SUCCESS,
  DASHBOARD_FILTER_FAIL,
  DASHBOARD_FILTER_REQUEST,
  DASHBOARD_FILTER_SUCCESS,
  DASHBOARD_GRID_CONFIG_FAIL,
  DASHBOARD_GRID_CONFIG_REQUEST,
  DASHBOARD_GRID_CONFIG_SUCCESS,
  DASHBOARD_KEY_UPDATE_FAIL,
  DASHBOARD_KEY_UPDATE_REQUEST,
  DASHBOARD_KEY_UPDATE_SUCCESS,
  DASHBOARD_REFRESH_CONFIG_UPDATE_SUCCESS,
  DASHBOARD_REVERT_SAVE_STATUS,
  DASHBOARD_SUBSCRIBE_FAIL,
  DASHBOARD_SUBSCRIBE_REQUEST,
  DASHBOARD_SUBSCRIBE_SUCCESS,
  DASHBOARD_SUBSCRIBE_UPDATE_FAIL,
  DASHBOARD_SUBSCRIBE_UPDATE_REQUEST,
  DASHBOARD_SUBSCRIBE_UPDATE_SUCCESS,
  DASHBOARD_TAB_CHANGE_SUCCESS,
  DASHBOARD_TRIGGER_SAVE_SUCCESS,
} from "../constants/dashboardConstants";

import { RootState, ThunkAppDispatch } from "../types/store";

export const changeDashboardTab =
  (tabId: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_TAB_CHANGE_SUCCESS,
      payload: tabId,
    });
  };

export const applyDashboardFilters =
  (filters: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_APPLIED_FILTER_REQUEST,
      });

      dispatch({
        type: DASHBOARD_APPLIED_FILTER_SUCCESS,
        payload: filters,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_APPLIED_FILTER_FAIL,
      });
    }
  };

export const makeChartAction =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_CHART_ACTION_REQUEST,
      });

      dispatch({
        type: DASHBOARD_CHART_ACTION_SUCCESS,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_CHART_ACTION_FAIL,
      });
    }
  };

export const openDashboardFilters =
  (status?: boolean) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_FILTER_REQUEST,
      });

      dispatch({
        type: DASHBOARD_FILTER_SUCCESS,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_FILTER_FAIL,
      });
    }
  };

export const editDashboard =
  (status: boolean) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_EDIT_REQUEST,
      });

      dispatch({
        type: DASHBOARD_EDIT_SUCCESS,
        payload: status,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_EDIT_FAIL,
      });
    }
  };

export const triggerSaveDashboard =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_TRIGGER_SAVE_SUCCESS,
    });
  };

export const isDashboardChanged =
  (status: boolean) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_CHANGE_STATUS,
      payload: status,
    });
  };

export const isChartAdded =
  (chartId: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_CHART_ADDED_SUCCESS,
      payload: chartId,
    });
  };

export const revertSaveDashboardStatus =
  (status: boolean | undefined) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_REVERT_SAVE_STATUS,
      payload: status,
    });
  };

export const openSubscribeMenuDashboard =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_SUBSCRIBE_REQUEST,
      });

      dispatch({
        type: DASHBOARD_SUBSCRIBE_SUCCESS,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_SUBSCRIBE_FAIL,
      });
    }
  };

export const updateSubscribePopoverDashboard =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_SUBSCRIBE_UPDATE_REQUEST,
      });

      dispatch({
        type: DASHBOARD_SUBSCRIBE_UPDATE_SUCCESS,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_SUBSCRIBE_UPDATE_FAIL,
      });
    }
  };

export const updateSelectedTab =
  (selectedTab: any) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_KEY_UPDATE_REQUEST,
      });

      dispatch({
        type: DASHBOARD_KEY_UPDATE_SUCCESS,
        payload: selectedTab,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_KEY_UPDATE_FAIL,
      });
    }
  };

export const updateGridConfig =
  (config: ITabConfig) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: DASHBOARD_GRID_CONFIG_REQUEST,
      });

      dispatch({
        type: DASHBOARD_GRID_CONFIG_SUCCESS,
        payload: config,
      });
    } catch (err) {
      dispatch({
        type: DASHBOARD_GRID_CONFIG_FAIL,
      });
    }
  };

export const updateDashboardState =
  (updates: {
    isPlaying?: boolean;
    refreshInterval?: number;
    liveRefresh?: boolean;
  }) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_REFRESH_CONFIG_UPDATE_SUCCESS,
      payload: updates,
    });
  };

export const reloadChartForDashboard =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: DASHBOARD_CHART_RELOAD_SUCCESS,
    });
  };
