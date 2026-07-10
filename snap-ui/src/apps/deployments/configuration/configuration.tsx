import React from "react";
import {
  copyToClipboard,
  getLanguageLabel,
  getTimeDisplay,
  isDefined,
  openNotification,
  timeConverter,
} from "utils/utilities";
import {
  artifactObjectByName,
  getDeploymentDetailsByIdAPI,
  updateTargetState,
} from "../apis";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";
import {
  Checkbox,
  Col,
  Divider,
  Popover,
  Row,
  Table,
  Tooltip,
  Typography,
} from "antd";
import {
  AppIcon,
  ComponentIcon,
  UploadIcon,
} from "assets/icons/movetodataInterfaceIcons";
import { PulseIcon } from "assets/icons/movetodataMiscellaneousIcons";
import { useNavigate } from "react-router";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import { CopyIcon } from "assets/icons/movetodataEditorIcons";
import { AddIcon } from "assets/icons/movetodataActionIcons";
import { TickIcon } from "assets/icons/movetodataNavigationIcon";

const { Title, Text } = Typography;

const stateIcon = (text: any) => {
  switch (text) {
    case "ARCHIVED":
      return (
        <Tooltip title={text}>
          <ComponentIcon color={"#6C757D"} />{" "}
        </Tooltip>
      );
    case "ACTIVE":
      return (
        <Tooltip title={text}>
          <PulseIcon color={"var(--movetodata-intent-danger)"} />{" "}
        </Tooltip>
      );
    case "TARGET":
      return (
        <Tooltip title={text}>
          <UploadIcon color={"var(--SUCCESS_COLOR)"} />{" "}
        </Tooltip>
      );
    default:
      return getLanguageLabel("none");
  }
};

export const fetchDeploymentDetails = async ({
  deploymentId,
  setIsLoading,
  setDeploymentDetails,
}: any) => {
  if (isDefined(deploymentId)) {
    setIsLoading(true); // Set loading state before fetching
    try {
      const response = await getDeploymentDetailsByIdAPI(deploymentId);
      console.log("deployment : " + response.data);
      setDeploymentDetails(response.data);
      setIsLoading(false); // Set loading state to false after fetching
    } catch (error) {
      console.error("Error fetching deployment details:", error);
      setIsLoading(false);
    }
  }
};

// Function to check if TARGET row should be highlighted
export const shouldHighlightTargetRow = (
  record: any,
  deploymentDetails: any
) => {
  if (record.state !== "TARGET") return false;

  let activeRecord: any;

  activeRecord = deploymentDetails.configurationComponentsModel.find(
    (item: any) => item.state === "ACTIVE"
  );

  if (!activeRecord) return false;

  // Check if any TARGET version does not match the ACTIVE version
  return (
    record.frontend !== activeRecord.frontend ||
    record.boson !== activeRecord.boson ||
    record.parler !== activeRecord.parler ||
    record.julia !== activeRecord.julia ||
    record.callisto !== activeRecord.callisto ||
    record.capture !== activeRecord.capture ||
    record.movetodataDocs !== activeRecord.movetodataDocs ||
    record.sparkHistoryServer !== activeRecord.sparkHistoryServer
  );
};

export const transformData = (data: any) => {
  const components = [
    "globalVersion",
    "frontend",
    "boson",
    "parler",
    "julia",
    "callisto",
    "capture",
    "movetodataDocs",
    "sparkHistoryServer",
  ];

  return data.flatMap(
    (item: { id: any; state: string; deployedAt: any; [key: string]: any }) =>
      components.map((component) => ({
        key: `${item.id}-${component}`,
        id: item.id,
        component: component,
        version: item[component],
        active: item.state === "ACTIVE" ? item[component] : "",
        target: item.state === "TARGET" ? item[component] : "",
        deployedAt: item.deployedAt,
      }))
  );
};

// Function to fetch artifact by name and navigate to its route
const fetchArtifactAndNavigate = async (
  triggerName: string,
  deploymentDetails: any
) => {
  try {
    const navigate = useNavigate();
    let response;
    if (deploymentDetails.branch) {
      response = await artifactObjectByName(
        triggerName,
        deploymentDetails.branch
      );
    }
    const artifactId = response?.data.id; // Assuming the response contains an `id` field
    navigate(`/portal/artifacts/${artifactId}`);
  } catch (error) {
    console.error("Error fetching artifact:", error);
  }
};

const fetchArtifactLatestTag = async (
  triggerName: string,
  deploymentDetails: any
) => {
  try {
    let response;
    if (deploymentDetails.branch) {
      response = await artifactObjectByName(
        triggerName,
        deploymentDetails.branch
      );
    }
    const latestTag = response?.data?.latestTag;
    return latestTag;
  } catch (error) {
    console.error("Error fetching latestTag:", error);
    throw error;
  }
};

const fetchArtifactLatestTags = async (
  components: string[],
  deploymentDetails: any
) => {
  const latestTags: { [key: string]: string } = {};
  await Promise.all(
    components.map(async (component) => {
      latestTags[component] = await fetchArtifactLatestTag(
        component,
        deploymentDetails
      );
    })
  );
  return latestTags;
};

export const mergedData = async (data: any, deploymentDetails: any) => {
  const transformedData = transformData(data);
  const components = [
    "globalVersion",
    "frontend",
    "boson",
    "parler",
    "julia",
    "callisto",
    "capture",
    "movetodataDocs",
    "sparkHistoryServer",
  ];

  const latestTags = await fetchArtifactLatestTags(
    components,
    deploymentDetails
  );

  const componentMap: { [key: string]: any } = {};

  transformedData.forEach((item: any) => {
    if (!componentMap[item.component]) {
      componentMap[item.component] = {
        key: item.component,
        component: (
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              fetchArtifactAndNavigate(item.component, deploymentDetails);
            }}
          >
            {item.component}
          </a>
        ),
        active: "",
        target: "",
        latest: latestTags[item.component],
        deployedAt: item.deployedAt,
      };
    }

    if (item.active) {
      componentMap[item.component].active = item.active;
    }
    if (item.target) {
      componentMap[item.component].target = item.target;
    }
  });

  return Object.values(componentMap);
};

export const getConfigurationColumns = (
  handleRevertModal: (record: any) => void
) => {
  const configurationColumns = [
    {
      title: "State",
      dataIndex: "state",
      render: (text: string) => (
        <div className="MoveToDataSpan">{stateIcon(text)}</div>
      ),
    },
    {
      title: "Global Version",
      dataIndex: "globalVersion",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Deployed At",
      dataIndex: "deployedAt",
      render: (text: number, row: any) => {
        if (!row.deployedAt) {
          return <div className="MoveToDataSpan">None</div>;
        }
        return <div className="MoveToDataSpan">{timeConverter(text)}</div>;
      },
    },
    {
      title: "Frontend",
      dataIndex: "frontend",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Boson",
      dataIndex: "boson",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Parler",
      dataIndex: "parler",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Julia",
      dataIndex: "julia",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Callisto",
      dataIndex: "callisto",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Capture",
      dataIndex: "capture",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "MoveToDataDocs",
      dataIndex: "movetodataDocs",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "SparkHistoryServer",
      dataIndex: "sparkHistoryServer",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Actions",
      dataIndex: "actions",
      render: (text: any, record: any) => {
        if (record.state === "ARCHIVED") {
          return (
            <MoveToDataButton
              intent="action"
              onClick={() => handleRevertModal(record)}
            >
              Revert
            </MoveToDataButton>
          );
        }
        return null;
      },
    },
  ];
  return configurationColumns;
};

export const getLicenseColumns = () => {
  const licenseColumns = [
    {
      title: "State",
      dataIndex: "state",
      render: (text: string) => (
        <div className="MoveToDataSpan">{stateIcon(text)}</div>
      ),
    },
    {
      title: "Client",
      dataIndex: "client",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Product",
      dataIndex: "product",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: "Base URL",
      dataIndex: "baseUrl",
      render: (text: string) => (
        <div className="MoveToDataSpan">{text ? text : getLanguageLabel("none")}</div>
      ),
    },
    {
      title: getLanguageLabel("details"),
      dataIndex: "details",
      render: (text: string, record: any) => (
        <Popover
          content={
            <div>
              <div>
                <Checkbox checked={record.displayBlockedFeatures} disabled />
                Display Blocked Features
              </div>
              <div>
                Users: {record.maximumUsers ? record.maximumUsers : getLanguageLabel("none")}
              </div>
              <div>
                Builds Per Day:{" "}
                {record.maximumBuildsPerDay
                  ? record.maximumBuildsPerDay
                  : getLanguageLabel("none")}
              </div>
              <div>
                Datasets:{" "}
                {record.maximumDatasets ? record.maximumDatasets : getLanguageLabel("none")}
              </div>
              <div>
                Dashboards:{" "}
                {record.maximumDashboards ? record.maximumDashboards : getLanguageLabel("none")}
              </div>
              <div>
                Charts: {record.maximumCharts ? record.maximumCharts : getLanguageLabel("none")}
              </div>
              <div>
                Repositories:{" "}
                {record.maximumRepositories
                  ? record.maximumRepositories
                  : getLanguageLabel("none")}
              </div>
            </div>
          }
          title={getLanguageLabel("details")}
        >
          <div
            style={{
              display: "flex",
              textDecoration: "underline dotted",
              cursor: "pointer",
            }}
          >
            View Details
          </div>
        </Popover>
      ),
    },
    {
      title: getLanguageLabel("expiringOn"),
      dataIndex: "expiresOn",
      render: (text: string) => (
        <div className="MoveToDataSpan">
          {text ? (
            <Tooltip title={timeConverter(Number(text))}>
              <div
                style={{
                  display: "flex",
                  textDecoration: "underline dotted",
                  cursor: "pointer",
                }}
              >
                {getTimeDisplay(Number(text))}
              </div>
            </Tooltip>
          ) : (
            getLanguageLabel("none")
          )}
        </div>
      ),
    },
    {
      title: getLanguageLabel("licenseKey"),
      dataIndex: "licenseKey",
      render: (text: string) => (
        <MoveToDataInput
          value={text ? text : getLanguageLabel("none")}
          readOnly
          suffix={
            <Tooltip title={getLanguageLabel("clickToCopyIntoClipboard")}>
              <div onClick={() => copyToClipboard(text ? text : getLanguageLabel("none"))}>
                <CopyIcon />
              </div>
            </Tooltip>
          }
        />
      ),
    },
  ];
  return licenseColumns;
};

export const getColumnsActiveTarget = ({
  setIsCreateConfigurationTargetModalOpen,
  shouldHighlightActiveRecord,
  dataSource,
}: any) => {
  const columnsActiveTarget = [
    {
      title: (
        <>
          <AppIcon /> {getLanguageLabel("component")} <br />
          <Text style={{ fontSize: "0.8rem" }} type="secondary">
            {getLanguageLabel("componentsOnThePlatform")}
          </Text>
        </>
      ),
      dataIndex: "component",
      key: "component",
    },
    {
      title: (
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <div>
            <PulseIcon color={"var(--movetodata-intent-danger)"} /> {getLanguageLabel("active")} <br />
            <Text style={{ fontSize: "0.8rem" }} type="secondary">
              {getLanguageLabel("thisIsTheCurrentlyActiveVersion")}
            </Text>
          </div>
        </div>
      ),
      dataIndex: "active",
      key: "active",
      render: (text: any, record: any) => (
        <Tooltip
          title={`Deployed At: ${new Date(record.deployedAt).toLocaleString()}`}
        >
          <div
            style={{
              display: "flex",
              textDecoration: shouldHighlightActiveRecord(record)
                ? "bold"
                : getLanguageLabel("none"),
              color: shouldHighlightActiveRecord(record) ? "red" : "green",
            }}
          >
            <div
              style={{
                display: "flex",
                textDecoration: "underline dotted",
                cursor: "pointer",
              }}
            >
              {text}
            </div>
          </div>
        </Tooltip>
      ),
    },
    {
      title: (
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <div>
            <UploadIcon color={"var(--SUCCESS_COLOR)"} /> {getLanguageLabel("target")} <br />
            <Text style={{ fontSize: "0.8rem" }} type="secondary">
              {getLanguageLabel("thisIsTheTargetVersion")}
            </Text>
          </div>
        </div>
      ),
      dataIndex: "target",
      key: "target",
    },
    {
      title: (
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <div>
            <UploadIcon color={"var(--SUCCESS_COLOR)"} /> {getLanguageLabel("latest")} <br />
            <Text style={{ fontSize: "0.8rem" }} type="secondary">
              {getLanguageLabel("thisIsTheLatestVersion")}
            </Text>
          </div>
        </div>
      ),
      dataIndex: "latest",
      key: "latest",
      render: (text: any, record: any) => {
        if (record.component === "globalVersion") {
          return <div>{text ? text : ""}</div>;
        }

        const activeRecord = dataSource.find(
          (item: any) => item.component === record.component && item.active
        );
        const hasMismatch =
          activeRecord &&
          record &&
          record.latest &&
          activeRecord.active !== record.latest;

        return (
          <div
            style={{
              color: hasMismatch ? "blue" : "inherit",
              textDecoration: hasMismatch ? "underline dotted" : getLanguageLabel("none"),
            }}
          >
            {hasMismatch ? (
              <Tooltip title={getLanguageLabel("clickToUpdateToLatest")}>
                <MoveToDataButton
                  icon={<UploadIcon />}
                  intent="action"
                  onClick={() => setIsCreateConfigurationTargetModalOpen(true)}
                  minimal
                  outlined
                >
                  {text ? text : getLanguageLabel("none")}
                </MoveToDataButton>
              </Tooltip>
            ) : (
              <Tooltip title={"Already on the latest."}>
                <MoveToDataButton
                  icon={<TickIcon />}
                  intent={getLanguageLabel("none")}
                  minimal
                  outlined
                >
                  {text ? text : getLanguageLabel("none")}
                </MoveToDataButton>
              </Tooltip>
            )}
          </div>
        );
      },
    },
  ];
  return columnsActiveTarget;
};

export const getTabItems = ({
  setIsCreateConfigurationTargetModalOpen,
  deploymentDetails,
  setIsCreateNewLicenseModalOpen,
  onChange,
  dataSource,
  configurationColumns,
  shouldHighlightActiveRecord,
}: any) => {
  const licenseColumns = getLicenseColumns();
  const columnsActiveTarget = getColumnsActiveTarget({
    setIsCreateConfigurationTargetModalOpen,
    shouldHighlightActiveRecord,
    dataSource,
  });
  const tabItems = [
    {
      key: "1",
      label: "Deployment States",
      children: (
        <div style={{ padding: "5px" }}>
          <Row justify="space-between">
            <Col>
              <Text type="secondary"></Text>
            </Col>
            <Col>
              <Tooltip placement="top" title={getLanguageLabel("updateTargetState")}>
                <MoveToDataButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => setIsCreateConfigurationTargetModalOpen(true)}
                >
                  {getLanguageLabel("updateTargetState")}
                </MoveToDataButton>
              </Tooltip>
            </Col>
          </Row>
          <br />
          <Table
            title={() => "Deployment State of the components"}
            columns={columnsActiveTarget}
            dataSource={dataSource}
            className="interactive"
            pagination={false}
            size={"small"}
          />

          <br />
          <Table
            columns={configurationColumns}
            dataSource={deploymentDetails?.configurationComponentsModel}
            onChange={onChange}
            className="interactive"
            pagination={false}
            rowClassName={(record) =>
              shouldHighlightTargetRow(record, deploymentDetails)
                ? "highlight-target-row"
                : ""
            }
          />
        </div>
      ),
    },
    {
      key: "2",
      label: "License",
      children: (
        <div style={{ padding: "5px" }}>
          <Row justify="space-between">
            <Col>
              <Title level={3}>
                {getLanguageLabel("license")} ({deploymentDetails?.licenseModel.length})
              </Title>
              <Text type="secondary">{getLanguageLabel("hereAreMoveToDataClientsLicenses")}</Text>
            </Col>
            <Col>
              <Tooltip placement="top" title={"Create New License"}>
                <MoveToDataButton
                  icon={<AddIcon />}
                  intent="action"
                  onClick={() => setIsCreateNewLicenseModalOpen(true)}
                >
                  {getLanguageLabel("newLicense")}
                </MoveToDataButton>
              </Tooltip>
            </Col>
          </Row>

          <Divider />
          <Table
            columns={licenseColumns}
            dataSource={deploymentDetails?.licenseModel}
            onChange={onChange}
            className="interactive"
            pagination={false}
          />
        </div>
      ),
    }
  ];
  return tabItems;
};

export const handleRevert = async ({
  record,
  deploymentDetails,
  deploymentId,
  setIsLoading,
  setDeploymentDetails,
}: any) => {
  try {
    const payload = {
      frontend: record.frontend,
      boson: record.boson,
      parler: record.parler,
      julia: record.julia,
      callisto: record.callisto,
      capture: record.capture,
      movetodataDocs: record.movetodataDocs,
      sparkHistoryServer: record.sparkHistoryServer,
      branch: deploymentDetails.branch,
    };

    const response = await updateTargetState(deploymentDetails.id, payload);
    if (response.data.success) {
      openNotification(
        "Success",
        "Configuration target updated successfully",
        "success"
      );
      fetchDeploymentDetails({
        deploymentId,
        setIsLoading,
        setDeploymentDetails,
      });
    } else {
      console.error("Failed to revert the state");
    }
  } catch (error) {
    console.error("Error reverting state:", error);
  }
};
