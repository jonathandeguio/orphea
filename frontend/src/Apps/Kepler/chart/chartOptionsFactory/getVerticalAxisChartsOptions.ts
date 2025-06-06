import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { getColorTheme, isDefined, isEmpty, notEmpty } from "utils/utilities";
import { getLabelOptions } from "./getLabelOptions";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getItemTooltipOptions, getTooltipOptions } from "./getTooltipOptions";
import { chartDataConfigProps, getRelativeFontSize } from "./index";

const getVerticalAxisChartsConfig = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (isEmpty(chartData.data.series)) {
    // Return some default configuration or handle the undefined case
    console.error(
      "chartData, chartData.stats, or chartData.data.series is undefined"
    );
    return {};
  }
  let data: Array<any> = [];

  const yAxisLabelLeft: string[] = [];
  const yAxisLabelRight: string[] = [];

  chartData.data.series.map((series: any, ind: number) => {
    if (isDefined(series.seriesData)) {
      series.index === "left"
        ? yAxisLabelLeft.push(series.axisName)
        : yAxisLabelRight.push(series.axisName);

      Object.keys(series.seriesData).map((props) => {
        const customizeSeries = chartCustomization.seriesCustomize?.find(
          (customSeries: any) => customSeries.id === series.id
        );

        let obj = {};

        if (series.type === "barChart") {
          obj = {
            type: "bar",
            barGap: 0,
            ...(customizeSeries?.stackedBars
              ? {
                  stack: `${series.id} ${ind}`,
                }
              : {}),
          };
        } else if (series.type === "lineChart") {
          obj = {
            type: "line",
            ...(customizeSeries?.stackedLine
              ? {
                  stack: `${series.id} ${ind}`,
                }
              : {}),

            connectNulls: true,
            smooth: customizeSeries?.lineChartStyle === "smooth",
            step: ["start", "middle", "end"].includes(
              customizeSeries?.lineChartStyle
            )
              ? customizeSeries?.lineChartStyle
              : false,
          };
        } else if (series.type === "lineAreaChart") {
          obj = {
            type: "line",
            stack: `${series.id} ${ind}`,
            areaStyle: {},
          };
        } else if (series.type === "scatterChart") {
          obj = {
            name: props,
            type: "scatter",
            symbol: customizeSeries?.symbol,
            symbolSize: customizeSeries?.symbolSize,
          };
        }

        obj = {
          ...obj,
          ...getLargeDataOptions(),
          name: props,
          itemStyle: {
            color: chartCustomization?.colorScheme?.[series.id]?.[props],
          },
          label: getLabelOptions(
            customizeSeries,
            chartData.request.chartType,
            undefined,
            dimensions
          ),
          yAxisIndex: series.index === "left" ? 0 : 1,
          data: series.seriesData[props],
          emphasis: {
            focus: "series",
          },
        };

        if (isDefined(chartCustomization.chartThreshold)) {
          chartCustomization.chartThreshold.map((threshold: any) => {
            if (
              props === threshold.series &&
              isDefined(threshold.thresholdType)
            ) {
              obj = {
                ...obj,
                markLine: {
                  data: [
                    {
                      type:
                        (obj as any).data.length <= 1 &&
                        threshold.thresholdType === "average"
                          ? "max"
                          : threshold.thresholdType,
                    },
                  ],
                  ...thresholdLabelLine(threshold, dimensions),
                },
              };
            }
          });
        }

        data.push(obj);
      });
    }
  });

  if (isDefined(chartCustomization.chartThreshold)) {
    chartCustomization.chartThreshold
      .filter(
        (threshold: any) =>
          isDefined(threshold.thresholdValue) &&
          (!isDefined(threshold.series) || !isDefined(threshold.thresholdType))
      )
      .map((threshold: any) => {
        data.push({
          label: {
            show: false,
          },
          type: "line",
          markLine: {
            ...thresholdLabelLine(threshold, dimensions),
            data: [
              {
                name: threshold.name,
                yAxis: threshold.thresholdValue,
              },
            ],
          },
        });
      });
  }

  const VerticalAxisChartOptions = {
    toolbox: {
      show: editMode,
      // orient: "vertical",
      // itemSize: 13,
      // top: 15,
      // right: -6,
      feature: {
        // myFeature: {
        //   show: true,
        //   title: "My custom feature",
        //   icon: "path://M432.45,595.444c0,2.177-4.661,6.82-11.305,6.82c-6.475,0-11.306-4.567-11.306-6.82s4.852-6.812,11.306-6.812C427.841,588.632,432.452,593.191,432.45,595.444L432.45,595.444z M421.155,589.876c-3.009,0-5.448,2.495-5.448,5.572s2.439,5.572,5.448,5.572c3.01,0,5.449-2.495,5.449-5.572C426.604,592.371,424.165,589.876,421.155,589.876L421.155,589.876z M421.146,591.891c-1.916,0-3.47,1.589-3.47,3.549c0,1.959,1.554,3.548,3.47,3.548s3.469-1.589,3.469-3.548C424.614,593.479,423.062,591.891,421.146,591.891L421.146,591.891zM421.146,591.891",
        //   onclick: function () {
        //     // do something
        //   },
        // },
        dataZoom: {
          yAxisIndex: "none",
          icon: {
            zoom: "path://", // hack to remove zoom button
            back: "path://", // hack to remove restore button
          },
        },
        // restore: {},
      },
    },
    tooltip: {
      show: chartCustomization.tooltip,
      trigger: chartCustomization.tooltipAxisTrigger,
      entrable: true,
      valueFormatter: (value: any) => {
        return nFormatter(
          value,
          chartCustomization.tooltipPrecision,
          chartCustomization.tooltipMode,
          chartCustomization.tooltipScale
        );
      },

      ...(chartCustomization.tooltipAxisPointer
        ? {
            axisPointer: {
              type: "cross",
              animation: false,
              label: {
                backgroundColor: "#505765",
              },
            },
          }
        : {}),

      formatter: (args: any) =>
        chartCustomization.tooltipAxisTrigger === "item"
          ? getItemTooltipOptions(args, chartData, chartCustomization)
          : getTooltipOptions(args, chartData, chartCustomization),
    },
    ...(chartCustomization.dataZoom
      ? {
          dataZoom: [
            {
              type: "slider",
              show: true,
              xAxisIndex: [0],
              top: "700%",
            },
            {
              type: "slider",
              show: true,
              yAxisIndex: [0],
              left: "700%",
            },
            {
              type: "inside",
              xAxisIndex: [0],
            },
            {
              type: "inside",
              yAxisIndex: [0],
            },
          ],
        }
      : {}),
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization)
      .color,
    xAxis: {
      splitNumber: true,
      type: "category",
      data: chartData.data.xAxisData,
      axisLabel: {
        fontSize: getRelativeFontSize(
          chartCustomization.xAxisFontSize,
          dimensions
        ),
        fontWeight: chartCustomization.xAxisFontWeight,
        color: chartCustomization.xAxisFontColor,
        formatter: (value: any) => {
          return nFormatter(
            value,
            chartCustomization.xAxisPrecision,
            chartCustomization.xAxisMode,
            chartCustomization.xAxisScale
          );
        },
      },
      splitLine: {
        show: chartCustomization.xaxisSplitLine !== "none",
        lineStyle: {
          type: chartCustomization.xaxisSplitLine,
        },
      },
      name: isEmpty(chartCustomization.xaxis)
        ? chartData.request.xaxis
        : chartCustomization.xaxis,

      nameTextStyle: {
        fontWeight: chartCustomization.xAxisLabelFontWeight,
        fontSize: getRelativeFontSize(
          chartCustomization.xAxisLabelFontSize,
          dimensions
        ),
        color: chartCustomization.xAxisLabelFontColor,
      },
      nameLocation: chartCustomization.xaxisTitlePosition,
      nameGap: chartCustomization.xaxisTitleMargin,
    },
    yAxis: [
      {
        // splitNumber: true,
        type: "value",
        name: isEmpty(chartCustomization.yaxisLeft)
          ? yAxisLabelLeft.join(", ")
          : chartCustomization.yaxisLeft,

        nameTextStyle: {
          fontWeight: chartCustomization.leftYAxisLabelFontWeight,
          fontSize: getRelativeFontSize(
            chartCustomization.leftYAxisLabelFontSize,
            dimensions
          ),
          color: chartCustomization.leftYAxisLabelFontColor,
        },
        nameLocation: chartCustomization.yaxisLeftTitlePosition,
        nameGap: chartCustomization.yaxisLeftTitleMargin,
        splitLine: {
          show: chartCustomization.yaxisSplitLine !== "none",
          lineStyle: {
            type: chartCustomization.yaxisSplitLine,
          },
        },
        axisLabel: {
          fontSize: getRelativeFontSize(
            chartCustomization.leftYAxisFontSize,
            dimensions
          ),
          fontWeight: chartCustomization.leftYAxisFontWeight,
          color: chartCustomization.leftYAxisFontColor,
          formatter: (value: any) => {
            return nFormatter(
              value,
              chartCustomization.leftYAxisPrecision,
              chartCustomization.leftYAxisMode,
              chartCustomization.leftYAxisScale
            );
          },
        },
      },
      {
        // splitNumber: true,
        type: "value",
        name: isEmpty(chartCustomization.yaxisRight)
          ? yAxisLabelRight.join(", ")
          : chartCustomization.yaxisRight,
        nameTextStyle: {
          fontWeight: chartCustomization.rightYAxisLabelFontWeight,
          fontSize: getRelativeFontSize(
            chartCustomization.rightYAxisLabelFontSize,
            dimensions
          ),
          color: chartCustomization.rightYAxisLabelFontColor,
        },
        nameLocation: chartCustomization.yAxisRightTitlePosition,
        nameGap: chartCustomization.yaxisRightTitleMargin,
        alignTicks: true,
        inverse: chartCustomization.reversed,
        axisLabel: {
          fontSize: getRelativeFontSize(
            chartCustomization.rightYAxisFontSize,
            dimensions
          ),
          fontWeight: chartCustomization.rightYAxisFontWeight,
          color: chartCustomization.rightYAxisFontColor,
          formatter: (value: any) => {
            return nFormatter(
              value,
              chartCustomization.rightYAxisPrecision,
              chartCustomization.rightYAxisMode,
              chartCustomization.rightYAxisScale
            );
          },
        },
      },
    ],
    series: data.map((serie) => ({
      ...serie,
      silent: !editMode,
      lineStyle: {
        width: 1,
      },
    })),
    barCategoryGap: "3%",
  };

  return VerticalAxisChartOptions;
};

export const thresholdLabelLine = (threshold: any, dimensions: any) => {
  return {
    label: {
      show: threshold.showThresholdLabel,
      formatter: (params: any) => {
        const nameVal = `${threshold.thresholdFontPrefix}${threshold.name}${threshold.thresholdFontSuffix}`;
        return notEmpty(threshold.name) ? nameVal : params.value;
      },
      color: threshold.thresholdFontColor,
      distance: threshold.thresholdLabelDistance,
      fontSize: getRelativeFontSize(threshold.thresholdFontSize, dimensions),
      fontWeight: threshold.thresholdFontWeight,
      position: threshold.thresholdLabelPosition,
    },
    lineStyle: {
      type: "solid",
      width: threshold.lineWidth,
      color: threshold.color,
    },
  };
};

export default getVerticalAxisChartsConfig;
