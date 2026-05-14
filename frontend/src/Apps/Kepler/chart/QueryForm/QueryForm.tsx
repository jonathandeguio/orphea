import { DatasetColumn } from "Apps/Kepler/kepler";
import { Form, Skeleton, Typography } from "antd";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isEmpty } from "utils/utilities";
import {
  fetchDataTrigger,
  updateQuery,
} from "../../../../redux/actions/keplerActions";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";
import { chartConfig } from "../charts.config";
import ColumnSelect from "./ColumnSelect";
import DimensionController from "./DimensionController";
import { LimitController } from "./LimitController";
import MapSeriesController from "./MapSeriesController";
import { ParameterController } from "./ParameterController";
import MetricController from "./QueryItemController";
import XAxisController from "./XAxisController";
const { Text } = Typography;

function QueryForm() {
  const query = useSelector((state: RootState) => state.kepler.query);
  const columns = useSelector((state: RootState) => state.kepler.columns);
  const dataForm = useSelector((state: RootState) => state.kepler.dataForm);
  const chart = useSelector((state: RootState) => state.kepler.chart);
  const allDatasetMapping = useSelector(
    (state: RootState) => state.datasetMapping
  );
  const dispatch = useDispatch<ThunkAppDispatch>();

  if (
    isEmpty(query) ||
    isEmpty(columns) ||
    isEmpty(dataForm) ||
    !(query as any).hasOwnProperty("chartType")
  ) {
    return <Skeleton active />;
  }
  const form = dataForm;

  const queryData: any = query;

  const querySkeleton = chartConfig[queryData.chartType];

  if (isEmpty(columns) || !allDatasetMapping[chart?.datasetId]) {
    return <Skeleton active />;
  }

  return (
    <>
      <div
        style={{
          padding: "0 0.7rem 0 0.7rem",
        }}
      >
        <Form
          layout={"vertical"}
          form={form}
          initialValues={queryData}
          onValuesChange={(a, b) => {
            dispatch(updateQuery(b));
            dispatch(fetchDataTrigger());
          }}
          onFinish={(values) => {}}
        >
          {querySkeleton.hasOwnProperty("xaxis") && (
            <div className="query_item">
              <div className="query_item__heading">
                {getLanguageLabel(querySkeleton.meta.xAxisHeading as any)}
              </div>

              <div className="query_item__body">
                <XAxisController
                  chartType={queryData.chartType}
                  form={form}
                  Columns={columns}
                  Type={"xaxis"}
                />
              </div>
            </div>
          )}
          {querySkeleton.hasOwnProperty("parameters") && (
            <div className="query_item">
              <div className="query_item__heading">
                {getLanguageLabel("parametersChart")}
              </div>

              <div className="query_item__body">
                <ParameterController />
              </div>
            </div>
          )}
          {querySkeleton.hasOwnProperty("longitude") && (
            <div className="query_item">
              <div className="query_item__heading">
                {getLanguageLabel("longitude")}
              </div>

              <div className="query_item__body">
                <ColumnSelect
                  name={"longitude"}
                  filter={(column: DatasetColumn) => column.type === "double"}
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
                            Longitude is required!
                          </Text>
                        </>
                      ),
                    },
                  ]}
                />
              </div>
            </div>
          )}
          {querySkeleton.hasOwnProperty("latitude") && (
            <div className="query_item">
              <div className="query_item__heading">
                {getLanguageLabel("latitude")}
              </div>

              <div className="query_item__body">
                <ColumnSelect
                  name={"latitude"}
                  filter={(column: DatasetColumn) => column.type === "double"}
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
                            Latitude is required!
                          </Text>
                        </>
                      ),
                    },
                  ]}
                />
              </div>
            </div>
          )}
          {/* MAP SERIES CONTROLLER */}
          {querySkeleton.hasOwnProperty("mapSeries") && (
            <Form.List name="mapSeries">
              {(fields, { add, remove }) => {
                return (
                  <div className="query_item">
                    <div className="query_item__heading">
                      {getLanguageLabel("layer")}
                    </div>

                    <div className="query_item__body">
                      <MapSeriesController
                        Type={"mapSeries"}
                        fields={fields}
                        columns={columns}
                        form={form}
                        add={add}
                        remove={remove}
                      />
                    </div>
                  </div>
                );
              }}
            </Form.List>
          )}
          {/* SERIES CONTROLLER */}
          {querySkeleton.hasOwnProperty("series") && (
            <MetricController chartType={queryData.chartType} form={form} />
          )}
          {querySkeleton.hasOwnProperty("dimensions") && (
            <DimensionController
              Heading={querySkeleton.meta.dimensionHeading}
              form={form}
            />
          )}
          {!["parameterChart", "bigNumber"].includes(queryData.chartType) && (
            <>
              <LimitController form={form} />
            </>
          )}
        </Form>
      </div>
    </>
  );
}

export { QueryForm };
