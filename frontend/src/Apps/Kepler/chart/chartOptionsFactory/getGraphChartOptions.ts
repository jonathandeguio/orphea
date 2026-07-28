import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * Graph / Network chart factory.
 *
 * Expected data format from backend:
 * chartData.data = {
 *   nodes: [{ id: string; name: string; symbolSize?: number; category?: number }],
 *   links: [{ source: string; target: string }],
 *   categories?: [{ name: string }]
 * }
 */
const getGraphChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getGraphChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const graphData = chartData.data;

  const nodes: { id: string; name: string; symbolSize?: number; category?: number }[] =
    isDefined(graphData.nodes) ? graphData.nodes : [];
  const links: { source: string; target: string }[] = isDefined(graphData.links)
    ? graphData.links
    : [];
  const categories: { name: string }[] = isDefined(graphData.categories)
    ? graphData.categories
    : [];

  const layout: string = chartCustomization.graphLayout ?? "force";
  const roam: boolean = chartCustomization.graphRoam ?? true;
  const showLabel: boolean = chartCustomization.graphShowLabel ?? true;

  return {
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "item",
    },
    legend:
      categories.length > 0
        ? {
            data: categories.map((c) => c.name),
          }
        : undefined,
    series: [
      {
        type: "graph",
        layout,
        roam,
        label: {
          show: showLabel,
          position: "right",
        },
        edgeSymbol: ["none", "arrow"],
        force: {
          repulsion: 100,
          edgeLength: 80,
        },
        nodes,
        links,
        categories: categories.length > 0 ? categories : undefined,
        emphasis: {
          focus: "adjacency",
          lineStyle: { width: 4 },
        },
        silent: !editMode,
      },
    ],
  };
};

export default getGraphChartOptions;
