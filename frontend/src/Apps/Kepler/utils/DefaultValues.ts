import { KeplerSeries } from "../kepler";

export const defaultCustomize: any = {
  // Common
  colorTheme: "theme1",
  customLabel: {},
  customTheme: [],
  chartTitleFontColor: "#00000000",
  subChartTitleFontColor: "#00000000",
  titleAlign: "left",
  subTitleAlign: "left",
  chartSubTitleCollapse: false,
  chartTitleCollapse: false,
  thresholdOperator: "gte",

  // Label
  showLabel: true,
  labelPosition: "inside",
  labelRotate: 0,
  labelFontSize: 28,
  labelFontWeight: 400,

  labelPrecision: 2,
  labelMode: "auto",
  labelScale: "K",

  // legend
  legend: false,
  legendType: "scroll",
  legendPosition: "top",
  legendAlign: "middle",

  // tooltip
  tooltip: true,
  tooltipMode: "auto",
  tooltipScale: "K",
  tooltipPrecision: 2,
  tooltipAxisTrigger: "axis",
  tooltipAxisPointer: true,

  // Grid
  gridMarginTop: "2",
  gridMarginRight: "1",
  gridMarginBottom: "1",
  gridMarginLeft: "1",

  // pieChart
  donut: true,
  sliceLabel: false,
  sliceLabelFontSize: 24,
  sliceLabelFontWeight: 400,
  nightangle: false,
  innerRadius: 40,
  radius: 70,
  sumFontSize: 90,
  sumFontWeight: 400,
  showSumMode: "auto",
  showSumScale: "K",
  showSumPrecision: 2,
  showSum: false,
  labelConfig: ["label"],
  pieLabelPercentagePrecision: 2,
  sliceLabelConfig: ["percentage"],
  sliceLabelPercentagePrecision: 2,

  sliceLabelMode: "auto",
  sliceLabelScale: "K",
  sliceLabelPrecision: 2,

  // AxisChart
  dataZoom: false,
  reversed: false,

  xaxis: "",
  xaxisSplitLine: "solid",
  xaxisTitlePosition: "middle",
  xaxisTitleMargin: 10,
  xAxisFontSize: 24,
  xAxisFontWeight: 400,
  xAxisLabelFontSize: 24,
  xAxisLabelFontWeight: 400,
  xAxisPrecision: 2,
  xAxisMode: "none",
  xAxisScale: "K",

  yaxisLeft: "",
  yaxisSplitLine: "solid",
  yaxisLeftTitlePosition: "middle",
  yaxisLeftTitleMargin: 10,
  leftYAxisFontSize: 24,
  leftYAxisFontWeight: 400,
  leftYAxisLabelFontSize: 24,
  leftYAxisLabelFontWeight: 400,
  leftYAxisPrecision: 2,
  leftYAxisMode: "none",
  leftYAxisScale: "K",

  yAxisRight: "",
  yAxisRightTitlePosition: "middle",
  yAxisRightTitleMargin: 10,
  rightYAxisFontSize: 14,
  rightYAxisFontWeight: 400,
  rightYAxislabelFontSize: 24,
  rightYAxislabelFontWeight: 400,
  rightYAxisPrecision: 2,
  rightYAxisMode: "none",
  rightYAxisScale: "K",

  // bigNumber
  horizontalAlignment: "left",
  subHeaderHorizontalAlignment: "left",
  verticalAlignment: "center",
  subheaderTop: "55",
  bigNumberTop: "50",

  bigNumberFontPrefix: "",
  bigNumberFontSuffix: "",
  bigNumberFontSize: 100,
  bigNumberFontWeight: 800,
  bigNumberFontColor: "#00000000",

  subHeaderFontPrefix: "",
  subHeaderFontSuffix: "",
  subHeaderFontSize: 50,
  subHeaderFontWeight: 400,
  subHeaderFontColor: "#00000000",

  bigNumberPrecision: 2,
  bigNumberMode: "auto",
  bigNumberScale: "K",
  subHeader: "",

  // Table
  summary: [],
  headerAlignment: "left",
  tablePrecision: 2,
  tableMode: "auto",
  tableScale: "K",
  tableHeaderFontSize: 14,
  tableBodyFontSize: 14,
  tableHeaderFontWeight: 800,
  tableBodyFontWeight: 400,
  tableHeaderFontColor: "#00000000",
  tableBodyFontColor: "#00000000",

  // mapChart
  scatterColor: "#0000ff",
  mapChartTileLayer: "",

  // gaugeChart
  axisLineWidth: 10,
  gaugeTitleFontSize: 36,
  gaugeTitleFontWeight: 400,
  gaugePrecision: 2,
  gaugeMode: "auto",
  gaugeScale: "K",

  // Parameter Chart
  parameterLabelPosition: "left",
  allowMultiselect: false,
  parameterFormSize: "middle",
  layout: "horizontal",

  //wordCloud
  wordCloudShape: "circle",
  wordCloudMinFontSize: 12,
  wordCloudMaxFontSize: 50,

  chartTitleFontSize: 24,
  chartTitleFontWeight: 400,

  subChartTitleFontSize: 20,
  subChartTitleFontWeight: 400,

  gridMarginTopInner: "5",
  gridMarginRightInner: "7",
  gridMarginBottomInner: "11",
  gridMarginLeftInner: "7",
};

export const defaultSeriesCustomize = {
  symbol: "circle",
  symbolSize: 10,
  lineChartStyle: "linear",
  stackedBars: true,
  stackedLine: false,
  showLabel: false,
  labelPosition: "top",
  labelRotate: 0,
  labelFontSize: 24,
  labelFontWeight: 400,
  labelPrecision: 2,
  labelMode: "auto",
  labelScale: "K",
};

export const defaultThresholdCustomize = {
  showThresholdLabel: true,
  color: "#ff0000",
  thresholdLabelPosition: "middle",
  thresholdType: "average",
  lineWidth: 1,
  thresholdLabelDistance: 10,
  thresholdFontPrefix: "",
  thresholdFontSuffix: "",
  thresholdFontSize: 24,
  thresholdFontWeight: 400,
};

export const defaultSeries: KeplerSeries = {
  id: "undefined_default",
  columnName: undefined,
  aggregate: undefined,
  groupBy: [],
  sort: "asc",
  seriesIndex: "right",
  seriesType: "barChart",
  reversed: false,
  seriesName: "Series",
  seriesCustomize: "",
};
