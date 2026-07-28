import { Table } from "antd";
import React, { useEffect, useState } from "react";
import {
  ObjectKeys,
  getLanguageLabel,
  isDefined,
  timeConverter,
} from "utils/utilities";
import { nFormatter } from "../../utils/NumberFormatter";
import { chartConfig } from "../charts.config";

interface KeplerChartDataTableProps {
  data: any;
  setChartTableData: any;
}

const KeplerChartDataTable = ({
  data,
  setChartTableData,
}: KeplerChartDataTableProps) => {
  const [columns, setColumns] = useState([]);
  const [dataArr, setDataArr] = useState([]);

  useEffect(() => {
    let DataObj: any = {};
    let Columns: any = [];
    let DataArr: any = [];
    setDataArr([]);
    setColumns([]);

    if (isDefined(data?.payload)) {
      const chartData = data?.payload;

      const chartType = chartData.request.chartType;

      const querySkeleton = chartConfig[chartType];

      if (
        chartType === "sunBurstChart" ||
        chartType === "treeMapChart" ||
        chartType === "treeChart"
      ) {
        ObjectKeys(chartData.data?.[0]).map((col) => {
          Columns.push({
            title: col,
            dataIndex: col,
            key: col,
          });
        });

        DataArr = chartData.data;
      } else if (
        chartType === "pieChart" ||
        chartType === "wordCloudChart" ||
        chartType === "funnelChart"
      ) {
        Columns.push({
          title: getLanguageLabel("name"),
          dataIndex: "name",
          key: "name",
        });
        Columns.push({
          title: getLanguageLabel("value"),
          dataIndex: "value",
          key: "value",
        });
        Object.keys(chartData.data).map((k: string) => {
          DataArr.push({ name: k, value: chartData.data[k][0] });
        });
      } else if (chartType === "table") {
        DataArr = data.payload.data?.tableData;
        data.payload.data?.columns?.map((col: any, index: number) => {
          Columns.push({
            title: col.name,
            dataIndex: col.name,
            key: col.name,
            render: (text: any, record: any, index: number) => {
              if (chartData.data.aggregateCols.includes(col.name) && col.max) {
                if (col.type === "double") {
                  text = text.toFixed(2);
                }
                if (col.type === "timestamp" || col.type === "date") {
                  text = timeConverter(text, false, false, "yyyy-MM-dd");
                }
                return (
                  <div className="ant-table-cell-metric table-chart__aggregated-cell">
                    <div
                      className="table-chart__aggregated-cell-formatter"
                      style={{
                        width: `${(1 - text / col.max) * 100}%`,
                      }}
                    ></div>
                    <div className="table-chart__aggregated-cell-text">
                      {nFormatter(text, 2, "auto", "K")}
                    </div>
                  </div>
                );
              }

              return (
                <div onClick={(e) => {}} className="ant-table-cell-custom">
                  {col.type === "timestamp"
                    ? timeConverter(text, false, false, "yyyy-MM-dd")
                    : String(text)}
                </div>
              );
            },
          });
        });
      } else if (chartType === "mapChart") {
        chartData.mapChartData?.length > 0 &&
          Object.keys(chartData.mapChartData[0]).map((k: any) => {
            Columns.push({
              title: k,
              dataIndex: k,
              key: k,
            });
          });

        DataArr = chartData.mapChartData;
      } else if (chartType === "heatmapChart") {
        // heatmap: xCategories, yCategories, data: [[xIdx, yIdx, value]]
        const xCats: string[] = chartData.data?.xCategories ?? [];
        const yCats: string[] = chartData.data?.yCategories ?? [];
        Columns.push({ title: "X", dataIndex: "x", key: "x" });
        Columns.push({ title: "Y", dataIndex: "y", key: "y" });
        Columns.push({ title: "Value", dataIndex: "value", key: "value" });
        (chartData.data?.data ?? []).forEach((row: [number, number, number]) => {
          DataArr.push({
            x: xCats[row[0]] ?? row[0],
            y: yCats[row[1]] ?? row[1],
            value: row[2],
          });
        });
      } else if (chartType === "sankeyChart") {
        // sankey: nodes, links
        Columns.push({ title: "Source", dataIndex: "source", key: "source" });
        Columns.push({ title: "Target", dataIndex: "target", key: "target" });
        Columns.push({ title: "Value", dataIndex: "value", key: "value" });
        (chartData.data?.links ?? []).forEach(
          (link: { source: string; target: string; value: number }) => {
            DataArr.push({ source: link.source, target: link.target, value: link.value });
          }
        );
      } else if (chartType === "graphChart") {
        // graph: nodes + links
        Columns.push({ title: "Source", dataIndex: "source", key: "source" });
        Columns.push({ title: "Target", dataIndex: "target", key: "target" });
        (chartData.data?.links ?? []).forEach(
          (link: { source: string; target: string }) => {
            DataArr.push({ source: link.source, target: link.target });
          }
        );
      } else if (chartType === "boxplotChart") {
        // boxplot: categories + [min, Q1, median, Q3, max]
        Columns.push({ title: "Category", dataIndex: "category", key: "category" });
        Columns.push({ title: "Min", dataIndex: "min", key: "min" });
        Columns.push({ title: "Q1", dataIndex: "q1", key: "q1" });
        Columns.push({ title: "Median", dataIndex: "median", key: "median" });
        Columns.push({ title: "Q3", dataIndex: "q3", key: "q3" });
        Columns.push({ title: "Max", dataIndex: "max", key: "max" });
        const cats: string[] = chartData.data?.categories ?? [];
        (chartData.data?.data ?? []).forEach((row: number[], idx: number) => {
          DataArr.push({
            category: cats[idx] ?? idx,
            min: row[0],
            q1: row[1],
            median: row[2],
            q3: row[3],
            max: row[4],
          });
        });
      } else if (chartType === "candlestickChart") {
        // candlestick: dates + [open, close, low, high]
        Columns.push({ title: "Date", dataIndex: "date", key: "date" });
        Columns.push({ title: "Open", dataIndex: "open", key: "open" });
        Columns.push({ title: "Close", dataIndex: "close", key: "close" });
        Columns.push({ title: "Low", dataIndex: "low", key: "low" });
        Columns.push({ title: "High", dataIndex: "high", key: "high" });
        const dates: string[] = chartData.data?.dates ?? [];
        (chartData.data?.data ?? []).forEach((row: number[], idx: number) => {
          DataArr.push({
            date: dates[idx] ?? idx,
            open: row[0],
            close: row[1],
            low: row[2],
            high: row[3],
          });
        });
      } else if (chartType === "parallelChart") {
        // parallel: axes + data rows
        const axes: string[] = chartData.data?.axes ?? [];
        axes.forEach((axisName: string) => {
          Columns.push({ title: axisName, dataIndex: axisName, key: axisName });
        });
        (chartData.data?.data ?? []).forEach((row: number[]) => {
          const rowObj: { [key: string]: number } = {};
          axes.forEach((axisName: string, i: number) => {
            rowObj[axisName] = row[i];
          });
          DataArr.push(rowObj);
        });
      } else if (chartType === "themeRiverChart") {
        // themeRiver: [[date, value, category]]
        Columns.push({ title: "Date", dataIndex: "date", key: "date" });
        Columns.push({ title: "Value", dataIndex: "value", key: "value" });
        Columns.push({ title: "Category", dataIndex: "category", key: "category" });
        (chartData.data?.data ?? []).forEach(
          (row: [string, number, string]) => {
            DataArr.push({ date: row[0], value: row[1], category: row[2] });
          }
        );
      } else if (chartType === "pictorialBarChart") {
        // pictorialBar: same format as VerticalAxisChart
        Columns.push({
          title: chartData.request.xaxis,
          dataIndex: chartData.request.xaxis,
          key: chartData.request.xaxis,
        });
        chartData.data.series?.forEach((s: any) => {
          s.seriesData &&
            Object.keys(s.seriesData).forEach((k: string) => {
              const col = s.groupBy?.length > 0 ? `${s.axisName} [${k}]` : k;
              Columns.push({ title: col, dataIndex: col, key: col });
            });
        });
        chartData.data.xAxisData?.forEach((x: string) => {
          DataObj[x] = { [chartData.request.xaxis]: x };
        });
        chartData.data.series?.forEach((s: any) => {
          s.seriesData &&
            Object.keys(s.seriesData).forEach((k: string) => {
              const col = s.groupBy?.length > 0 ? `${s.axisName} [${k}]` : k;
              s?.seriesData[k]?.forEach((dataPoint: any) => {
                if (dataPoint.length === 2) {
                  DataObj[dataPoint[0]] = {
                    ...DataObj[dataPoint[0]],
                    [col]: dataPoint[1],
                  };
                }
              });
            });
        });
        DataArr = Object.keys(DataObj).map((k) => DataObj[k]);
      } else if (chartType === "radarChart") {
        chartData.data.series.map((series: any) => {
          chartData.request.dimensions.map((d: string) => {
            Columns.push({
              title: d,
              dataIndex: d,
              key: d,
            });
          });
          Columns.push({
            title: ObjectKeys(series.seriesData)[0],
            dataIndex: ObjectKeys(series.seriesData)[0],
            key: ObjectKeys(series.seriesData)[0],
          });
        });

        chartData.data.series?.map((s: any) => {
          s.seriesData &&
            Object.keys(s.seriesData).map((k: string) => {
              ObjectKeys(s?.seriesData[k])?.map((dKey: any) => {
                let inObj: any = {};
                chartData.request.dimensions.forEach((d: string, i: number) => {
                  inObj[d] = dKey.split(",")[i];
                });
                DataObj[dKey] = {
                  ...DataObj[dKey],
                  ...inObj,
                  [k]: s?.seriesData[k][dKey][0][0],
                };
              });
            });
        });

        DataArr = Object.keys(DataObj).map((k) => DataObj[k]);
      } else if (querySkeleton.meta.showData) {
        Columns.push({
          title: chartData.request.xaxis,
          dataIndex: chartData.request.xaxis,
          key: chartData.request.xaxis,
        });

        chartData.data.series?.map((s: any) => {
          s.seriesData &&
            Object.keys(s.seriesData).map((k: string) => {
              const col = s.groupBy?.length > 0 ? `${s.axisName} [${k}]` : k;

              Columns.push({
                title: col,
                dataIndex: col,
                key: col,
              });
            });
        });

        chartData.data.xAxisData?.map((x: string, ind: number) => {
          DataObj[x] = { [chartData.request.xaxis]: x };
        });

        chartData.data.series?.map((s: any) => {
          s.seriesData &&
            Object.keys(s.seriesData).map((k: string) => {
              const col = s.groupBy?.length > 0 ? `${s.axisName} [${k}]` : k;

              s?.seriesData[k]?.map((dataArr: any) => {
                if (dataArr.length == 2) {
                  DataObj[dataArr[0]] = {
                    ...DataObj[dataArr[0]],
                    [col]: dataArr[1],
                  };
                }
              });
            });
        });

        DataArr = Object.keys(DataObj).map((k) => DataObj[k]);
      }
      setChartTableData(DataArr);

      setDataArr(DataArr);
      setColumns(Columns);
    }
  }, [data]);

  return (
    <div style={{ margin: "0.7rem 2rem", overflow: "scroll", height: "100%" }}>
      <Table
        className="excel-style-table"
        dataSource={dataArr}
        scroll={{ x: "100%", y: "100%" }}
        columns={columns}
        size={"small"}
      />
    </div>
  );
};

const withKeplerChartDataTable = (component: any) => {
  const MemoComponent = React.memo(component);

  return (props: KeplerChartDataTableProps) => {
    const data = React.useMemo(() => props.data, [props.data]);
    const setChartTableData = React.useMemo(
      () => props.setChartTableData,
      [props.data]
    );

    return <MemoComponent data={data} setChartTableData={setChartTableData} />;
  };
};

export default withKeplerChartDataTable(KeplerChartDataTable);
