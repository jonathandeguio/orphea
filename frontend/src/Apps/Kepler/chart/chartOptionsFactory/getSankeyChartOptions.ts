import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

const getSankeyChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getSankeyChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  // Expected data format from backend:
  // chartData.data = {
  //   nodes: [{ name: string }],
  //   links: [{ source: string, target: string, value: number }]
  // }
  const sankeyData = chartData.data;

  const nodes: { name: string }[] = isDefined(sankeyData.nodes)
    ? sankeyData.nodes
    : [];
  const links: { source: string; target: string; value: number }[] = isDefined(
    sankeyData.links
  )
    ? sankeyData.links
    : [];

  const orient: string = chartCustomization.sankeyOrient ?? "horizontal";

  return {
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "item",
      triggerOn: "mousemove",
    },
    series: [
      {
        type: "sankey",
        layout: "none",
        orient,
        emphasis: { focus: "adjacency" },
        nodes,
        links,
        lineStyle: {
          color: "gradient",
          curveness: 0.5,
        },
        label: {
          position: orient === "horizontal" ? "right" : "top",
        },
        silent: !editMode,
      },
    ],
  };
};

export default getSankeyChartOptions;
