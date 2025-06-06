import { ObjectKeys, getColorTheme } from "utils/utilities";
import { chartDataConfigProps } from ".";
import { getLabelOptions } from "./getLabelOptions";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getRadarTooltip } from "./getTooltipOptions";

export default ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.stats || !chartData.data) {
    // Return some default configuration or handle the undefined case
    console.error("chartData, chartData.stats, or chartData.data is undefined");
    return {};
  }

  let radarData: string[] = [];
  ObjectKeys(chartData.data.series?.[0].seriesData).map(
    (seriesDataKey: any) => {
      radarData = ObjectKeys(
        chartData.data.series[0].seriesData[seriesDataKey]
      );
    }
  );

  let max: undefined | number = undefined;

  ObjectKeys(chartData.stats).forEach((sName) => {
    max = max
      ? Math.max(chartData.stats[sName].max, max)
      : chartData.stats[sName].max; // = ObjectKeys();
  });

  return {
    color: getColorTheme(chartCustomization?.colorTheme, chartCustomization)
      .color,
    tooltip: {
      show: chartCustomization.tooltip,
      formatter: (args: any) =>
        getRadarTooltip(args, chartData, chartCustomization),
    },
    radar: {
      indicator: radarData.map((d) => ({
        name: d,
        max: max,
      })),
    },
    series: chartData.data?.series
      ?.map((series: any, index: number) => {
        const customizeSeries = chartCustomization.seriesCustomize?.find(
          (customSeries: any) => customSeries.id === series.id
        );

        return ObjectKeys(series.seriesData).map((dKey) => {
          return {
            ...getLargeDataOptions(),
            name: dKey,
            label: getLabelOptions(
              customizeSeries,
              chartData.request.chartType,
              undefined,
              dimensions
            ),
            type: "radar",
            ...(chartCustomization.colorTheme === "custom"
              ? {
                  itemStyle: {
                    color: chartCustomization?.colorScheme?.[series.id]?.[dKey],
                  },
                }
              : {}),
            data: [
              {
                value: ObjectKeys(series.seriesData[dKey]).map((sKey) => {
                  return series.seriesData[dKey][sKey][0][0];
                }),
                extras: series.seriesData[dKey],
                name: dKey,
              },
            ],
          };
        })[0];
      })
      .map((serie: any) => ({ ...serie, silent: !editMode })),
  };
};
