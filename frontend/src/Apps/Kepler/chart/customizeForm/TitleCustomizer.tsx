import { Form, Radio, Switch } from "antd";
import {
  CenterAlignIcon,
  LeftAlignIcon,
  RightAlignIcon,
} from "assets/icons/boslerFileIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { getLanguageLabel } from "utils/utilities";
import { FontCustomizer } from "./FontCustomizer";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";

interface ITitleCustomizer {
  skeleton: any;
}

export const TitleCustomizer: React.FC<ITitleCustomizer> = ({ skeleton }) => {
  const defaultCustomize = useSelector(
    (state: RootState) => state.kepler.customize
  );
  return (
    <div className="titleCustomizer radioButtonPadding">
      <BoslerCollapse
        collapsible={defaultCustomize.titleToggle ? "HEADER" : "DISABLED"}
        key="chartTitleCollapse"
        header={
          <Form.Item
            label={
              <div className="query_item__heading">
                {getLanguageLabel("title")}
              </div>
            }
            name="titleToggle"
          >
            <Switch size="small" />
          </Form.Item>
        }
      >
        <>
          <Form.Item label={getLanguageLabel("title")} name="title">
            <BoslerInput
              maxLength={250}
              showCount={{
                formatter: (args) => <>{250 - args.value.length}</>,
              }}
            />
          </Form.Item>

          <FontCustomizer name="chartTitle" />

          <Form.Item label={getLanguageLabel("align")} name="titleAlign">
            <Radio.Group size="small">
              <Radio.Button value="left">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <LeftAlignIcon />
                </div>
              </Radio.Button>
              <Radio.Button value="center">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <CenterAlignIcon />
                </div>
              </Radio.Button>
              <Radio.Button value="right">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <RightAlignIcon />
                </div>
              </Radio.Button>
            </Radio.Group>
          </Form.Item>
        </>
      </BoslerCollapse>
      <BoslerCollapse
        collapsible={defaultCustomize.subTitleToggle ? "HEADER" : "DISABLED"}
        key="chartSubTitleCollapse"
        header={
          <Form.Item
            label={
              <div className="query_item__heading">
                {getLanguageLabel("subtitle")}
              </div>
            }
            name="subTitleToggle"
          >
            <Switch size="small" />
          </Form.Item>
        }
      >
        <>
          <Form.Item label={getLanguageLabel("subtitle")} name="subTitle">
            <BoslerInput
              maxLength={250}
              showCount={{
                formatter: (args) => <>{250 - args.value.length}</>,
              }}
            />
          </Form.Item>

          <FontCustomizer name="subChartTitle" />

          <Form.Item label={getLanguageLabel("align")} name="subTitleAlign">
            <Radio.Group size="small">
              <Radio.Button value="left">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <LeftAlignIcon />
                </div>
              </Radio.Button>
              <Radio.Button value="center">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <CenterAlignIcon />
                </div>
              </Radio.Button>
              <Radio.Button value="right">
                <div
                  className="flex"
                  style={{ alignItems: "center", justifyContent: "center" }}
                >
                  <RightAlignIcon />
                </div>
              </Radio.Button>
            </Radio.Group>
          </Form.Item>
        </>
      </BoslerCollapse>
    </div>
  );
};
