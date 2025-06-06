import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { isDefined, isEmpty } from "utils/utilities";
import { getRelativeFontSize } from ".";

export const getTValue = (tParam: any, chartType?: string) => {
  if (isDefined(chartType)) {
    if (chartType === "sunBurstChart") {
      return tParam.name;
    } else if (chartType === "horizontalBarChart") {
      return tParam.value[0];
    }
  }
  if (isDefined(tParam.value)) {
    if (Array.isArray(tParam.value)) {
      return tParam.value.length <= 1 ? tParam.value[0] : tParam.value[1];
    } else {
      return tParam.value;
    }
  }

  return tParam.name;
};

export const getPieLabelFormatter = (chartCustomization: any, name: string) => {
  return (value: any) => {
    const values: any = {
      label: chartCustomization.customLabel?.[value.name] ?? value.name,
      percentage: value.percent + "%",
      value: nFormatter(
        getTValue(value),
        chartCustomization[`${name}Precision`],
        chartCustomization[`${name}Mode`],
        chartCustomization[`${name}Scale`]
      ),
    };

    return Array.isArray(chartCustomization[`${name}Config`])
      ? chartCustomization[`${name}Config`]
          .map((label: string) => values[label])
          .join(", ")
      : value.percent + "%";
  };
};

export const getTreeMapLabelFormatter = (
  chartCustomization: any,
  name: string
) => {
  return (value: any) => {
    const values: any = {
      label: chartCustomization.customLabel?.[value.name] ?? value.name,
      value: nFormatter(
        getTValue(value),
        chartCustomization[`${name}Precision`],
        chartCustomization[`${name}Mode`],
        chartCustomization[`${name}Scale`]
      ),
    };

    return Array.isArray(chartCustomization[`${name}Config`])
      ? chartCustomization[`${name}Config`]
          .map((label: string) => values[label])
          .join("\n\n")
      : value.percent + "%";
  };
};

export const getSliceLabelOptions = (
  chartCustomization: any,
  dimensions: any
) => {
  return {
    position: "inside",
    show:
      Array.isArray(chartCustomization["sliceLabelConfig"]) &&
      chartCustomization["sliceLabelConfig"].length > 0,
    formatter: getPieLabelFormatter(chartCustomization, "sliceLabel"),
    color: chartCustomization.sliceLabelFontColor,
    fontWeight: chartCustomization.sliceLabelFontWeight,
    fontSize: getRelativeFontSize(
      chartCustomization.sliceLabelFontSize,
      dimensions
    ),
  };
};

export const getLabelOptions = (
  chartCustomization: any,
  chartType: string,
  position: undefined | string = undefined,
  dimensions: any
) => {
  if (isDefined(chartCustomization)) {
    return {
      show:
        chartCustomization.showLabel &&
        (!Array.isArray(chartCustomization.labelConfig) ||
          chartCustomization.labelConfig.length > 0),
      position: position ?? chartCustomization.labelPosition,
      rotate: chartCustomization.labelRotate,
      color: chartCustomization.labelFontColor,
      fontSize: getRelativeFontSize(
        chartCustomization.labelFontSize,
        dimensions
      ),
      fontWeight: chartCustomization.labelFontWeight,
      formatter: (params: any) => {
        if (chartType === "pieChart") {
          return getPieLabelFormatter(chartCustomization, "label")(params);
        }
        if (chartType === "treeMapChart") {
          return getTreeMapLabelFormatter(chartCustomization, "label")(params);
        }

        const value = getTValue(params, chartType);

        return nFormatter(
          value,
          chartCustomization.labelPrecision,
          chartCustomization.labelMode,
          chartCustomization.labelScale
        );
      },
    };
  } else {
    return {
      show: false,
    };
  }
};
