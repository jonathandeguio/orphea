import { ColumnSelectLabel } from "Apps/Kepler/chart/QueryForm/ColumnSelect";
import { getDatasetColumns } from "Apps/Kepler/dashboard/Dashboard.api";
import { getResourceApi } from "Apps/explorer/explorer.api";
import { Col, Row, Select, Switch, Tag, Tooltip } from "antd";
import { RemoveIcon, SyncIcon } from "assets/icons/boslerActionIcons";
import { DataCellsIcon } from "assets/icons/boslerDataIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton, {
  TBoslerButtonIntent,
} from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { TDatasetColumn } from "components/Filters/Filters.view";
import { getDatamartConfig } from "pages/Settings/apis";
import React, { useEffect, useState } from "react";
import { NULL_UUID } from "utils/Common.constants";
import { isDefined } from "utils/utilities";
import { DatamartConstants } from "./Datamart.constants";
import {
  getDatamartSyncAPI,
  performDatasetSyncAPI,
  putSyncAPI,
} from "./Sync.apis";
import styles from "./Sync.module.scss";
import { TDatamartSync } from "./Sync.types";

interface IDatamartSync {
  datasetId: string;
  branch: string;
}

export const DatamartSync = ({ datasetId, branch }: IDatamartSync) => {
  const [fileName, setFileName] = useState("");
  const [realFileName, setRealFileName] = useState("");
  const [columns, setColumns] = useState([]);
  const [syncId, setSyncId] = useState("");
  const [datamartConfig, setDatamartConfig] =
    useState<TDatamartSync>(DatamartConstants);
  const [sourceId, setSourceId] = useState("");
  const [backEndColumns, setBackendColumns] = useState([]);
  const [collapse, setCollapse] = useState(false);
  const [intent, setIntent] = useState<TBoslerButtonIntent>("primary");
  const [syncIntent, setSyncIntent] = useState<TBoslerButtonIntent>("none");

  const getColumns = async () => {
    const data = await getDatasetColumns(datasetId, branch, NULL_UUID);
    const _columns: any = [];
    data.map((column: any) => {
      const columnObj = {
        name: column.headerName,
        value: column.headerName,
        type: column.type,
      };
      _columns.push(columnObj);
    });
    setColumns(_columns);
  };

  const getDataMart = () => {
    // setLoading(true);
    getDatamartSyncAPI(datasetId)
      .then(({ data }) => {
        setDatamartConfig(data);
        setBackendColumns(data.syncIndexes[0].columns);
      })
      .catch(() => {})
      .finally(() => {
        // setLoading(false);
      });
  };

  const handleOnClick = () => {
    let payload = {
      key: "datamart",
      tableName: fileName,
      datasetId: datasetId,
      branch: branch,
      sourceId: sourceId,
      autoSyncOnBuild: true,
      syncIndexes: [backEndColumns],
    };
    if (syncId != "") {
      payload = {
        ...payload,
        //      id: syncId,
      };
    }
    putSyncAPI(payload)
      .then(({ data }) => {
        setIntent("success");
        setSyncId(data.id);
      })
      .catch((error) => {
        setIntent("dangerous");
      })
      .finally(() => {});
  };

  useEffect(() => {
    getDatamartConfig().then((data: any) => {
      setSourceId(data.dataSourceConfigId);
    });
  }, []);

  useEffect(() => {
    getResourceApi(datasetId)
      .then((data) => {
        setRealFileName(data.data.name);
      })
      .catch((error) => {});
    if (isDefined(datasetId)) {
      getDataMart();
      getColumns();
    }
  }, [datasetId]);

  return (
    <div className={styles.requestContainer}>
      <BoslerCollapse
        key={""}
        collapsible={collapse ? "ICON" : "DISABLED"}
        defaultCollpased={false}
        header={
          <>
            <div className="--flex-space-between">
              <div style={{ display: "flex" }}>
                <div className="--flex-align-center">
                  <div className="--flex-align-center">
                    <DataCellsIcon />
                    <span style={{ marginRight: "5px" }}>Datamart Enabled</span>
                  </div>
                </div>
              </div>
              <div>
                <Switch
                  size="small"
                  // defaultChecked={true}
                  onChange={(val: boolean) => {
                    setCollapse(val);
                  }}
                />
              </div>
            </div>
          </>
        }
      >
        <>
          <Row justify={"space-between"} align={"middle"}>
            <Col>
              Source : <Tag color="green">Datamart</Tag>
            </Col>
            <Col span={8}>
              <div>
                <Tooltip title={"Datamart Table Name"}>
                  <BoslerInput
                    value={datamartConfig?.tableName}
                    onChange={(e) => {
                      setFileName(e.target.value);
                    }}
                  />
                </Tooltip>
              </div>
            </Col>
          </Row>
          <br />
          <Row justify={"space-between"}>
            <Col span={8}>
              <div className={styles.indexRow}>
                <Select
                  mode="multiple"
                  value={backEndColumns}
                  className="--width100"
                  placeholder="Select column for Index"
                  onChange={(option) => {
                    setBackendColumns(option);
                  }}
                  optionLabelProp="label"
                  options={columns}
                >
                  {columns.map((option: TDatasetColumn) => (
                    <Select.Option value={option.value}>
                      <ColumnSelectLabel
                        name={option.name}
                        type={option.type}
                        datasetId={option.datasetId}
                      />
                    </Select.Option>
                  ))}
                </Select>
                <BoslerButton
                  intent="dangerous"
                  icon={<RemoveIcon />}
                  icononly
                  minimal
                >
                  DELETE
                </BoslerButton>
              </div>
            </Col>
            <Col span={8}>
              <div className="--flex-space-between">
                <BoslerButton
                  intent={syncIntent}
                  onClick={() => {
                    performDatasetSyncAPI(
                      datamartConfig.datasetId,
                      datamartConfig.syncId
                    )
                      .then(() => {
                        setSyncIntent("success");
                      })
                      .catch(() => {
                        setSyncIntent("dangerous");
                      })
                      .finally(() => {
                        // setLoading(false);
                      });
                  }}
                >
                  Sync
                </BoslerButton>
                <BoslerButton onClick={handleOnClick} intent={intent}>
                  <SyncIcon color="white" />
                  Update and Create Sync
                </BoslerButton>
              </div>
            </Col>
          </Row>
        </>
      </BoslerCollapse>
    </div>
  );
};
