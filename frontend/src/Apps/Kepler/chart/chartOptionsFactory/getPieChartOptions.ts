import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { getColorTheme } from "utils/utilities";
import { getThreshold } from "../charts.utils";
import { getLabelOptions, getSliceLabelOptions } from "./getLabelOptions";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getItemTooltipOptions } from "./getTooltipOptions";
import { chartDataConfigProps, getRelativeFontSize } from "./index";

const getPieChartConfig = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  const series: Array<any> = [];
  let nightangleData = chartCustomization.nightangle
    ? {
        roseType: "area",
        itemStyle: {
          borderRadius: 8,
        },
      }
    : {};
  let radius = [
    `${chartCustomization.innerRadius ?? 40}%`,
    `${chartCustomization.radius ?? 70}%`,
  ];
  let sum = 0;

  Object.keys(chartData.data).forEach((dKey: any) => {
    sum = sum + Number(chartData.data[dKey][0]);
    series.push({
      label: getLabelOptions(
        chartCustomization,
        chartData.request.chartType,
        "outside",
        dimensions
      ),
      itemStyle: {
        color: chartCustomization?.colorScheme?.["pieChart"]?.[dKey],
      },
      value: chartData.data[dKey][0],
      name: dKey,
    });
  });

  const pieSeries: any[] = [
    {
      ...getLargeDataOptions(),
      type: "pie",
      radius: radius,
      data: series,
      percentPrecision: chartCustomization.pieLabelPercentagePrecision,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: "rgba(0, 0, 0, 0.5)",
        },
      },
    },
  ];

  if (chartCustomization.showSum) {
    const seriesCopy = series.map((item) => ({
      ...item,
      label: undefined,
    }));
    pieSeries.push({
      type: "pie",
      radius: radius,
      data: seriesCopy,
      avoidLabelOverlap: true,
      percentPrecision: chartCustomization.sliceLabelPercentagePrecision,
      label: {
        color: getThreshold(
          chartCustomization,
          sum,
          chartCustomization.sumFontColor
        ).color,
        fontWeight: chartCustomization.sumFontWeight,
        fontSize: getRelativeFontSize(
          chartCustomization.sumFontSize,
          dimensions
        ),
        position: "center",
        formatter: () => {
          return nFormatter(
            sum,
            chartCustomization.showSumPrecision,
            chartCustomization.showSumMode,
            chartCustomization.showSumScale
          );
        },
      },
      labelLine: {
        show: false,
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: "rgba(0, 0, 0, 0.5)",
        },
      },
    });
  }
  if (chartCustomization.sliceLabel) {
    const seriesCopy = series.map((item) => ({
      ...item,
      label: getSliceLabelOptions(chartCustomization, dimensions),
    }));

    pieSeries.push({
      type: "pie",
      percentPrecision: chartCustomization.sliceLabelPercentagePrecision,
      radius: radius,
      data: seriesCopy,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: "rgba(0, 0, 0, 0.5)",
        },
      },
    });
  }

  const pieOptions = {
    tooltip: {
      show: chartCustomization.tooltip,
      trigger: "item",
      formatter: (args: any) =>
        getItemTooltipOptions(args, chartData, chartCustomization),
    },

    ...nightangleData,
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization)
      .color,
    series: pieSeries.map((serie) => ({ ...serie, silent: !editMode })),
  };

  return pieOptions;
};

export default getPieChartConfig;
