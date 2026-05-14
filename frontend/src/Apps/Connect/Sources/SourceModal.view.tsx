import { Card, Col, Input, Row, Select, Switch, Tag, Typography } from "antd";
import axios from "axios";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";

import { FolderIcon } from "assets/icons/boslerFileIcons";
import { Buffer } from "buffer";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import { useFileExplorerService } from "hooks/useFileExplorerService";
import { Link } from "react-router-dom";
import {
  getLanguageLabel,
  getSourceIcon,
  isDefined,
  notEmpty,
  openNotification,
} from "utils/utilities";
import { SearchIcon } from "../../../assets/icons/boslerActionIcons";
import { DatabaseIcon } from "../../../assets/icons/boslerDataIcons";
import { KeyIcon } from "../../../assets/icons/boslerInterfaceIcons";
import {
  LibraryIcon,
  LightBulbIcon,
} from "../../../assets/icons/boslerMiscellaneousIcons";
import {
  ArrowHorizontalIcon,
  SingleChevronRightIcon,
  TickIcon,
} from "../../../assets/icons/boslerNavigationIcon";
import { openFileExplorerModal } from "../../../redux/ModalSlice";
import { listAgents } from "../../../redux/actions/agentActions";
import { listSources } from "../../../redux/actions/sourceActions";
import { ThunkAppDispatch } from "../../../redux/types/store";
import { handleConnectUpdateAPI } from "../Connect.api";
import { SourceAuthTypeEnum } from "../Enums/SourceAuthTypeEnum";
import { SourceTypeEnum } from "../Enums/SourceTypeEnum";
import { connectors, initialSourceDetails } from "./Source.constants";
import { isSourceConfigValid } from "./Source.utils";
import { TestConnectionButton } from "./TestConnection.view";

const { Text } = Typography;
const { Option } = Select;

const SourceModal = ({
  isVisible,
  setIsVisible,
  updateDetails,
  defaultParent,
}: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [searchText, setSearchText] = useState("");

  const initialConnector = {
    type: null,
    icon: null,
  };

  const [selectedConnector, setSelectedConnector] = useState({
    ...initialConnector,
  });

  const [filteredConnectors, setFilteredConnectors] = useState(connectors);

  const selectConnector = (icon: any, type: any, subType: any) => {
    if (type == "jdbc") {
      setNewSourceDetails({
        ...newSourceDetails,
        dbmsType: subType,
        type: type,
      });
      setSelectedConnector({ type: subType, icon: icon });
    } else {
      setNewSourceDetails({
        ...newSourceDetails,
        type: type,
      });
      setSelectedConnector({ type: type, icon: icon });
    }
  };

  const deSelectConnector = () => {
    setSelectedConnector({ ...initialConnector });
  };

  const [newSourceDetails, setNewSourceDetails] = useState({
    ...initialSourceDetails,
  });

  const [selectedParent, setSelectedParent] = useState(
    updateDetails ? updateDetails.parent.name : null
  );
  const { getFileIndex, fetchResource } = useFileExplorerService();

  const { agents } = useSelector((state) => (state as $TSFixMe).agentList);

  const addParentFolder = ({ id, path, name }: any) => {
    setNewSourceDetails({ ...newSourceDetails, parent: id });
    setSelectedParent(name);
  };

  const resetState = () => {
    setIsVisible(false);
    setSelectedParent(null);
    setSelectedConnector({ ...initialConnector });
    setNewSourceDetails({ ...initialSourceDetails });
  };

  const tagRender = (props: $TSFixMe) => {
    const { label, closable, onClose } = props;
    const onPreventMouseDown = (event: $TSFixMe) => {
      event.preventDefault();
      event.stopPropagation();
    };
    return (
      <Tag
        color="cyan"
        onMouseDown={onPreventMouseDown}
        closable={closable}
        onClose={onClose}
        style={{ marginRight: 3 }}
      >
        {label}
      </Tag>
    );
  };

  const handleOk = async () => {
    if (!isSourceConfigValid(newSourceDetails)) {
      openNotification(
        "Details incomplete",
        "Please enter the complete database details",
        "warning"
      );
      return;
    }

    axios
      .post(`/connect/source/create`, JSON.stringify(newSourceDetails))
      .then(({ data }) => {
        fetchResource(data.id);
        dispatch(listSources());
        resetState();
      });
  };

  const handleUpdate = async () => {
    if (!isSourceConfigValid(newSourceDetails)) {
      openNotification(
        "Details incomplete",
        "Please enter the complete database details",
        "warning"
      );
      return;
    }

    const body = {
      ...newSourceDetails,
      sourceConfig: updateDetails.sourceDetails.sourceConfig,
      id: updateDetails.sourceDetails.id,
      name: newSourceDetails.name,
      description: newSourceDetails.description,
      parent: newSourceDetails.parent,
      directLoad: newSourceDetails.directLoad,
      agentId: newSourceDetails.agentId,
    };
    handleConnectUpdateAPI("source", JSON.stringify(body)).then(() => {
      dispatch(listSources());
      updateDetails.updateSource();
      resetState();
    });
  };

  useEffect(() => {
    if (notEmpty(defaultParent)) {
      getFileIndex(defaultParent).then((data) => {
        setSelectedParent(data.name);
        setNewSourceDetails({ ...newSourceDetails, parent: defaultParent });
      });
    }
  }, [defaultParent]);

  useEffect(() => {
    if (updateDetails) {
      setNewSourceDetails({ ...updateDetails.sourceDetails });
      setSelectedConnector({
        type:
          updateDetails.sourceDetails != "jdbc"
            ? updateDetails.sourceDetails.type
            : updateDetails.sourceDetails.dbmsType,
        icon: getSourceIcon(
          updateDetails.sourceDetails.type,
          updateDetails.sourceDetails?.dbmsType
        ) as any,
      });
      setSelectedParent(updateDetails.parent.name);
    }
  }, [updateDetails]);

  useEffect(() => {
    const newFilteredConnectors = connectors.filter((connector: any) =>
      connector.id.toLowerCase().includes(searchText.toLowerCase())
    );
    setFilteredConnectors(newFilteredConnectors);
  }, [searchText]);
  console.log(newSourceDetails, selectedConnector);
  return (
    <>
      <BoslerModal
        headingIcon={
          !selectedConnector.type ? <DatabaseIcon /> : selectedConnector.icon
        }
        heading={
          !selectedConnector.type ? (
            "Source"
          ) : (
            <div
              style={{
                display: "flex",
                alignItems: "center",
              }}
            >
              <div
                onClick={() => deSelectConnector()}
                className="BoslerBtnHeading"
                style={{
                  fontSize: "14px",
                }}
              >
                Source
              </div>
              <SingleChevronRightIcon />
              <div
                className="BoslerBtnHeading"
                style={{
                  fontSize: "14px",
                }}
              >
                {selectedConnector.type}
              </div>
            </div>
          )
        }
        extraActionHeading={
          <BoslerInput
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            placeholder={getLanguageLabel("search")}
            suffix={<SearchIcon />}
            autofocus
          />
        }
        closable={false}
        open={isVisible}
        onCancel={() => setIsVisible(false)}
        width={990}
        footerExtraText={getLanguageLabel("connectAdminOnlyMessage")}
        footerButtonArea={
          selectedConnector.type && (
            <BoslerButton
              intent="primary"
              onClick={updateDetails ? handleUpdate : handleOk}
              icon={<TickIcon />}
              textTransform="none"
            >
              {updateDetails
                ? getLanguageLabel("update")
                : getLanguageLabel("create")}
            </BoslerButton>
          )
        }
        information={
          <div>
            <div style={{ padding: "20px" }}>
              <div className="text-and-icon-align">
                <LightBulbIcon />
                <Text strong>Direct Connection</Text>
              </div>
              <div style={{ paddingTop: "5px", paddingLeft: "20px" }}>
                {getLanguageLabel("connectFirewallSettings")}
              </div>
            </div>
            <div style={{ padding: "20px" }}>
              <div className="text-and-icon-align">
                <LibraryIcon />
                <Text strong>{getLanguageLabel("learn")}</Text>
              </div>
              <div style={{ paddingTop: "10px", paddingLeft: "20px" }}>
                <Link to="/learn/">DataConnection Guidelines</Link>
                <br />
                <Link to="/learn/">Best Practices</Link>
                <br />
                <Link to="/learn/">Data Transfer Security</Link>
                <br />
                <Link to="/learn/">Governance Policy and Guidelines</Link>
              </div>
            </div>
            <div style={{ padding: "20px" }}>
              <div className="text-and-icon-align">
                <ArrowHorizontalIcon />
                <Text strong>
                  {getLanguageLabel("direct")} & {getLanguageLabel("onPremise")}
                </Text>
              </div>

              <div style={{ paddingTop: "5px", paddingLeft: "20px" }}>
                <Text>{getLanguageLabel("directAgentInstallation")}</Text>
              </div>
            </div>
            <div style={{ padding: "20px" }}>
              <div className="text-and-icon-align">
                <KeyIcon />
                <Text strong>{getLanguageLabel("access")}</Text>
              </div>
              <div style={{ paddingTop: "5px", paddingLeft: "20px" }}>
                {getLanguageLabel("dataIngestionRestriction")}
              </div>
            </div>
          </div>
        }
      >
        {!selectedConnector.type ? (
          <Row style={{ height: "60vh" }} gutter={[16, 16]}>
            {filteredConnectors.map((connector) => {
              return (
                <Col span={12}>
                  <Card
                    className="Selectable-Cards"
                    style={
                      isDefined(connector.disabled)
                        ? {
                            cursor: "not-allowed",
                            border: "1px dotted red",
                          }
                        : { cursor: "pointer" }
                    }
                    onClick={() => {
                      if (!isDefined(connector.disabled))
                        selectConnector(
                          connector.icon,
                          connector.type,
                          connector.subType
                        );
                    }}
                  >
                    {connector.label}
                  </Card>
                </Col>
              );
            })}
          </Row>
        ) : (
          <div
            style={{
              height: "60vh",
              marginBottom: "10px",
            }}
          >
            <div className="BoslerHeader1">{getLanguageLabel("general")}</div>
            <Row
              justify={"space-between"}
              align="middle"
              style={{ marginTop: "10px" }}
              gutter={[16, 16]}
            >
              <Col span={8}>
                <Text>{getLanguageLabel("name")}</Text>
              </Col>
              <Col span={16}>
                <BoslerInput
                  bordered
                  autofocus
                  onChange={(e) =>
                    setNewSourceDetails({
                      ...newSourceDetails,
                      name: e.target.value,
                    })
                  }
                  value={newSourceDetails.name}
                  required
                />
              </Col>
            </Row>

            <Row
              justify={"space-between"}
              align="top"
              style={{ marginTop: "10px" }}
              gutter={[16, 16]}
            >
              <Col span={8}>
                <Text>{getLanguageLabel("description")}</Text>
              </Col>
              <Col span={16}>
                <BoslerInput
                  onChange={(e) =>
                    setNewSourceDetails({
                      ...newSourceDetails,
                      description: e.target.value,
                    })
                  }
                  value={newSourceDetails.description}
                  required
                />
              </Col>
            </Row>
            <Row
              justify={"space-between"}
              align="top"
              style={{ marginTop: "10px" }}
              gutter={[16, 16]}
            >
              <Col span={8}>{getLanguageLabel("parentFolder")}</Col>
              <Col span={16}>
                <BoslerButton
                  intent={selectedParent ? "success" : "warning"}
                  icon={<FolderIcon />}
                  onClick={() => {
                    dispatch(
                      openFileExplorerModal({
                        type: ["FOLDER"],
                        action: (data: any) => {
                          addParentFolder(data);
                        },
                        activeId: defaultParent,
                      })
                    );
                  }}
                >
                  {selectedParent
                    ? selectedParent
                    : getLanguageLabel("parentFolder")}
                </BoslerButton>
                <Text
                  type="secondary"
                  style={{
                    marginTop: "0px",
                    paddingRight: "1.5rem",
                    fontSize: "0.7rem",
                  }}
                >
                  {getLanguageLabel("folderPlacement")}
                </Text>
              </Col>
            </Row>

            {newSourceDetails.type === "jdbc" && (
              <>
                <div className="BoslerHeader1">
                  {getLanguageLabel("database").toUpperCase()}
                </div>

                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "10px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>
                      {getLanguageLabel("server")} & {getLanguageLabel("port")}
                    </Text>
                  </Col>
                  <Col span={10}>
                    <BoslerInput
                      placeholder={getLanguageLabel("server")}
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          server: e.target.value,
                        })
                      }
                      value={newSourceDetails.server}
                      required
                    />
                  </Col>
                  <Col span={6}>
                    <BoslerInput
                      placeholder={"443"}
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          port: e.target.value,
                        })
                      }
                      value={newSourceDetails.port}
                      required
                    />
                  </Col>
                </Row>
                {newSourceDetails.dbmsType == SourceTypeEnum.SNOWFLAKE && (
                  <Row
                    justify={"space-between"}
                    align="middle"
                    style={{ marginTop: "10px" }}
                    gutter={[16, 16]}
                  >
                    <Col span={8}>
                      <Text>{getLanguageLabel("warehouse")}</Text>
                    </Col>
                    <Col span={16}>
                      <BoslerInput
                        placeholder={getLanguageLabel("warehouse")}
                        onChange={(e) =>
                          setNewSourceDetails({
                            ...newSourceDetails,
                            warehouse: e.target.value,
                          })
                        }
                        value={newSourceDetails.warehouse}
                        required
                      />
                    </Col>
                  </Row>
                )}

                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "10px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>{getLanguageLabel("database")}</Text>
                  </Col>
                  <Col span={16}>
                    <BoslerInput
                      placeholder={getLanguageLabel("database")}
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          database: e.target.value,
                        })
                      }
                      value={newSourceDetails.database}
                      required
                    />
                  </Col>
                </Row>

                {newSourceDetails.dbmsType == SourceTypeEnum.SNOWFLAKE && (
                  <Row
                    justify={"space-between"}
                    align="middle"
                    style={{ marginTop: "10px" }}
                    gutter={[16, 16]}
                  >
                    <Col span={8}>
                      <Text>{getLanguageLabel("schema")} (Optional)</Text>
                    </Col>
                    <Col span={16}>
                      <BoslerInput
                        placeholder={getLanguageLabel("schema")}
                        onChange={(e) =>
                          setNewSourceDetails({
                            ...newSourceDetails,
                            schema: e.target.value,
                          })
                        }
                        value={newSourceDetails.schema}
                      />
                    </Col>
                  </Row>
                )}

                <div className="BoslerHeader1">
                  {getLanguageLabel("authentication")}
                </div>
                {newSourceDetails.dbmsType == SourceTypeEnum.SNOWFLAKE && (
                  <>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      gutter={[16, 16]}
                      style={{ marginTop: "10px" }}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("type")}</Text>
                      </Col>
                      <Col span={16}>
                        <Select
                          placeholder={getLanguageLabel("type")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              authType: e,
                            })
                          }
                          value={newSourceDetails.authType}
                          options={[
                            {
                              label: "Username & Password",
                              value: SourceAuthTypeEnum.DEFAULT,
                            },
                            {
                              label: "Key Pair",
                              value: SourceAuthTypeEnum.KEYPAIR,
                            },
                          ]}
                        />
                      </Col>
                    </Row>
                  </>
                )}
                {newSourceDetails.authType == SourceAuthTypeEnum.KEYPAIR ? (
                  <>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      gutter={[16, 16]}
                      style={{ marginTop: "10px" }}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("userName")}</Text>
                      </Col>
                      <Col span={16}>
                        <BoslerInput
                          placeholder={getLanguageLabel("userName")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              username: e.target.value,
                            })
                          }
                          value={newSourceDetails.username}
                          required
                        />
                      </Col>
                    </Row>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      style={{ marginTop: "10px" }}
                      gutter={[16, 16]}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("privateKey")}</Text>
                      </Col>
                      <Col span={16}>
                        <Input.TextArea
                          size="large"
                          className="input"
                          placeholder={getLanguageLabel("privateKey")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              privateKey: e.target.value,
                            })
                          }
                          value={newSourceDetails.privateKey}
                          required
                        />
                      </Col>
                    </Row>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      style={{ marginTop: "10px" }}
                      gutter={[16, 16]}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("privateKeyPassPhrase")}</Text>
                      </Col>
                      <Col span={16}>
                        <BoslerInput
                          className="input"
                          placeholder={getLanguageLabel("privateKeyPassPhrase")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              privateKeyPassPhrase: e.target.value,
                            })
                          }
                          value={newSourceDetails.privateKeyPassPhrase}
                        />
                      </Col>
                    </Row>
                  </>
                ) : (
                  <>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      gutter={[16, 16]}
                      style={{ marginTop: "10px" }}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("userName")}</Text>
                      </Col>
                      <Col span={16}>
                        <BoslerInput
                          placeholder={getLanguageLabel("userName")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              username: e.target.value,
                            })
                          }
                          value={newSourceDetails.username}
                          required
                        />
                      </Col>
                    </Row>
                    <Row
                      justify={"space-between"}
                      align="middle"
                      style={{ marginTop: "10px" }}
                      gutter={[16, 16]}
                    >
                      <Col span={8}>
                        <Text>{getLanguageLabel("password")}</Text>
                      </Col>
                      <Col span={16}>
                        <Input.Password
                          className="input"
                          placeholder={getLanguageLabel("password")}
                          onChange={(e) =>
                            setNewSourceDetails({
                              ...newSourceDetails,
                              password: Buffer.from(
                                e.target.value,
                                "ascii"
                              ).toString("base64"),
                            })
                          }
                          value={
                            isDefined(newSourceDetails.password)
                              ? Buffer.from(
                                  newSourceDetails.password,
                                  "base64"
                                ).toString("ascii")
                              : ""
                          }
                          required
                        />
                      </Col>
                    </Row>
                  </>
                )}

                {newSourceDetails.dbmsType == SourceTypeEnum.SNOWFLAKE && (
                  <Row
                    justify={"space-between"}
                    align="middle"
                    style={{ marginTop: "10px" }}
                    gutter={[16, 16]}
                  >
                    <Col span={8}>
                      <Text>{getLanguageLabel("userRole")} (Optional)</Text>
                    </Col>
                    <Col span={16}>
                      <BoslerInput
                        placeholder={getLanguageLabel("userRole")}
                        onChange={(e) =>
                          setNewSourceDetails({
                            ...newSourceDetails,
                            userRole: e.target.value,
                          })
                        }
                        value={newSourceDetails.userRole}
                      />
                    </Col>
                  </Row>
                )}
              </>
            )}

            {newSourceDetails.type === "rest" && (
              <>
                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "10px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>{getLanguageLabel("token")}</Text>
                  </Col>
                  <Col span={16}>
                    <BoslerInput
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          token: e.target.value,
                        })
                      }
                      value={newSourceDetails.token}
                      required
                    />
                  </Col>
                </Row>

                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "10px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>URL</Text>
                  </Col>
                  <Col span={16}>
                    <BoslerInput
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          url: e.target.value,
                        })
                      }
                      value={newSourceDetails.url}
                      required
                    />
                  </Col>
                </Row>

                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "10px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>{getLanguageLabel("method")}</Text>
                  </Col>
                  <Col span={16}>
                    <Select
                      placeholder={getLanguageLabel("method")}
                      onChange={(e) =>
                        setNewSourceDetails({
                          ...newSourceDetails,
                          method: e,
                        })
                      }
                      value={newSourceDetails.method}
                    >
                      <Option value={"GET"}>{"GET"}</Option>
                      <Option value={"POST"}>{"POST"}</Option>
                      <Option value={"PUT"}>{"PUT"}</Option>
                      <Option value={"DELETE"}>{"DELETE"}</Option>
                    </Select>
                  </Col>
                </Row>
              </>
            )}

            {newSourceDetails.type === "FOLDER" && (
              <Row
                justify={"space-between"}
                align="middle"
                style={{ marginTop: "10px" }}
                gutter={[16, 16]}
              >
                <Col span={8}>
                  <Text>{getLanguageLabel("folder")}</Text>
                </Col>
                <Col span={16}>
                  <BoslerInput
                    placeholder="Folder Path : e.g. /data/folder1"
                    value={newSourceDetails.path}
                    required
                    onChange={(e) =>
                      setNewSourceDetails({
                        ...newSourceDetails,
                        path: e.target.value,
                      })
                    }
                  />
                </Col>
              </Row>
            )}

            <div className="BoslerHeader1">
              {getLanguageLabel("connection").toUpperCase()}
            </div>
            <Row align="middle">
              <Col>
                <Switch
                  size="small"
                  defaultChecked={newSourceDetails.directLoad}
                  onChange={(val: boolean) =>
                    setNewSourceDetails({
                      ...newSourceDetails,
                      directLoad: val,
                      agentId: [],
                    })
                  }
                  checkedChildren={getLanguageLabel("direct")}
                  unCheckedChildren={getLanguageLabel("agent")}
                />
              </Col>
            </Row>

            {!newSourceDetails.directLoad ? (
              <div>
                <Select
                  mode="tags"
                  showArrow
                  placeholder={getLanguageLabel("select")}
                  maxTagCount="responsive"
                  showSearch={undefined}
                  tagRender={tagRender}
                  style={{ width: "100%" }}
                  options={agents?.map((agent: any) => {
                    return { id: agent.id, value: agent.name };
                  })}
                  onClick={() => {
                    if (agents == null) dispatch(listAgents());
                  }}
                  onSelect={(val: $TSFixMe, option: $TSFixMe) => {
                    let agents = newSourceDetails.agentId;
                    if (!newSourceDetails.agentId.includes(option.id))
                      agents = [...agents, option.id];
                    setNewSourceDetails({
                      ...newSourceDetails,
                      agentId: agents,
                    });
                  }}
                  onDeselect={(val: $TSFixMe, option: $TSFixMe) => {
                    const agents = newSourceDetails.agentId.filter(
                      (agent: any) => agent !== option.id
                    );
                    setNewSourceDetails({
                      ...newSourceDetails,
                      agentId: agents,
                    });
                  }}
                />
              </div>
            ) : (
              <TestConnectionButton source={newSourceDetails} />
            )}
          </div>
        )}
      </BoslerModal>
    </>
  );
};

export default SourceModal;
