import React from "react";
import { Table } from "antd";
import { Link } from "react-router-dom";
import { getLanguageLabel } from "utils/utilities";

const MoveToDataHomeTable = ({ dataSource, onChange }: any) => {
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
            className="MoveToDataSpan"
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
          <div className="MoveToDataSpan">
            {activeComponent ? activeComponent.globalVersion : "N/A"}
          </div>
        );
      },
    },
    {
      title: "Deployed At",
      dataIndex: "deployedAt",
      render: (text: string, record: any) => {
        const activeComponent = record.configurationComponentsModel?.find(
          (component: any) => component.state === "ACTIVE"
        );

        // Function to calculate time difference and return relative time string
        const getTimeAgo = (timestamp: number) => {
          const now = new Date();
          const past = new Date(timestamp);
          const diffInSeconds = (now.getTime() - past.getTime()) / 1000;

          // Time constants
          const secondsInMinute = 60;
          const secondsInHour = 60 * secondsInMinute;
          const secondsInDay = 24 * secondsInHour;
          const secondsInWeek = 7 * secondsInDay;
          const secondsInMonth = 30 * secondsInDay; // Approximate for months
          const secondsInYear = 365 * secondsInDay;

          if (diffInSeconds < secondsInDay) {
            const hours = Math.floor(diffInSeconds / secondsInHour);
            return `${hours} hour${hours > 1 ? "s" : ""} ago`;
          } else if (diffInSeconds < secondsInWeek) {
            const days = Math.floor(diffInSeconds / secondsInDay);
            return `${days} day${days > 1 ? "s" : ""} ago`;
          } else if (diffInSeconds < secondsInMonth) {
            const weeks = Math.floor(diffInSeconds / secondsInWeek);
            return `${weeks} week${weeks > 1 ? "s" : ""} ago`;
          } else if (diffInSeconds < secondsInYear) {
            const months = Math.floor(diffInSeconds / secondsInMonth);
            return `${months} month${months > 1 ? "s" : ""} ago`;
          } else {
            const years = Math.floor(diffInSeconds / secondsInYear);
            if (years === 1) {
              const months = Math.floor(
                (diffInSeconds % secondsInYear) / secondsInMonth
              );
              return months > 0
                ? `1 year ${months} month${months > 1 ? "s" : ""} ago`
                : `1 year ago`;
            } else {
              return `${years} years ago`;
            }
          }
        };

        // Render time ago for deployedAt field or return "N/A" if no active component
        return (
          <div className="MoveToDataSpan">
            {activeComponent ? getTimeAgo(activeComponent.deployedAt) : "N/A"}
          </div>
        );
      },
    },
  ];

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

export default MoveToDataHomeTable;
