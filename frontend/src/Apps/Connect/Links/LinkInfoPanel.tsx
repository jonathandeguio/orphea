import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { Alert, Checkbox, Col, Row, Tooltip, Typography } from "antd";
import {
  CopyIcon,
  EditIcon
} from "assets/icons/boslerEditorIcons";
import { GitMergeBranchIcon } from "assets/icons/boslerExternalIcons";
import { FolderIcon } from "assets/icons/boslerFileIcons";
import { TableIcon } from "assets/icons/boslerTableIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { ScheduleTriggerType } from "components/bottomBar/Schedules/SchedulesModal.constants";
import { getExpressionString } from "components/common/CronJob/CronJob.services";
import React, { useState } from "react";
import {
  copyToClipboard,
  getLanguageLabel,
  getSourceIcon,
  getUser,
  getUserLanguage,
} from "utils/utilities";
import { ISourceConfig } from "../Sources/Source";
import { DatasetButtonPopover } from "./DatasetButtonPopover.view";
import { ILink } from "./Link.types";
import LinkModal from "./LinkModal.view";
import { SourceButtonPopover } from "./SourceButtonPopover";

const { Text } = Typography;

interface IProps {
  dataset: { id: string; name: string };
  source: ISourceConfig;
  getLink: () => void;
  link: ILink;
}

const LinkInfoPanel = ({ dataset, source, getLink, link }: IProps) => {
  const navigator = useNavigateHelper();
  const [tooltipTitle, setTooltipTitle] = useState(
    getLanguageLabel("clickToCopyIntoClipboard")
  );
  const [isUpdateLinkModalOpen, setIsUpdateLinkModalOpen] = useState(false);
  const userLan = getUserLanguage(getUser());

  return (
    <div className="--flex-col-center">
      <div
        className="--p20"
        style={{
          height: "100%",
          width: "100%",
          display: "flex",
          flexDirection: "column",
          minHeight: "0px",
          transition: "all 1s linear",
        }}
      >
        <Row justify={"space-between"} align="middle">
          <Col>
            <Text type="secondary" strong>
              {getLanguageLabel("general").toUpperCase()}
            </Text>
          </Col>
          <Col>
            <BoslerButton
              icon={<EditIcon />}
              intent="primary"
              onClick={() => setIsUpdateLinkModalOpen(true)}
            >
              {getLanguageLabel("edit")}
            </BoslerButton>
          </Col>
        </Row>
        <Row
          justify={"space-between"}
          align="middle"
          style={{ marginTop: "6px" }}
          gutter={[16, 16]}
        >
          <Col span={8}>
            <Text>{getLanguageLabel("name")}</Text>
          </Col>
          <Col span={16}>
            <Text>{link.name}</Text>
          </Col>
        </Row>
        {link.description && (
          <>
            <Row
              justify={"space-between"}
              align="top"
              style={{ marginTop: "6px" }}
              gutter={[16, 16]}
            >
              <Col span={8}>
                <Text>{getLanguageLabel("description")}</Text>
              </Col>
              <Col span={16}>{link.description}</Col>
            </Row>
          </>
        )}
        <Row
          justify={"space-between"}
          align="top"
          style={{ marginTop: "6px" }}
          gutter={[16, 16]}
        >
          <Col span={8}>
            <Text>{getLanguageLabel("parentFolder")}</Text>
          </Col>
          <Col span={16}>
            <BoslerButton
              intent="none"
              icon={<FolderIcon />}
              onClick={() => {
                if (link?.parent) navigator(link.parent as string);
              }}
              minimal
            >
              {parent.name}
            </BoslerButton>
          </Col>
        </Row>
        {!link.dataLiveLoad && (
          <>
            {dataset && (
              <>
                <br />
                <Text type="secondary" strong>
                  {getLanguageLabel("dataset").toUpperCase()}
                </Text>
                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "6px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>{getLanguageLabel("dataset")}</Text>
                  </Col>
                  <Col span={16}>
                    <DatasetButtonPopover dataset={dataset}>
                      <BoslerButton
                        onClick={() =>
                          navigator(dataset.id, { branch: link.branch })
                        }
                        intent={"primary"}
                        icon={<TableIcon color={"#4C90F0"} />}
                        minimal
                      >
                        {dataset.name}
                      </BoslerButton>
                    </DatasetButtonPopover>
                  </Col>
                </Row>

                <Row
                  justify={"space-between"}
                  align="middle"
                  style={{ marginTop: "6px" }}
                  gutter={[16, 16]}
                >
                  <Col span={8}>
                    <Text>{getLanguageLabel("branch")}</Text>
                  </Col>
                  <Col span={16}>
                    <BoslerButton icon={<GitMergeBranchIcon />} minimal>
                      {link.branch}
                    </BoslerButton>
                  </Col>
                </Row>
              </>
            )}
          </>
        )}
        {source && (
          <>
            <br />
            <Text type="secondary" strong className="--mt10">
              {getLanguageLabel("dataSource").toUpperCase()}
            </Text>

            <>
              <Row
                justify={"space-between"}
                align="middle"
                style={{ marginTop: "6px" }}
                gutter={[16, 16]}
              >
                <Col span={8}>
                  <Text>{getLanguageLabel("dataSource")}</Text>
                </Col>
                <Col span={16}>
                  <SourceButtonPopover source={source}>
                    <BoslerButton
                      onClick={() => navigator(source.id)}
                      icon={getSourceIcon(source.type, source.dbmsType)}
                      minimal
                    >
                      {source.name}
                    </BoslerButton>
                  </SourceButtonPopover>
                </Col>
              </Row>
              {!link.dataLiveLoad && (
                <>
                  <div className="customizer-subHeader">
                    <BoslerCollapse
                      key="additionSettings"
                      collapsible={"HEADER"}
                      header={
                        <div className="query_item__heading">
                          {getLanguageLabel("additional")}
                        </div>
                      }
                    >
                      <>
                        {link.type.toUpperCase() === "FOLDER" && (
                          <>
                            <Row
                              justify={"space-between"}
                              align="middle"
                              style={{ marginTop: "6px" }}
                              gutter={[16, 16]}
                            >
                              <Col span={8}>
                                <Text>{getLanguageLabel("delete")}</Text>
                              </Col>
                              <Col span={16}>
                                <Checkbox
                                  checked={link.deleteFilesAfterUpload}
                                />
                                &nbsp;&nbsp;
                                <Text
                                  style={{
                                    marginTop: "0px",
                                    fontSize: "0.7rem",
                                  }}
                                >
                                  {getLanguageLabel("deleteFilesAfterUpload")}
                                </Text>
                              </Col>
                            </Row>
                          </>
                        )}
                        <Row
                          justify={"space-between"}
                          align="middle"
                          style={{ marginTop: "6px" }}
                          gutter={[16, 16]}
                        >
                          <Col span={8}>
                            <Text>{getLanguageLabel("saveMode")}</Text>
                          </Col>
                          <Col span={16}>
                            <Text>{link.writeMode}</Text>
                          </Col>
                        </Row>
                        <Row
                          justify={"space-between"}
                          align="middle"
                          style={{ marginTop: "6px" }}
                          gutter={[16, 16]}
                        >
                          <Col span={8}>
                            <Text>{getLanguageLabel("trigger")}</Text>
                          </Col>
                          <Col span={16}>
                            <Text>{link.trigger}</Text>
                          </Col>
                        </Row>
                        {link.trigger === ScheduleTriggerType.CRON && (
                          <>
                            <Row
                              justify={"space-between"}
                              align="top"
                              style={{ marginTop: "6px" }}
                              gutter={[16, 16]}
                            >
                              <Col span={8}>
                                <Text>{getLanguageLabel("schedules")}</Text>
                              </Col>
                              <Col span={16}>
                                <BoslerInput
                                  value={link.cronExpression}
                                  readOnly
                                  suffix={
                                    <Tooltip title={tooltipTitle}>
                                      <div
                                        onClick={() => {
                                          copyToClipboard(
                                            link.cronExpression,
                                            setTooltipTitle
                                          );
                                        }}
                                      >
                                        <CopyIcon />
                                      </div>
                                    </Tooltip>
                                  }
                                />
                                <Row>
                                  <Col span={24}>
                                    <Alert
                                      type="info"
                                      message={getExpressionString(
                                        link.cronExpression,
                                        userLan
                                      )}
                                      style={{
                                        marginBottom: "0.5rem",
                                      }}
                                      banner
                                    />
                                  </Col>
                                </Row>
                              </Col>
                            </Row>
                          </>
                        )}
                      </>
                    </BoslerCollapse>
                  </div>
                </>
              )}
            </>
          </>
        )}
      </div>
      {isUpdateLinkModalOpen && (
        <LinkModal
          isVisible={isUpdateLinkModalOpen}
          setIsVisible={setIsUpdateLinkModalOpen}
          updateDetails={{
            linkDetails: link,
            source: source,
            dataset: dataset,
            parent: parent,
            updateLink: getLink,
          }}
        />
      )}
    </div>
  );
};

export default LinkInfoPanel;
