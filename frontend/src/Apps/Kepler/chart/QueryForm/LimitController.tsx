import { DatasetColumn } from "Apps/Kepler/kepler";
import { Form, InputNumber, Select, Space, Typography } from "antd";
import { SortAscIcon, SortDescIcon } from "assets/icons/boslerSortIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React, { useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import {
  getLanguageLabel,
  isDefined
} from "utils/utilities";
import { getAggregateOptions } from "../charts.utils";
import ColumnSelect from "./ColumnSelect";

const { Text } = Typography;

export const LimitSortingController = ({
  form,
  chartType,
  querySkeleton,
}: {
  form: any;
  chartType: string;
  querySkeleton: any;
}) => {
  const seriesItems = useSelector(
    (state: RootState) => state.kepler.query.series
  );

  const [column, setColumn] = useState<DatasetColumn | undefined>(undefined);

  const aggregateOptions = useMemo(
    () => getAggregateOptions(column?.type, chartType),
    [column, chartType]
  );

  const sortingMethodOptions = useMemo(() => {
    let options = [
      ...seriesItems
        .filter((series: any) => isDefined(series.columnName))
        .map((series: any) => ({
          value: series.id,
          label: (
            <div className="flex">
              {series.seriesName}&nbsp;&nbsp;
              <div style={{ opacity: 0.6 }}>
                {series.aggregate}({series.columnName})
              </div>
            </div>
          ),
        })),
      {
        value: "custom",
        label: <div style={{ opacity: 0.6 }}>{getLanguageLabel("custom")}</div>,
      },
    ];
    if (querySkeleton.hasOwnProperty("xaxis")) {
      options.unshift({
        value: "xaxis",
        label: (
          <div className="flex">
            {getLanguageLabel("none")} &nbsp;&nbsp;
            <div style={{ opacity: 0.6 }}>{getLanguageLabel("xAxis")}</div>
          </div>
        ),
      });
    }

    return options;
  }, [querySkeleton, seriesItems]);

  return (
    <BoslerCollapse
      collapsible="HEADER"
      key="collapsibleLimitAndSorting"
      defaultCollpased={false}
      header={<div className="query_item__heading">{getLanguageLabel("LimitAndSorting")}</div>}
    >
      <>
        <Form.Item
          name={"sort"}
          style={{ marginLeft: "0.5rem" }}
          label={
            <div className="query_item__heading">
              <strong>{getLanguageLabel("sort")}</strong>
            </div>
          }
        >
          <Space.Compact style={{ width: "100%" }}>
            <Form.Item style={{ minWidth: "35%" }} name={["sortingDirection"]}>
              <Select
                options={[
                  {
                    value: "asc",
                    label: <><div className="text-and-icon-center"><SortAscIcon /> {getLanguageLabel("ascending")}</div></>,
                  },
                  { value: "desc", label: <><div className="text-and-icon-center"><SortDescIcon /> {getLanguageLabel("descending")}</div></>},
                ]}
              />
            </Form.Item>
            <Form.Item style={{ width: "100%" }} name={["sortingMethod"]}>
              <Select options={sortingMethodOptions} />
            </Form.Item>
          </Space.Compact>
        </Form.Item>
        {form.getFieldValue(["sortingMethod"]) == "custom" && (
          <>
            <ColumnSelect
              name={["customColumnName"]}
              listName={"series"}
              onSelect={setColumn}
              customStyle={{ paddingLeft: "0.5rem" }}
              rules={[
                {
                  required: true,
                  message: (
                    <>
                      <Text
                        type="secondary"
                        style={{
                          fontSize: "10px",
                          color: "var(--bosler-intent-danger)",
                        }}
                      >
                        Column Name is required!
                      </Text>
                    </>
                  ),
                },
              ]}
            />
            <Form.Item
              rules={[
                {
                  required: true,
                  message: (
                    <>
                      <Text
                        type="secondary"
                        style={{
                          fontSize: "10px",
                          color: "var(--bosler-intent-danger)",
                        }}
                      >
                        Aggregate func is required!
                      </Text>
                    </>
                  ),
                },
              ]}
              style={{ marginLeft: "0.5rem" }}
              name={["customAggregateFunction"]}
            >
              <Select
                placeholder={getLanguageLabel("aggregateFunction")}
                variant={"filled"}
                optionLabelProp={"title"}
                options={aggregateOptions}
                popupClassName={"aggregatePopup"}
              />
            </Form.Item>
          </>
        )}

        <Form.Item
          validateDebounce={800}
          style={{ marginLeft: "0.5rem" }}
          name={"rowLimit"}
          label={
            <div className="query_item__heading">
              <strong>{getLanguageLabel("limitRows")}</strong>
            </div>
          }
        >
          <InputNumber min={0} max={50000} style={{ width: "100%" }} />
        </Form.Item>
      </>
    </BoslerCollapse>
  );
};
