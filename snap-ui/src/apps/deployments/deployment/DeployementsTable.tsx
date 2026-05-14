import React from "react";
import { Table } from "antd";
import { Link } from "react-router-dom";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";
import { TrashIcon } from "assets/icons/orpheaMiscellaneousIcons";
import { getLanguageLabel } from "utils/utilities";

const DeploymentsTable = ({
  dataSource,
  onDelete,
  setDeleteDeploymentDetails,
  setDeleteModal,
}: any) => {
  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp);
    const hours = date.getUTCHours().toString().padStart(2, "0");
    const minutes = date.getUTCMinutes().toString().padStart(2, "0");
    const seconds = date.getUTCSeconds().toString().padStart(2, "0");
    return `${hours}:${minutes}:${seconds}`;
  };

  const columns = [
    {
      title: `${getLanguageLabel("name")}`,
      dataIndex: "deploymentModel",
      sorter: (a: any, b: any) => a.name.localeCompare(b.name),
      render: (text: string, record: any) => (
        <Link to={`/portal/deployments/configuration/${record.id}`}>
          {record.name}
        </Link>
      ),
    },
    {
      title: `${getLanguageLabel("branch")}`,
      dataIndex: "branch",
      render: (text: string) => (
        <div className="OrpheaSpan">{text ? text : "None"}</div>
      ),
    },
    {
      title: `Active`,
      dataIndex: "configurationComponentsModel",
      render: (configurationComponentsModel: any[]) => {
        const activeComponent = configurationComponentsModel.find(
          (component) => component.state === "ACTIVE"
        );
        const targetComponent = configurationComponentsModel.find(
          (component) => component.state === "TARGET"
        );

        const activeVersion = activeComponent
          ? activeComponent.globalVersion
          : "N/A";
        const targetVersion = targetComponent
          ? targetComponent.globalVersion
          : "N/A";

        const isSame = activeVersion === targetVersion;

        return (
          <div
            className="OrpheaSpan"
            style={{
              color:
                activeVersion !== "N/A"
                  ? isSame
                    ? "green"
                    : "red"
                  : "inherit",
              textDecoration: "underline dotted",
            }}
          >
            {activeVersion}
          </div>
        );
      },
    },
    {
      title: `Target`,
      dataIndex: "configurationComponentsModel",
      render: (configurationComponentsModel: any[]) => {
        const activeComponent = configurationComponentsModel.find(
          (component) => component.state === "TARGET"
        );

        return (
          <div className="OrpheaSpan">
            {activeComponent ? activeComponent.globalVersion : "N/A"}
          </div>
        );
      },
    },
    {
      title: `${getLanguageLabel("location")}`,
      dataIndex: "location",
      render: (text: string) => (
        <div className="OrpheaSpan">{text ? text : "None"}</div>
      ),
    },
    {
      title: `${getLanguageLabel("email")}`,
      dataIndex: "email",
      render: (text: string) => (
        <div className="OrpheaSpan">{text ? text : "None"}</div>
      ),
    },
    {
      title: "Start Time",
      dataIndex: "timeWindowStart",
      render: (text: number) => (
        <div className="OrpheaSpan">{text ? formatTime(text) : "None"}</div>
      ),
    },
    {
      title: "End Time",
      dataIndex: "timeWindowEnd",
      render: (text: number) => (
        <div className="OrpheaSpan">{text ? formatTime(text) : "None"}</div>
      ),
    },
    {
      title: "Override Deployment Time",
      dataIndex: "overRideTimeWindow",
      render: (text: number) => (
        <div className="OrpheaSpan">{text ? text : "None"}</div>
      ),
    },
    {
      title: "Deployment Method",
      dataIndex: "deploymentMethod",
      render: (text: string) => (
        <div className="OrpheaSpan">{text ? text : "Automatic"}</div>
      ),
    },
    {
      title: "Deployed At",
      dataIndex: "deployedAt",
      render: (text: string, record: any) => {
        const activeComponent = record.configurationComponentsModel?.find(
          (component: any) => component.state === "ACTIVE"
        );

        // Convert the timestamp to MM-DD-YYYY HH:MM:SS format
        const formatTimestamp = (timestamp: number) => {
          const date = new Date(timestamp);
          return date.toLocaleString("en-US", {
            month: "2-digit",
            day: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
          });
        };

        return (
          <div className="OrpheaSpan">
            {activeComponent
              ? formatTimestamp(activeComponent.deployedAt)
              : "N/A"}
          </div>
        );
      },
    },
    {
      title: getLanguageLabel("delete"),
      dataIndex: "id",
      render: (text: any, record: any) => {
        return (
          <>
            <OrpheaButton
              onClick={() => {
                setDeleteDeploymentDetails({ ...record });
                setDeleteModal(true);
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
    // Handle table change events if needed
  }

  return (
    <div className="table-container">
      <Table
        columns={columns}
        dataSource={dataSource}
        onChange={onChange}
        className="interactive"
        pagination={false}
      />
    </div>
  );
};

export default DeploymentsTable;
