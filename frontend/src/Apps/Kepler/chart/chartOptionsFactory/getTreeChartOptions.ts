import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from ".";
import { getItemTooltipOptions } from "./getTooltipOptions";

const getTreeChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getTreeChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  // Reuses the same hierarchical data building logic as sunBurstChart.
  // The backend returns raw rows; hierarchy is built from chartData.request.dimensions.
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
            name: value,
            dimension: key,
            ...(lastIndex === index ? { value: item[axisName] } : {}),
            children: [],
          };
          temp.push(newChild);
          temp = newChild.children;
        }
      });
    });
  }

  // Wrap in a single root node if multiple top-level nodes exist
  const treeData =
    hierarchy.length === 1
      ? hierarchy
      : [{ name: "Root", children: hierarchy }];

  const orient: string = chartCustomization.treeOrient ?? "LR";
  const layout: string = chartCustomization.treeLayout ?? "orthogonal";

  return {
    color: getColorTheme(chartCustomization?.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip,
      trigger: "item",
      formatter: (args: any) =>
        getItemTooltipOptions(args, chartData, chartCustomization),
    },
    series: [
      {
        type: "tree",
        data: treeData,
        top: "5%",
        left: "7%",
        bottom: "5%",
        right: "20%",
        symbolSize: 7,
        orient,
        layout,
        label: {
          position: "left",
          verticalAlign: "middle",
          fontSize: 12,
        },
        leaves: {
          label: {
            position: "right",
            verticalAlign: "middle",
          },
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750,
        silent: !editMode,
      },
    ],
  };
};

export default getTreeChartOptions;
