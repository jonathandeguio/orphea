export interface TooltipInfo {
  seriesIndex?: number;
  dataIndex?: number;
  name?: string;
  x?: number;
  y?: number;
  position?: number[] | string | Function;
  id: string;
}

export interface ChartState {
  loading?: boolean;
  chart?: KeplerChart;
  error?: any;
  status?: boolean | undefined;
}

export interface DatasetColumn {
  headerName: string;
  field: string;
  type: string;
}

export interface KeplerSeries {
  id: string;
  seriesId?: string;
  seriesName: string;
  columnName?: string;
  aggregate?: string;
  groupBy: string[];
  sort: string;
  seriesType: string;
  seriesIndex: string;
  reversed: boolean;
  seriesCustomize: string;
}

export interface KeplerFilter {
  id?: string;
  datasetId?: string;
  columnName: string;
  operator: string;
  FilterValue: string;
  checked: boolean;
  columnType?: string;
}

export interface KeplerQuery {
  id?: string;
  chartUUID: string;
  datasetId: string;
  branch: string;
  transactionId: string;
  chartType: string;

  xaxis?: string;
  xaxisSort?: string;
  xAxisTimeGrain: string;
  longitude: string;
  latitude: string;
  mapZoom: string;
  mapCenter: string;

  mapSeries?: string;
  series: KeplerSeries[];
  dimensions?: string[];
  filter: KeplerFilter[];
  fetchCachedData: boolean;
  saveInCache: boolean;
  userLocale?: string;
}

export interface KeplerCustomize {
  id: string;
  colorTheme: string;
  subHeader: string;
  bigNumberFontSize: string;
  subHeaderFontSize: string;
  scatterColor: string;
  suffix: string;
  prefix: string;
  xAxis: string;
  xAxisTitlePosition: string;
  xAxisTitleMargin: string;
  xAxisSplitLine: string;
  showSum: boolean;
  percentagePrecision: number;
  percentageFontSize: number;
  mapChartTileLayer: string;
  yAxisLeft: string;
  yAxisLeftTitlePosition: string;
  yAxisLeftTitleMargin: string;
  yAxisSplitLine: string;
  yAxisRight: string;
  yAxisRightTitlePosition: string;
  yAxisRightTitleMargin: string;
  dataZoom: boolean;
  sortBars: boolean;
  legend: boolean;
  tooltip: boolean;
  tooltipAxisPointer: boolean;
  tooltipAxisTrigger: string;
  legendType: string;
  donut: boolean;
  innerRadius: string;
  nightangle: boolean;
  outerRadius: string;
  gridMarginTop: string;
  gridMarginRight: string;
  gridMarginBottom: string;
  gridMarginLeft: string;
  legendPosition: string;
  lineChartStyle: string;
  colorScheme: string;
  stackedBars: boolean;
  showLabel: boolean;
  reversed: boolean;
  seriesCustomize: string;
  bigNumberColor: string;
  bigNumberSubheaderColor: string;
  bigNumberTop: string;
  subheaderTop: string;
}

export interface KeplerChart {
  id?: string;
  parent?: string;
  name?: string;
  description?: string;
  datasetId?: string;
  branch?: string;
  query?: KeplerQuery;
  customize?: KeplerCustomize;
  dashboard?: any;
  tabsForCharts?: any;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface KeplerStore {
  isChartSaved: boolean;
  chart: any;
  query?: KeplerQuery;
  customize: any;
  data: any;
  columns: DatasetColumn[];
  dataForm: any;
  customizeForm: any;
}

export interface DatasetFilters {
  name: string;
  type: string;
  expression: string;
  value: string | number;
}

export interface DatasetStatsColumns {
  stats: {
    displayColumns: number;
    displayRows: number;
  };
  columns: Array<DatasetStatsColumns>;
}

interface series {
  columnName: string;
  aggregate: string;
  sort: string;
  groupBy: Array<string>;
}

interface ChartFilter {
  columnName: string;
  operator: string;
  filterValue: string;
}

interface ColorTheme {
  name: string;
  color: string[];
}

interface KeplerChartResponse {
  rows: number;
  trimmedData: boolean;
  cachedData: boolean;
  data: any;
  stats: any;
  request: KeplerQuery;
}
