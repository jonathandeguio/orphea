import { KeplerChartResponse } from "Apps/Kepler/kepler";
import { ObjectKeys, getColorTheme, isDefined, isEmpty } from "utils/utilities";
import { Dimensions } from "../ChartComponent/ParentChartComponent";
import { chartConfig } from "../charts.config";
import getBigNumberOptions from "./getBigChartOptions";
import getGuageChartOptions from "./getGaugeChartOptions";
import getHorizontalBarChartOptions from "./getHorizontalBarChartOptions";
import getParameterChartOptions from "./getParameterChartOptions";
import getPieChartOptions from "./getPieChartOptions";
import getRadarChartOptions from "./getRadarChartOptions";
import getSunBurstChartOptions from "./getSunBurstChartOptions";
import getTreeMapChartOptions from "./getTreeMapChartOptions";
import getVerticalAxisChartsOptions from "./getVerticalAxisChartsOptions";
import getWaterFallChartOptions from "./getWaterFallChartOptions";
import getWordCloudChartOptions from "./getWordCloudChartOptions";

export type chartDataConfigProps = {
  chartData: KeplerChartResponse;
  chartCustomization: any;
  dimensions: Dimensions;
  isDarkTheme: boolean;
  editMode: boolean;
};
export const getRelativeFontSize = (
  fontSize: number,
  dimensions: Dimensions,
  minFontSize: number = 8, // Minimum font size for readability
  maxFontSize: number = 72 // Maximum font size to avoid overly large text
) => {
  // Calculate a scaling factor based on the geometric mean of width and height
  const scalingFactor = Math.sqrt(dimensions.width * dimensions.height) / 3000;

  // Calculate the scaled font size, ensuring some effect from the scaling factor
  const scaledFontSize = fontSize * (0.5 + scalingFactor / 2.3); // scaling factor has a balanced effect

  // Ensure the font size remains between the min and max bounds
  return Math.min(Math.max(scaledFontSize, minFontSize), maxFontSize);
};
const generateChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  isDarkTheme,
  editMode,
}: chartDataConfigProps) => {
  if (
    !isDefined(chartData?.request?.chartType) ||
    !isDefined(chartData?.data) ||
    !isDefined(chartCustomization)
  )
    return undefined;

  const optionGeneratorMap: { [key: string]: any } = {
    pieChart: getPieChartOptions,
    bigNumber: getBigNumberOptions,
    horizontalBarChart: getHorizontalBarChartOptions,
    VerticalAxisChart: getVerticalAxisChartsOptions,
    sunBurstChart: getSunBurstChartOptions,
    radarChart: getRadarChartOptions,
    gaugeChart: getGuageChartOptions,
    parameterChart: getParameterChartOptions,
    treeMapChart: getTreeMapChartOptions,
    waterFallChart: getWaterFallChartOptions,
    wordCloudChart: getWordCloudChartOptions,
    table: (props: chartDataConfigProps) => props.chartData,
  };

  let chartOptions: any = optionGeneratorMap[chartData.request.chartType]?.({
    chartData,
    chartCustomization,
    dimensions,
    isDarkTheme,
    editMode,
  });

  chartOptions = postProcessOptions(
    chartOptions,
    chartData,
    chartCustomization
  );

  return chartOptions;
};

export const getColorScheme = (
  chartData: any,
  chartCustomization: any,
  keepOld: boolean
): { chartOptions: any; chartData: any; chartCustomization: any } => {
  const colorScheme: any = {};
  if (chartData?.request?.chartType == "PARAMETERCHART") return colorScheme;

  const chartType = chartData?.request?.chartType;

  if (isDefined(chartType)) {
    const querySkeleton = chartConfig[chartData?.request.chartType];

    let i = 0;
    const colorArr = getColorTheme(
      chartCustomization?.colorTheme,
      chartCustomization
    ).color;
    if (querySkeleton.meta.hasTheme) {
      if (chartType === "treeMapChart") {
        if (!isEmpty(chartData?.request?.dimensions)) {
          const groupBy = chartData.request.dimensions[0];
          colorScheme[chartType] = {};
          chartData?.data.map((obj: any) => {
            colorScheme[chartType][obj[groupBy]] = keepOld
              ? chartCustomization?.colorScheme?.[chartType]?.[obj[groupBy]] ??
                colorArr[i % colorArr.length]
              : colorArr[i % colorArr.length];
            i = i + 1;
          });
        }
      } else if (
        chartType === "pieChart" ||
        chartType === "gaugeChart" ||
        chartType === "wordCloudChart"
      ) {
        colorScheme[chartType] = {};
        ObjectKeys(chartData?.data).forEach((prop) => {
          colorScheme[chartType][prop] = keepOld
            ? chartCustomization?.colorScheme?.[chartType]?.[prop] ??
              colorArr[i % colorArr.length]
            : colorArr[i % colorArr.length];
          i = i + 1;
        });
      } else if (isDefined(chartData?.data?.series)) {
        chartData?.data?.series?.forEach((series: any) => {
          const seriesColor: { [key: string]: any } = {};

          ObjectKeys(series.seriesData).forEach((prop) => {
            seriesColor[prop] = keepOld
              ? chartCustomization?.colorScheme?.[series.id]?.[prop] ??
                colorArr[i % colorArr.length]
              : colorArr[i % colorArr.length];
            i = i + 1;
          });

          colorScheme[series.id] = seriesColor;
        });
      }
    }
  }

  return colorScheme;
};

const postProcessOptions = (
  chartOptions: any,
  chartData: any,
  chartCustomization: any
) => {
  return chartOptions;
};

export default generateChartOptions;
