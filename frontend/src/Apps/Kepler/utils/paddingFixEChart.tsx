import { isDefined, notEmpty } from "utils/utilities";

export function fixYAxisNameGap(chartInstance: any, options: any) {
  const globalModel = chartInstance._api.getModel();
  const yAxisList = globalModel.option.yAxis;
  const xAxisList = globalModel.option.xAxis;

  let maxXAxisLabelFontSize = 0;
  let maxYAxisLabelFontSize = 0;

  const yAxisNameGaps = [
    options?.yAxis?.[0]?.nameGap ?? 0,
    options?.yAxis?.[1]?.nameGap ?? 0,
  ];
  const xAxisNameGap = options?.xAxis?.nameGap;

  const ctx = document.createElement("canvas").getContext("2d");

  if (isDefined(ctx)) {
    const yAxisNameGapList: any[] = [];
    const yAxisPaddingList: any[] = [];
    const xAxisNameGapList: any[] = [];
    const xAxisPaddingList: any[] = [];

    if (notEmpty(xAxisList)) {
      xAxisList.forEach(
        ({ nameTextStyle, axisLabel, name }: any, index: number) => {
          const nameFontSize = nameTextStyle?.fontSize ?? 12;
          const nameFontFamily = nameTextStyle?.fontFamily ?? "sans-serif";

          const axisLabelFontSize = axisLabel?.fontSize ?? 12;
          const axisLabelFontFamily = axisLabel?.fontFamily ?? "sans-serif";
          maxXAxisLabelFontSize = Math.max(
            maxXAxisLabelFontSize,
            axisLabelFontSize
          );
          ctx.save();
          ctx.font = `${axisLabelFontSize}px ${axisLabelFontFamily}`;
          const xAxis = globalModel.getComponent("xAxis", index)?.axis;

          const viewLabels = xAxis.getViewLabels();
          let maxHeight = 0;
          if (notEmpty(viewLabels)) {
            const firstItem = viewLabels[0];
            const metrics = ctx.measureText(firstItem.formattedLabel);
            maxHeight =
              metrics.actualBoundingBoxAscent +
              metrics.actualBoundingBoxDescent;
          }

          const axisLabelMargin = axisLabel?.margin ?? 8;
          xAxisNameGapList.push(
            maxHeight + (xAxisNameGap ?? 0) + axisLabelMargin
          );
          ctx.restore();

          ctx.save();
          ctx.font = `${nameFontSize}px ${nameFontFamily}`;
          let height = 0;
          if (isDefined(name) && name !== "") {
            const nameMetrics = ctx.measureText(name);
            height =
              nameMetrics.actualBoundingBoxAscent +
              nameMetrics.actualBoundingBoxDescent;
          }
          xAxisPaddingList.push(
            maxHeight + (xAxisNameGap ?? 0) + axisLabelMargin + height + 1
          );

          ctx.restore();
        }
      );
      chartInstance.setOption({
        xAxis: xAxisNameGapList.map((nameGap) => ({ nameGap })),
        grid: {
          bottom: xAxisPaddingList[0],
          top: 10,
        },
      });
    }

    if (notEmpty(yAxisList)) {
      yAxisList.forEach(
        ({ nameTextStyle, axisLabel, name, nameGap }: any, index: number) => {
          const nameFontSize = nameTextStyle?.fontSize ?? 12;
          const nameFontFamily = nameTextStyle?.fontFamily ?? "sans-serif";

          const axisLabelFontSize = axisLabel?.fontSize ?? 12;
          const axisLabelFontFamily = axisLabel?.fontFamily ?? "sans-serif";

          maxYAxisLabelFontSize = Math.max(
            maxYAxisLabelFontSize,
            axisLabelFontSize
          );

          ctx.save();
          ctx.font = `${axisLabelFontSize}px ${axisLabelFontFamily}`;
          const yAxis = globalModel.getComponent("yAxis", index)?.axis;

          const labelMaxWidth = Math.max(
            ...yAxis
              .getViewLabels()
              .map((item: any) => ctx.measureText(item.formattedLabel).width)
          );
          const axisLabelMargin = axisLabel?.margin ?? 8;

          if (name) {
            yAxisNameGapList.push(
              labelMaxWidth + (yAxisNameGaps[index] ?? 0) + axisLabelMargin
            );
          } else {
            yAxisNameGapList.push(2);
          }
          ctx.restore();

          ctx.save();
          ctx.font = `${nameFontSize}px ${nameFontFamily}`;
          let height = 0;

          if (isDefined(name) && name !== "") {
            const nameMetrics = ctx.measureText(name);
            height =
              nameMetrics.actualBoundingBoxAscent +
              nameMetrics.actualBoundingBoxDescent;
            yAxisPaddingList.push(
              labelMaxWidth +
                (yAxisNameGaps[index] ?? 0) +
                axisLabelMargin +
                height + 5
            );
          } else {
            yAxisPaddingList.push(2);
          }

          ctx.restore();
        }
      );
      chartInstance.setOption({
        yAxis: yAxisNameGapList.map((nameGap) => ({ nameGap })),
        grid: {
          left: yAxisPaddingList[0],
          right: yAxisPaddingList?.[1] ?? 20,
        },
      });
    }
  }
}
