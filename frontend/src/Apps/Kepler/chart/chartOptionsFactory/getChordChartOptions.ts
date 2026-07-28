import { chartDataConfigProps } from "./index";

/**
 * Chord diagram factory.
 *
 * IMPORTANT: `series.type: 'chord'` is only available from ECharts 6.0.0.
 * The current project uses ECharts 5.4.3. This chart type is disabled in
 * SliderController until a migration to ECharts 6 is performed.
 *
 * This factory returns an empty object as a safe fallback. When ECharts is
 * upgraded to v6, replace the body with a proper chord implementation using:
 *   series: [{ type: 'chord', nodes: [...], links: [...] }]
 */
const getChordChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  console.warn(
    "getChordChartOptions: chord chart requires ECharts >= 6.0.0. " +
      "Current version is 5.4.3. This chart type is disabled."
  );
  return {};
};

export default getChordChartOptions;
