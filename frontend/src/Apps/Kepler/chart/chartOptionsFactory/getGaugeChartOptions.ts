import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { ObjectKeys, getColorTheme, isDefined, isEmpty } from "utils/utilities";
import { chartDataConfigProps, getRelativeFontSize } from ".";
import { getThreshold } from "../charts.utils";
import { getLargeDataOptions } from "./getLargeDataOptions";

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

  const gaugeOptions = {
    color: getColorTheme(chartCustomization?.colorTheme, chartCustomization)
      .color,
    series: [
      {
        ...getLargeDataOptions(),
        type: "gauge",
        min: 0,
        max:
          chartCustomization.maxValue ??
          chartData.stats[ObjectKeys(chartData.stats)[0]][0],
        pointer: {
          icon: "path://M12.8,0.7l12,40.1H0.7L12.8,0.7z",
          length: "12%",
          width: 20,
          offsetCenter: [0, "-60%"],
        },
        axisLabel: {
          distance: 25,
          formatter: (value: any) =>
            nFormatter(
              value,
              chartCustomization.gaugePrecision,
              chartCustomization.gaugeMode,
              chartCustomization.gaugeScale
            ),
        },
        axisLine:
          isDefined(chartCustomization.chartThreshold) &&
          chartCustomization.chartThreshold.length > 0
            ? {
                lineStyle: {
                  width: chartCustomization.axisLineWidth,
                  color: [
                    ...chartCustomization.chartThreshold
                      .filter((threshold: any) => {
                        return isDefined(threshold.thresholdValue);
                      })
                      .map((threshold: any, index: number) => [
                        threshold.thresholdValue /
                          (chartCustomization.maxValue ??
                            chartData.stats[ObjectKeys(chartData.stats)[0]][0]),
                        threshold.color,
                      ]),

                    [1, "#E6EBF8"],
                  ],
                },
              }
            : {},

        data: Object.keys(chartData.data).map((key, index) => {
          return {
            value: chartData.data[key][0],
            name: isEmpty(chartData.request.dimensions)
              ? chartCustomization.customLabel?.[
                  `${chartData.request.series[0].aggregate}(${chartData.request.series[0].columnName})`
                ] ??
                `${chartData.request.series[0].aggregate}(${chartData.request.series[0].columnName})`
              : chartCustomization.customLabel?.[key] ?? key,
            detail: {
              valueAnimation: true,
              offsetCenter: ["0%", `${60 - (index + 1) * 25}%`],
            },
            title: {
              offsetCenter: [
                "0%",
                `${
                  getRelativeFontSize(
                    chartCustomization.gaugeTitleFontSize,
                    dimensions
                  ) *
                    0.1 +
                  70 -
                  (index + 1) * 25
                }%`,
              ],
            },
            ...(chartCustomization?.colorTheme === "custom"
              ? {
                  itemStyle: {
                    color: chartCustomization?.colorScheme?.gaugeChart?.[key],
                  },
                }
              : {}),
          };
        }),
        progress: {
          show: true,
          width: 0,
          overlap: true,
        },
        title: {
          color: chartCustomization.gaugeTitleFontColor,
          fontSize: getRelativeFontSize(
            chartCustomization.gaugeTitleFontSize,
            dimensions
          ),
          fontWeight: chartCustomization.gaugeTitleFontWeight,
        },
        detail: {
          borderRadius: 3,
          color: chartCustomization.gaugeTitleFontColor ?? "inherit",
          fontSize: getRelativeFontSize(
            chartCustomization.gaugeTitleFontSize,
            dimensions
          ),
          fontWeight: chartCustomization.gaugeTitleFontWeight,
          formatter: (value: any) =>
            nFormatter(
              value,
              chartCustomization.gaugePrecision,
              chartCustomization.gaugeMode,
              chartCustomization.gaugeScale
            ),
        },
      },
      {
        type: "gauge",
        min: 0,
        max:
          chartCustomization.maxValue ??
          chartData.stats[ObjectKeys(chartData.stats)[0]][0],
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },

        splitLine: {
          show: false,
        },
        axisLabel: {
          show:
            isDefined(chartCustomization.chartThreshold) &&
            chartCustomization.chartThreshold.length > 0,
          color: chartCustomization.thresholdLabelFontcolor,
          fontWeight: chartCustomization.thresholdLabelFontWeight,
          fontSize: getRelativeFontSize(
            chartCustomization.thresholdLabelFont,
            dimensions
          ),
          distance: chartCustomization.thresholdLabelDistance ?? -60,
          rotate: "tangential",
          formatter:
            isDefined(chartCustomization.chartThreshold) &&
            chartCustomization.chartThreshold.length > 0
              ? (value: any) =>
                  getThreshold(chartCustomization, value, "#000000").name
              : undefined,
        },
      },
      chartCustomization.goalValue
        ? {
            type: "gauge",
            min: 0,
            max:
              chartCustomization.maxValue ??
              chartData.stats[ObjectKeys(chartData.stats)[0]][0],
            pointer: {
              icon: "path://M2.9,0.7L2.9,0.7c1.4,0,2.6,1.2,2.6,2.6v115c0,1.4-1.2,2.6-2.6,2.6l0,0c-1.4,0-2.6-1.2-2.6-2.6V3.3C0.3,1.9,1.4,0.7,2.9,0.7z",
              length: "20%",
              width: 4,
              showAbove: true,
              offsetCenter: [0, "-90%"],
              itemStyle: {
                color: "#FF0000",
              },
            },
            axisLine: {
              show: false,
            },
            axisTick: {
              show: false,
            },
            axisLabel: {
              show: false,
            },
            splitLine: {
              show: false,
            },
            data: [
              {
                value: chartCustomization.goalValue,
                name: "Goal",
              },
            ],
            title: { show: false },
            detail: { show: false },
          }
        : {},
    ].map((serie) => ({ ...serie, silent: !editMode })),
  };
  return gaugeOptions;
};
