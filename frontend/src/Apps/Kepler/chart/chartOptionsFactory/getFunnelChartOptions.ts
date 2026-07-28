import { getColorTheme } from "utils/utilities";
import { getItemTooltipOptions } from "./getTooltipOptions";
import { chartDataConfigProps } from "./index";

const getFunnelChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getFunnelChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const seriesData: Array<{ name: string; value: number }> = [];

  Object.keys(chartData.data).forEach((dKey: string) => {
    seriesData.push({
      name: dKey,
      value: Number(chartData.data[dKey][0]),
    });
  });

  const sortDirection: string = chartCustomization.funnelSort ?? "descending";
  const gap: number = chartCustomization.funnelGap ?? 2;
  const minSize: string = chartCustomization.funnelMinSize ?? "0%";
  const maxSize: string = chartCustomization.funnelMaxSize ?? "100%";

  const maxValue = seriesData.reduce((acc, item) => Math.max(acc, item.value), 0);

  return {
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip,
      trigger: "item",
      formatter: (args: any) =>
        getItemTooltipOptions(args, chartData, chartCustomization),
    },
    series: [
      {
        type: "funnel",
        left: "10%",
        width: "80%",
        min: 0,
        max: maxValue > 0 ? maxValue : 100,
        minSize,
        maxSize,
        sort: sortDirection,
        gap,
        label: {
          show: true,
          position: chartCustomization.funnelLabelPosition ?? "inside",
        },
        labelLine: {
          length: 10,
          lineStyle: { width: 1 },
        },
        data: seriesData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
        silent: !editMode,
      },
    ],
  };
};

export default getFunnelChartOptions;
