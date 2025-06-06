import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from ".";
import { getItemStyle } from "../charts.utils";
import { getLabelOptions } from "./getLabelOptions";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getItemTooltipOptions } from "./getTooltipOptions";

export default ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    // Return some default configuration or handle the undefined case
    console.error(
      "chartData, chartData.stats, or chartData.series is undefined"
    );
    return {};
  }
  let hierarchy: { [key: string]: any }[] = [];

  let axisName = "";
  if (
    isDefined(chartData.request?.series) &&
    chartData.request.series.length > 0
  ) {
    axisName = chartData.stats;
  }

  if (isDefined(chartData.data)) {
    chartData.data?.forEach((item: any) => {
      const keys = chartData.request.dimensions;
      const lastIndex = (keys?.length ?? 1) - 1;

      let temp: any = hierarchy;
      keys?.forEach((key: string, index: number) => {
        const value = item[key];

        const childArr: any = temp.filter((t: any) => t.name === value);

        const child = childArr.length > 0 ? childArr[0] : undefined;

        if (isDefined(child)) {
          child.value = child.value + 1;
          temp = child.children;
        } else {
          const newChild = {
            name: `${value}`,
            dimension: key,
            ...(lastIndex === index ? { value: item[axisName] } : {}),
            children: [],
            itemStyle: getItemStyle(
              chartCustomization,
              value,
              item[axisName],
              "treeMapChart"
            ),
          };

          temp.push(newChild);
          temp = newChild.children;
        }
      });
    });
  }

  return {
    color: getColorTheme(chartCustomization?.colorTheme, chartCustomization)
      .color,
    tooltip: {
      show: chartCustomization.tooltip,
      trigger: "item",
      formatter: (args: any) =>
        getItemTooltipOptions(args, chartData, chartCustomization),
    },
    ...(chartCustomization?.legend
      ? {
          legend: {
            type: chartCustomization?.legendType,
            orient: "horizontal",
            icon: "rect",
            itemWidth: 15,
            itemHeight: 15,
            ...(chartCustomization?.legendPosition === "right"
              ? {
                  orient: "vertical",
                  right: 30,
                  top: 10,
                  textStyle: {
                    width: 120,
                    overflow: "truncate",
                  },
                }
              : {}),
          },
        }
      : {}),
    series: {
      ...getLargeDataOptions(),
      type: "treemap",
      silent: !editMode,
      universalTransition: true,
      data: hierarchy,
      nodeClick: undefined,
      label: getLabelOptions(
        chartCustomization,
        chartData.request.chartType,
        undefined,
        dimensions
      ),
    },
  };
};
