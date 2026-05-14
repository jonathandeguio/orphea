import { isDefined, isEmpty } from "utils/utilities";
import { chartConfig } from "../../charts.config";

type LegendItems = {
  name: string;
  color: string;
};

export type SeriesLegend = {
  seriesName: string;
  groupBy: string[];
  items: LegendItems[];
};

export const getLegendData = (
  chartData: any,
  chartCustomization: any
): SeriesLegend[] => {
  if (isDefined(chartData?.request?.chartType)) {
    const skeleton = chartConfig[chartData.request.chartType];
    if (skeleton.meta.legend) {
      // prepare data
      if (chartData.request.chartType === "treeMapChart") {
        if (!isEmpty(chartData?.request?.dimensions)) {
          const groupBy = chartData.request.dimensions[0];
          const chartDataSet = new Set(
            (chartData?.data).map((obj: any) => obj[groupBy])
          );
          console.log(
            "colors",
            chartCustomization?.colorScheme["treeMapChart"]
          );
          return [
            {
              groupBy: [groupBy],
              seriesName: chartData.request.series[0].seriesName,
              items: [...chartDataSet].map((item: any) => ({
                name: item,
                color:
                  chartCustomization?.colorScheme["treeMapChart"]?.[item] ??
                  "#000000",
              })),
            },
          ];
        }
      } else if (
        chartData.request.chartType === "gaugeChart" ||
        chartData.request.chartType === "pieChart"
      ) {
        return [
          {
            groupBy:
              chartData.request.chartType === "radarChart"
                ? chartData.request.dimensions
                : chartData.request.series[0].groupBy,
            seriesName: chartData.request.series[0].seriesName,
            items: Object.keys(chartData?.data).map((key) => ({
              name: key,
              color:
                chartCustomization?.colorScheme[
                  chartData?.request?.chartType
                ]?.[key] ?? "#000000",
            })),
          },
        ];
      } else if (skeleton.customization.series && chartData.data?.series) {
        return chartData.data?.series?.map((series: any) => ({
          groupBy: series.groupBy,
          seriesName: series.name,
          items: Object.keys(series.seriesData).map((sDataKey: any) => ({
            name: sDataKey,
            color:
              chartCustomization?.colorScheme?.[series?.id]?.[sDataKey] ??
              "#000000",
          })),
        }));
      }
    }
  }
  return [];
};
