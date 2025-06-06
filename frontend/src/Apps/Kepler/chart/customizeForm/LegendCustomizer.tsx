import { Collapse, Form, Switch } from "antd";
import { UnorderedListIcon } from "assets/icons/boslerInterfaceIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React, { useCallback, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { getLanguageLabel } from "utils/utilities";
import { getChartIcon } from "../charts.utils";

const { Panel } = Collapse;

export const LegendCustomizer = ({ query, form }: any) => {
  const dispatch = useDispatch();
  const customizeForm = useSelector(
    (state: RootState) => state.kepler.customizeForm
  );
  const legendValue = Form.useWatch("legend", customizeForm);

  const chartIcon = useMemo(
    () => getChartIcon(query.chartType, query?.series?.[0]?.seriesType),
    [query]
  );

  const legendPosition = Form.useWatch("legendPosition", form);
  const legendAlign = Form.useWatch("legendAlign", form);

  const activeClass = useMemo(
    () =>
      legendPosition +
      legendAlign?.charAt(0).toUpperCase() +
      legendAlign?.slice(1),
    [legendPosition, legendAlign]
  );

  const clickHandler = useCallback(
    (position: string, align: string) => {
      form?.setFieldValue("legendPosition", position);
      form?.setFieldValue("legendAlign", align);
      dispatch({
        type: "UPDATE_CUSTOMIZE",
        payload: {
          legendPosition: position,
          legendAlign: align,
        },
      });
    },
    [form]
  );

  return (
    <BoslerCollapse
      key="legendPanel"
      collapsible={legendValue === true ? "HEADER" : "DISABLED"}
      header={
        <Form.Item
          name="legend"
          label={
            <div className="query_item__heading">
              {getLanguageLabel("legend")}
            </div>
          }
          valuePropName="checked"
        >
          <Switch size={"small"} />
        </Form.Item>
      }
    >
      <>
        <div className="legendSelector">
          <div className="verticalBlock">
            <div
              onClick={() => {
                clickHandler("left", "start");
              }}
              className={`${
                activeClass == "leftStart" ? "legendSelector-active" : ""
              } legendSelector-leftStart`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
            <div
              onClick={() => {
                clickHandler("left", "middle");
              }}
              className={`${
                activeClass == "leftMiddle" ? "legendSelector-active" : ""
              } legendSelector-leftMiddle`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
            <div
              onClick={() => {
                clickHandler("left", "end");
              }}
              className={`${
                activeClass == "leftEnd" ? "legendSelector-active" : ""
              } legendSelector-leftEnd`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
          </div>
          <div
            style={{
              height: "100%",
              width: "100%",
              flexDirection: "column",
              display: "flex",
            }}
          >
            <div className="horizontalBlock">
              <div
                onClick={() => {
                  clickHandler("top", "start");
                }}
                className={`${
                  activeClass == "topStart" ? "legendSelector-active" : ""
                } legendSelector-topStart`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
              <div
                onClick={() => {
                  clickHandler("top", "middle");
                }}
                className={`${
                  activeClass == "topMiddle" ? "legendSelector-active" : ""
                } legendSelector-topMiddle`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
              <div
                onClick={() => {
                  clickHandler("top", "end");
                }}
                className={`${
                  activeClass == "topEnd" ? "legendSelector-active" : ""
                } legendSelector-topEnd`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
            </div>
            <div className="middleBlock">
              <div>{chartIcon}</div>
              <div>
                <Form.Item name="legendPosition">
                  {/* <Radio.Group size="small">
                    <Radio.Button value="top">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <ArrowUpIcon />
                      </div>
                    </Radio.Button>
                    <Radio.Button value="right">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <ArrowRightIcon />
                      </div>
                    </Radio.Button>
                    <Radio.Button value="bottom">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <ArrowDownIcon />
                      </div>
                    </Radio.Button>
                    <Radio.Button value="left">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <ArrowLeftIcon />
                      </div>
                    </Radio.Button>
                  </Radio.Group> */}
                </Form.Item>
              </div>
              <div>
                <Form.Item name="legendAlign">
                  {/* <Radio.Group size="small">
                    <Radio.Button value="start">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <TopAlignIcon />
                      </div>
                    </Radio.Button>
                    <Radio.Button value="middle">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <MiddleAlignIcon />
                      </div>
                    </Radio.Button>
                    <Radio.Button value="end">
                      <div
                        className="flex"
                        style={{
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <BottomAlignIcon />
                      </div>
                    </Radio.Button>
                  </Radio.Group> */}
                </Form.Item>
              </div>
            </div>
            <div className="horizontalBlock">
              <div
                onClick={() => {
                  clickHandler("bottom", "start");
                }}
                className={`${
                  activeClass == "bottomStart" ? "legendSelector-active" : ""
                } legendSelector-bottomStart`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
              <div
                onClick={() => {
                  clickHandler("bottom", "middle");
                }}
                className={`${
                  activeClass == "bottomMiddle" ? "legendSelector-active" : ""
                } legendSelector-bottomMiddle`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
              <div
                onClick={() => {
                  clickHandler("bottom", "end");
                }}
                className={`${
                  activeClass == "bottomEnd" ? "legendSelector-active" : ""
                } legendSelector-bottomEnd`}
              >
                <div className="displayingLegend">
                  <UnorderedListIcon />
                </div>
              </div>
            </div>
          </div>
          <div className="verticalBlock">
            <div
              onClick={() => {
                clickHandler("right", "start");
              }}
              className={`${
                activeClass == "rightStart" ? "legendSelector-active" : ""
              } legendSelector-rightStart`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
            <div
              onClick={() => {
                clickHandler("right", "middle");
              }}
              className={`${
                activeClass == "rightMiddle" ? "legendSelector-active" : ""
              } legendSelector-rightMiddle`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
            <div
              onClick={() => {
                clickHandler("right", "end");
              }}
              className={`${
                activeClass == "rightEnd" ? "legendSelector-active" : ""
              } legendSelector-rightEnd`}
            >
              <div className="displayingLegend">
                <UnorderedListIcon />
              </div>
            </div>
          </div>
        </div>
      </>
    </BoslerCollapse>
  );
};
