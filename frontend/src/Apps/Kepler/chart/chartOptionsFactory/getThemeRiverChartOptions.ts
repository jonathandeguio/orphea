import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * ThemeRiver (stream graph) factory.
 *
 * Expected data format from backend:
 * chartData.data = {
 *   data: [[date: string, value: number, category: string], ...]
 * }
 */
const getThemeRiverChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getThemeRiverChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const riverData = chartData.data;
  const rawData: [string, number, string][] = isDefined(riverData.data)
    ? riverData.data
    : [];

  const emphasisMode: string = chartCustomization.themeRiverEmphasis ?? "series";

  return {
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "axis",
      axisPointer: { type: "line", lineStyle: { color: "rgba(0,0,0,0.2)", width: 1 } },
    },
    singleAxis: {
      type: "time",
      top: 50,
      bottom: 50,
      axisPointer: {
        animation: true,
        label: { show: true },
      },
    },
    series: [
      {
        type: "themeRiver",
        emphasis: { focus: emphasisMode },
        data: rawData,
        silent: !editMode,
      },
    ],
  };
};

export default getThemeRiverChartOptions;
