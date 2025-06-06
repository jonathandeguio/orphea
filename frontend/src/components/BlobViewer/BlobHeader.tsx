import React, { useState } from "react";
/** @jsxImportSource @emotion/react */

import { Typography } from "antd";
import { getLanguageLabel } from "utils/utilities";

import { DownloadIcon } from "../../assets/icons/boslerInterfaceIcons";
import Avatars from "../Avatars/Avatars";
import BoslerButton from "../BoslerComponents/ButtonComponent/BoslerButton";
import Comments from "../Comments/Comments.view";

import CustomBreadCrumb from "components/Nav/Manage/breadCrumb";
import { downloadBlobFile } from "./BlobViewer.utils";
import { BoslerInfoPopover } from "components/CommonUI/BoslerInfoPopover/BoslerInfoPopover.view";

const { Text } = Typography;

const BlobHeader = ({ file, fileDetails }: any) => {
  return (
    <div className="blob-container-header">
      <CustomBreadCrumb />
      <div className="blob-container-header-btns">
        <BoslerInfoPopover id={fileDetails.id} type={fileDetails.type} />
        <Comments id={fileDetails.id} />
        <Avatars link={`/topic/${fileDetails.id}`} />
        <BoslerButton
          intent="action"
          icon={<DownloadIcon />}
          onClick={() =>
            downloadBlobFile(file, fileDetails.name, fileDetails.subType)
          }
        >
          {getLanguageLabel("download")}
        </BoslerButton>
      </div>
    </div>
  );
};

export default BlobHeader;
