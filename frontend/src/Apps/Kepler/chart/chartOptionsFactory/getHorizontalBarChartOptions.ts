import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { getColorTheme, isDefined, isEmpty } from "utils/utilities";
import { chartDataConfigProps, getRelativeFontSize } from ".";
import { getLabelOptions } from "./getLabelOptions";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getTooltipOptions } from "./getTooltipOptions";
import { thresholdLabelLine } from "./getVerticalAxisChartsOptions";

const getHorizontalBarChartConfig = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!isDefined(chartData?.data?.series)) {
    // Return some default configuration or handle the undefined case
    console.error("chartData, chartData.stats, or chartData.data is undefined");
    return {};
  }

  let data: Array<any> = [];

  const yAxisLabelLeft: string[] = [];
  const yAxisLabelRight: string[] = [];

  chartData.data.series.map((series: any, index: number) => {
    series.index === "left"
      ? yAxisLabelLeft.push(series.axisName)
      : yAxisLabelRight.push(series.axisName);

    for (const props in series.seriesData) {
      const customizeSeries = chartCustomization.seriesCustomize.find(
        (customSeries: any) => customSeries.id === series.id
      );

      console.log("series.seriesData", series.seriesData);

      data.push({
        ...getLargeDataOptions(),
        name: props,
        itemStyle: {
          color: chartCustomization?.colorScheme?.[series.id]?.[props],
        },
        yAxisIndex: series.index === "left" ? 0 : 1,
        type: "bar",
        barGap: 0,
        ...(customizeSeries?.stackedBars
          ? {
              stack: `${series.name} ${index}`,
            }
          : {}),
        label: getLabelOptions(
          customizeSeries,
          chartData.request.chartType,
          undefined,
          dimensions
        ),
        emphasis: {
          focus: "series",
        },
        data: series.seriesData[props].map((point: any) => [
          point[1],
          point[0],
        ]),
      });
    }
  });

  if (
    isDefined(chartCustomization.chartThreshold) &&
    chartCustomization.chartThreshold.length > 0
  ) {
    chartCustomization.chartThreshold
      .filter((threshold: any) => {
        return isDefined(threshold.thresholdValue);
      })
      .map((threshold: any, index: number) => {
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
                xAxis: threshold.thresholdValue,
              },
            ],
          },
        });
      });
  }

  const horizontalBarChartConfig = {
    toolbox: {
      show: editMode,
      // orient: "vertical",
      // itemSize: 13,
      // top: 15,
      // right: -6,
      feature: {
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

      ...(chartCustomization.tooltipAxisTrigger === "item"
        ? {}
        : {
            formatter: (args: any) =>
              getTooltipOptions(args, chartData, chartCustomization),
          }),
    },
    // legend: getLegendOptions(chartCustomization),
    ...(chartCustomization.dataZoom
      ? {
          dataZoom: {
            start: 0,
            end: 100,
          },
        }
      : {}),
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization)
      .color,
    xAxis: {
      type: "value",
      splitLine: {
        show: chartCustomization.xaxisSplitLine !== "none",
        lineStyle: {
          type: chartCustomization.xaxisSplitLine,
        },
      },
      nameTextStyle: {
        fontWeight: chartCustomization.xAxisLabelFontWeight,
        fontSize: getRelativeFontSize(
          chartCustomization.xAxisLabelFontSize,
          dimensions
        ),
        color: chartCustomization.xAxisLabelFontColor,
      },
      name: isEmpty(chartCustomization.xaxis)
        ? yAxisLabelLeft.join(", ")
        : chartCustomization.xaxis,
      nameLocation: chartCustomization.xaxisTitlePosition,
      nameGap: chartCustomization.xaxisTitleMargin,
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
    },
    yAxis: {
      type: "category",
      data: chartData.data.xAxisData,
      nameTextStyle: {
        fontWeight: chartCustomization.leftYAxisLabelFontWeight,
        fontSize: getRelativeFontSize(
          chartCustomization.leftYAxisLabelFontSize,
          dimensions
        ),
        color: chartCustomization.leftYAxisLabelFontColor,
      },
      name: isEmpty(chartCustomization.yaxisLeft)
        ? chartData.request.xaxis
        : chartCustomization.yaxisLeft,
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
    series: data.map((serie) => ({ ...serie, silent: !editMode })),
    barCategoryGap: "3%",
  };

  return horizontalBarChartConfig;
};

export default getHorizontalBarChartConfig;
