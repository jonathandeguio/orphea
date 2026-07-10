import React, { useState, useEffect } from "react";
import { Table, Tooltip, Typography } from "antd";
import { Link } from "react-router-dom";
import MoveToDataInput from "../../../components/InputComponent/MoveToDataInput";
import MoveToDataButton from "../../../components/ButtonComponent/MoveToDataButton";
import {
  BuildIcon,
  SyncIcon,
  StopIcon,
  WarningIcon,
  SearchIcon,
  CrossIcon,
} from "../../../assets/icons/movetodataActionIcons";
import { CopyIcon, EditIcon } from "../../../assets/icons/movetodataEditorIcons";
import {
  copyToClipboard,
  getLanguageLabel,
  globalSearch,
  isDefined,
  openNotification,
  timeConverter,
} from "utils/utilities";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";
import { useDispatch } from "react-redux";
import { ThunkAppDispatch } from "../../../redux/types/store";
import UserPopOver from "components/UserPopover/userpopover";
import { getAllTriggerDetails } from "redux/actions/TriggerActions";
import { runTrigger, userById } from "../apis";
import MoveToDataModal from "../../../components/MoveToDataModalContainer";
import { Form, Select } from "antd";
import EditTriggerModal from "./EditTriggerModal";

const { Item } = Form;

const { Text } = Typography;

// Build Status

export const ABORTED = "ABORTED";
export const FAILED = "FAILED";
export const SUCCESS = "SUCCESS";
export const COMPLETED = "COMPLETED";
export const CANCELLED = "CANCELLED";
export const ERROR = "ERROR";
export const ACTIVE = "ACTIVE";
export const INFO = "INFO";

const TriggersTable = ({ allTriggers }: { allTriggers: any }) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [FilteredData, setFilteredData] = useState();
  const [usernames, setUsernames] = useState<{ [key: string]: any }>({});
  // const [isModalVisible, setIsModalVisible] = useState(false);
  const [isBuildModalVisible, setIsBuildModalVisible] = useState(false);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState({ name: "" });
  const [artifactName, setArtifactName] = useState("");
  const [isBuildButtonDisabled, setIsBuildButtonDisabled] = useState(true);
  const [tooltipVisible, setTooltipVisible] = useState(false);

  const handleTooltipVisibleChange = (visible: boolean) => {
    setTooltipVisible(visible);
  };

  const columns = [
    {
      title: getLanguageLabel("action"),
      dataIndex: "id",
      render: (text: any, record: any) => (
        <div style={{ display: "flex", gap: "8px" }}>
          <MoveToDataButton
            onClick={() => {
              showBuildModal(record);
            }}
            icon={<BuildIcon />}
            icononly
            disabled={record.buildStatus === "ACTIVE"}
            intent={record.buildStatus === "ACTIVE" ? "none" : "primary"}
            loading={record.buildStatus === "ACTIVE"}
          >
            {getLanguageLabel("build")}
          </MoveToDataButton>
          <MoveToDataButton
            onClick={() => {
              showEditModal(record);
            }}
            icon={<EditIcon />}
            icononly
            disabled={record.buildStatus === "ACTIVE"}
            intent={record.buildStatus === "ACTIVE" ? "none" : "primary"}
            loading={record.buildStatus === "ACTIVE"}
          >
            {getLanguageLabel("edit")}
          </MoveToDataButton>
        </div>
      ),
    },
    {
      title: `${getLanguageLabel("name")}`,
      dataIndex: "name",
      render: (text: any, record: any) => (
        <Link to={`/portal/artifacts/${record.id}`}>
          <Tooltip
            placement="right"
            title={<div>{getLanguageLabel("totalBuildsInfo")}</div>}
          >
            {record.name}(
            {record.triggerArtifactModels
              ? record.triggerArtifactModels.length
              : 0}
            )
          </Tooltip>
        </Link>
      ),
    },
    {
      title: getLanguageLabel("branch"),
      dataIndex: "branch",
      render: (text: string) => <>{text}</>,
    },
    {
      title: getLanguageLabel("commitId"),
      dataIndex: "commitId",
      render: (latestCommitId: string, record: any) => {
        if (!latestCommitId) {
          return <div className="MoveToDataSpan">{getLanguageLabel("none")}</div>;
        }

        const cleanUrl = record.repoUrl.replace(/\.git$/, "");
        const commitUrl = `${cleanUrl}/commit/${latestCommitId}`;
        const shortCommitId = latestCommitId.slice(0, 7);

        return (
          <Tooltip title={latestCommitId}>
            <a href={commitUrl} target="_blank" rel="noopener noreferrer">
              {shortCommitId}...
            </a>
          </Tooltip>
          //
        );
      },
    },
    {
      title: getLanguageLabel("latestTag"),
      dataIndex: "latestTag",
      render: (text: number) => (
        <div className="MoveToDataSpan">
          {text ? text : getLanguageLabel("none")}
        </div>
      ),
    },
    {
      title: getLanguageLabel("url"),
      dataIndex: "repoUrl",
      render: (repoUrl: string) => (
        <a href={repoUrl} target="_blank" rel="noopener noreferrer">
          {repoUrl}
        </a>
      ),
    },
    {
      title: getLanguageLabel("buildBy"),
      dataIndex: "buildBy",
      render: (buildBy: any, record: any) => {
        if (!record.buildBy) {
          return <></>;
        }

        const user = usernames[record.buildBy];

        return (
          <>
            <Text>
              <UserPopOver record={user}>
                {user?.name || record.buildBy}
              </UserPopOver>
            </Text>
          </>
        );
      },
    },
    {
      title: getLanguageLabel("registry"),
      dataIndex: "harborProjectName",
      render: (text: string) => <div className="MoveToDataSpan">{text}</div>,
    },
    {
      title: getLanguageLabel("buildType"),
      dataIndex: "buildType",
      render: (text: string) => <div className="MoveToDataSpan">{text}</div>,
    },

    {
      title: getLanguageLabel("lastBuildAt"),
      dataIndex: "buildAt",
      render: (text: number, row: any) => {
        if (!row.buildAt) {
          return <div className="MoveToDataSpan">{getLanguageLabel("none")}</div>;
        }
        return <div className="MoveToDataSpan">{timeConverter(text)}</div>;
      },
    },
    {
      title: getLanguageLabel("lastBuildStatus"),
      dataIndex: "buildStatus",
      render: (e: any, row: any) => {
        if (row.buildStatus) {
          const getStatusTooltip = () => {
            if (row.buildStatus === ACTIVE) {
              return "Currently build is active";
            } else if (row.buildStatus === SUCCESS) {
              return "Build completed successfully";
            } else if (row.buildStatus === ABORTED) {
              return "Build aborted";
            } else {
              return row.buildStatus.toString();
            }
          };
          return (
            <Tooltip title={getStatusTooltip()}>
              {row.buildStatus === ACTIVE ? (
                <SyncIcon size={22} color={"#08c"} spin />
              ) : row.buildStatus === SUCCESS ? (
                <div className="success-tick-circle text-and-icon-center">
                  <TickIcon color="#ffffff" />
                </div>
              ) : row.buildStatus === ABORTED ? (
                <div className="text-and-icon-center">
                  <StopIcon color="#FF0000" />
                </div>
              ) : (
                <div className="text-and-icon-center">
                  <WarningIcon size={26} color="#FFA500" />
                </div>
              )}
            </Tooltip>
          );
        } else {
          return <Text>{getLanguageLabel("unknown")}</Text>;
        }
      },
    },
  ];

  useEffect(() => {
    const fetchUsernames = async () => {
      const uniqueIds: string[] = Array.from(
        new Set(allTriggers.map((trigger: any) => trigger.buildBy))
      ) as string[];

      const newUsernames: { [key: string]: any } = {};

      await Promise.all(
        uniqueIds.map(async (id: string) => {
          if (id && !usernames[id]) {
            try {
              const response = await userById(id);
              newUsernames[id] = response.data;
            } catch (error) {
              console.error("Error fetching user data for ID:", id);
            }
          }
        })
      );

      setUsernames((prev) => ({ ...prev, ...newUsernames }));
    };

    if (allTriggers && allTriggers.length) {
      fetchUsernames();
    }
  }, [allTriggers]);

  const handleRun = (id: any) => {
    dispatch(getAllTriggerDetails());
    runTrigger(id).then(({ data }) => {
      if (isDefined(data)) {
        // openNotification("Build Started", "Check to view logs", "success");
      } else {
        openNotification(data, "Check to view logs", "error");
      }
    });
    dispatch(getAllTriggerDetails());
  };

  const showBuildModal = (record: any) => {
    setSelectedRecord(record);
    setIsBuildModalVisible(true);
  };

  const showEditModal = (record: any) => {
    setSelectedRecord(record);
    setIsEditModalVisible(true);
  };

  const handleBuildOk = (record: any) => {
    if (artifactName.toUpperCase() === record.name.toUpperCase()) {
      if (record) {
        setIsBuildModalVisible(false);
        setArtifactName("");
        setIsBuildButtonDisabled(true);
        handleRun(record.id);
      }
    }
  };

  const handleBuildCancel = () => {
    setIsBuildModalVisible(false);
    setArtifactName("");
    setIsBuildButtonDisabled(true);
  };

  const handleArtifactNameChange = (e: any) => {
    const value = e.target.value;
    setArtifactName(value);
    setIsBuildButtonDisabled(value !== selectedRecord.name);
  };

  return (
    <>
      <MoveToDataInput
        placeholder={getLanguageLabel("search")}
        allowClear
        onChange={(e: any) => {
          setFilteredData(globalSearch(e.target.value, allTriggers, columns));
        }}
        suffix={<SearchIcon />}
      />
      <div className="table-container">
        <Table
          columns={columns}
          dataSource={FilteredData !== undefined ? FilteredData : allTriggers}
          pagination={false}
        />
      </div>

      <MoveToDataModal
        headingIcon={<BuildIcon />}
        heading={getLanguageLabel("confirmBuild") + selectedRecord.name}
        open={isBuildModalVisible}
        onCancel={handleBuildCancel}
        onOk={() => handleBuildOk(selectedRecord)}
        footerButtonArea={[
          <MoveToDataButton
            key="build"
            icon={<BuildIcon />}
            disabled={isBuildButtonDisabled}
            onClick={() => handleBuildOk(selectedRecord)}
            intent="action"
          >
            {getLanguageLabel("build")}
          </MoveToDataButton>,
          <MoveToDataButton
            key="cancel"
            icon={<CrossIcon />}
            onClick={handleBuildCancel}
            intent="none"
          >
            {getLanguageLabel("cancel")}
          </MoveToDataButton>,
        ]}
      >
        <MoveToDataInput
          placeholder={getLanguageLabel("enterArtifactName")}
          value={artifactName}
          onChange={handleArtifactNameChange}
        />
      </MoveToDataModal>
      <EditTriggerModal
        selectedRecord={selectedRecord}
        isOpen={isEditModalVisible}
        setIsOpen={setIsEditModalVisible}
      />
    </>
  );
};

export default TriggersTable;
