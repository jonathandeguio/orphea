import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { chartDataConfigProps, getRelativeFontSize } from ".";
import { getThreshold } from "../charts.utils";
import { isDefined, notEmpty } from "utils/utilities";

const getBigNumberChartConfig = ({
  chartData,
  chartCustomization,
  dimensions,
  isDarkTheme,
}: chartDataConfigProps) => {
  let bigNumberFontColor =
    !isDefined(chartCustomization.bigNumberFontColor) ||
    chartCustomization.bigNumberFontColor == "#00000000"
      ? isDarkTheme
        ? "#e4e6ec"
        : "#000000"
      : chartCustomization.bigNumberFontColor;

  let bigNumberSubheaderFontColor =
    !isDefined(chartCustomization.subHeaderFontColor) ||
    chartCustomization.subHeaderFontColor == "#00000000"
      ? isDarkTheme
        ? "#e4e6ec"
        : "#000000"
      : chartCustomization.subHeaderFontColor;
  const bigNumberOptions = {
    graphic: {
      elements: [
        {
          type: "text",
          left: chartCustomization.horizontalAlignment,
          top: isDefined(chartCustomization.bigNumberTop)
            ? `${chartCustomization.bigNumberTop}%`
            : chartCustomization.verticalAlignment,
          style: {
            text: `${chartCustomization.bigNumberFontPrefix}${nFormatter(
              Number(chartData.data),
              chartCustomization.bigNumberPrecision,
              chartCustomization.bigNumberMode,
              chartCustomization.bigNumberScale
            )}${chartCustomization.bigNumberFontSuffix}`,
            fontSize: getRelativeFontSize(
              chartCustomization.bigNumberFontSize,
              dimensions
            ),
            fontWeight: chartCustomization.bigNumberFontWeight,
            fill: getThreshold(
              chartCustomization,
              chartData.data,
              bigNumberFontColor
            ).color,
          },
        },
        notEmpty(chartCustomization.subHeader)
          ? {
              type: "text",
              left: chartCustomization.subHeaderHorizontalAlignment,
              top: `${chartCustomization.subheaderTop}%`,
              style: {
                text: chartCustomization.subHeader,
                fontSize: getRelativeFontSize(
                  chartCustomization.subHeaderFontSize,
                  dimensions
                ),
                fill: bigNumberSubheaderFontColor,
                fontWeight: chartCustomization.subHeaderFontWeight,
              },
            }
          : { type: "text" },
      ],
    },
  };

  return bigNumberOptions;
};

export default getBigNumberChartConfig;
