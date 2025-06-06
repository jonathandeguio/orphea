import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { ObjectKeys } from "utils/utilities";
import { getItemStyle } from "../charts.utils";
import { getLargeDataOptions } from "./getLargeDataOptions";
import { getItemTooltipOptions } from "./getTooltipOptions";
import { chartDataConfigProps } from "./index";

const getWordCloudChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  return {
    tooltip: {
      show: chartCustomization.tooltip,
      valueFormatter: (value: any) => {
        return nFormatter(
          value,
          chartCustomization.tooltipPrecision,
          chartCustomization.tooltipMode,
          chartCustomization.tooltipScale
        );
      },

      formatter: (args: any) =>
        getItemTooltipOptions(args, chartData, chartCustomization),
    },
    series: [
      {
        ...getLargeDataOptions(),
        type: "wordCloud",
        gridSize: 2,
        sizeRange: [
          chartCustomization.wordCloudMinFontSize,
          chartCustomization.wordCloudMaxFontSize,
        ],
        rotationRange: [-90, 90],

        shape: chartCustomization.wordCloudShape,
        width: 600,
        height: 400,
        drawOutOfBound: true,
        data: ObjectKeys(chartData?.data).map((key) => ({
          name: chartCustomization?.customLabel[key] ?? key,
          value: chartData?.data[key][0][0],

          textStyle: {
            color:
              getItemStyle(
                chartCustomization,
                key,
                chartData?.data[key][0][0],
                "wordCloudChart"
              ).color ?? "#000000",
          },
        })),
      },
    ].map((serie) => ({ ...serie, silent: !editMode })),
  };
};

export default getWordCloudChartOptions;
