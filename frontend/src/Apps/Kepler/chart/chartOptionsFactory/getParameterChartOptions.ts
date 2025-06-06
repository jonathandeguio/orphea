import { chartDataConfigProps } from ".";

export default ({
  chartData,
  chartCustomization,
  dimensions,
  isDarkTheme,
}: chartDataConfigProps) => {
  console.log(
    "chartCustomization.allowMultiselect",
    chartCustomization.allowMultiselect
  );
  return {
    chartData: chartData,
    chartCustomization: {
      layout: chartCustomization.layout,
      parameterFormSize: chartCustomization.parameterFormSize,
      allowMultiselect: chartCustomization.allowMultiselect,
      parameterLabelPosition: chartCustomization.parameterLabelPosition,
    },
  };
};
