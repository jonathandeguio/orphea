import { Alert, Table } from "antd";

import { FolderIcon } from "assets/icons/boslerFileIcons";
import axios from "axios";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React, { useEffect, useState } from "react";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { CrossIcon } from "../../assets/icons/boslerActionIcons";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";

interface TProps {
  id: string;
  branch: string;
  transactionId: string;
  view: boolean;
}
const FilesModal = ({ id, branch, transactionId, view }: TProps) => {
  const [visible, setVisible] = useState(view);
  const [datasetName, setDatasetName] = useState("");

  const handleCancel = () => {
    setVisible(false);
  };

  const loadName = async () => {
    try {
      const { data: datasetData } = await axios.get(`/kitab/${id}`);
      setDatasetName(datasetData.name);
    } catch (error) {
      openNotification(`Failed to fetch name`, " ", "error");
    }
  };

  function callback(key: $TSFixMe) {}

  const [fileData, setfileData] = useState([]);

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
    <>
      <BoslerModal
        headingIcon={<FolderIcon />}
        heading={getLanguageLabel("titles")}
        open={visible}
        onCancel={handleCancel}
        footerButtonArea={
          <BoslerButton
            icon={<CrossIcon />}
            intent="none"
            key="back"
            onClick={handleCancel}
          >
            {getLanguageLabel("close")}
          </BoslerButton>
        }
      >
        <Alert
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
      </BoslerModal>
    </>
  );
};

export default FilesModal;
