import { Collapse, Divider, Form, InputNumber, Radio, Select } from "antd";
import {
  BottomAlignIcon,
  MiddleAlignIcon,
  TopAlignIcon,
} from "assets/icons/boslerActionIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { KeplerConfig } from "../charts.config";
import { FontCustomizer } from "./FontCustomizer";
import NumberCustomizer from "./NumberCustomizer";

const { Panel } = Collapse;

export const LeftYAxisCustomizer = () => {
  return (
    <BoslerCollapse
      header={
        <div className="query_item__heading">{getLanguageLabel("yAxis")}</div>
      }
      key="leftYAxisPanel"
      collapsible="HEADER"
    >
      <div className="radioButtonPadding">
        {/* SHOW X AND Y AXIS CONTROL FOR OLY LINECHART, BARTCHART, LINEAREACHART */}
        {
          <>
            <Form.Item name="yaxisLeft" label={getLanguageLabel("title")}>
              <BoslerInput
                placeholder={getLanguageLabel("title")}
                maxLength={KeplerConfig.chartLabelMaxLength}
                showCount={{
                  formatter: (args) => (
                    <>{KeplerConfig.chartLabelMaxLength - args.value.length}</>
                  ),
                }}
              />
            </Form.Item>
            <FontCustomizer
              label={`${getLanguageLabel("font")}`}
              name="leftYAxisLabel"
            />

            <Form.Item
              name="yaxisLeftTitleMargin"
              label={getLanguageLabel("margin")}
            >
              <InputNumber
                placeholder={getLanguageLabel("margin")}
                style={{ width: "100%" }}
              />
            </Form.Item>
            <Form.Item
              name="yaxisLeftTitlePosition"
              label={getLanguageLabel("position")}
              style={{ width: "100%" }}
            >
              {/* <Select options={verticalAlignOptionsList} /> */}
              <Radio.Group size="small">
                <Radio.Button value="start">
                  <div
                    className="flex"
                    style={{ alignItems: "center", justifyContent: "center" }}
                  >
                    <TopAlignIcon />
                  </div>
                </Radio.Button>
                <Radio.Button value="middle">
                  <div
                    className="flex"
                    style={{ alignItems: "center", justifyContent: "center" }}
                  >
                    <MiddleAlignIcon />
                  </div>
                </Radio.Button>
                <Radio.Button value="end">
                  <div
                    className="flex"
                    style={{ alignItems: "center", justifyContent: "center" }}
                  >
                    <BottomAlignIcon />
                  </div>
                </Radio.Button>
              </Radio.Group>
            </Form.Item>
            <Divider />

            <FontCustomizer
              name="leftYAxis"
              label={getLanguageLabel("tickFont")}
            />

            <NumberCustomizer name={"leftYAxis"} />
            <Form.Item
              name="yaxisSplitLine"
              label={getLanguageLabel("splitLine")}
            >
              <Select
                options={[
                  {
                    label: getLanguageLabel("none"),
                    value: "none",
                  },
                  {
                    label: getLanguageLabel("solid"),
                    value: "solid",
                  },
                  {
                    label: getLanguageLabel("dashed"),
                    value: "dashed",
                  },
                  {
                    label: getLanguageLabel("dotted"),
                    value: "dotted",
                  },
                ]}
              />
            </Form.Item>
          </>
        }
      </div>
    </BoslerCollapse>
  );
};
