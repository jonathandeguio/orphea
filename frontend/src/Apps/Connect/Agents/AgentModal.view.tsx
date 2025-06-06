import { Card, Col, Row, Tooltip, Typography } from "antd";
import { DataAgentsIcon } from "assets/icons/boslerDataIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import {
  useFileExplorerService,
  useResourceHook,
} from "hooks/useFileExplorerService";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import {
  copyToClipboard,
  getLanguageLabel,
  notEmpty,
  openNotification,
} from "utils/utilities";
import { FolderIcon } from "../../../assets/icons/boslerFileIcons";
import {
  HelpIcon,
  PulseIcon,
} from "../../../assets/icons/boslerMiscellaneousIcons";
import {
  ArrowHorizontalIcon,
  SingleChevronRightIcon,
  TickIcon,
} from "../../../assets/icons/boslerNavigationIcon";
import { CopyCellIcon } from "../../../assets/icons/boslerTableIcons";
import { openFileExplorerModal } from "../../../redux/ModalSlice";
import { createAgent, listAgents } from "../../../redux/actions/agentActions";
import { ThunkAppDispatch } from "../../../redux/types/store";
import { handleConnectUpdateAPI } from "../Connect.api";
import { initialAgentDetails } from "./Agents.constants";

const PUBLIC_URL = window.location.origin;

const { Text } = Typography;

const AgentModal = ({
  isVisible,
  setIsVisible,
  updateDetails,
  defaultParent,
}: any) => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const [oneTimeCode, setOneTimeCode] = useState(false);
  const { createdAgent } = useSelector(
    (state) => (state as $TSFixMe).agentCreate
  );

  const [newAgentDetails, setNewAgentDetails] = useState({
    ...initialAgentDetails,
    parent: defaultParent,
  });

  useResourceHook(defaultParent, {
    resolveCallback: (data: { name: React.SetStateAction<string | null> }) => {
      setSelectedParent(data.name);
      setNewAgentDetails({ ...newAgentDetails, parent: defaultParent });
    },
  });

  const [selectedParent, setSelectedParent] = useState<string | null>(null);
  const [path, setPath] = useState<any[]>([]);

  const addParentFolder = ({ id, path, name }: any) => {
    setNewAgentDetails({ ...newAgentDetails, parent: id });
    setSelectedParent(name);
    setPath(path);
  };

  const resetState = () => {
    setIsVisible(false);
    setSelectedParent(null);
    setNewAgentDetails({ ...initialAgentDetails });
  };

  const { fetchResource } = useFileExplorerService();

  const handleOk = async () => {
    if (!(newAgentDetails.name && newAgentDetails.parent)) {
      openNotification(
        "Details Incomplete",
        "Please enter the complete details",
        "warning"
      );
      return;
    }
    await dispatch(
      createAgent({
        name: newAgentDetails.name,
        description: newAgentDetails.description,
        parent: newAgentDetails.parent,
      })
    ).then((data: $TSFixMe) => {
      fetchResource(data.agent.id);

      dispatch(listAgents());
      copyToClipboard(
        `bash <(/usr/bin/curl -k -s ${PUBLIC_URL}/api/connect/agent/install/${data.oneTimeCode})`
      );
      setOneTimeCode(true);
      resetState();
    });
  };

  const handleUpdate = async () => {
    if (!(newAgentDetails.name && newAgentDetails.parent)) {
      openNotification(
        "Details Incomplete",
        "Please enter the complete details",
        "warning"
      );
      return;
    }

    const body = {
      id: updateDetails.agentDetails.id,
      name: newAgentDetails.name,
      description: newAgentDetails.description,
    };

    handleConnectUpdateAPI("agent", JSON.stringify(body)).then(() => {
      dispatch(listAgents());
      updateDetails.updateAgent();
      resetState();
    });
  };

  useEffect(() => {
    if (updateDetails) {
      setNewAgentDetails({ ...updateDetails.agentDetails });
      setSelectedParent(updateDetails.parent.name);
    }
  }, [updateDetails]);

  return (
    <>
      <BoslerModal
        headingIcon={<DataAgentsIcon />}
        heading={getLanguageLabel("agent")}
        open={isVisible}
        onOk={handleOk}
        onCancel={() => setIsVisible(false)}
        footerExtraText={getLanguageLabel("connectAdminOnlyMessage")}
        width={800}
        footerButtonArea={
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
        }
        information={
          <div style={{ padding: "10px", width: "300px" }}>
            <div className="text-and-icon-align">
              <PulseIcon />
              <Text strong>Bridge</Text>
            </div>
            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("agentInfo")}
              </Text>
            </div>
            <br />
            <div className="text-and-icon-align">
              <HelpIcon />
              <Text strong>Support</Text>
            </div>

            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("agentSupport")} <strong>Linux</strong>
                <Text type="secondary">
                  (Ubuntu, Redhat, Centos, Fedora){" "}
                </Text>& <strong>MacOS</strong>.{" "}
                {getLanguageLabel("windowsAvailabilityMessage")}
              </Text>
              <br />
              <Link to="/learn/">{getLanguageLabel("learn")}</Link>
            </div>

            <br />
            <div className="text-and-icon-align">
              <ArrowHorizontalIcon />
              <Text strong>
                {getLanguageLabel("direct")} & {getLanguageLabel("onPremise")}
              </Text>
            </div>

            <div style={{ paddingTop: "2px", paddingLeft: "20px" }}>
              <Text style={{ fontSize: "0.8rem" }}>
                {getLanguageLabel("directAgentInstallation")}
              </Text>
            </div>
          </div>
        }
      >
        <div className="BoslerHeader1">{getLanguageLabel("name")}</div>
        <BoslerInput
          bordered
          autofocus
          onChange={(e) =>
            setNewAgentDetails({
              ...newAgentDetails,
              name: e.target.value,
            })
          }
          value={newAgentDetails.name}
          name="agentName"
          required
          placeholder={getLanguageLabel("agentName")}
        />
        <div className="BoslerHeader1">{getLanguageLabel("description")}</div>
        <BoslerInput
          onChange={(e) =>
            setNewAgentDetails({
              ...newAgentDetails,
              description: e.target.value,
            })
          }
          required
          value={newAgentDetails.description}
          placeholder={getLanguageLabel("descriptionOpt")}
        />

        <Row
          justify={"space-between"}
          gutter={[16, 16]}
          style={{
            marginTop: "10px",
          }}
        >
          <Col span={8}>{getLanguageLabel("parentFolder")}</Col>
          <Col span={16}>
            <BoslerButton
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
              style={{
                display: "inline-flex",
                alignItems: "center",
                cursor: "pointer",
              }}
              intent={selectedParent ? "success" : "warning"}
            >
              {selectedParent
                ? selectedParent
                : getLanguageLabel("parentFolder")}
            </BoslerButton>
            <br />
            {notEmpty(path) ? (
              <Text
                type="secondary"
                style={{
                  marginTop: "5px",
                  fontSize: "0.6rem",
                }}
              >
                <div
                  style={{
                    flexDirection: "row",
                    display: "flex",
                    alignItems: "center",
                  }}
                >
                  <div
                    style={{
                      fontWeight: 700,
                      marginRight: "1rem",
                      fontSize: "0.7rem",
                    }}
                  >
                    {getLanguageLabel("selected")}:
                  </div>
                  {path?.map((p, idx) => (
                    <div style={{ display: "flex", fontSize: "0.7rem" }}>
                      <>{p.name}</>
                      {idx + 1 != path.length && <SingleChevronRightIcon />}
                    </div>
                  ))}
                </div>
              </Text>
            ) : (
              <Text
                type="secondary"
                style={{
                  marginTop: "5px",
                  fontSize: "0.6rem",
                }}
              >
                {getLanguageLabel("folderPlacement")}
              </Text>
            )}
          </Col>
        </Row>
      </BoslerModal>

      {createdAgent && (
        <BoslerModal
          heading={getLanguageLabel("agentDetails")}
          open={oneTimeCode}
          onOk={() => setOneTimeCode(false)}
          onCancel={() => setOneTimeCode(false)}
          footerButtonArea={
            <BoslerButton
              icon={<CopyCellIcon />}
              onClick={() =>
                copyToClipboard(
                  `bash <(/usr/bin/curl -k -s ${PUBLIC_URL}/api/connect/agent/install/${oneTimeCode})`
                )
              }
              minimal
            >
              {getLanguageLabel("code")}
            </BoslerButton>
          }
        >
          <Text>{getLanguageLabel("agentMsg")}</Text>

          <Card className="interactive" style={{ margin: "0.5rem 0" }}>
            <Text strong>
              {`bash < (/ usr/ bin/ curl -k -s ${PUBLIC_URL}/ api/ connect/ agent/ install/ ${createdAgent.oneTimeCode})`}
              <Tooltip title={"Click to copy agent secret id"}>
                <BoslerButton
                  icon={<CopyCellIcon />}
                  onClick={() =>
                    copyToClipboard(
                      `bash <(/usr/bin/curl -k -s ${PUBLIC_URL}/api/connect/agent/install/${createdAgent.oneTimeCode})`
                    )
                  }
                  minimal
                  icononly
                />
              </Tooltip>
            </Text>
          </Card>

          <Row justify="end"></Row>
        </BoslerModal>
      )}
    </>
  );
};

export default AgentModal;
