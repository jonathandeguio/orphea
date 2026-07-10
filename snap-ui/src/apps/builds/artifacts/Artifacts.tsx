import { Col, Divider, Row, Table, Tooltip, Typography } from "antd";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import axios from "axios"; // Ensure axios is imported
import styles from "./Artifact.module.scss";

import React from "react";
import {
  copyToClipboard,
  formatDuration,
  getLanguageLabel,
  getSocketClient,
  globalSearch,
  isDefined,
  openNotification,
  timeConverter,
} from "utils/utilities";
import {
  BuildIcon,
  CrossIcon,
  RemoveIcon,
  SearchIcon,
  StopIcon,
  SyncIcon,
  WarningIcon,
} from "assets/icons/movetodataActionIcons";

import { ThunkAppDispatch } from "redux/types/store";

import { ComponentIcon } from "assets/icons/movetodataInterfaceIcons";
import { TrashIcon } from "assets/icons/movetodataMiscellaneousIcons";
import { ArrowLeftIcon, TickIcon } from "assets/icons/movetodataNavigationIcon";
import { getDefaultFavicon } from "components/movetodataLoader/FavIconLoader";
import MoveToDataModal from "components/MoveToDataModalContainer";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import NoData from "components/NoData";
import { useNavigate, useParams } from "react-router";
import {
  getAllArtifactsDetails as getAllArtifactsByTriggerId,
  getAllArtifactsDetails,
} from "redux/actions/ArtifactActions";
import MoveToDataLoader from "components/movetodataLoader";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import { artifactLog, deleteArtifact, runTrigger } from "../apis";
import "../BuildsStyle.scss";
import { CopyIcon } from "assets/icons/movetodataEditorIcons";

const { Title, Text } = Typography;

// Build Status
export const ABORTED = "ABORTED";
export const FAILED = "FAILED";
export const SUCCESS = "SUCCESS";
export const DELETED = "DELETED";
export const COMPLETED = "COMPLETED";
export const CANCELLED = "CANCELLED";
export const ERROR = "ERROR";
export const ACTIVE = "ACTIVE";
export const INFO = "INFO";

const getTriggerDetailsByIdAPI = (id: string) => {
  return axios.get(`/build/trigger/${id}`);
};

const Artifacts = () => {
  const navigate = useNavigate();
  const [FilteredData, setFilteredData] = useState();
  const [log, setLog] = useState<string>();
  const [triggerName, setTriggerName] = useState(""); // Add state for trigger name

  const { id } = useParams();
  const [duration, setDuration] = useState(0);

  const { allArtifacts, loading } = useSelector(
    (state) => (state as any).allArtifactDetails
  );

  const [deleteArtifactModal, setDeleteArtifactModal] = useState(false);
  const [deleteArtifactDetails, setDeleteArtifactDetails] = useState({
    name: "",
    id: "",
  });
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [artifactName, setArtifactName] = useState("");
  const [isBuildButtonDisabled, setIsBuildButtonDisabled] = useState(true);
  const [isCancelHovered, setIsCancelHovered] = useState(false);
  const [tooltipVisible, setTooltipVisible] = useState(false);

  const [openArtifactId, setOpenArtifactId] = useState<string | null>(null);
  const handleTooltipVisibleChange = (visible: boolean) => {
    setTooltipVisible(visible);
  };

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
    setArtifactName("");
    setIsBuildButtonDisabled(true);
  };

  const handleOk = () => {
    if (artifactName.toUpperCase() === triggerName.toUpperCase()) {
      // Perform the build action
      console.log("Building with artifact name:", artifactName);
      setIsModalVisible(false);
      setArtifactName("");
      setIsBuildButtonDisabled(true);
      handleRun();
      setTimeout(() => {
        id && dispatch(getAllArtifactsDetails(id));
      }, 4000);
    }
  };

  const handleArtifactNameChange = (e: any) => {
    const value = e.target.value;
    setArtifactName(value);
    setIsBuildButtonDisabled(value !== triggerName);
  };

  const deleteArtifactHandler = () => {
    setDeleteArtifactModal(false);

    deleteArtifact(deleteArtifactDetails.id)
      .then(() => {
        if (isDefined(id)) {
          dispatch(getAllArtifactsByTriggerId(id));
        }
        openNotification(
          "Artifact Deleted",
          "Artifact has been deleted successfully",
          "success"
        );
      })
      .catch(() => {
        openNotification(
          "Unable to Delete Artifact",
          "Artifact not found",
          "error"
        );
      });
  };

  const handleDeleteCancel = () => {
    setDeleteArtifactModal(false);
  };

  function handleRun() {
    if (isDefined(id)) {
      dispatch(getAllArtifactsByTriggerId(id));

      runTrigger(id).then(({ data }) => {
        if (isDefined(data)) {
          // openNotification(data, "Check to view logs", "success");
        } else {
          openNotification(data, "Check to view logs", "error");
        }
      });

      dispatch(getAllArtifactsByTriggerId(id));
    }
  }

  useEffect(() => {
    const client = getSocketClient();

    client.activate();
    console.log(id);
    client.onConnect = (frame) => {
      console.log("Connected: " + frame);

      client.subscribe(`/topic/logs/${openArtifactId}`, function (logMessage) {
        const newLogs = JSON.parse(logMessage.body);
        setLog((prev) => prev + "\n" + newLogs.message);
      });

      client.subscribe(`/topic/build/artifacts/${id}`, function (mail) {
        const msg = JSON.parse(mail.body).message;
        if (msg == "artifactUpdates") {
          if (isDefined(id)) {
            dispatch(getAllArtifactsByTriggerId(id));
          }
        }
      });
    };

    return () => {
      client.deactivate();
    };
  }, [openArtifactId]);

  const [isLogModalOpen, setIsLogModalOpen] = useState(false);

  const dispatch = useDispatch<ThunkAppDispatch>();

  const columns = [
    {
      title: getLanguageLabel("status").toUpperCase(),
      dataIndex: "status",
      render: (e: any, row: any) => {
        const getStatusTooltip = () => {
          if (row.buildStatus === ACTIVE) {
            return "Currently build is active";
          } else if (row.buildStatus == SUCCESS) {
            return "Build completed successfully";
          } else if (row.buildStatus == ABORTED) {
            return "Build aborted";
          } else if (row.buildStatus == DELETED) {
            return "Artifact deleted";
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
            ) : row.buildStatus === DELETED ? (
              <div className="text-and-icon-center">
                <RemoveIcon color="var(--DANGEROUS_COLOR)" />
              </div>
            ) : (
              <div className="text-and-icon-center">
                <WarningIcon size={26} color="#FFA500" />
              </div>
            )}
          </Tooltip>
        );
      },
    },
    {
      title: "Build Tag",
      dataIndex: "tag",
      sorter: (a: any, b: any) => a.tag.localeCompare(b.tag),
      render: (text: number) => (
        <div className="MoveToDataSpan">{text ? text : "None"}</div>
      ),
    },
    {
      title: "Artifact Id",
      dataIndex: "id",
      width: 200,
      render: (text: string) => {
        if (text) {
          const shortText = `${text.slice(0, 14)}...`; // Shortened ID with "..."
          return (
            <MoveToDataInput
              value={shortText}
              readOnly
              suffix={
                <Tooltip
                  title={getLanguageLabel("clickToCopyIntoClipboard")}
                  onOpenChange={handleTooltipVisibleChange}
                >
                  <div
                    onClick={() =>
                      copyToClipboard(
                        `/movetodata/snap/artifactory/artifacts/${text}`
                      )
                    }
                  >
                    <CopyIcon />
                  </div>
                </Tooltip>
              }
            />
          );
        } else {
          return <p>No Data</p>;
        }
      },
    },
    {
      title: "CommitId",
      dataIndex: "commitId",
      render: (latestCommitId: string, record: any) => {
        if (!latestCommitId) {
          return <div className="MoveToDataSpan">None</div>;
        }

        const shortCommitId = latestCommitId.slice(0, 7);

        return (
          <a
            href={`https://github.com/MoveToData-io/${triggerName}/commit/${latestCommitId}`} //////////////////////////////////////
            target="_blank"
            rel="noopener noreferrer"
            className={"commitedId"}
          >
            <Tooltip title={latestCommitId}>{shortCommitId}...</Tooltip>
          </a>
        );
      },
    },
    {
      title: getLanguageLabel("startedAt").toUpperCase(),
      dataIndex: "startedAt",
      render: (text: number) => (
        <div className="MoveToDataSpan">{timeConverter(text)}</div>
      ),
    },
    {
      title: getLanguageLabel("finishedAt").toUpperCase(),
      dataIndex: "finishedAt",
      render: (text: number, row: any) => {
        if (!row.finishedAt) {
          return <div className="MoveToDataSpan">None</div>;
        }
        return <div className="MoveToDataSpan">{timeConverter(text)}</div>;
      },
    },
    {
      title: getLanguageLabel("duration").toUpperCase(),
      dataIndex: "duration",
      render: (text: any, row: any) => {
        if (!row.finishedAt) {
          const currentTime = Date.now(); // Get the current time in milliseconds
          return formatDuration(currentTime - row.startedAt);
        }
        return (
          <div className="MoveToDataSpan">
            {formatDuration(row.finishedAt - row.startedAt)}
          </div>
        );
      },
    },
    {
      title: getLanguageLabel("detailedLogs").toUpperCase(),
      dataIndex: "id",
      render: (id: string) => (
        <MoveToDataButton
          onClick={() => openLog(id)}
          icon={<ComponentIcon />}
          minimal
        >
          {getLanguageLabel("detailedLogs")}
        </MoveToDataButton>
      ),
    },
    {
      title: getLanguageLabel("delete"),
      dataIndex: "id",
      render: (text: any, record: any) => {
        return (
          <>
            <MoveToDataButton
              onClick={() => {
                setDeleteArtifactDetails({ ...record });
                setDeleteArtifactModal(true);
              }}
              intent={"dangerous"}
              icon={<TrashIcon />}
              minimal
              icononly
            />
          </>
        );
      },
    },
  ];

  function onChange(pagination: any, filters: any, sorter: any, extra: any) {
    //
  }

  useEffect(() => {
    if (id !== undefined) {
      dispatch(getAllArtifactsByTriggerId(id));
    }
  }, []);

  useEffect(() => {
    // Fetch the trigger details by ID to get the trigger name
    if (id) {
      getTriggerDetailsByIdAPI(id)
        .then((response) => {
          setTriggerName(response.data.name);
        })
        .catch((error) => {
          console.error("Error fetching trigger details:", error);
        });
    }

    // Start the interval when the component mounts
    const interval = setInterval(() => {
      // Increment the duration by 1 second
      setDuration((prevDuration) => prevDuration + 1000);
    }, 1000); // Update every second

    // Clear the interval when the component unmounts
    return () => clearInterval(interval);
  }, [id]); // Run only once when the component mounts

  useEffect(() => {
    return () => {
      let favicon = document.querySelector('link[rel="icon"]') as any;
      favicon.href = getDefaultFavicon();
    };
  }, [allArtifacts]);
  function openLog(id: string) {
    setIsLogModalOpen(true);
    setOpenArtifactId(id);
    artifactLog(id).then(({ data }) => {
      setLog(data);
    });
  }

  if (loading) return <MoveToDataLoader />;

  return (
    <>
      <MoveToDataModal
        headingIcon={<TrashIcon color="var(--DANGEROUS_COLOR)" />}
        heading={getLanguageLabel("areYouSureYouWantToDeleteThis?")}
        open={deleteArtifactModal}
        onCancel={handleDeleteCancel}
        onOk={() => deleteArtifactHandler()}
        footerButtonArea={
          <MoveToDataButton
            icon={<TrashIcon />}
            onClick={() => deleteArtifactHandler()}
            intent="dangerous"
          >
            {getLanguageLabel("delete")}
          </MoveToDataButton>
        }
      >
        {deleteArtifactDetails.id}
      </MoveToDataModal>

      <div className="settings-center-block">
        <Title className={styles.build_title} level={3}>
          <div onClick={() => navigate(-1)} className={styles.title_cursor}>
            <div className="text-and-icon-center">
              <ArrowLeftIcon size={30} />{" "}
              {getLanguageLabel("builds").toUpperCase()}
            </div>
          </div>
        </Title>
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                {triggerName && triggerName.toUpperCase()} Build Artifacts (
                {allArtifacts && allArtifacts.length})
              </Title>
              <Text type="secondary">
                Here are {triggerName} build artifacts.
              </Text>
            </Col>
            <Col>
              {/* <Comments id={id} /> */}

              <Tooltip
                placement="top"
                title={"Run a new build on this trigger."}
              >
                <MoveToDataButton
                  icon={<BuildIcon />}
                  intent="action"
                  onClick={() => {
                    showModal();
                  }}
                >
                  Build
                </MoveToDataButton>
              </Tooltip>
              <MoveToDataModal
                headingIcon={<BuildIcon />}
                heading={"Are you sure you want to build : " + triggerName}
                open={isModalVisible}
                onCancel={handleCancel}
                onOk={() => handleOk()}
                footerButtonArea={[
                  <MoveToDataButton
                    key="build" // Add key to avoid warnings
                    icon={<BuildIcon />}
                    disabled={isBuildButtonDisabled}
                    onClick={handleOk}
                    intent="action"
                  >
                    {getLanguageLabel("build")}
                  </MoveToDataButton>,
                  <MoveToDataButton
                    key="cancel" // Add key to avoid warnings
                    icon={<CrossIcon />}
                    onClick={handleCancel}
                    intent="none"
                    style={{
                      backgroundColor: isCancelHovered
                        ? "#c80000"
                        : "transparent",
                      color: isCancelHovered ? "black" : "inherit",
                      transition: "background-color 0.3s",
                    }}
                    onMouseEnter={() => setIsCancelHovered(true)}
                    onMouseLeave={() => setIsCancelHovered(false)}
                  >
                    {getLanguageLabel("cancel")}
                  </MoveToDataButton>,
                ]}
              >
                <MoveToDataInput
                  placeholder="Enter artifact name"
                  value={artifactName}
                  onChange={handleArtifactNameChange}
                />
              </MoveToDataModal>
            </Col>
          </Row>

          <Divider />
        </p>

        <MoveToDataInput
          placeholder={getLanguageLabel("search")}
          allowClear
          onChange={(e: any) => {
            setFilteredData(
              globalSearch(e.target.value, allArtifacts, columns)
            );
          }}
          suffix={<SearchIcon />}
        />

        <Table
          columns={columns}
          dataSource={FilteredData !== undefined ? FilteredData : allArtifacts}
          onChange={onChange}
          className="interactive"
          pagination={false}
        />
      </div>
      <MoveToDataModal
        className={styles.modal}
        destroyOnClose
        headingIcon={<ComponentIcon />}
        heading={getLanguageLabel("detailedLogs")}
        open={isLogModalOpen}
        width={900}
        onCancel={() => setIsLogModalOpen(false)}
        footer={
          <div className={styles.footer}>
            <MoveToDataButton
              onClick={() => setIsLogModalOpen(false)}
              size="large"
              outlined
              style={{ margin: "0 20px" }}
            >
              Close
            </MoveToDataButton>
          </div>
        }
      >
        {log ? (
          <pre className={styles.pre}>{log}</pre>
        ) : (
          <NoData heading={"No logs found"} />
        )}
      </MoveToDataModal>
    </>
  );
};

export default Artifacts;
