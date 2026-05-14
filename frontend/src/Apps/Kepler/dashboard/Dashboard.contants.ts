import { getLanguageLabel } from "utils/utilities";

export const CHART_HEIGHT = 8;
export const CHART_MIN_HEIGHT = 2;
export const CHART_MAX_HEIGHT = 30 * 10;

export const CHART_WIDTH = 4;
export const CHART_MIN_WIDTH = 1;
export const CHART_MAX_WIDTH = 16;

export const HEADER_HEIGHT = 6;
export const HEADER_MIN_HEIGHT = 4;
export const HEADER_MAX_HEIGHT = 10;

export const HEADER_WIDTH = 22;
export const HEADER_MIN_WIDTH = 14;
export const HEADER_MAX_WIDTH = 46;

export const TEXT_HEIGHT = 8;
export const TEXT_MIN_HEIGHT = 4;
export const TEXT_MAX_HEIGHT = 30 * 10;

export const TEXT_WIDTH = 4;
export const TEXT_MIN_WIDTH = 1;
export const TEXT_MAX_WIDTH = 16;

export const EDITOR_HEIGHT = 8;
export const EDITOR_MIN_HEIGHT = 1;
export const EDITOR_MAX_HEIGHT = 30 * 10;

export const EDITOR_WIDTH = 4;
export const EDITOR_MIN_WIDTH = 1;
export const EDITOR_MAX_WIDTH = 16;

export const DIVIDER_HEIGHT = 0.25;
export const DIVIDER_MIN_HEIGHT = 0.25;
export const DIVIDER_MAX_HEIGHT = 2;

export const DIVIDER_WIDTH = 4;
export const DIVIDER_MIN_WIDTH = 2;
export const DIVIDER_MAX_WIDTH = 30 * 10;

export const FILE_HEIGHT = 6;
export const FILE_MIN_HEIGHT = 2;
export const FILE_MAX_HEIGHT = 30 * 10;

export const FILE_WIDTH = 4;
export const FILE_MIN_WIDTH = 1;
export const FILE_MAX_WIDTH = 16;

export const MARKDOWN_HEIGHT = 16;
export const MARKDOWN_MIN_HEIGHT = 8;
export const MARKDOWN_MAX_HEIGHT = 46;

export const MARKDOWN_WIDTH = 32;
export const MARKDOWN_MIN_WIDTH = 18;
export const MARKDOWN_MAX_WIDTH = 46;

export const DEFAULT_HEIGHT = 14;
export const DEFAULT_WIDTH = 28;
export const DEFAULT_MIN_HEIGHT = 14;

export const DEFAULT_MAX_HEIGHT = 184;
export const DEFAULT_MIN_WIDTH = 18;
export const DEFAULT_MAX_WIDTH = 184;

export const DEFAULT_MARKDOWN =
  "IyBIZWFkZXIgMQojIyBIZWFkZXIgMgojIyMgSGVhZGVyIDMKYApDaGFydCBUcmFuc2Zvcm1hdGlvbiA6IApgCgpgYGAKZm9yIChpbnQgaSA9IDA7IGkgPCBzaXplOyBpKyspIHsKICAgIGNvdXQgPDwgaSA8PCBlbmRsOwp9CmBgYAoKICBDbGljayBoZXJlIHRvIGxlYXJuIG1vcmUgYWJvdXQgW21hcmtkb3duIGZvcm1hdHRpbmddKGh0dHBzOi8vYml0Lmx5LzFkUU9mUksp";

export const GRID_CONFIG = {
  width: "100",
  className: "layout",
  rowHeight: 30,
  breakpoints: { lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 },
  cols: { lg: 16, md: 12, sm: 10, xs: 6, xxs: 4 },
  isBounded: false,
  /*
  If you are applying compactType, then comment out verticalCompact : false
  */
  compactType: "vertical",
  // verticalCompact: false,
  margin: [5, 5],
  resizeHandles: ["se"],
  useCSSTransforms: true,
  preventCollision: false,
  containerPadding: [5, 5],
};

// Filter Tooltip
export const FILTER_TOOLTIP_HEADING = "Applied Filters";

// Chart Error Element
export const CHART_ERROR_ELEMENT_HEAD =
  "Error : There is an error accessing this chart.";
export const CHART_ERROR_ELEMENT_SUBHEAD =
  "You may not have access to the underlined data.";

// Chart Element Header

export const CHART_HEADER_CACHE = "This will override cache";
export const CHART_HEADER_DATALINEAGE = getLanguageLabel("dataLineage");
export const CHART_HEADER_DATASET = getLanguageLabel("openDataset");
export const CHART_HEADER_EXIT_FS = getLanguageLabel("exitFullscreen");
export const CHART_HEADER_ENTER_FS = getLanguageLabel("expand");
export const CHART_HEADER_EDIT = getLanguageLabel("edit");
export const CHART_HEADER_RELOAD = getLanguageLabel("reload");
export const CHART_HEADER_DELETE = getLanguageLabel("delete");

// Header Element
