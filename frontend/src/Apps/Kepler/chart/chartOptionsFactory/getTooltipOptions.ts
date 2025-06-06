import { nFormatter } from "Apps/Kepler/utils/NumberFormatter";
import { getTValue } from "./getLabelOptions";

export const getRadarTooltip = (
  args: any,
  chartdata: any,
  chartCustomization: any
) => {
  const parent =
    document.getElementsByClassName("tooltip_header")[0]?.parentElement;
  if (parent) parent.classList.add("tooltip_wrapper");

  let tooltip = `<div class="tooltip_header">${
    chartCustomization.customLabel?.[args.name] ?? args.name
  }</div>`;

  let i = 0;

  tooltip += `<div class="tooltip_body">`;
  Object.keys(args.data.extras).map((key: string) => {
    tooltip += `<div class="tooltip_item">
        <div class="tooltip_name">
          <div class="tooltip_marker" style="background-color: ${
            args.color
          };"></div> 
          <div>${chartCustomization.customLabel?.[key] ?? key}</div>
        </div> 
        <div>${
          nFormatter(
            args.data.extras[key][0][0],
            chartCustomization.tooltipPrecision,
            chartCustomization.tooltipMode,
            chartCustomization.tooltipScale
          ) ?? 0
        }</div>
      </div>`;

    i = i + 1;
  });

  tooltip += "</div>";
  return tooltip;
};

export const getTooltipOptions = (
  args: any,
  chartdata: any,
  chartCustomization: any
) => {
  const parent =
    document.getElementsByClassName("tooltip_header")[0]?.parentElement;
  if (parent) parent.classList.add("tooltip_wrapper");

  let tooltip = `<div class="tooltip_header">${args[0].axisValue}</div>`;

  let i = 0;

  tooltip += `<div class="tooltip_body">`;
  chartdata.data.series.map((series: any) => {
    tooltip += `<div class="tooltip_series">${series.name}</div>`;
    if (args[i]?.seriesName === "Placeholder") {
      i = i + 1;
    }
    Object.keys(series.seriesData).map((key: string) => {
      if (key === args[i]?.seriesName) {
        tooltip += `<div class="tooltip_item">
          <div class="tooltip_name">
            <div class="tooltip_marker" style="background-color: ${
              args[i].color
            };"></div>
            <div>${chartCustomization.customLabel?.[key] ?? key}</div>
          </div>
          <div>${
            args[i].value
              ? nFormatter(
                  args[i].value[
                    ["VerticalAxisChart", "waterFallChart"].includes(
                      chartdata.request.chartType
                    )
                      ? 1
                      : 0
                  ],
                  chartCustomization.tooltipPrecision,
                  chartCustomization.tooltipMode,
                  chartCustomization.tooltipScale
                ) ?? 0
              : 0
          }</div>
        </div>`;

        i = i + 1;
      }
    });
  });

  tooltip += "</div>";

  return tooltip;
};

export const getItemTooltipOptions = (
  args: any,
  chartdata: any,
  chartCustomization: any
) => {
  let tooltip = ``;
  if (
    ["VerticalAxisChart", "waterFallChart"].includes(
      chartdata.request.chartType
    )
  ) {
    console.log("WE ARE HERE", args.seriesName);
    tooltip += `<div>${
      chartCustomization.customLabel?.[args.seriesName] ?? args.seriesName
    }</div>`;
  }

  return (
    tooltip +
    `${args.marker} ${
      chartCustomization.customLabel?.[args.name] ?? args.name
    } <strong style="padding-left:1rem;">${nFormatter(
      getTValue(args),
      chartCustomization.tooltipPrecision,
      chartCustomization.tooltipMode,
      chartCustomization.tooltipScale
    )}</strong>`
  );
};
