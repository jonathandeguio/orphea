import { Alert, Table } from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { isDefined } from "utils/utilities";

interface TProps {
  id: string;
  branch: string;
  transactionId: string;
}
export const Files: React.FC<TProps> = ({ id, branch, transactionId }) => {
  const [fileData, setfileData] = useState([]);

  const [datasetName, setDatasetName] = useState("");

  const loadName = async () => {
    try {
      const { data: datasetData } = await axios.get(`/kitab/${id}`);
      setDatasetName(datasetData.name);
    } catch (error) {
      // openNotification(`Failed to fetch name`, " ", "error");
    }
  };
  const fetch_files = async () => {
    try {
      const { data } = await axios.get(
        `/dataset/files/${id}/${branch}/${transactionId}`
      );

      setfileData(data);
    } catch (error) {
      // openNotification(`Failed to access files.`, " ", "error");
    }
  };

  useEffect(() => {
    if (isDefined(id) && isDefined(branch) && isDefined(transactionId)) {
      fetch_files();
      loadName();
    }
  }, [id, branch, transactionId]);

  return (
    <div style={{ height: "100%", margin: "0 1rem" }}>
      <Alert
        style={{
          margin: "1rem 0",
        }}
        message={datasetName}
        type="success"
        showIcon
        className="pipeline-menu-schedule-node"
      />

      <Table
        dataSource={(fileData as $TSFixMe).rows}
        columns={(fileData as $TSFixMe).columns}
        pagination={false}
        bordered={true}
      />
    </div>
  );
};
