import { getColorScheme } from "Apps/Kepler/chart/chartOptionsFactory";
import { chartConfig } from "Apps/Kepler/chart/charts.config";
import {
  getAggregateOptionsValuesList,
  syncCustomizeSeries,
} from "Apps/Kepler/chart/charts.utils";

import { KeplerStore } from "Apps/Kepler/kepler";
import {
  defaultCustomize,
  defaultSeries,
} from "Apps/Kepler/utils/DefaultValues";
import { generateUUID, isEmpty } from "utils/utilities";

const initialState: KeplerStore = {
  isChartSaved: true,
  dataForm: null,
  customizeForm: null,
  query: undefined,
  queryError: {
    status: "PENDING",
    error: null,
  },
  chartStatus: "LOADING",
  chart: null,
  data: null,
  columns: [],
  customize: defaultCustomize,
};
const uuid = require("uuid");
export const keplerReducer = (state = initialState, action: any) => {
  switch (action.type) {
    case "START_CHART_SAVE": {
      return {
        ...state,
        chartStatus: "LOADING",
      };
    }
    case "FINISH_CHART_SAVE": {
      return {
        ...state,
        chartStatus: "LOADED",
      };
    }

    case "CLEANUP": {
      let updatedState = {
        isChartSaved: true,
        dataForm: null,
        customizeForm: null,
        query: undefined,
        chart: null,
        data: null,
        columns: [],
        customize: null,
        chartStatus: "LOADING",
      };
      return updatedState;
    }
    case "INITIAL_LOAD": {
      let updatedState = {
        ...state,
        ...action.payload,
        customize: {
          ...defaultCustomize,
          ...action.payload?.customize,
        },
        data: {
          loading: false,
          error: false,
          chartData: action.payload.data,
        },
        chartStatus: "LOADED",
        isChartSaved: true,
      };

      updatedState = onUpdateQuery(state, updatedState);

      // action.payload.dataForm.submit();

      return updatedState;
    }
    case "CHANGE_VERSION": {
      return {
        ...state,
        ...action.payload,
        data: {
          ...state.data,
          loading: false,
          error: false,
          chartData: undefined,
        },
      };
    }

    case "SET_COLUMNS": {
      return {
        ...state,
        columns: action.payload,
      };
    }

    case "UPDATE_CUSTOMIZE": {
      let updatedState = {
        ...state,
        isChartSaved: false,
        customize: {
          ...state.customize,
          ...action.payload,
        },
      };

      if (action.payload.colorTheme === "custom")
        state.customizeForm.setFieldValue("colorTheme", "custom");
      else if (
        state.customize.colorTheme !== updatedState.customize.colorTheme
      ) {
        updatedState.customize.colorScheme = getColorScheme(
          updatedState.data?.payload,
          updatedState.customize,
          false
        );
      }

      return updatedState;
    }
    case "FORM_SUBMIT": {
      state.dataForm.submit();
      return state;
    }
    case "UPDATE_QUERY": {
      let updatedState = {
        ...state,
        isChartSaved: false,
        query: {
          ...state.query,
          ...action.payload,
        },
      };

      updatedState = onUpdateQuery(state, updatedState);
      state.dataForm.submit();

      return updatedState;
    }
    case "UPDATE_QUERY_ERROR": {
      let updatedState = {
        ...state,
        queryError: {
          status: action.payload.status,
          error: action.payload.error,
        },
      };
      return updatedState;
    }
    case "SILENT_UPDATE_QUERY": {
      return {
        ...state,
        chartStatus: "LOADED",
        isChartSaved: action?.payload?.chartUUID ? true : false,
      };
    }
    case "UPDATE_CHART": {
      return {
        ...state,
        chart: { ...state.chart, ...action.payload.chart },
        chartStatus: "LOADED",
        isChartSaved: action.payload.isChartSaved,
      };
    }
    case "FETCH_DATA_REQUEST": {
      return {
        ...state,
        data: {
          ...state.data,
          loading: true,
          error: false,
        },
      };
    }
    case "FETCH_DATA_ERROR": {
      return {
        ...state,
        data: {
          loading: false,
          payload: null,
          error: true,
        },
      };
    }
    case "FETCH_DATA_SUCCESS": {
      let customize = state.customize;
      if (state.query?.chartType != "parameterChart") {
        customize = {
          ...state.customize,
          seriesCustomize: syncCustomizeSeries(
            state.query?.series,
            state.customize?.seriesCustomize
          ),
          colorScheme: getColorScheme(action.payload, state.customize, true),
        };

        state?.customizeForm?.setFieldsValue(customize);
      }

      return {
        ...state,
        data: {
          ...state.data,
          loading: false,
          payload: action.payload,
          error: false,
        },
        customize: customize,
      };
    }

    default: {
      return state;
    }
  }
};

const setDefaultSeries = (query: any) => ({
  ...query,
  series: [{ ...defaultSeries, id: generateUUID() }],
});

const setUndefinedXAxis = (query: any) => ({
  ...query,
  xaxis: undefined,
});

const updateSeriesIndex = (series: any, chartType: string) =>
  series.map((s: any) => ({
    ...s,
    seriesIndex: "left",
  }));

const ensureSingleSeries = (series: any) => [series[0]];

const validateAggregateFunctions = (
  series: any,
  columns: any,
  chartType: string
) =>
  series.map((s: any, index: number) => {
    const selectedColumn = columns.find(
      (col: any) => col.headerName === s.columnName
    );
    const aggregateOptions = getAggregateOptionsValuesList(
      selectedColumn?.type,
      chartType
    );

    return {
      ...s,
      aggregate: aggregateOptions.includes(s.aggregate)
        ? s.aggregate
        : aggregateOptions[0],
    };
  });

const onUpdateQuery = (state: any, updatedState: any) => {
  const querySkeleton = chartConfig[updatedState.query.chartType];

  if (!querySkeleton.hasOwnProperty("dimensions")) {
    updatedState.query.dimensions = [];
  }

  if (isEmpty(updatedState.query.series)) {
    updatedState.query = setDefaultSeries(updatedState.query);
  }

  if (!querySkeleton.meta.isAxisChart) {
    updatedState.query = setUndefinedXAxis(updatedState.query);
  }

  const leftFields =
    updatedState.query.series?.filter((s: any) => s.seriesIndex === "left") ||
    [];

  if (
    leftFields.length === 0 ||
    querySkeleton.meta.isSingleSeries ||
    querySkeleton.meta.isSingleDirection
  ) {
    updatedState.query.series = updateSeriesIndex(
      updatedState.query.series,
      updatedState.query.chartType
    );
  }

  if (
    querySkeleton.meta.isSingleSeries &&
    updatedState.query.series.length > 0
  ) {
    updatedState.query.series = ensureSingleSeries(updatedState.query.series);
  }

  if (!isEmpty(state.columns) && updatedState.query.series) {
    updatedState.query.series = validateAggregateFunctions(
      updatedState.query.series,
      state.columns,
      updatedState.query.chartType
    );
  }

  if (
    !querySkeleton.hasOwnProperty("xaxis") &&
    updatedState.query.sortingMethod === "xaxis"
  ) {
    updatedState.query.sortingMethod = "custom";
    updatedState.dataForm.setFieldValue("sortingMethod", "custom");
  }

  updatedState.dataForm.setFieldValue("xaxis", updatedState.query.xaxis);
  updatedState.dataForm.setFieldValue("series", updatedState.query.series);

  updatedState = {
    ...updatedState,
  };

  return updatedState;
};
