import { getLanguageLabel } from "utils/utilities";

export const SELECT_BEFORE_ITEMS = [
  {
    label: "Ascending",
    value: "ASC",
  },
  {
    label: "Descending",
    value: "DSC",
  },
];

export const SELECT_AFTER_ITEMS = [
  {
    label: getLanguageLabel("name"),
    value: "name",
  },
  {
    label: getLanguageLabel("createdAt"),
    value: "createdAt",
  },
  {
    label: getLanguageLabel("updatedAt"),
    value: "updatedAt",
  },
  {
    label: getLanguageLabel("chartType"),
    value: "chartType",
  },
  {
    label: `${getLanguageLabel("chart")} ${getLanguageLabel("id")}`,
    value: "chartId",
  },
  {
    label: getLanguageLabel("datasetId"),
    value: "datasetId",
  },
];

export const DEFAULT_BEFORESTATE = "ASC";
export const DEFAULT_AFTERSTATE = "name";

export const CHARTS_TAB = getLanguageLabel("chart");
export const NO_CHARTS = getLanguageLabel("noChartsToAdd");

export const ELEMENTS_TAB = getLanguageLabel("layout");
export const ELEMENTS_TAB_TEXT = getLanguageLabel("text");
export const ELEMENTS_TAB_HEADER = getLanguageLabel("header");
export const ELEMENTS_TAB_DIVIDER = getLanguageLabel("divider");
export const ELEMENTS_TAB_MARKDOWN = getLanguageLabel("markdown");
