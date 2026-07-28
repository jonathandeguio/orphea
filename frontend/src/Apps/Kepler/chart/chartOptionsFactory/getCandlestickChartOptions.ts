import { isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * Candlestick (OHLC) chart factory.
 *
 * Expected data format from backend:
 * chartData.data = {
 *   dates: string[],
 *   data: [[open, close, low, high], ...]   // ECharts candlestick order
 * }
 */
const getCandlestickChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getCandlestickChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const candleData = chartData.data;

  const dates: string[] = isDefined(candleData.dates) ? candleData.dates : [];
  const rawData: number[][] = isDefined(candleData.data) ? candleData.data : [];

  const bullishColor: string = chartCustomization.candlestickBullishColor ?? "#06b96b";
  const bearishColor: string = chartCustomization.candlestickBearishColor ?? "#ef4665";

  return {
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "axis",
      axisPointer: { type: "cross" },
    },
    grid: {
      top: "10%",
      left: "10%",
      right: "10%",
      bottom: "15%",
    },
    xAxis: {
      type: "category",
      data: dates,
      scale: true,
      boundaryGap: true,
      axisLine: { onZero: false },
      splitLine: { show: false },
      splitNumber: 20,
      min: "dataMin",
      max: "dataMax",
    },
    yAxis: {
      type: "value",
      scale: true,
      splitArea: { show: true },
    },
    dataZoom: [
      { type: "inside", start: 0, end: 100 },
      { show: true, type: "slider", bottom: "5%", start: 0, end: 100 },
    ],
    series: [
      {
        type: "candlestick",
        data: rawData,
        itemStyle: {
          color: bullishColor,
          color0: bearishColor,
          borderColor: bullishColor,
          borderColor0: bearishColor,
        },
        silent: !editMode,
      },
    ],
  };
};

export default getCandlestickChartOptions;
