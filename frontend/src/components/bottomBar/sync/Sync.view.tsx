import { getSourceDetailsAPI } from "Apps/Connect/Connect.api";
import { initialLinkDetails } from "Apps/Connect/Links/Link.constants";
import { SourceButtonPopover } from "Apps/Connect/Links/SourceButtonPopover";
import { ColumnSelectLabel } from "Apps/Kepler/chart/QueryForm/ColumnSelect";
import { getDatasetColumns } from "Apps/Kepler/dashboard/Dashboard.api";
import { Checkbox, Col, Form, Row, Select, Tooltip } from "antd";
import { useForm } from "antd/es/form/Form";
import { WarningState } from "assets/Illustrations/EmptyState";
import { AddIcon, RemoveIcon, SyncIcon } from "assets/icons/boslerActionIcons";
import { DatabaseIcon } from "assets/icons/boslerDataIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton, {
  TBoslerButtonIntent,
} from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import NoData from "components/CommonUI/NoData";
import { TDatasetColumn } from "components/Filters/Filters.view";
import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { NULL_UUID } from "utils/Common.constants";
import {
  generateKey,
  getLanguageLabel,
  getSourceIcon,
  isDefined,
  notEmpty,
} from "utils/utilities";
import { openFileExplorerModal } from "../../../redux/ModalSlice";
import { CreateSyncBtn } from "./CreateSyncButton";
import {
  getDatasetSyncAPI,
  getUnInitializedDataMartsAPI,
  putSyncAPI,
} from "./Sync.apis";
import styles from "./Sync.module.scss";
import { TDatasetSync, TSync, TSyncIndex } from "./Sync.types";
import { SyncHeader } from "./SyncHeader";
import { SyncNowBtn } from "./SyncNowButton";

const SyncBody = ({
  datasetSyncs,
  setDatasetSyncs,
  key,
  datasetSync,
}: {
  datasetSyncs: TDatasetSync[];
  setDatasetSyncs: any;
  key: number;
  datasetSync: TDatasetSync;
}) => {
  const [form] = useForm();
  const dispatch = useDispatch();
  const { sources } = useSelector((state) => (state as $TSFixMe).sourceList);
  const [intent, setIntent] = useState<TBoslerButtonIntent>("primary");
  const [newLinkDetails, setNewLinkDetails] = useState({
    ...initialLinkDetails,
  });
  const [loading, setLoading] = useState(false);
  const [source, setSource] = useState(datasetSync.sourceId);
  const [selectedSource, setSelectedSource] = useState(null);
  const [columns, setColumns] = useState([]);
  const [syncIndexes, setSyncIndexes] = useState<TSyncIndex[]>(
    datasetSync.syncIndexes
  );
  const [errorText, setErrorText] = useState("");

  const addSourceDetails = ({ id, path, name, subType }: any) => {
    setNewLinkDetails({
      ...newLinkDetails,
      sourceId: id,
    });

    const targetSource: any = sources?.find((source: any) => source.id == id);
    setSelectedSource(targetSource);
  };

  const handleDeleteIndex = (key: number) => {
    const _syncIndexes: TSyncIndex[] = [];
    syncIndexes.map((syncIndex: TSyncIndex, _key: number) => {
      if (_key != key) {
        _syncIndexes.push(syncIndex);
      }
    });
    setSyncIndexes(_syncIndexes);
  };

  const handleAddIndex = () => {
    setSyncIndexes([
      ...syncIndexes,
      { key: generateKey("sync-index"), id: undefined, columns: [] },
    ]);
  };

  const getSourceDetails = (source: string) => {
    getSourceDetailsAPI(source).then(({ data }) => {
      setSelectedSource(data);
    });
  };

  const getColumns = async () => {
    const data = await getDatasetColumns(
      datasetSync.datasetId,
      datasetSync.branch,
      NULL_UUID
    );
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

  useEffect(() => {
    if (source) {
      getSourceDetails(source);
    }
  }, [source]);

  return (
    <div>
      <Form
        form={form}
        initialValues={datasetSync}
        onChange={(values) => {
          console.log(form.getFieldsValue());
          console.log("values on change : ", values);
        }}
        onFinish={(values) => {
          console.log(values);
          const simpleSyncIndexes: string[][] = [];
          syncIndexes.map((syncIndex: TSyncIndex) => {
            simpleSyncIndexes.push(syncIndex.columns);
          });
          const payload = {
            isDatamartEnabled: false,
            ...datasetSync,
            ...values,
            syncIndexes: simpleSyncIndexes,
          };
          console.log(payload);
          setLoading(true);
          putSyncAPI(payload)
            .then(({ data }) => {
              console.log("CURRENT : ", datasetSync);
              console.log("CURENT RESULT : ", data);
              setIntent("success");
              const newDatasetSyncs: TDatasetSync[] = [];
              datasetSyncs.map((_datasetSync: TDatasetSync, _key: number) => {
                if (key == _key) {
                  newDatasetSyncs.push(data);
                } else {
                  newDatasetSyncs.push(_datasetSync);
                }
              });
              setDatasetSyncs(newDatasetSyncs);
            })
            .catch((error) => {
              setIntent("dangerous");
              setErrorText(error.response.data.description);
            })
            .finally(() => {
              setLoading(false);
            });
        }}
      >
        <Row justify={"space-between"}>
          {datasetSync.isDataMartSyncSpec != true && (
            <Col span={6}>
              <Form.Item
                label={getLanguageLabel("dataSource")}
                name="sourceId"
                rules={[
                  { required: true, message: "Please input your source!" },
                ]}
              >
                {source ? (
                  <SourceButtonPopover source={selectedSource}>
                    <BoslerButton
                      icon={
                        selectedSource ? (
                          getSourceIcon(
                            (selectedSource as $TSFixMe)["type"],
                            (selectedSource as $TSFixMe)["dbmsType"]
                          )
                        ) : (
                          <DatabaseIcon />
                        )
                      }
                      onClick={() => {
                        dispatch(
                          openFileExplorerModal({
                            type: ["SOURCE"],
                            action: (data) => {
                              addSourceDetails(data);
                            },
                            activeId: datasetSync.datasetId,
                          })
                        );
                      }}
                      intent={notEmpty(selectedSource) ? "success" : "warning"}
                      icononly
                    >
                      {notEmpty(selectedSource)
                        ? (selectedSource as any)?.name
                        : "Select Source"}
                    </BoslerButton>
                  </SourceButtonPopover>
                ) : (
                  <BoslerButton
                    onClick={() => {
                      dispatch(
                        openFileExplorerModal({
                          type: ["SOURCE"],
                          action: (data) => {
                            form.setFieldValue("sourceId", data.id);
                            setSource(data.id);
                          },
                          activeId: datasetSync.datasetId,
                        })
                      );
                    }}
                    intent={source ? "success" : "none"}
                    icon={<DatabaseIcon />}
                    icononly
                  />
                )}
              </Form.Item>
            </Col>
          )}
          <Col span={18}>
            <Form.Item
              label={getLanguageLabel("table")}
              name="tableName"
              rules={[
                { required: true, message: "Please input your table name!" },
              ]}
            >
              <BoslerInput />
            </Form.Item>
          </Col>
        </Row>

        {syncIndexes.map((syncIndex: TSyncIndex, _key: number) => {
          return (
            <div className={styles.indexRow}>
              <Select
                mode="multiple"
                value={syncIndex.columns}
                style={{ width: "100%" }}
                placeholder="Select column for Index"
                onChange={(option) => {
                  const _syncIndexes = [...syncIndexes];
                  _syncIndexes[_key].columns = option;
                  setSyncIndexes(_syncIndexes);
                  console.log(_syncIndexes);
                  console.log(option);
                  console.log(_syncIndexes[_key].columns);
                }}
                optionLabelProp="label"
                options={columns}
                onFocus={() => getColumns()}
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
                onClick={() => handleDeleteIndex(_key)}
                icon={<RemoveIcon />}
                icononly
                minimal
              />
            </div>
          );
        })}

        <div className={styles.syncBodyBottomBtns}>
          {datasetSync.isDataMartSyncSpec == true && (
            <BoslerButton
              icon={<AddIcon />}
              onClick={handleAddIndex}
              size={"small"}
              minimal
            >
              {getLanguageLabel("addIndex")}
            </BoslerButton>
          )}
        </div>
        <br />
        <div className={styles.syncBodyBottomBtns}>
          <Form.Item name="autoSyncOnBuild" valuePropName="checked">
            <Checkbox>{getLanguageLabel("autoSyncOnDatasetBuild")}</Checkbox>
          </Form.Item>
          <div className="--flex-row-end --flex-gap10">
            <Form.Item>
              <SyncNowBtn datasetSync={datasetSync} />
            </Form.Item>
            <Form.Item>
              {intent === "dangerous" && (
                <div className={styles.errorMessage}>{errorText}</div>
              )}
              <Tooltip title="Syncs the datasets and updates the config.">
                <BoslerButton
                  htmlType="submit"
                  icon={<SyncIcon />}
                  intent={intent}
                  loading={loading}
                >
                  {datasetSync.id ? "update & sync" : "create & sync"}
                </BoslerButton>
              </Tooltip>
            </Form.Item>
          </div>
        </div>
      </Form>
    </div>
  );
};

const DatasetSync = ({ datasetId, branch }: TSync) => {
  const [datasetSyncs, setDatasetSyncs] = useState<TDatasetSync[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isDefined(datasetId) && isDefined(branch)) {
      getDatasetSyncs();
    }
  }, [datasetId, branch]);

  const getDatasetSyncs = () => {
    setLoading(true);
    getDatasetSyncAPI(datasetId, branch)
      .then(({ data }) => {
        getUnInitializedDataMartsAPI(datasetId, branch)
          .then(({ data: unInitializedDataMartSyncSpecs }) => {
            setDatasetSyncs(data.concat(unInitializedDataMartSyncSpecs));
          })
          .catch(() => {
            setDatasetSyncs(data);
          });
      })
      .catch(() => {})
      .finally(() => {
        setLoading(false);
      });
  };

  if (loading) {
    return <BoslerLoader />;
  }
  return (
    <div className={styles.wrapper}>
      <CreateSyncBtn
        datasetSyncs={datasetSyncs}
        setDatasetSyncs={setDatasetSyncs}
        datasetId={datasetId}
        branch={branch}
      />
      {datasetSyncs.length == 0 ? (
        <NoData
          icon={<WarningState />}
          heading={getLanguageLabel("noSyncsPresent")}
        />
      ) : (
        <>
          {datasetSyncs.map((datasetSync: TDatasetSync, _key: number) => (
            <div
              className={styles.requestContainer}
              style={{
                background: datasetSync.isDataMartSyncSpec
                  ? "var(--bosler-bkg-color-muted)"
                  : "auto",
              }}
            >
              <BoslerCollapse
                header={
                  <SyncHeader
                    datasetSync={datasetSync}
                    datasetSyncs={datasetSyncs}
                    setDatasetSyncs={setDatasetSyncs}
                  />
                }
                key={datasetSync.id}
                collapsible={"HEADER"}
              >
                <SyncBody
                  datasetSync={datasetSync}
                  datasetSyncs={datasetSyncs}
                  setDatasetSyncs={setDatasetSyncs}
                  key={_key}
                />
              </BoslerCollapse>
            </div>
          ))}
        </>
      )}
    </div>
  );
};

export default DatasetSync;
