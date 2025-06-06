import { Form, InputNumber, Space } from "antd";
import {
  GaugeIcon,
  GroupedColumnIcon,
  LineChartIcon,
  MapIcon,
  PieChartIcon,
  RadarIcon,
  ScatterIcon,
  SmallAreaChartIcon,
  StackedGroupedBarIcon,
  SunburstIcon,
  TreeMapIcon,
  WaterFallIcon,
  WordCloudIcon,
} from "assets/icons/boslerChartIcons";
import { BigNumberIcon } from "assets/icons/boslerDataIcons";
import { HelpIcon } from "assets/icons/boslerMiscellaneousIcons";
import { TableCellIcon } from "assets/icons/boslerTableIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React, { useMemo } from "react";
import { getLanguageLabel } from "utils/utilities";
import { getChartIcon } from "../charts.utils";

interface IGridCustomizer {
  query: any;
  type: "INNER" | "OUTER";
}

export const GridCustomizer: React.FC<IGridCustomizer> = ({ query, type }) => {
  const chartIcon = useMemo(
    () => getChartIcon(query.chartType, query?.series?.[0]?.seriesType),
    [query]
  );

  return (
    <BoslerCollapse
      key="customizerCollapse"
      header={
        <div className="query_item__heading">
          {type == "OUTER"
            ? getLanguageLabel("canvas")
            : getLanguageLabel("innerPadding")}
        </div>
      }
      collapsible="HEADER"
    >
      <div className="gridCustomizer">
        <div style={{ display: "flex", justifyContent: "center" }}>
          <Space.Compact direction="vertical">
            {type == "OUTER" ? (
              <Form.Item name={"gridMarginTop"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Top"
                />
              </Form.Item>
            ) : (
              <Form.Item name={"gridMarginTopInner"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Top"
                />
              </Form.Item>
            )}
          </Space.Compact>
        </div>
        <div
          style={{
            display: "flex",
            padding: "0.7rem 0",
            justifyContent: "space-between",
          }}
        >
          <Space.Compact direction="horizontal">
            {type == "OUTER" ? (
              <Form.Item name={"gridMarginLeft"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Left"
                />
              </Form.Item>
            ) : (
              <Form.Item name={"gridMarginLeftInner"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Left"
                />
              </Form.Item>
            )}
          </Space.Compact>
          {chartIcon}
          <Space.Compact direction="horizontal">
            {type == "INNER" ? (
              <Form.Item name={"gridMarginRightInner"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Right"
                />
              </Form.Item>
            ) : (
              <Form.Item name={"gridMarginRight"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Right"
                />
              </Form.Item>
            )}
          </Space.Compact>
        </div>
        <div style={{ display: "flex", justifyContent: "center" }}>
          <Space.Compact direction="vertical">
            {type == "INNER" ? (
              <Form.Item name={"gridMarginBottomInner"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Bottom"
                />
              </Form.Item>
            ) : (
              <Form.Item name={"gridMarginBottom"}>
                <InputNumber
                  min={0}
                  max={50}
                  formatter={(value) => `${value}%`}
                  placeholder="Bottom"
                />
              </Form.Item>
            )}
          </Space.Compact>
        </div>
      </div>
    </BoslerCollapse>
  );
};
