import { DASHBOARD_REFRESH_DEFAULT_CONFIG } from "Apps/Kepler/dashboard/DashboardRefreshBtn/DashboardRefreshBtn.contants";
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

export const editDashboardReducer = (
  state = {
    loading: false,
    editable: false,
    filterMenu: false,
    subscribeMenu: false,
    error: "NO ERRO",
    selectedTab: { key: "1", id: "-1" },
    chartAction: 0,
    filters: new Map(),
    subscribePopover: false,
    triggerSave: 0,
    revertSaveStatus: undefined,
    isDashboardChanged: false,
    isNewChartAdded: [],
    tabId: "",
    dashboardRefreshConfig: DASHBOARD_REFRESH_DEFAULT_CONFIG,
    reloadDashboardElement: 0,
    gridConfig: {
      background: "var(--bosler-bkg-color-muted)",
      topPadding: 2,
      rightPadding: 2,
      bottomPadding: 2,
      leftPadding: 2,
      preventCollision: false,
      allowOverlap: true,
    },
  },
  action: $TSFixMe
) => {
  switch (action.type) {
    case DASHBOARD_EDIT_REQUEST:
      return { ...state, loading: true };

    case DASHBOARD_EDIT_SUCCESS:
      return { ...state, loading: false, editable: action.payload };

    case DASHBOARD_TRIGGER_SAVE_SUCCESS:
      return {
        ...state,
        loading: false,
        triggerSave: state.triggerSave + 1,
      };

    case DASHBOARD_REVERT_SAVE_STATUS:
      if (action.payload != undefined) {
        return {
          ...state,
          loading: false,
          revertSaveStatus: action.payload,
          isDashboardChanged: action.payload ? false : true,
        };
      }
      return {
        ...state,
        loading: false,
        revertSaveStatus: action.payload,
      };

    case DASHBOARD_CHANGE_STATUS:
      return {
        ...state,
        isDashboardChanged: action.payload,
      };

    case DASHBOARD_CHART_ADDED_SUCCESS:
      return {
        ...state,
        isNewChartAdded: [],
      };

    case DASHBOARD_EDIT_FAIL:
      return {
        ...state,
        loading: false,
        error: "Cannot change edit property",
      };

    case DASHBOARD_TAB_CHANGE_SUCCESS:
      return { ...state, tabId: action.payload };

    case DASHBOARD_APPLIED_FILTER_REQUEST:
      return { ...state, loading: true };

    case DASHBOARD_APPLIED_FILTER_SUCCESS:
      return { ...state, loading: false, filters: action.payload };

    case DASHBOARD_APPLIED_FILTER_FAIL:
      return {
        ...state,
        loading: false,
        error: "Cannot get dashboard filters",
      };

    case DASHBOARD_FILTER_REQUEST:
      return { ...state, loading: true };

    case DASHBOARD_FILTER_SUCCESS:
      return { ...state, loading: false, filterMenu: !state.filterMenu };

    case DASHBOARD_FILTER_FAIL:
      return {
        ...state,
        loading: false,
        error: "Cannot change filterMenu property",
      };

    case DASHBOARD_CHART_ACTION_REQUEST:
      return { ...state, loading: true };

    case DASHBOARD_CHART_ACTION_SUCCESS:
      return { ...state, loading: false, chartAction: state.chartAction + 1 };

    case DASHBOARD_CHART_ACTION_FAIL:
      return {
        ...state,
        loading: false,
        error: "Cannot change filterMenu property",
      };

    case DASHBOARD_KEY_UPDATE_REQUEST:
      return state;

    case DASHBOARD_KEY_UPDATE_SUCCESS:
      return { ...state, selectedTab: action.payload };

    case DASHBOARD_KEY_UPDATE_FAIL:
      return state;

    case DASHBOARD_SUBSCRIBE_REQUEST:
      return state;

    case DASHBOARD_SUBSCRIBE_SUCCESS:
      return { ...state, subscribeMenu: !state.subscribeMenu };

    case DASHBOARD_SUBSCRIBE_FAIL:
      return state;

    case DASHBOARD_SUBSCRIBE_UPDATE_REQUEST:
      return state;

    case DASHBOARD_SUBSCRIBE_UPDATE_SUCCESS:
      return { ...state, subscribePopover: !state.subscribePopover };

    case DASHBOARD_SUBSCRIBE_UPDATE_FAIL:
      return state;

    case DASHBOARD_GRID_CONFIG_REQUEST:
      return state;

    case DASHBOARD_GRID_CONFIG_SUCCESS:
      return { ...state, gridConfig: action.payload };

    case DASHBOARD_GRID_CONFIG_FAIL:
      return state;
    case DASHBOARD_REFRESH_CONFIG_UPDATE_SUCCESS:
      return {
        ...state,
        dashboardRefreshConfig: action.payload,
      };
    case DASHBOARD_CHART_RELOAD_SUCCESS:
      return {
        ...state,
        reloadDashboardElement: state.reloadDashboardElement + 1,
      };

    default:
      return state;
  }
};
