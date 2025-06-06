import { DatasetColumn } from "Apps/Kepler/kepler";
import { Form, Skeleton, Typography } from "antd";
import React, { useCallback, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isEmpty } from "utils/utilities";
import {
  updateQuery,
  updateQueryError,
} from "../../../../redux/actions/keplerActions";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";
import { chartConfig } from "../charts.config";
import ColumnSelect from "./ColumnSelect";
import DimensionController from "./DimensionController";
import { LimitSortingController } from "./LimitController";
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
  const form: any = dataForm;

  if (
    isEmpty(query) ||
    isEmpty(columns) ||
    isEmpty(dataForm) ||
    !(query as any).hasOwnProperty("chartType")
  ) {
    return <Skeleton active />;
  }

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
            form.submit();
            console.log("FORM VALUES CHANGED", a, b);
            dispatch(
              updateQueryError({
                status: "VALIDATING",
                error: null,
              })
            );
            dispatch(updateQuery(b));
          }}
          onFinish={(values) => {
            console.log("FORM VALUES FINISHED");
            dispatch(
              updateQueryError({
                status: "FINISHED",
                error: null,
              })
            );
          }}
          onFinishFailed={(errorInf) => {
            console.log(
              "FORM VALUES FINISH ERROR",
              errorInf,
              errorInf.errorFields.length === 1 &&
                errorInf.errorFields[0].name[0] === "dimensions"
            );
            // fix added when dimensions are not set form returns validation error
            if (
              errorInf.errorFields.length === 1 &&
              errorInf.errorFields[0].name[0] === "dimensions"
            ) {
              dispatch(
                updateQueryError({
                  status: "FINISHED",
                  error: null,
                })
              );
            } else if (errorInf.errorFields.length > 0) {
              dispatch(
                updateQueryError({
                  status: "ERROR",
                  error: errorInf,
                })
              );
            } else {
              dispatch(
                updateQueryError({
                  status: "FINISHED",
                  error: null,
                })
              );
            }
          }}
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
              header={getLanguageLabel("groupBy")}
              Heading={querySkeleton.meta.dimensionHeading}
              form={form}
            />
          )}
          {!["parameterChart", "bigNumber"].includes(queryData.chartType) && (
            <>
              <LimitSortingController
                querySkeleton={querySkeleton}
                form={form}
                chartType={queryData.chartType}
              />
            </>
          )}
        </Form>
      </div>
    </>
  );
}

export { QueryForm };
