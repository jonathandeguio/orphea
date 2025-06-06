import { createVersionAPI } from "components/VersionHistory/VersionHistory.api";
import { getLanguageLabel } from "utils/utilities";
import { versionUpdate } from "../../../redux/actions/versionActions";
import { EDIT_MODE } from "../../../redux/constants/resourcePermissionConstants";
import store from "../../../redux/store";
import {
  CHART_HEIGHT,
  CHART_MAX_HEIGHT,
  CHART_MAX_WIDTH,
  CHART_MIN_HEIGHT,
  CHART_MIN_WIDTH,
  CHART_WIDTH,
  DEFAULT_HEIGHT,
  DEFAULT_MAX_HEIGHT,
  DEFAULT_MAX_WIDTH,
  DEFAULT_MIN_HEIGHT,
  DEFAULT_MIN_WIDTH,
  DEFAULT_WIDTH,
  DIVIDER_HEIGHT,
  DIVIDER_MAX_HEIGHT,
  DIVIDER_MAX_WIDTH,
  DIVIDER_MIN_HEIGHT,
  DIVIDER_MIN_WIDTH,
  DIVIDER_WIDTH,
  EDITOR_HEIGHT,
  EDITOR_MAX_HEIGHT,
  EDITOR_MAX_WIDTH,
  EDITOR_MIN_HEIGHT,
  EDITOR_MIN_WIDTH,
  EDITOR_WIDTH,
  FILE_HEIGHT,
  FILE_MAX_HEIGHT,
  FILE_MAX_WIDTH,
  FILE_MIN_HEIGHT,
  FILE_MIN_WIDTH,
  FILE_WIDTH,
  HEADER_HEIGHT,
  HEADER_MAX_HEIGHT,
  HEADER_MAX_WIDTH,
  HEADER_MIN_HEIGHT,
  HEADER_MIN_WIDTH,
  HEADER_WIDTH,
  MARKDOWN_HEIGHT,
  MARKDOWN_MAX_HEIGHT,
  MARKDOWN_MAX_WIDTH,
  MARKDOWN_MIN_HEIGHT,
  MARKDOWN_MIN_WIDTH,
  MARKDOWN_WIDTH,
  TEXT_HEIGHT,
  TEXT_MAX_HEIGHT,
  TEXT_MAX_WIDTH,
  TEXT_MIN_HEIGHT,
  TEXT_MIN_WIDTH,
  TEXT_WIDTH,
} from "./Dashboard.contants";

export const getDefaultMeasurementsOfElements = (layoutElementType: string) => {
  let height, width, minHeight, maxHeight, minWidth, maxWidth;
  if (layoutElementType === "chart") {
    height = CHART_HEIGHT;
    width = CHART_WIDTH;
    minHeight = CHART_MIN_HEIGHT;
    maxHeight = CHART_MAX_HEIGHT;
    minWidth = CHART_MIN_WIDTH;
    maxWidth = CHART_MAX_WIDTH;
  } else if (layoutElementType === "divider") {
    height = DIVIDER_HEIGHT;
    width = DIVIDER_WIDTH;
    minHeight = DIVIDER_MIN_HEIGHT;
    maxHeight = DIVIDER_MAX_HEIGHT;
    minWidth = DIVIDER_MIN_WIDTH;
    maxWidth = DIVIDER_MAX_WIDTH;
  } else if (layoutElementType === "file") {
    height = FILE_HEIGHT;
    width = FILE_WIDTH;
    minHeight = FILE_MIN_HEIGHT;
    maxHeight = FILE_MAX_HEIGHT;
    minWidth = FILE_MIN_WIDTH;
    maxWidth = FILE_MAX_WIDTH;
  } else if (layoutElementType === "header") {
    height = HEADER_HEIGHT;
    width = HEADER_WIDTH;
    minHeight = HEADER_MIN_HEIGHT;
    maxHeight = HEADER_MAX_HEIGHT;
    minWidth = HEADER_MIN_WIDTH;
    maxWidth = HEADER_MAX_WIDTH;
  } else if (layoutElementType === "text") {
    height = TEXT_HEIGHT;
    width = TEXT_WIDTH;
    minHeight = TEXT_MIN_HEIGHT;
    maxHeight = TEXT_MAX_HEIGHT;
    minWidth = TEXT_MIN_WIDTH;
    maxWidth = TEXT_MAX_WIDTH;
  } else if (layoutElementType === "editor") {
    height = EDITOR_HEIGHT;
    width = EDITOR_WIDTH;
    minHeight = EDITOR_MIN_HEIGHT;
    maxHeight = EDITOR_MAX_HEIGHT;
    minWidth = EDITOR_MIN_WIDTH;
    maxWidth = EDITOR_MAX_WIDTH;
  } else if (layoutElementType === "markdown") {
    height = MARKDOWN_HEIGHT;
    width = MARKDOWN_WIDTH;
    minHeight = MARKDOWN_MIN_HEIGHT;
    maxHeight = MARKDOWN_MAX_HEIGHT;
    minWidth = MARKDOWN_MIN_WIDTH;
    maxWidth = MARKDOWN_MAX_WIDTH;
  } else {
    height = DEFAULT_HEIGHT;
    width = DEFAULT_WIDTH;
    minHeight = DEFAULT_MIN_HEIGHT;
    maxHeight = DEFAULT_MAX_HEIGHT;
    minWidth = DEFAULT_MIN_WIDTH;
    maxWidth = DEFAULT_MAX_WIDTH;
  }

  return {
    height: height,
    width: width,
    minHeight: minHeight,
    maxHeight: maxHeight,
    minWidth: minWidth,
    maxWidth: maxWidth,
  };
};

// Header Elements

export const getHeaderFont = (fontWeight: string) => {
  if (fontWeight == "1") {
    return "large";
  } else if (fontWeight == "2") {
    return "x-large";
  } else if (fontWeight == "3") {
    return "xx-large";
  }
};

export const getHeaderBackground = (backgroundColor: string) => {
  if (backgroundColor == "1") {
    return "var(--background-color)";
  } else if (backgroundColor == "2") {
    return "transparent";
  }
};

export const isAutoSaveTime = (lastVersionTime: number) => {
  const currentTimeMillis = Date.now();
  const timeDifferenceMillis = currentTimeMillis - lastVersionTime;
  const twentyFourHoursInMillis = 24 * 60 * 60 * 1000;

  return timeDifferenceMillis - twentyFourHoursInMillis > 0;
};

export const autoSaveVersionCallback = (
  resourceId: string,
  pageType: "dashboard" | "chart",
  dispatch: any,
  setAutoSaveReady: any,
  changeResourceMode?: any
) => {
  const state = store.getState();

  if (
    pageType == "dashboard"
      ? state.version.isAutoSaveReady
      : !state.kepler.isChartSaved
  ) {
    createVersionAPI(resourceId, pageType, `Auto saved version`)?.then(
      ({ data }: any) => {
        setAutoSaveReady(false);
        dispatch(versionUpdate());

        if (pageType == "chart") {
          setTimeout(() => changeResourceMode(EDIT_MODE), 200);
        }
      }
    );
  }
};

export const fontWeightItems = [
  {
    key: "1",
    label: getLanguageLabel("small"),
  },
  {
    key: "2",
    label: getLanguageLabel("medium"),
  },
  {
    key: "3",
    label: getLanguageLabel("large"),
  },
];

export const fontBackgroundItems = [
  {
    key: "1",
    label: getLanguageLabel("white"),
  },
  {
    key: "2",
    label: getLanguageLabel("transparent"),
  },
];
